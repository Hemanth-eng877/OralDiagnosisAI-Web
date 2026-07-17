# OralDiagnosisAI-Web — Final Project Report

**Report date:** 2026-07-17  
**Assessment basis:** repository source, test assets, generated documentation, and GitHub Actions workflow configuration. Runtime CI and load-test execution results were not available during report generation.

## 1. Project Overview

OralDiagnosisAI-Web is a Flask-based clinical workflow application for user registration, authentication, patient record management, oral-image upload, TensorFlow Lite diagnosis, and diagnosis-report review. The repository also contains a React/Vite frontend, Firebase/Firestore integration, browser automation, security review assets, and k6 performance testing.

## 2. Technology Stack

| Layer | Technologies |
| --- | --- |
| Backend | Python 3, Flask 3.1.2, Werkzeug |
| Data and identity integration | Firebase Admin SDK, Firestore |
| AI/image processing | TensorFlow Lite, NumPy, Pillow |
| Frontend | React 18, Vite, Firebase JavaScript SDK |
| Test automation | pytest, Selenium, pytest-html, openpyxl, Vitest/Testing Library |
| Performance testing | k6 |
| CI/CD and security automation | GitHub Actions, Semgrep, Gitleaks, Trivy, Dependency Review |

## 3. GitHub Repository

Repository: [Hemanth-eng877/OralDiagnosisAI-Web](https://github.com/Hemanth-eng877/OralDiagnosisAI-Web)

The configured `origin` remote points to this repository. Twelve Flask routes were identified, covering authentication, dashboard, patient management, diagnosis, reports, and protected upload retrieval.

## 4. GitHub Actions

| Workflow | Purpose | Triggers | Gating behavior |
| --- | --- | --- | --- |
| `.github/workflows/test.yml` | Python tests, React build/tests, coverage/artifact upload | Push and pull request to `main`/`master` | Standard test failure |
| `.github/workflows/selenium-e2e.yml` | Chrome-based E2E test suite, HTML/Excel/XML reports, screenshots/logs | Push, pull request, manual | Only smoke-test failures gate the workflow |
| `.github/workflows/security-review.yml` | Semgrep, Gitleaks, Trivy, and dependency review | Push, pull request, manual | Critical findings only; detected secrets are classified as critical |

All three workflows are configured in source. This report does not assert that they have executed successfully in GitHub Actions.

## 5. Selenium Automation Summary

The Selenium suite defines **100 parameterized E2E cases** in `selenium/data/test_catalog.py`. It covers authentication, dashboard, patient CRUD, search/filter detection, image upload, AI diagnosis, reports, navigation, validation, session handling, and regression categories.

- Chrome and ChromeDriver are provisioned by the E2E workflow.
- Failure screenshots are captured in `selenium/screenshots/`; framework logs are captured in `selenium/reports/` and CI logs.
- The workflow produces `Selenium_Test_Report.xlsx`, `selenium-report.html`, and JUnit XML artifacts.
- The full suite is reporting-only; smoke-marked critical paths control the E2E job outcome.

## 6. Security Review Summary

The SAST review identified **10 findings**: 2 critical, 3 high, 4 medium, and 1 low.

| Severity | Count | Key risks |
| --- | ---: | --- |
| Critical | 2 | Predictable Flask secret fallback; Firebase service-account key present locally |
| High | 3 | Missing CSRF controls; upload-image IDOR; extension-only upload validation |
| Medium | 4 | Missing headers/cookie hardening; no rate limits; PHI/error exposure; weak validation |
| Low | 1 | Flask 3.1.2 advisory (CVE-2026-27205) |

**Security score: 35/100.** This score reflects unresolved critical session/credential exposure risk and high-impact data-access controls; it is not a runtime penetration-test score. Remediate critical and high findings before production deployment. Detailed evidence is available in `Vulnerability Test Results/security-review.md` and `findings.xlsx`.

## 7. Performance Testing Summary

The k6 framework supplies constant-load profiles of **100 virtual users for one minute per scenario**:

| Scenario | Script | Status |
| --- | --- | --- |
| Login | `k6/login-load.js` | Framework created; not executed |
| Dashboard and reports search | `k6/dashboard-load.js` | Framework created; not executed |
| Add patient and patient list | `k6/patient-load.js` | Framework created; not executed |
| Image upload and AI diagnosis | `k6/upload-load.js` | Framework created; not executed |

The scripts capture request rate, average/minimum/maximum response duration, throughput, error rate, and success rate. The configured thresholds are error rate below 5% and p95 duration below 2 seconds. `Performance_Test_Report.xlsx` and `Load_Test_Report.xlsx` are populated result-entry templates with response-time charts; no performance result values are claimed until an approved environment is exercised.

## 8. Test Coverage

| Measure | Count | Notes |
| --- | ---: | --- |
| Documented QA cases | 120 | From `TEST_SUMMARY.md`; includes manual and automation-planning coverage |
| Selenium E2E cases | 100 | Parameterized catalog |
| Backend Python tests | 12 | Test functions in `tests/` |
| Frontend tests | 3 | Vitest cases in React component tests |
| Executable automated cases | 115 | Selenium + backend + frontend; categories may overlap functionally |
| Flask endpoints | 12 | Route inventory in `app.py` |
| Automation coverage | 95.8% of documented QA cases | 115 executable automated cases ÷ 120 documented cases; planning metric, not pass rate |

## 9. Deployment Status

**Deployment readiness: Not ready for production; conditionally ready for controlled staging after remediation.**

Positive readiness evidence includes configured GitHub Actions, a Vite frontend build script, automated test assets, and performance/security frameworks. Production release remains blocked by the two critical and three high security findings, plus the absence of recorded CI, security-scan, and performance-test execution results. The existing deployment status workbook lists CI workflow and Vite build as configured.

## 10. Overall Project Statistics

| Statistic | Value |
| --- | --- |
| Total test cases (documented) | 120 |
| Selenium test cases | 100 |
| Backend tests | 12 |
| Frontend tests | 3 |
| Executable automated tests | 115 |
| Security findings | 10 (2 critical, 3 high, 4 medium, 1 low) |
| Performance results | Not executed; 4 k6 scenarios prepared |
| Automation coverage | 95.8% planning coverage |
| Security score | 35/100 |
| Deployment readiness | Not production-ready pending security remediation and evidence of CI/performance execution |

## Recommended Release Gate

1. Rotate/revoke the Firebase credential and remove production secret fallbacks.
2. Implement CSRF protection, upload ownership enforcement, and robust image validation.
3. Upgrade Flask and implement headers, secure cookies, validation limits, and rate limiting.
4. Run all GitHub Actions workflows successfully against the release candidate.
5. Execute k6 tests against staging, record results in the Excel reports, and approve capacity thresholds.
