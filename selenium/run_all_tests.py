"""Run future Selenium tests and generate HTML and Excel reports."""
import subprocess
import sys
from pathlib import Path

from utils.framework import write_excel_report


ROOT = Path(__file__).resolve().parent


def main():
    for directory in (ROOT / "reports", ROOT / "screenshots", ROOT / "data"):
        directory.mkdir(parents=True, exist_ok=True)
    result = subprocess.run(
        [sys.executable, "-m", "pytest", str(ROOT / "tests"), f"--html={ROOT / 'reports' / 'selenium_report.html'}", "--self-contained-html"],
        cwd=ROOT.parent,
        check=False,
    )
    write_excel_report([{"test": "Selenium suite", "outcome": "passed" if result.returncode == 0 else "failed"}], ROOT / "reports" / "selenium_execution.xlsx")
    return result.returncode


if __name__ == "__main__":
    raise SystemExit(main())
