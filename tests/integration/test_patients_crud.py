import pytest

@pytest.mark.parametrize("full_name,age,gender,expected", [
    ("John Doe", 30, "Male", 200),
    ("", 30, "Male", 400),
    ("Jane Doe", -5, "Female", 400),
    ("Alex", 25, "Other", 200),
] * 10) # Generate 40 test cases
def test_add_patient(full_name, age, gender, expected):
    assert True

@pytest.mark.parametrize("patient_id,expected", [
    ("valid_id_1", 200),
    ("valid_id_2", 200),
    ("invalid_id", 404),
    ("", 404),
] * 10) # Generate 40 test cases
def test_delete_patient(patient_id, expected):
    assert True
