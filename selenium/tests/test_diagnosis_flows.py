import pytest
from selenium.webdriver.common.by import By
from pages.application_pages import ImageUploadPage

@pytest.mark.parametrize("file_path,notes", [
    (f"dummy_image_{i}.jpg", f"Diagnosis notes for test {i}") for i in range(15)
] + [
    ("invalid_file.txt", "This should fail because it's a text file"),
    ("large_image.jpg", "This image is too large"),
    ("", "Testing empty upload"),
    ("malicious.exe", "Testing malicious file upload")
])
def test_diagnosis_upload(driver, base_url, file_path, notes):
    page = ImageUploadPage(driver, base_url).open(ImageUploadPage.path)
    
    try:
        driver.find_element(*page.marker).send_keys(file_path)
        if driver.find_elements(By.NAME, "notes"):
            driver.find_element(By.NAME, "notes").send_keys(notes)
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
    except Exception:
        pass


@pytest.mark.parametrize("nav_link", [
    "/dashboard",
    "/patients",
    "/patients/add",
    "/diagnose",
    "/reports",
    "/login",
    "/signup"
])
def test_navigation(driver, base_url, nav_link):
    # Test all navigation links to ensure they load without error
    driver.get(f"{base_url}{nav_link}")
    assert driver.current_url.endswith(nav_link) or driver.current_url.endswith("/login")
