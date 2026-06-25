# Attendance Module – Use Case Specifications

> Created By: An Nguyen · Date Created: 25/5/2026
> Based on the HRM attendance implementation (`AttendanceImportService`, `AttendanceService`, `EmployeeController`, `ManagerController`, `AttendanceDAO`).

---

## UC-02 Import Monthly Attendance Data from Excel

| Field | Content |
|---|---|
| **ID and Name** | UC-02 Import Monthly Attendance Data from Excel |
| **Created By** | An Nguyen |
| **Date Created** | 25/5/2026 |
| **Primary Actor** | HR Manager |
| **Secondary Actors** | File Storage System, Excel Parser |

**Description:** The HR Manager uploads an Excel (.xlsx) file containing the previous month's raw check-in/check-out records for a department (or the whole company). The system parses, validates each row, derives attendance status and working hours, then stores the records and reports an import summary.

**Trigger:** The HR Manager selects **Import Attendance** in the Attendance Management screen.

**Preconditions:**
- PRE-1. The HR Manager has logged in successfully.
- PRE-2. The HR Manager has the `IMPORT_ATTENDANCE` permission and is HR staff.
- PRE-3. Today is within the import window (the 1st or 2nd day of the month).

**Postconditions:**
- POST-1. Valid attendance rows are inserted/updated (upsert) in the database.
- POST-2. An uploaded-file record is created with import status (Imported / Partial / Failed), total/imported/failed counts, and a note.
- POST-3. The uploaded file is stored on the server with a unique generated name.

**Normal Flow:**
1. The HR Manager opens the Attendance menu and selects Import Attendance.
2. The system displays the import form with the allowed month (previous month) pre-filled and the import window status.
3. The HR Manager selects the department (optional — blank means whole company) and chooses an `.xlsx` file.
4. The HR Manager confirms the upload.
5. The system validates the import window, month/year range, file presence, `.xlsx` extension and content type.
6. The system saves the file, creates an uploaded-file record, and parses the sheet.
7. For each row, the system validates employee code, work date (must belong to the selected month/year), time-in/time-out, department membership; derives status (Present/Late/Absent/Leave/Holiday/Weekend) and caps working hours at 8h unless an approved OT exists.
8. The system upserts valid rows inside a transaction and commits.
9. The system displays an import summary: total, imported, failed, status, and per-row errors.

**Alternative Flows:**
- **A1. Partial success:** At step 7, if some rows fail validation, the system imports the valid rows, sets status to *Partial*, and lists each failed row with its error message.

**Exceptions:**
- **2.0.E1 Import window closed:** If today is after the 2nd day of the month, or the selected month is not the immediately previous month, the system blocks the import and shows a window-error message.
- **2.0.E2 Invalid file:** If the file is missing, not `.xlsx`, or has an unacceptable content type, the system shows an error and stays on the form.
- **2.0.E3 Unreadable / corrupt file:** If the parser cannot read the file, the system marks the file *Failed* and shows a format error.
- **2.0.E4 Transaction failure:** If a database/system error occurs mid-import, the system rolls back all changes (old data preserved), marks the file *Failed*, and notifies the HR Manager.

**Priority:** High
**Frequency of Use:** Medium. Once per department per month, concentrated in the first two days of each month.
**Other Information:** Import is restricted to a 2-day window for the previous month only. Each row requires employee code + work date; hours are auto-capped at the 8h standard without approved OT. Status codes: 0 Present, 1 Late, 2/3 Absent, 4 Leave, 5 Holiday, 6 Weekend.
**Assumptions:** The Excel file follows the expected column layout. Employee codes already exist in the system.
**Related NFRs:** NFR-01 (Performance), NFR-04 (Security), NFR-07 (Auditability), NFR-10 (Approval Chain Audit).

---

## UC-03 Adjust Attendance Record

| Field | Content |
|---|---|
| **ID and Name** | UC-03 Adjust Attendance Record |
| **Created By** | An Nguyen |
| **Date Created** | 25/5/2026 |
| **Primary Actor** | HR Manager |
| **Secondary Actors** | — |

**Description:** The HR Manager corrects a single attendance record (check-in / check-out time) for an employee. The system recalculates working hours and status, saves the change with a mandatory reason, and records the adjustment in history.

**Trigger:** The HR Manager selects **Edit** on an attendance row in the Attendance Detail screen.

**Preconditions:**
- PRE-1. The HR Manager has logged in successfully.
- PRE-2. The HR Manager has the `EDIT_ATTENDANCE` permission and is HR staff.
- PRE-3. The attendance record exists and is still within the edit window (until day 5 of the following month).

