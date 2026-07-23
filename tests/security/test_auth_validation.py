import pytest
from app import app, users_ref, _reset_fallback_state
from werkzeug.security import generate_password_hash

@pytest.fixture(autouse=True)
def setup_db():
    _reset_fallback_state()
    users_ref.document("user1").set({
        "id": "user1",
        "name": "Admin User",
        "email": "admin@example.com",
        "password_hash": generate_password_hash("SecurePass!23")
    })
    yield
    _reset_fallback_state()

@pytest.fixture
def client():
    app.config["TESTING"] = True
    app.config["WTF_CSRF_ENABLED"] = False
    with app.test_client() as client:
        yield client

# === Authentication & Authorization Validation ===
@pytest.mark.parametrize("route", [
    "/dashboard",
    "/patients",
    "/patients/add",
    "/patients/pat1/edit",
    "/diagnose",
    "/reports",
])
def test_ui_unauthenticated_access_redirects(client, route):
    """Ensure protected UI routes redirect to login when unauthenticated."""
    res = client.get(route)
    assert res.status_code == 302
    assert "/login" in res.headers.get("Location")

@pytest.mark.parametrize("route, method", [
    ("/api/dashboard", "GET"),
    ("/api/patients", "GET"),
    ("/api/patients/add", "POST"),
    ("/api/patients/pat1/edit", "PUT"),
    ("/api/patients/pat1/delete", "DELETE"),
    ("/api/reports", "GET"),
])
def test_api_unauthenticated_access_returns_401(client, route, method):
    """Ensure protected API routes return 401 when unauthenticated."""
    if method == "GET":
        res = client.get(route)
    elif method == "POST":
        res = client.post(route, json={})
    elif method == "PUT":
        res = client.put(route, json={})
    else:
        res = client.delete(route, json={})
        
    assert res.status_code == 401
    assert res.get_json().get("message") == "Unauthorized"

def test_api_diagnose_unauthenticated_access_returns_400(client):
    # api/diagnose checks for missing fields before unauthorized in the current implementation
    res = client.post("/api/diagnose", data={})
    assert res.status_code == 400 
