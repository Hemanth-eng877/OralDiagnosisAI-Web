import time
import uuid

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options


BASE_URL = "http://127.0.0.1:5000"


def make_driver():
    options = Options()
    options.add_argument("--headless=new")
    options.add_argument("--window-size=1440,1000")
    return webdriver.Chrome(options=options)


def test_login_flow():
    driver = make_driver()
    email = f"clinician_{uuid.uuid4().hex[:8]}@example.com"
    password = "SecurePass123"
    try:
        driver.get(f"{BASE_URL}/signup")
        driver.find_element(By.NAME, "name").send_keys("Test Clinician")
        driver.find_element(By.NAME, "email").send_keys(email)
        driver.find_element(By.NAME, "password").send_keys(password)
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()

        driver.find_element(By.NAME, "email").send_keys(email)
        driver.find_element(By.NAME, "password").send_keys(password)
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
        time.sleep(1)

        assert "dashboard" in driver.current_url.lower()
        assert "Clinical Overview" in driver.page_source
    finally:
        driver.quit()


if __name__ == "__main__":
    test_login_flow()