**Postconditions:**
- POST-1. The attendance record's time-in, time-out, hours worked, and status are updated.
- POST-2. An adjustment-history entry is created storing old value, new value, reason, and the editor's user ID.

**Normal Flow:**
1. The HR Manager opens Attendance Detail for an employee and selects Edit on a record.
2. The system displays the update form with current time-in / time-out and the adjustment history; the form is read-only if the edit window is closed.
3. The HR Manager enters the corrected time-in / time-out and a reason for the change.
4. The HR Manager confirms the update.
5. The system validates the reason is present, the times are valid, and time-out is after time-in.
6. The system recomputes status and working hours (capping at 8h unless approved OT exists), updates the record, and writes the adjustment history within a transaction.
7. The system displays a success message and returns to the detail list.

**Alternative Flows:**
- **A1. Invalid time input:** At step 5, if a time is malformed or time-out precedes time-in, the system shows an error and the record is not changed.

**Exceptions:**
- **3.0.E1 Edit window closed:** If the current date is after day 5 of the month following the work date, the system rejects the edit with a deadline message.
- **3.0.E2 Missing reason:** If no reason is entered, the system blocks saving and asks for a reason.
- **3.0.E3 Record not found:** If the attendance record no longer exists, the system shows an error and aborts.
- **3.0.E4 System error on status resolution:** If determining the status fails (DB error), the system shows a system-error message and does not save.

**Priority:** High
**Frequency of Use:** Medium. Used to correct import errors or device misreads, mostly in the first days of each month.
**Other Information:** Editing is locked after the 5th of the next month. Every change is auditable through the adjustment-history table (old → new, reason, editor).
**Assumptions:** Only authorized HR staff adjust records; each adjustment is justified with a reason.
**Related NFRs:** NFR-04 (Security), NFR-07 (Auditability), NFR-10 (Approval Chain Audit).

---

## UC-04 View and Export Monthly Attendance Report

| Field | Content |
|---|---|
| **ID and Name** | UC-04 View and Export Monthly Attendance Report |
| **Created By** | An Nguyen |
| **Date Created** | 25/5/2026 |
| **Primary Actor** | HR Manager / Department Manager |
| **Secondary Actors** | Excel Exporter |

**Description:** The actor reviews the monthly attendance summary for the company or a department, drills into a single employee's daily records, and optionally exports the report to Excel.

**Trigger:** The actor selects **Attendance Overview** in the Attendance Management screen.

**Preconditions:**
- PRE-1. The actor has logged in successfully.
- PRE-2. The actor has `VIEW_ALL_ATTENDANCE` (whole company) or `VIEW_DEPARTMENT_ATTENDANCE` (own department only).

**Postconditions:**
- POST-1. No data is modified.
- POST-2. If exported, an `.xlsx` report file is generated and downloaded.

**Normal Flow:**
1. The actor opens the Attendance Overview screen.
2. The system reads the selected month, year, and department (defaults to current month/year).
3. The system computes per-employee monthly summaries (present, late, absent, leave, holiday, weekend days, worked hours, standard working days, approved OT days).
4. The system displays the paginated summary table.
5. The actor selects an employee to view daily detail.
6. The system displays the employee's daily records with status and OT-day markers, filterable by day.
7. The actor selects Export.
8. The system generates an Excel report for the chosen scope and period and streams it for download.

**Alternative Flows:**
- **A1. Department-scope manager:** If the actor only has `VIEW_DEPARTMENT_ATTENDANCE`, the system restricts the scope to the manager's own department regardless of the selected department.

**Exceptions:**
- **4.0.E1 No permission:** If the actor has neither attendance-view permission, the system shows an access-denied message and no data.
- **4.0.E2 Employee detail not found:** If the selected employee has no data for the period, the system shows a "no attendance data" message.
- **4.0.E3 Export forbidden:** If the actor lacks view permission when exporting, the system returns a 403 Forbidden response.

**Priority:** Medium
**Frequency of Use:** High. Reviewed throughout the month and at month-end for payroll preparation.
**Other Information:** Standard working days exclude weekends and active holidays. The overview supports month/year/department filters, pagination, and per-day filtering in the detail view.
**Assumptions:** Attendance data for the period has already been imported. Managers only see their own department's data.
**Related NFRs:** NFR-01 (Performance), NFR-04 (Security), NFR-07 (Auditability).
