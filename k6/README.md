# k6 Performance Framework

All scenarios use a constant **100 virtual users for 1 minute**. Each virtual user creates a synthetic `@example.com` account and uses its own session cookie. Run only against a dedicated non-production environment because patient and diagnosis records are created.

## Prerequisites

- Start the Flask application: `python app.py`
- Install [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/).
- Ensure the target has Firebase/test data storage configured. The upload scenario invokes the TensorFlow model and is deliberately the most resource-intensive test.

## Run locally

From the repository root, set the target (optional) and run each scenario:

```powershell
$env:BASE_URL = 'http://127.0.0.1:5000'
$env:REPORT_DIR = 'k6/reports'
k6 run k6/login-load.js
k6 run k6/dashboard-load.js
k6 run k6/patient-load.js
k6 run k6/upload-load.js
```

Each run writes a machine-readable summary to `k6/reports/`. k6's terminal summary includes request rate, average/minimum/maximum response time, throughput, error rate, and success rate. Preserve these JSON files as build artifacts and copy the observed metrics into the supplied Excel report templates.

## Run in GitHub Actions

Use a self-hosted runner or a dedicated staging environment; do not run 100-VU upload traffic against production. Install k6, start or target the staging deployment, then run:

```yaml
- name: Run k6 load tests
  env:
    BASE_URL: ${{ secrets.STAGING_BASE_URL }}
    REPORT_DIR: k6/reports
  run: |
    k6 run k6/login-load.js
    k6 run k6/dashboard-load.js
    k6 run k6/patient-load.js
    k6 run k6/upload-load.js

- uses: actions/upload-artifact@v4
  if: always()
  with:
    name: k6-performance-reports
    path: k6/reports/
```
