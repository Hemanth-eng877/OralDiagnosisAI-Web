import os
import uuid
from datetime import datetime
from functools import wraps

import firebase_admin
import numpy as np
from flask import (
    abort,
    Flask,
    flash,
    redirect,
    render_template,
    request,
    send_from_directory,
    session,
    url_for,
)
from PIL import Image
from firebase_admin import credentials, firestore
from werkzeug.security import check_password_hash, generate_password_hash
from werkzeug.utils import secure_filename


BASE_DIR = os.path.abspath(os.path.dirname(__file__))
UPLOAD_FOLDER = os.path.join(BASE_DIR, "uploads")
MODEL_PATH = os.path.join(BASE_DIR, "oral_model.tflite")
ALLOWED_EXTENSIONS = {"png", "jpg", "jpeg", "webp"}
CLASS_LABELS = {0: "Normal", 1: "OSCC"}

app = Flask(__name__)
app.config["SECRET_KEY"] = os.environ.get("SECRET_KEY", "change-this-secret-key-in-production")
app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER
app.config["MAX_CONTENT_LENGTH"] = 8 * 1024 * 1024

interpreter = None
input_details = None
output_details = None

if not firebase_admin._apps:
    cred = credentials.Certificate("firebase-key.json")
    firebase_admin.initialize_app(cred)
db = firestore.client()
users_ref = db.collection("users")
patients_ref = db.collection("patients")
diagnoses_ref = db.collection("diagnosis_records")


def load_model():
    global interpreter, input_details, output_details
    if interpreter is not None:
        return
    if not os.path.exists(MODEL_PATH):
        app.logger.warning("oral_model.tflite was not found at %s", MODEL_PATH)
        return
    import tensorflow as tf

    interpreter = tf.lite.Interpreter(model_path=MODEL_PATH)
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()


def login_required(view):
    @wraps(view)
    def wrapped_view(*args, **kwargs):
        if "user_id" not in session:
            flash("Please log in to continue.", "warning")
            return redirect(url_for("login"))
        return view(*args, **kwargs)

    return wrapped_view


def allowed_file(filename):
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_EXTENSIONS


def _serialize_doc(doc):
    data = doc.to_dict() or {}
    data["id"] = doc.id
    return data


def _created_at_value(data):
    created_at = data.get("created_at")
    if hasattr(created_at, "to_datetime"):
        return created_at.to_datetime()
    return created_at or datetime.min


def _get_user_by_email(email):
    docs = users_ref.where("email", "==", email).limit(1).stream()
    for doc in docs:
        return _serialize_doc(doc)
    return None


def _get_user_by_id(user_id):
    doc = users_ref.document(user_id).get()
    if not doc.exists:
        return None
    return _serialize_doc(doc)


def _get_patient_by_id(patient_id):
    doc = patients_ref.document(str(patient_id)).get()
    if not doc.exists:
        return None
    return _serialize_doc(doc)


def _get_diagnosis_by_id(diagnosis_id):
    doc = diagnoses_ref.document(str(diagnosis_id)).get()
    if not doc.exists:
        return None
    return _serialize_doc(doc)


def current_user():
    if "user_id" not in session:
        return None
    return _get_user_by_id(session["user_id"])


@app.context_processor
def inject_user():
    return {"current_user": current_user()}


def preprocess_image(image_path):
    image = Image.open(image_path).convert("RGB")
    image = image.resize((224, 224))
    image_array = np.asarray(image, dtype=np.float32) / 255.0
    return np.expand_dims(image_array, axis=0)


def predict_image(image_path):
    load_model()
    if interpreter is None:
        raise RuntimeError("Model file oral_model.tflite is missing from the project root.")

    image_array = preprocess_image(image_path)
    expected_dtype = input_details[0]["dtype"]
    if expected_dtype != np.float32:
        image_array = image_array.astype(expected_dtype)

    interpreter.set_tensor(input_details[0]["index"], image_array)
    interpreter.invoke()
    predictions = interpreter.get_tensor(output_details[0]["index"])
    scores = np.asarray(predictions[0], dtype=np.float32)
    class_index = int(np.argmax(scores))
    confidence = float(scores[class_index])

    if confidence <= 1.0:
        confidence *= 100.0

    return CLASS_LABELS.get(class_index, "Unknown"), round(confidence, 2)


@app.route("/")
def index():
    if "user_id" in session:
        return redirect(url_for("dashboard"))
    return redirect(url_for("login"))


@app.route("/signup", methods=["GET", "POST"])
def signup():
    if request.method == "POST":
        name = request.form.get("name", "").strip()
        email = request.form.get("email", "").strip().lower()
        password = request.form.get("password", "")

        if not name or not email or not password:
            flash("All fields are required.", "danger")
            return render_template("signup.html")

        if not email or not email.endswith("@gmail.com") or "@" not in email:
            flash("Please enter a valid Gmail address.", "danger")
            return render_template("signup.html")

        existing = _get_user_by_email(email)
        if existing:
            flash("An account with this Gmail already exists.", "danger")
            return render_template("signup.html")

        user_ref = users_ref.document()
        user_id = user_ref.id
        user_ref.set(
            {
                "id": user_id,
                "name": name,
                "email": email,
                "password_hash": generate_password_hash(password),
            }
        )
        flash("Account created. Please log in.", "success")
        return redirect(url_for("login"))

    return render_template("signup.html")


