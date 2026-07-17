import io
import os
import sys
from pathlib import Path

import pytest
from PIL import Image

ROOT_DIR = Path(__file__).resolve().parent.parent
if str(ROOT_DIR) not in sys.path:
    sys.path.insert(0, str(ROOT_DIR))

import app as app_module


@pytest.fixture(autouse=True)
def reset_state():
    app_module._reset_fallback_state()
    app_module.app.config.update(TESTING=True)
    yield
    app_module._reset_fallback_state()


@pytest.fixture()
def client():
    with app_module.app.test_client() as client:
        yield client


def test_index_redirects_to_login(client):
    response = client.get("/")
    assert response.status_code == 302
    assert response.headers["Location"].endswith("/login")


def test_signup_creates_user_and_redirects_to_login(client):
    response = client.post(
        "/signup",
        data={"name": "Test Clinician", "email": "qauser@example.com", "password": "SecurePass123"},
        follow_redirects=True,
    )
    assert response.status_code == 200
    assert b"Account created" in response.data


def test_login_authenticates_existing_user_and_redirects_to_dashboard(client):
    client.post(
        "/signup",
        data={"name": "Test Clinician", "email": "loginuser@example.com", "password": "SecurePass123"},
    )
    response = client.post(
        "/login",
        data={"email": "loginuser@example.com", "password": "SecurePass123"},
        follow_redirects=True,
    )
    assert response.status_code == 200
    assert b"Clinical Overview" in response.data


def test_patients_page_requires_login(client):
    response = client.get("/patients")
    assert response.status_code == 302
    assert response.headers["Location"].endswith("/login")


def test_add_patient_saves_patient_for_logged_in_user(client):
    client.post(
        "/signup",
        data={"name": "Test Clinician", "email": "patientuser@example.com", "password": "SecurePass123"},
    )
    client.post(
        "/login",
        data={"email": "patientuser@example.com", "password": "SecurePass123"},
    )
    response = client.post(
        "/patients/add",
        data={
            "full_name": "Sample Patient",
            "age": "44",
            "gender": "Female",
            "phone": "5551234",
            "email": "patient@example.com",
            "notes": "Needs follow-up",
        },
        follow_redirects=True,
    )
    assert response.status_code == 200
    assert b"Patient record added" in response.data
    assert b"Sample Patient" in response.data


def test_diagnose_route_saves_diagnosis_for_valid_input(client, monkeypatch, tmp_path):
    client.post(
        "/signup",
        data={"name": "Test Clinician", "email": "diagnoseuser@example.com", "password": "SecurePass123"},
    )
    client.post(
        "/login",
        data={"email": "diagnoseuser@example.com", "password": "SecurePass123"},
    )
    client.post(
        "/patients/add",
        data={
            "full_name": "Diagnose Patient",
            "age": "52",
            "gender": "Male",
            "phone": "5555678",
            "email": "diag@example.com",
            "notes": "High-risk",
        },
    )

    monkeypatch.setattr(app_module, "predict_image", lambda path: ("OSCC", 97.2))

    image_path = tmp_path / "sample.png"
    image = Image.new("RGB", (224, 224), color=(255, 0, 0))
    image.save(image_path)
    with image_path.open("rb") as handle:
        response = client.post(
            "/diagnose",
            data={"patient_id": "1", "image": (io.BytesIO(handle.read()), image_path.name)},
            content_type="multipart/form-data",
            follow_redirects=True,
        )
    assert response.status_code == 200
    assert b"Diagnosis completed and saved" in response.data
    assert b"OSCC" in response.data


def test_reports_page_returns_search_results(client, monkeypatch):
    monkeypatch.setattr(app_module, "predict_image", lambda path: ("OSCC", 97.2))
    client.post(
        "/signup",
        data={"name": "Test Clinician", "email": "reportuser@example.com", "password": "SecurePass123"},
    )
    client.post(
        "/login",
        data={"email": "reportuser@example.com", "password": "SecurePass123"},
    )
    client.post(
        "/patients/add",
        data={"full_name": "Report Patient", "age": "63", "gender": "Other", "phone": "5559999", "email": "report@example.com", "notes": ""},
    )
    client.post(
        "/diagnose",
        data={"patient_id": "1", "image": (io.BytesIO(b"fake-image"), "sample.png")},
        content_type="multipart/form-data",
        follow_redirects=True,
    )
    response = client.get("/reports?q=Report")
    assert response.status_code == 200
    assert b"Search and review reports" in response.data
    assert b"No diagnosis reports found" not in response.data


def test_allowed_file_accepts_expected_extensions():
    assert app_module.allowed_file("scan.png") is True
    assert app_module.allowed_file("scan.jpg") is True
    assert app_module.allowed_file("scan.webp") is True
    assert app_module.allowed_file("scan.pdf") is False


def test_preprocess_image_returns_expected_shape(tmp_path):
    image_path = tmp_path / "input.png"
    Image.new("RGB", (256, 256), color=(10, 20, 30)).save(image_path)
    array = app_module.preprocess_image(str(image_path))
    assert array.shape == (1, 224, 224, 3)
    assert array.min() >= 0.0
    assert array.max() <= 1.0
