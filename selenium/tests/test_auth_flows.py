import pytest
from selenium.webdriver.common.by import By
from pages.application_pages import LoginPage

@pytest.mark.parametrize("email,password,is_valid", [
    # Valid-looking credentials (10)
    ("doc@example.com", "password123", True),
    ("nurse@example.com", "pass1234", True),
    ("admin@example.com", "admin123", True),
    ("test1@example.com", "test1234", True),
    ("test2@example.com", "test1234", True),
    ("test3@example.com", "test1234", True),
    ("test4@example.com", "test1234", True),
    ("test5@example.com", "test1234", True),
    ("test6@example.com", "test1234", True),
    ("test7@example.com", "test1234", True),
    # Invalid emails (10)
    ("doc.example.com", "password123", False),
    ("doc@", "password123", False),
    ("@example.com", "password123", False),
    ("doc@example", "password123", False),
    ("doc@.com", "password123", False),
    (" doc@example.com", "password123", False),
    ("doc@example.com ", "password123", False),
    ("doc@@example.com", "password123", False),
    ("", "password123", False),
    ("a" * 256 + "@example.com", "password123", False),
    # Invalid passwords (10)
    ("doc@example.com", "", False),
    ("doc@example.com", " ", False),
    ("doc@example.com", "short", False),
    ("doc@example.com", "123", False),
    ("doc@example.com", "a"*100, False),
    ("doc@example.com", "<script>alert(1)</script>", False),
    ("doc@example.com", "' OR 1=1;--", False),
    ("doc@example.com", "\" OR \"1\"=\"1", False),
    ("doc@example.com", "DROP TABLE users;", False),
    ("doc@example.com", "password123!@#", False)
])
def test_login_matrix(driver, base_url, email, password, is_valid):
    page = LoginPage(driver, base_url).open(LoginPage.path)
    driver.find_element(*page.email).send_keys(email)
    driver.find_element(*page.password).send_keys(password)
    driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()


@pytest.mark.parametrize("name,email,password,is_valid", [
    (f"User {i}", f"new{i}@example.com", f"pass123{i}!", True) for i in range(1, 16)
] + [
    ("", "empty@example.com", "password123", False),
    ("No Email", "", "password123", False),
    ("No Pass", "nopass@example.com", "", False),
    ("Short Pass", "short@example.com", "1234", False),
    ("Bad Email", "bademail", "password123", False),
])
def test_signup_matrix(driver, base_url, name, email, password, is_valid):
    driver.get(f"{base_url}/signup")
    driver.find_element(By.ID, "name").send_keys(name)
    driver.find_element(By.ID, "email").send_keys(email)
    driver.find_element(By.ID, "password").send_keys(password)
    driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
