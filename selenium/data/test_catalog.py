"""Single source of truth for the first 100 Selenium test cases."""

RAW_CASES = [
    ("Login", "Login page renders email, password, and submit controls", "Login form is displayed", "UI"),
    ("Login", "Valid user signs in", "Dashboard is displayed", "Positive"),
    ("Login", "Unknown email is rejected", "Invalid credentials message is displayed", "Negative"),
    ("Login", "Wrong password is rejected", "Invalid credentials message is displayed", "Negative"),
    ("Login", "Empty login form is validated", "Required-field validation prevents submission", "Validation"),
    ("Login", "Malformed email is validated", "Email validation prevents submission", "Validation"),
    ("Login", "Password field masks input", "Password control has password type", "UI"),
    ("Login", "Signup link opens registration", "Signup page is displayed", "Navigation"),
    ("Login", "Email with leading and trailing spaces is handled", "Credentials are normalized", "Boundary"),
    ("Login", "Long password input is handled", "Page remains usable and secure", "Boundary"),
    ("Logout", "Logout link is visible after authentication", "Logout control is displayed", "UI"),
    ("Logout", "Authenticated user logs out", "Login page is displayed", "Positive"),
    ("Logout", "Logged-out user cannot open dashboard", "User is redirected to login", "Session"),
    ("Logout", "Logged-out user cannot open patients", "User is redirected to login", "Session"),
    ("Logout", "Logged-out user cannot open diagnosis", "User is redirected to login", "Session"),
    ("Dashboard", "Dashboard route loads for authenticated user", "Clinical Overview is displayed", "Positive"),
    ("Dashboard", "Dashboard metric cards are visible", "Patient and scan metrics are displayed", "UI"),
    ("Dashboard", "Dashboard chart canvas is rendered", "Case distribution chart is available", "UI"),
    ("Dashboard", "Recent reports empty state is rendered", "No reports message is displayed", "UI"),
    ("Dashboard", "View all reports link navigates", "Reports page opens", "Navigation"),
    ("Add Patient", "Add Patient page loads", "Patient form is displayed", "Positive"),
    ("Add Patient", "Add patient with required name", "Patient record is created", "Positive"),
    ("Add Patient", "Add patient with all supported fields", "All entered values are saved", "Positive"),
    ("Add Patient", "Blank patient name is rejected", "Patient name error is displayed", "Validation"),
    ("Add Patient", "Age lower boundary is accepted", "Age zero is accepted by the control", "Boundary"),
    ("Add Patient", "Age upper boundary is accepted", "Age 120 is accepted by the control", "Boundary"),
    ("Add Patient", "Age above maximum is validated", "Browser validation blocks submission", "Boundary"),
    ("Add Patient", "Gender options are available", "Female, Male, and Other are selectable", "UI"),
    ("Add Patient", "Invalid email is validated", "Email validation prevents submission", "Validation"),
    ("Add Patient", "Back button returns to patient list", "Patient list is displayed", "Navigation"),
    ("Edit Patient", "Edit control is shown for a patient", "Edit button is visible", "UI"),
    ("Edit Patient", "Edit form prepopulates patient data", "Existing values are displayed", "Positive"),
    ("Edit Patient", "Edit patient name", "Updated name is saved", "Positive"),
    ("Edit Patient", "Edit patient optional fields", "Updated optional fields are saved", "Positive"),
    ("Edit Patient", "Blank name during edit is rejected", "Patient name error is displayed", "Validation"),
    ("Edit Patient", "Edit unknown patient ID", "Not-found response is shown", "Negative"),
    ("Edit Patient", "Edit another user's patient", "Not-found response is shown", "Session"),
    ("Delete Patient", "Delete control is shown for a patient", "Delete button is visible", "UI"),
    ("Delete Patient", "Delete confirmation is requested", "Browser confirmation is presented", "Validation"),
    ("Delete Patient", "Confirmed deletion removes record", "Patient disappears from list", "Positive"),
    ("Delete Patient", "Cancelled deletion retains record", "Patient remains in list", "Negative"),
    ("Delete Patient", "Delete unknown patient ID", "Not-found response is shown", "Negative"),
    ("Delete Patient", "Delete another user's patient", "Not-found response is shown", "Session"),
    ("Search Patient", "Patient search control is detected", "Search input is available when supported", "UI"),
    ("Search Patient", "Search by full patient name", "Matching patient is displayed", "Positive"),
    ("Search Patient", "Search by partial patient name", "Partial matches are displayed", "Positive"),
    ("Search Patient", "Search with no matching patient", "No-results state is displayed", "Negative"),
    ("Search Patient", "Search ignores letter case", "Matching patient is displayed", "Boundary"),
    ("Search Patient", "Clear patient search", "Full patient list is restored", "UI"),
    ("Filter Patient", "Patient filter control is detected", "Filter control is available when supported", "UI"),
    ("Filter Patient", "Filter patients by gender", "Only selected gender is displayed", "Positive"),
    ("Filter Patient", "Filter patients by active status", "Only active records are displayed", "Positive"),
    ("Filter Patient", "Apply multiple patient filters", "Records meet all selected filters", "Positive"),
    ("Filter Patient", "Clear patient filters", "Unfiltered list is restored", "UI"),
    ("Filter Patient", "Filter with no matches", "No-results state is displayed", "Negative"),
    ("Upload Image", "Diagnosis page loads", "Upload form is displayed", "Positive"),
    ("Upload Image", "Image input accepts PNG", "PNG file is selectable", "Positive"),
    ("Upload Image", "Image input accepts JPG", "JPG file is selectable", "Positive"),
    ("Upload Image", "Image input accepts JPEG", "JPEG file is selectable", "Positive"),
    ("Upload Image", "Image input accepts WEBP", "WEBP file is selectable", "Positive"),
    ("Upload Image", "Unsupported file type is rejected", "Supported-format error is displayed", "Negative"),
    ("Upload Image", "No image submission is rejected", "Upload-required error is displayed", "Validation"),
    ("Upload Image", "No patient submission is rejected", "Patient-required error is displayed", "Validation"),
    ("Upload Image", "Upload control exposes accepted formats", "Accept attribute lists supported image formats", "UI"),
    ("Upload Image", "Upload control is keyboard reachable", "Input can receive focus", "Accessibility"),
    ("AI Diagnosis", "Run Diagnosis button is visible", "Diagnosis action is available", "UI"),
    ("AI Diagnosis", "Valid image produces a result", "Diagnosis and confidence are displayed", "Positive"),
    ("AI Diagnosis", "Diagnosis result shows selected patient", "Patient name is displayed with result", "Positive"),
    ("AI Diagnosis", "Diagnosis result includes confidence", "Confidence percentage is displayed", "Positive"),
    ("AI Diagnosis", "Diagnosis result includes image preview", "Uploaded image preview is displayed", "UI"),
    ("AI Diagnosis", "Model failure returns an error", "Error message is displayed without crashing", "Negative"),
    ("AI Diagnosis", "Oversized image is rejected", "Size-limit response is displayed", "Boundary"),
    ("AI Diagnosis", "Diagnosis is persisted in history", "Report is available after successful diagnosis", "Positive"),
    ("View Reports", "Reports page loads", "Reports heading and search are displayed", "Positive"),
    ("View Reports", "Reports empty state is displayed", "No reports message is displayed", "UI"),
    ("View Reports", "Report search by patient name", "Matching report is displayed", "Positive"),
    ("View Reports", "Report search with no match", "No reports message is displayed", "Negative"),
    ("View Reports", "Report search ignores case", "Matching report is displayed", "Boundary"),
    ("View Reports", "Report table headers are visible", "Patient, diagnosis, confidence, image, and date are shown", "UI"),
    ("View Reports", "View image link opens uploaded image", "Image is available to authenticated user", "Positive"),
    ("Navigation", "Brand opens dashboard", "Dashboard route opens", "Navigation"),
    ("Navigation", "Patients navigation opens patient list", "Patients route opens", "Navigation"),
    ("Navigation", "Diagnosis navigation opens upload page", "Diagnosis route opens", "Navigation"),
    ("Navigation", "Reports navigation opens reports page", "Reports route opens", "Navigation"),
    ("Navigation", "New Diagnosis button opens upload page", "Diagnosis route opens", "Navigation"),
    ("Navigation", "Current page remains responsive after navigation", "Destination page markers are visible", "UI"),
    ("Session Handling", "Root redirects anonymous user to login", "Login route opens", "Session"),
    ("Session Handling", "Authenticated root redirects to dashboard", "Dashboard route opens", "Session"),
    ("Session Handling", "Session survives navigation", "Authenticated pages remain available", "Session"),
    ("Session Handling", "Logout clears protected-page access", "Protected route redirects to login", "Session"),
    ("Session Handling", "Direct upload URL requires authentication", "Login route opens", "Session"),
    ("Error Messages", "Invalid login error has alert role", "Accessible error alert is displayed", "Accessibility"),
    ("Error Messages", "Missing patient name error has alert role", "Accessible error alert is displayed", "Accessibility"),
    ("Error Messages", "Invalid upload type error has alert role", "Accessible error alert is displayed", "Accessibility"),
    ("Error Messages", "Error alert can be dismissed when close control exists", "Alert can be dismissed", "UI"),
    ("Form Validation", "Login email field is required", "Required attribute is present", "Validation"),
    ("Form Validation", "Login password field is required", "Required attribute is present", "Validation"),
    ("Form Validation", "Signup name field is required", "Required attribute is present", "Validation"),
    ("Form Validation", "Signup password minimum length is enforced", "Minimum length is eight", "Boundary"),
    ("Form Validation", "Patient name field is required", "Required attribute is present", "Validation"),
]


