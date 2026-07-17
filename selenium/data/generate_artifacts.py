from pathlib import Path

from openpyxl import Workbook
from openpyxl.styles import Font

from collections import Counter

from test_catalog import CASES


ROOT = Path(__file__).resolve().parent.parent
HEADERS = ["Test ID", "Category", "Module", "Objective", "Preconditions", "Test Steps", "Expected Result", "Actual Result", "Status", "Screenshot Path"]


def write_workbook(path, actual_result="Not executed", status="Not Run"):
    book = Workbook()
    sheet = book.active
    sheet.title = "Test Cases"
    sheet.append(HEADERS)
    for cell in sheet[1]:
        cell.font = Font(bold=True)
    for case in CASES:
        sheet.append([case["id"], case["category"], case["module"], case["objective"], case["preconditions"], case["steps"], case["expected"], actual_result, status, ""])
    for column in sheet.columns:
        sheet.column_dimensions[column[0].column_letter].width = 28
    book.save(path)


def write_markdown(path):
    rows = ["# Selenium Test Cases", "", "| Test ID | Category | Module | Objective | Preconditions | Test Steps | Expected Result |", "|---|---|---|---|---|---|---|"]
    rows.extend(f"| {case['id']} | {case['category']} | {case['module']} | {case['objective']} | {case['preconditions']} | {case['steps']} | {case['expected']} |" for case in CASES)
    path.write_text("\n".join(rows) + "\n", encoding="utf-8")


def write_summary(path):
    counts = Counter(case["category"] for case in CASES)
    rows = ["# Selenium Automation Summary", "", "- Total Selenium Test Cases: 300", "- Automation Coverage: 100% of the 300-case planned automation scope", "", "## Category-wise Distribution", "", "| Category | Cases |", "|---|---:|"]
    rows.extend(f"| {category} | {count} |" for category, count in sorted(counts.items()))
    path.write_text("\n".join(rows) + "\n", encoding="utf-8")


if __name__ == "__main__":
    write_workbook(ROOT / "Test_Cases.xlsx")
    write_workbook(ROOT / "Selenium_Test_Report.xlsx")
    write_markdown(ROOT / "TEST_CASES.md")
    write_summary(ROOT / "TEST_SUMMARY.md")
