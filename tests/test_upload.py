import os
import tempfile
import time
import urllib.request
import uuid

import pytest
from PIL import Image
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options


BASE_URL = "http://127.0.0.1:5000"


def server_available():
    try:
        with urllib.request.urlopen(f"{BASE_URL}/", timeout=2) as response:
            return response.status < 500
    except Exception:
        return False


def make_driver():
    options = Options()
    options.add_argument("--headless=new")
    options.add_argument("--window-size=1440,1000")
    return webdriver.Chrome(options=options)


def signup_and_login(driver):
    email = f"upload_{uuid.uuid4().hex[:8]}@example.com"
    password = "SecurePass123"
    driver.get(f"{BASE_URL}/signup")
    driver.find_element(By.NAME, "name").send_keys("Upload Tester")
    driver.find_element(By.NAME, "email").send_keys(email)
    driver.find_element(By.NAME, "password").send_keys(password)
    driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
    driver.find_element(By.NAME, "email").send_keys(email)
    driver.find_element(By.NAME, "password").send_keys(password)
    driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()


def create_patient(driver):
    driver.get(f"{BASE_URL}/patients/add")
    driver.find_element(By.NAME, "full_name").send_keys("Upload Patient")
    driver.find_element(By.NAME, "age").send_keys("42")
    driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()


def create_test_image():
    handle = tempfile.NamedTemporaryFile(suffix=".jpg", delete=False)
    handle.close()
    Image.new("RGB", (224, 224), color=(180, 80, 90)).save(handle.name)
    return handle.name


@pytest.mark.skipif(not server_available(), reason="Flask app is not running")
def test_upload_image():
    driver = make_driver()
    image_path = create_test_image()
    try:
        signup_and_login(driver)
        create_patient(driver)
        driver.get(f"{BASE_URL}/diagnose")
        driver.find_element(By.NAME, "patient_id").click()
        driver.find_element(By.CSS_SELECTOR, "select[name='patient_id'] option:nth-child(2)").click()
        driver.find_element(By.NAME, "image").send_keys(image_path)
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
        time.sleep(1)

        assert "Diagnosis" in driver.page_source
        assert "confidence" in driver.page_source.lower()
    finally:
        driver.quit()
        os.unlink(image_path)


if __name__ == "__main__":
    test_upload_image()
