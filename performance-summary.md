# Performance Test Summary

## Test configuration

| Setting | Value |
| --- | --- |
| Tool | k6 |
| Load model | Constant virtual users |
| Virtual users | 100 |
| Duration | 1 minute per scenario |
| Target | `BASE_URL` or `http://127.0.0.1:5000` |
| Data policy | Synthetic accounts and patients only |

## Scenario coverage

| Script | User journey | Primary metrics |
| --- | --- | --- |
| `k6/login-load.js` | Login | RPS, login response time, success/error rate |
| `k6/dashboard-load.js` | Authenticated dashboard and reports search | RPS, dashboard/reports response time, success/error rate |
| `k6/patient-load.js` | Add patient and list patients | Throughput, create/list response time, success/error rate |
| `k6/upload-load.js` | Image upload and AI diagnosis | Throughput, upload response time, success/error rate |

## Results

The framework has been created but not executed as part of this static setup. Populate the following from the k6 JSON summaries after running against the approved test environment.

| Scenario | Requests/sec | Avg response (ms) | Min response (ms) | Max response (ms) | Throughput | Error rate | Success rate |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Login | Pending | Pending | Pending | Pending | Pending | Pending | Pending |
| Dashboard | Pending | Pending | Pending | Pending | Pending | Pending | Pending |
| Add Patient / Patient List | Pending | Pending | Pending | Pending | Pending | Pending | Pending |
| Image Upload / Diagnosis | Pending | Pending | Pending | Pending | Pending | Pending | Pending |
| Reports | Pending | Pending | Pending | Pending | Pending | Pending | Pending |

## Acceptance thresholds

- HTTP error rate: less than 5%.
- 95th percentile request duration: less than 2 seconds (configured k6 threshold).
- Review upload/diagnosis separately because model inference is intentionally more resource-intensive than page reads.

See `k6/README.md` for local and GitHub Actions execution instructions. `Performance_Test_Report.xlsx` and `Load_Test_Report.xlsx` are result-entry templates with charts.
