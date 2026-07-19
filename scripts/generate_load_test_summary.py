"""Create a concise Markdown report from k6's summary export."""

import json
import os
import sys
from pathlib import Path


def values(metrics, name):
    return metrics.get(name, {}).get("values", {})


def number(value, digits=2):
    if value is None:
        return "N/A"
    return f"{float(value):.{digits}f}"


def main():
    report_dir = Path(sys.argv[1] if len(sys.argv) > 1 else "reports")
    source = report_dir / "load-test-results.json"
    target = report_dir / "load-test-summary.md"
    k6_exit_code = int(os.environ.get("K6_EXIT_CODE", "1"))

    try:
        payload = json.loads(source.read_text(encoding="utf-8"))
        metrics = payload.get("metrics", {})
    except (OSError, json.JSONDecodeError) as exc:
        target.write_text(
            "# k6 Load Test Summary\n\n"
            "| Metric | Value |\n| --- | --- |\n"
            "| Overall Result | FAIL |\n"
            f"| Reason | k6 results could not be read: {exc} |\n",
            encoding="utf-8",
        )
        print("result=FAIL")
        return

    requests = values(metrics, "http_reqs")
    duration = values(metrics, "http_req_duration")
    failed = values(metrics, "http_req_failed")
    checks = values(metrics, "checks")
    is_passing = (
        k6_exit_code == 0
        and float(failed.get("rate", 1)) < 0.05
        and float(checks.get("rate", 0)) > 0.95
    )
    result = "PASS" if is_passing else "FAIL"

    target.write_text(
        "# k6 Load Test Summary\n\n"
        "| Metric | Value |\n"
        "| --- | ---: |\n"
        "| Virtual Users | 100 |\n"
        "| Test Duration | 1 minute |\n"
        f"| Total Requests | {number(requests.get('count'), 0)} |\n"
        f"| Requests per Second (RPS) | {number(requests.get('rate'))} |\n"
        f"| Average Response Time | {number(duration.get('avg'))} ms |\n"
        f"| Minimum Response Time | {number(duration.get('min'))} ms |\n"
        f"| Maximum Response Time | {number(duration.get('max'))} ms |\n"
        f"| Failed Request Rate | {number(float(failed.get('rate', 0)) * 100)}% |\n"
        f"| Check Pass Rate | {number(float(checks.get('rate', 0)) * 100)}% |\n"
        f"| Overall Result | {result} |\n",
        encoding="utf-8",
    )
    print(f"result={result}")


if __name__ == "__main__":
    main()