@app.route("/login", methods=["GET", "POST"])
def login():
    if request.method == "POST":
        email = request.form.get("email", "").strip().lower()
        password = request.form.get("password", "")
        user = _get_user_by_email(email)

        if user is None or not check_password_hash(user["password_hash"], password):
            flash("Invalid email or password.", "danger")
            return render_template("login.html")

        session.clear()
        session["user_id"] = user["id"]
        session["user_name"] = user["name"]
        flash("Welcome back.", "success")
        return redirect(url_for("dashboard"))

    return render_template("login.html")


@app.route("/logout")
@login_required
def logout():
    session.clear()
    flash("You have been logged out.", "info")
    return redirect(url_for("login"))


@app.route("/dashboard")
@login_required
def dashboard():
    user_id = session["user_id"]
    patients = list(patients_ref.where("user_id", "==", user_id).stream())
    diagnoses = list(diagnoses_ref.where("user_id", "==", user_id).stream())

    patient_count = len(patients)
    report_count = len(diagnoses)
    oscc_count = sum(1 for doc in diagnoses if (doc.to_dict() or {}).get("diagnosis") == "OSCC")

    recent_reports = []
    for doc in sorted(diagnoses, key=lambda item: _created_at_value(item.to_dict() or {}), reverse=True)[:5]:
        data = doc.to_dict() or {}
        patient_doc = patients_ref.document(str(data.get("patient_id"))).get()
        patient_name = patient_doc.to_dict().get("full_name") if patient_doc.exists else ""
        payload = {
            "id": doc.id,
            "patient_id": data.get("patient_id"),
            "patient_name": patient_name,
            "diagnosis": data.get("diagnosis"),
            "confidence": data.get("confidence"),
            "image_filename": data.get("image_filename"),
            "created_at": data.get("created_at"),
        }
        recent_reports.append(payload)

    return render_template(
        "dashboard.html",
        patient_count=patient_count,
        report_count=report_count,
        oscc_count=oscc_count,
        recent_reports=recent_reports,
    )


@app.route("/patients")
@login_required
def patients():
    records = []
    for doc in patients_ref.where("user_id", "==", session["user_id"]).stream():
        data = _serialize_doc(doc)
        records.append(data)
    records.sort(key=lambda item: _created_at_value(item), reverse=True)
    return render_template("patients.html", patients=records)


@app.route("/patients/add", methods=["GET", "POST"])
@login_required
def add_patient():
    if request.method == "POST":
        fields = patient_form_data()
        if not fields["full_name"]:
            flash("Patient name is required.", "danger")
            return render_template("patient_form.html", patient=fields, mode="Add")

        patient_ref = patients_ref.document()
        patient_id = patient_ref.id
        patient_ref.set(
            {
                "id": patient_id,
                "user_id": session["user_id"],
                "full_name": fields["full_name"],
                "age": fields["age"],
                "gender": fields["gender"],
                "phone": fields["phone"],
                "email": fields["email"],
                "notes": fields["notes"],
                "created_at": firestore.SERVER_TIMESTAMP,
            }
        )
        flash("Patient record added.", "success")
        return redirect(url_for("patients"))

    return render_template("patient_form.html", patient={}, mode="Add")


@app.route("/patients/<patient_id>/edit", methods=["GET", "POST"])
@login_required
def edit_patient(patient_id):
    patient = get_patient_or_404(patient_id)
    if request.method == "POST":
        fields = patient_form_data()
        if not fields["full_name"]:
            flash("Patient name is required.", "danger")
            return render_template("patient_form.html", patient=fields, mode="Edit")

        patients_ref.document(str(patient_id)).set(
            {
                "id": patient_id,
                "user_id": session["user_id"],
                "full_name": fields["full_name"],
                "age": fields["age"],
                "gender": fields["gender"],
                "phone": fields["phone"],
                "email": fields["email"],
                "notes": fields["notes"],
            },
            merge=True,
        )
        flash("Patient record updated.", "success")
        return redirect(url_for("patients"))

    return render_template("patient_form.html", patient=patient, mode="Edit")


@app.route("/patients/<patient_id>/delete", methods=["POST"])
@login_required
def delete_patient(patient_id):
    get_patient_or_404(patient_id)
    patients_ref.document(str(patient_id)).delete()
    flash("Patient record deleted.", "info")
    return redirect(url_for("patients"))


def patient_form_data():
    age_raw = request.form.get("age", "").strip()
    age = int(age_raw) if age_raw.isdigit() else None
    return {
        "full_name": request.form.get("full_name", "").strip(),
        "age": age,
        "gender": request.form.get("gender", "").strip(),
        "phone": request.form.get("phone", "").strip(),
        "email": request.form.get("email", "").strip(),
        "notes": request.form.get("notes", "").strip(),
    }