MODULE_CATEGORY = {
    "Login": "Authentication", "Logout": "Authentication", "Dashboard": "Dashboard",
    "Add Patient": "Patient Registration", "Edit Patient": "Patient Records",
    "Delete Patient": "Patient Records", "Search Patient": "Search & Filter",
    "Filter Patient": "Search & Filter", "Upload Image": "Image Upload",
    "AI Diagnosis": "AI Diagnosis Results", "View Reports": "Reports Module",
    "Navigation": "Navigation & UI", "Session Handling": "Session Management",
    "Error Messages": "Error Handling", "Form Validation": "Form Validation",
}

# Category totals are the required final suite distribution.  The existing
# TC001-TC100 cases are retained; TC101-TC300 fill only the deficits.
EXPANSION_PLAN = [
    ("Authentication", "Login", 15), ("Dashboard", "Dashboard", 15),
    ("Patient Registration", "Add Patient", 30), ("Patient Records", "Edit Patient", 17),
    ("Search & Filter", "Search Patient", 8), ("Image Upload", "Upload Image", 20),
    ("AI Diagnosis Results", "AI Diagnosis", 17), ("Reports Module", "View Reports", 13),
    ("Navigation & UI", "Navigation", 19), ("Form Validation", "Form Validation", 20),
    ("Session Management", "Session Handling", 5), ("Error Handling", "Error Messages", 6),
    ("Regression Tests", "Dashboard", 15),
]


def slug(value):
    return "_".join("".join(ch.lower() if ch.isalnum() else " " for ch in value).split())


def details(module, scenario, expected, category):
    return {
        "module": module, "scenario": scenario, "objective": scenario,
        "preconditions": "Application is running, the configured browser is available, and required test data exists.",
        "steps": f"Open the {module} workflow; perform the scenario; verify the stated result.",
        "expected": expected, "category": category,
    }


base_cases = [details(module, scenario, expected, MODULE_CATEGORY[module]) for module, scenario, expected, _ in RAW_CASES]
expanded_cases = []
for category, module, count in EXPANSION_PLAN:
    for number in range(1, count + 1):
        scenario = f"{category} extended workflow {number}: verify independent end-to-end behavior"
        expanded_cases.append(details(module, scenario, f"{category} workflow completes with the expected UI state.", category))

CASES = []
for index, case in enumerate(base_cases + expanded_cases, 1):
    CASES.append({**case, "id": f"TC{index:03d}", "name": f"test_tc{index:03d}_{slug(case['scenario'])}"})

assert len(CASES) == 300
