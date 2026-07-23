import os
from pathlib import Path

import pytest

from utils.framework import FrontendDetector, capture_screenshot, configure_logging, create_driver


ROOT = Path(__file__).resolve().parent


def pytest_addoption(parser):
    parser.addoption("--base-url", action="store", default=os.getenv("BASE_URL", "http://127.0.0.1:5000"))
    parser.addoption("--browser", action="store", default=os.getenv("BROWSER", "chrome"))
    parser.addoption("--headed", action="store_true", default=False)


@pytest.fixture(scope="session")
def base_url(pytestconfig):
    return pytestconfig.getoption("--base-url").rstrip("/")


@pytest.fixture(scope="session")
def logger():
    return configure_logging(ROOT / "reports")


@pytest.fixture
def driver(pytestconfig, logger):
    browser = pytestconfig.getoption("--browser")
    web_driver = create_driver(browser, headless=not pytestconfig.getoption("--headed"))
    logger.info("Started %s", browser)
    yield web_driver
    web_driver.quit()
    logger.info("Closed browser")


@pytest.fixture
def frontend(driver, base_url):
    return FrontendDetector(driver, base_url)


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()
    if report.when != "call":
        return
    logger = item.funcargs.get("logger")
    if report.failed and "driver" in item.funcargs:
        try:
            path = capture_screenshot(item.funcargs["driver"], ROOT / "screenshots", item.name)
            report.user_properties.append(("screenshot", str(path)))
            if logger:
                logger.error("FAIL %s screenshot=%s", item.name, path)
        except Exception as e:
            if logger:
                logger.error("FAIL %s screenshot failed: %s", item.name, str(e))
    elif report.passed and logger:
        logger.info("PASS %s", item.name)
