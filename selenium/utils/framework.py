import logging
import os
from datetime import datetime
from pathlib import Path

from openpyxl import Workbook
from openpyxl.styles import Font
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.support import expected_conditions as conditions
from selenium.webdriver.support.ui import WebDriverWait
from webdriver_manager.chrome import ChromeDriverManager

from pages.application_pages import (
    DashboardPage, ImageUploadPage, LoginPage, NavigationPage,
    PatientFormPage, PatientListPage, ReportsPage,
)


def create_driver(browser="chrome", headless=None):
    """Create Chrome on Windows or Linux/GitHub Actions."""
    if browser.lower() != "chrome":
        raise ValueError("Only Chrome is currently configured")
    options = Options()
    if headless if headless is not None else os.getenv("HEADLESS", "true").lower() == "true":
        options.add_argument("--headless=new")
    for argument in ("--window-size=1440,1080", "--no-sandbox", "--disable-dev-shm-usage"):
        options.add_argument(argument)
    return webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)


def visible(driver, locator, timeout=10):
    return WebDriverWait(driver, timeout).until(conditions.visibility_of_element_located(locator))


def clickable(driver, locator, timeout=10):
    return WebDriverWait(driver, timeout).until(conditions.element_to_be_clickable(locator))


def capture_screenshot(driver, directory, name="failure"):
    directory = Path(directory)
    directory.mkdir(parents=True, exist_ok=True)
    path = directory / f"{name}_{datetime.now():%Y%m%d_%H%M%S_%f}.png"
    driver.save_screenshot(str(path))
    return path


def write_excel_report(results, path):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    workbook = Workbook()
    sheet = workbook.active
    sheet.title = "Execution Results"
    sheet.append(["Test", "Outcome", "Details", "Timestamp"])
    for cell in sheet[1]:
        cell.font = Font(bold=True)
    for result in results:
        sheet.append([result.get("test", ""), result.get("outcome", ""), result.get("details", ""), datetime.now().isoformat(timespec="seconds")])
    workbook.save(path)
    return path


def configure_logging(directory):
    directory = Path(directory)
    directory.mkdir(parents=True, exist_ok=True)
    logger = logging.getLogger("selenium_framework")
    if not logger.handlers:
        logger.setLevel(logging.INFO)
        handler = logging.FileHandler(directory / "selenium.log", encoding="utf-8")
        handler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
        logger.addHandler(handler)
    return logger


class FrontendDetector:
    """Checks the deployed UI by route plus live DOM markers."""
    def __init__(self, driver, base_url, timeout=10):
        self.driver, self.base_url, self.timeout = driver, base_url, timeout

    def _page(self, page_type):
        return page_type(self.driver, self.base_url, self.timeout)

    def detect_public(self):
        return {"login": self._page(LoginPage).detected()}

    def detect_authenticated(self):
        pages = {
            "dashboard": DashboardPage, "patient_form": PatientFormPage,
            "patient_list": PatientListPage, "image_upload": ImageUploadPage,
            "reports": ReportsPage,
        }
        found = {name: self._page(page).detected() for name, page in pages.items()}
        found["logout"] = NavigationPage(self.driver, self.base_url, self.timeout).logout_detected()
        return found
