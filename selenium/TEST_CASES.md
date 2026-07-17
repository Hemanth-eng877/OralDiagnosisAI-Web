# Selenium Test Cases

| Test ID | Module | Category | Scenario | Expected Result |
|---|---|---|---|---|
| TC001 | Login | UI | Login page renders email, password, and submit controls | Login form is displayed |
| TC002 | Login | Positive | Valid user signs in | Dashboard is displayed |
| TC003 | Login | Negative | Unknown email is rejected | Invalid credentials message is displayed |
| TC004 | Login | Negative | Wrong password is rejected | Invalid credentials message is displayed |
| TC005 | Login | Validation | Empty login form is validated | Required-field validation prevents submission |
| TC006 | Login | Validation | Malformed email is validated | Email validation prevents submission |
| TC007 | Login | UI | Password field masks input | Password control has password type |
| TC008 | Login | Navigation | Signup link opens registration | Signup page is displayed |
| TC009 | Login | Boundary | Email with leading and trailing spaces is handled | Credentials are normalized |
| TC010 | Login | Boundary | Long password input is handled | Page remains usable and secure |
| TC011 | Logout | UI | Logout link is visible after authentication | Logout control is displayed |
| TC012 | Logout | Positive | Authenticated user logs out | Login page is displayed |
| TC013 | Logout | Session | Logged-out user cannot open dashboard | User is redirected to login |
| TC014 | Logout | Session | Logged-out user cannot open patients | User is redirected to login |
| TC015 | Logout | Session | Logged-out user cannot open diagnosis | User is redirected to login |
| TC016 | Dashboard | Positive | Dashboard route loads for authenticated user | Clinical Overview is displayed |
| TC017 | Dashboard | UI | Dashboard metric cards are visible | Patient and scan metrics are displayed |
| TC018 | Dashboard | UI | Dashboard chart canvas is rendered | Case distribution chart is available |
| TC019 | Dashboard | UI | Recent reports empty state is rendered | No reports message is displayed |
| TC020 | Dashboard | Navigation | View all reports link navigates | Reports page opens |
| TC021 | Add Patient | Positive | Add Patient page loads | Patient form is displayed |
| TC022 | Add Patient | Positive | Add patient with required name | Patient record is created |
| TC023 | Add Patient | Positive | Add patient with all supported fields | All entered values are saved |
| TC024 | Add Patient | Validation | Blank patient name is rejected | Patient name error is displayed |
| TC025 | Add Patient | Boundary | Age lower boundary is accepted | Age zero is accepted by the control |
| TC026 | Add Patient | Boundary | Age upper boundary is accepted | Age 120 is accepted by the control |
| TC027 | Add Patient | Boundary | Age above maximum is validated | Browser validation blocks submission |
| TC028 | Add Patient | UI | Gender options are available | Female, Male, and Other are selectable |
| TC029 | Add Patient | Validation | Invalid email is validated | Email validation prevents submission |
| TC030 | Add Patient | Navigation | Back button returns to patient list | Patient list is displayed |
| TC031 | Edit Patient | UI | Edit control is shown for a patient | Edit button is visible |
| TC032 | Edit Patient | Positive | Edit form prepopulates patient data | Existing values are displayed |
| TC033 | Edit Patient | Positive | Edit patient name | Updated name is saved |
| TC034 | Edit Patient | Positive | Edit patient optional fields | Updated optional fields are saved |
| TC035 | Edit Patient | Validation | Blank name during edit is rejected | Patient name error is displayed |
| TC036 | Edit Patient | Negative | Edit unknown patient ID | Not-found response is shown |
| TC037 | Edit Patient | Session | Edit another user's patient | Not-found response is shown |
| TC038 | Delete Patient | UI | Delete control is shown for a patient | Delete button is visible |
| TC039 | Delete Patient | Validation | Delete confirmation is requested | Browser confirmation is presented |
| TC040 | Delete Patient | Positive | Confirmed deletion removes record | Patient disappears from list |
| TC041 | Delete Patient | Negative | Cancelled deletion retains record | Patient remains in list |
| TC042 | Delete Patient | Negative | Delete unknown patient ID | Not-found response is shown |
| TC043 | Delete Patient | Session | Delete another user's patient | Not-found response is shown |
| TC044 | Search Patient | UI | Patient search control is detected | Search input is available when supported |
| TC045 | Search Patient | Positive | Search by full patient name | Matching patient is displayed |
| TC046 | Search Patient | Positive | Search by partial patient name | Partial matches are displayed |
| TC047 | Search Patient | Negative | Search with no matching patient | No-results state is displayed |
| TC048 | Search Patient | Boundary | Search ignores letter case | Matching patient is displayed |
| TC049 | Search Patient | UI | Clear patient search | Full patient list is restored |
| TC050 | Filter Patient | UI | Patient filter control is detected | Filter control is available when supported |
| TC051 | Filter Patient | Positive | Filter patients by gender | Only selected gender is displayed |
| TC052 | Filter Patient | Positive | Filter patients by active status | Only active records are displayed |
| TC053 | Filter Patient | Positive | Apply multiple patient filters | Records meet all selected filters |
| TC054 | Filter Patient | UI | Clear patient filters | Unfiltered list is restored |
| TC055 | Filter Patient | Negative | Filter with no matches | No-results state is displayed |
| TC056 | Upload Image | Positive | Diagnosis page loads | Upload form is displayed |
| TC057 | Upload Image | Positive | Image input accepts PNG | PNG file is selectable |
| TC058 | Upload Image | Positive | Image input accepts JPG | JPG file is selectable |
| TC059 | Upload Image | Positive | Image input accepts JPEG | JPEG file is selectable |
| TC060 | Upload Image | Positive | Image input accepts WEBP | WEBP file is selectable |
| TC061 | Upload Image | Negative | Unsupported file type is rejected | Supported-format error is displayed |
| TC062 | Upload Image | Validation | No image submission is rejected | Upload-required error is displayed |
| TC063 | Upload Image | Validation | No patient submission is rejected | Patient-required error is displayed |
| TC064 | Upload Image | UI | Upload control exposes accepted formats | Accept attribute lists supported image formats |
| TC065 | Upload Image | Accessibility | Upload control is keyboard reachable | Input can receive focus |
| TC066 | AI Diagnosis | UI | Run Diagnosis button is visible | Diagnosis action is available |
| TC067 | AI Diagnosis | Positive | Valid image produces a result | Diagnosis and confidence are displayed |
| TC068 | AI Diagnosis | Positive | Diagnosis result shows selected patient | Patient name is displayed with result |
| TC069 | AI Diagnosis | Positive | Diagnosis result includes confidence | Confidence percentage is displayed |
| TC070 | AI Diagnosis | UI | Diagnosis result includes image preview | Uploaded image preview is displayed |
| TC071 | AI Diagnosis | Negative | Model failure returns an error | Error message is displayed without crashing |
| TC072 | AI Diagnosis | Boundary | Oversized image is rejected | Size-limit response is displayed |
| TC073 | AI Diagnosis | Positive | Diagnosis is persisted in history | Report is available after successful diagnosis |
| TC074 | View Reports | Positive | Reports page loads | Reports heading and search are displayed |
| TC075 | View Reports | UI | Reports empty state is displayed | No reports message is displayed |
| TC076 | View Reports | Positive | Report search by patient name | Matching report is displayed |
| TC077 | View Reports | Negative | Report search with no match | No reports message is displayed |
| TC078 | View Reports | Boundary | Report search ignores case | Matching report is displayed |
| TC079 | View Reports | UI | Report table headers are visible | Patient, diagnosis, confidence, image, and date are shown |
| TC080 | View Reports | Positive | View image link opens uploaded image | Image is available to authenticated user |
| TC081 | Navigation | Navigation | Brand opens dashboard | Dashboard route opens |
| TC082 | Navigation | Navigation | Patients navigation opens patient list | Patients route opens |
| TC083 | Navigation | Navigation | Diagnosis navigation opens upload page | Diagnosis route opens |
| TC084 | Navigation | Navigation | Reports navigation opens reports page | Reports route opens |
| TC085 | Navigation | Navigation | New Diagnosis button opens upload page | Diagnosis route opens |
| TC086 | Navigation | UI | Current page remains responsive after navigation | Destination page markers are visible |
| TC087 | Session Handling | Session | Root redirects anonymous user to login | Login route opens |
| TC088 | Session Handling | Session | Authenticated root redirects to dashboard | Dashboard route opens |
| TC089 | Session Handling | Session | Session survives navigation | Authenticated pages remain available |
| TC090 | Session Handling | Session | Logout clears protected-page access | Protected route redirects to login |
| TC091 | Session Handling | Session | Direct upload URL requires authentication | Login route opens |
| TC092 | Error Messages | Accessibility | Invalid login error has alert role | Accessible error alert is displayed |
| TC093 | Error Messages | Accessibility | Missing patient name error has alert role | Accessible error alert is displayed |
| TC094 | Error Messages | Accessibility | Invalid upload type error has alert role | Accessible error alert is displayed |
| TC095 | Error Messages | UI | Error alert can be dismissed when close control exists | Alert can be dismissed |
| TC096 | Form Validation | Validation | Login email field is required | Required attribute is present |
| TC097 | Form Validation | Validation | Login password field is required | Required attribute is present |
| TC098 | Form Validation | Validation | Signup name field is required | Required attribute is present |
| TC099 | Form Validation | Boundary | Signup password minimum length is enforced | Minimum length is eight |
| TC100 | Form Validation | Validation | Patient name field is required | Required attribute is present |