def get_patient_or_404(patient_id):
    patient = _get_patient_by_id(patient_id)
    if patient is None or patient.get("user_id") != session["user_id"]:
        abort(404)
    return patient


@app.route("/diagnose", methods=["GET", "POST"])
@login_required
def diagnose():
    patients_list = []
    for doc in patients_ref.where("user_id", "==", session["user_id"]).stream():
        patients_list.append(_serialize_doc(doc))
    patients_list.sort(key=lambda item: item.get("full_name", ""))

    if request.method == "POST":
        patient_id = request.form.get("patient_id")
        patient = _get_patient_by_id(patient_id)
        file = request.files.get("image")

        if patient is None or patient.get("user_id") != session["user_id"]:
            flash("Please select a valid patient.", "danger")
            return render_template("diagnose.html", patients=patients_list)
        if file is None or file.filename == "":
            flash("Please upload an oral image.", "danger")
            return render_template("diagnose.html", patients=patients_list)
        if not allowed_file(file.filename):
            flash("Supported image formats are PNG, JPG, JPEG, and WEBP.", "danger")
            return render_template("diagnose.html", patients=patients_list)

        filename = secure_filename(file.filename)
        unique_filename = f"{uuid.uuid4().hex}_{filename}"
        saved_path = os.path.join(app.config["UPLOAD_FOLDER"], unique_filename)
        file.save(saved_path)

        try:
            diagnosis, confidence = predict_image(saved_path)
        except Exception as exc:
            os.remove(saved_path)
            flash(str(exc), "danger")
            return render_template("diagnose.html", patients=patients_list)

        try:
            if "user_id" not in session:
                raise ValueError("session['user_id'] is missing")
            if patient is None:
                raise ValueError("patient is missing")
            if patient.get("id") is None:
                raise ValueError("patient['id'] is missing")
            if patient.get("full_name") is None:
                raise ValueError("patient['full_name'] is missing")
            if not unique_filename:
                raise ValueError("unique_filename is missing")
            if diagnosis is None:
                raise ValueError("diagnosis is missing")
            if confidence is None:
                raise ValueError("confidence is missing")

            app.logger.info(
                "Saving diagnosis to Firestore: user_id=%s patient_id=%s patient_name=%s image=%s diagnosis=%s confidence=%s",
                session["user_id"],
                patient["id"],
                patient.get("full_name"),
                unique_filename,
                diagnosis,
                confidence,
            )

            diagnosis_ref = db.collection("diagnosis_records").document()
            diagnosis_data = {
                "id": diagnosis_ref.id,
                "user_id": session["user_id"],
                "patient_id": patient["id"],
                "patient_name": patient["full_name"],
                "image_filename": unique_filename,
                "diagnosis": diagnosis,
                "confidence": float(confidence),
                "created_at": firestore.SERVER_TIMESTAMP,
            }
            diagnosis_ref.set(diagnosis_data)
            app.logger.info("Diagnosis saved to Firestore successfully: %s", diagnosis_ref.id)
        except Exception as e:
            app.logger.exception("Firestore save failed")
            flash(str(e), "danger")
            return render_template(
                "diagnose.html",
                patients=patients_list,
                result={"patient": patient, "diagnosis": diagnosis, "confidence": confidence, "image_filename": unique_filename},
            )

        flash("Diagnosis completed and saved.", "success")
        return render_template(
            "diagnose.html",
            patients=patients_list,
            result={"patient": patient, "diagnosis": diagnosis, "confidence": confidence, "image_filename": unique_filename},
        )

    return render_template("diagnose.html", patients=patients_list)


@app.route("/reports")
@login_required
def reports():
    search = request.args.get("q", "").strip()
    records = []
    for doc in diagnoses_ref.where("user_id", "==", session["user_id"]).stream():
        data = doc.to_dict() or {}
        patient_doc = patients_ref.document(str(data.get("patient_id"))).get()
        patient_name = patient_doc.to_dict().get("full_name") if patient_doc.exists else ""
        patient_age = patient_doc.to_dict().get("age") if patient_doc.exists else None
        patient_gender = patient_doc.to_dict().get("gender") if patient_doc.exists else ""
        payload = {
            "id": data.get("id") or doc.id,
            "patient_id": data.get("patient_id"),
            "patient_name": patient_name,
            "age": patient_age,
            "gender": patient_gender,
            "image_filename": data.get("image_filename"),
            "diagnosis": data.get("diagnosis"),
            "confidence": data.get("confidence"),
            "created_at": data.get("created_at"),
        }
        records.append(payload)

    if search:
        records = [record for record in records if search.lower() in (record.get("patient_name") or "").lower()]

    records.sort(key=lambda item: _created_at_value(item), reverse=True)
    return render_template("reports.html", reports=records, search=search)


@app.route("/uploads/<path:filename>")
@login_required
def uploaded_file(filename):
    return send_from_directory(app.config["UPLOAD_FOLDER"], filename)


if __name__ == "__main__":
    os.makedirs(UPLOAD_FOLDER, exist_ok=True)
    load_model()
    app.run(debug=False, host="127.0.0.1", port=5000)
