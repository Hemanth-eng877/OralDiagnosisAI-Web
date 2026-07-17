from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait


class BasePage:
    def __init__(self, driver, base_url, timeout=10):
        self.driver = driver
        self.base_url = base_url.rstrip("/")
        self.wait = WebDriverWait(driver, timeout)

    def open(self, path):
        self.driver.get(f"{self.base_url}/{path.lstrip('/')}")
        return self

    def visible(self, locator):
        return bool(self.driver.find_elements(*locator))

    def at_path(self, path):
        return self.driver.current_url.rstrip("/").endswith(path)


class LoginPage(BasePage):
    path = "/login"
    email = (By.CSS_SELECTOR, "input[name='email']")
    password = (By.CSS_SELECTOR, "input[name='password']")

    def detected(self):
        self.open(self.path)
        return self.visible(self.email) and self.visible(self.password)


class DashboardPage(BasePage):
    path = "/dashboard"
    marker = (By.CSS_SELECTOR, "h1, h2, .hero-title")

    def detected(self):
        self.open(self.path)
        return self.at_path(self.path) and self.visible(self.marker)


class PatientFormPage(BasePage):
    path = "/patients/add"
    marker = (By.CSS_SELECTOR, "#full_name, input[name='full_name']")

    def detected(self):
        self.open(self.path)
        return self.at_path(self.path) and self.visible(self.marker)


class PatientListPage(BasePage):
    path = "/patients"
    marker = (By.XPATH, "//a[normalize-space()='Add Patient']")

    def detected(self):
        self.open(self.path)
        return self.at_path(self.path) and self.visible(self.marker)


class ImageUploadPage(BasePage):
    path = "/diagnose"
    marker = (By.CSS_SELECTOR, "input[type='file'][name='image']")

    def detected(self):
        self.open(self.path)
        return self.at_path(self.path) and self.visible(self.marker)


class ReportsPage(BasePage):
    path = "/reports"
    marker = (By.CSS_SELECTOR, "input[name='q'], input[type='search']")

    def detected(self):
        self.open(self.path)
        return self.at_path(self.path) and self.visible(self.marker)


class NavigationPage(BasePage):
    logout = (By.XPATH, "//a[normalize-space()='Logout']")

    def logout_detected(self):
        return self.visible(self.logout)
