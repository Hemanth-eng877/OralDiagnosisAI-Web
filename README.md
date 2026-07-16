# OralDiagnosisAI-Web

Production-ready Flask web application for oral image diagnosis using a TensorFlow Lite model.

## Features

- Flask backend with SQLite persistence
- Login, signup, logout, and session management
- Patient CRUD workflow
- Oral image upload and TensorFlow Lite prediction
- Diagnosis history and patient report search
- Bootstrap 5 responsive healthcare UI
- Selenium workflow tests

## Model Contract

Place `oral_model.tflite` in the project root.

- Input image size: `224x224`
- Normalization: pixel values divided by `255.0`
- Output shape: `[1][2]`
- Class `0`: `Normal`
- Class `1`: `OSCC`

## Run

```powershell
python -m venv venv
.\venv\Scripts\activate
pip install -r requirements.txt
python app.py
```

Open `http://127.0.0.1:5000`.

The SQLite database is created automatically from `schema.sql` as `database.db` when the app starts.

## Selenium Tests

Start the app first:

```powershell
python app.py
```

Then run:

```powershell
python tests\test_auth.py
python tests\test_upload.py
python tests\test_diagnosis_workflow.py
```

The upload and workflow tests require a valid `oral_model.tflite` in the project root.
