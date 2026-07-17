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


def create_test_image():
    handle = tempfile.NamedTemporaryFile(suffix=".png", delete=False)
    handle.close()
    Image.new("RGB", (256, 256), color=(120, 160, 180)).save(handle.name)
    return handle.name


@pytest.mark.skipif(not server_available(), reason="Flask app is not running")
def test_diagnosis_workflow():
    driver = make_driver()
    image_path = create_test_image()
    email = f"workflow_{uuid.uuid4().hex[:8]}@example.com"
    password = "SecurePass123"
    try:
        driver.get(f"{BASE_URL}/signup")
        driver.find_element(By.NAME, "name").send_keys("Workflow Tester")
        driver.find_element(By.NAME, "email").send_keys(email)
        driver.find_element(By.NAME, "password").send_keys(password)
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()

        driver.find_element(By.NAME, "email").send_keys(email)
        driver.find_element(By.NAME, "password").send_keys(password)
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()

        driver.get(f"{BASE_URL}/patients/add")
        driver.find_element(By.NAME, "full_name").send_keys("Workflow Patient")
        driver.find_element(By.NAME, "age").send_keys("55")
        driver.find_element(By.NAME, "phone").send_keys("5551234567")
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()

        driver.get(f"{BASE_URL}/diagnose")
        driver.find_element(By.CSS_SELECTOR, "select[name='patient_id'] option:nth-child(2)").click()
        driver.find_element(By.NAME, "image").send_keys(image_path)
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
        time.sleep(1)

        assert "Diagnosis Result" in driver.page_source
        assert "confidence" in driver.page_source.lower()

        driver.get(f"{BASE_URL}/reports?q=Workflow")
        assert "Workflow Patient" in driver.page_source
        assert "View image" in driver.page_source
    finally:
        driver.quit()
        os.unlink(image_path)


if __name__ == "__main__":
    test_diagnosis_workflow()
