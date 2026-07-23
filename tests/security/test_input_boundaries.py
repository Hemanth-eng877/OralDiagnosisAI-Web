import pytest
from app import app, patients_ref, _reset_fallback_state

@pytest.fixture(autouse=True)
def setup_db():
    _reset_fallback_state()
    yield
    _reset_fallback_state()

@pytest.fixture
def auth_client():
    app.config["TESTING"] = True
    app.config["WTF_CSRF_ENABLED"] = False
    with app.test_client() as client:
        with client.session_transaction() as sess:
            sess['user_id'] = 'user1'
        yield client

# === Input Boundary Validation ===
@pytest.mark.parametrize("name_length", [
    0,      # Empty
    1,      # Shortest
    255,    # Typical max length
    10000,  # Extremely long string
])
def test_patient_name_boundaries(auth_client, name_length):
    name = "A" * name_length
    res = auth_client.post("/api/patients/add", json={"full_name": name})
    
    if name_length == 0:
        assert res.status_code == 400
        assert res.get_json()["message"] == "Patient name is required."
    else:
        # The application should either accept it or return 400 if it validates max length
        assert res.status_code in [200, 400, 413]

@pytest.mark.parametrize("age", [
    -1, 0, 150, 999, "invalid", None
])
def test_patient_age_boundaries(auth_client, age):
    res = auth_client.post("/api/patients/add", json={"full_name": "Test", "age": age})
    # Since the application doesn't strictly validate age boundaries in the API, 
    # it should generally accept it and not crash (return 500).
    assert res.status_code in [200, 400]

@pytest.mark.parametrize("special_chars", [
    "Test User !@#$%^&*()_+",
    "User\nWith\nNewlines",
    "User\tWith\tTabs",
    "User\x00With\x00Nulls",
])
def test_patient_name_special_characters(auth_client, special_chars):
    res = auth_client.post("/api/patients/add", json={"full_name": special_chars})
    assert res.status_code in [200, 400]
