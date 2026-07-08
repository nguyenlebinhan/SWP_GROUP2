-- Seed test data for payroll, contracts, attendance, and overtime.
-- Assumes DBInitializer already created tables and base seed users/employees/departments/positions/form types.
-- MySQL Workbench safe update mode friendly: no table-wide DELETE/UPDATE.

SET @payroll_year = 2026;
SET @payroll_month = 6;
SET @period_start = DATE('2026-06-01');
SET @period_end = DATE('2026-06-30');

SET @emp_full = (SELECT employeeId FROM Employees WHERE employeeCode = 'EMP001');
SET @emp_mid_start = (SELECT employeeId FROM Employees WHERE employeeCode = 'EMP002');
SET @emp_mid_end = (SELECT employeeId FROM Employees WHERE employeeCode = 'EMP003');
SET @emp_hr_manager = (SELECT employeeId FROM Employees WHERE employeeCode = 'EMP004');
SET @emp_no_insurance = (SELECT employeeId FROM Employees WHERE employeeCode = 'EMP005');
SET @emp_leave = (SELECT employeeId FROM Employees WHERE employeeCode = 'EMP006');
SET @emp_no_attendance = (SELECT employeeId FROM Employees WHERE employeeCode = 'EMP007');
SET @overtime_type = (SELECT formTypeId FROM Form_Types WHERE formTypeCode = 'OVERTIME');
SET @hr_user = (SELECT userId FROM Employees WHERE employeeId = @emp_hr_manager);

START TRANSACTION;

-- Remove generated payroll for this test month only, by primary key.
DELETE FROM Payroll
WHERE payrollId > 0
  AND periodStart = @period_start
  AND periodEnd = @period_end
  AND employeeId IN (@emp_full, @emp_mid_start, @emp_mid_end, @emp_no_insurance, @emp_leave, @emp_no_attendance);

UPDATE Employees SET dependentCount = 2, unionMember = 1 WHERE employeeCode = 'EMP001';
UPDATE Employees SET dependentCount = 1, unionMember = 0 WHERE employeeCode = 'EMP002';
UPDATE Employees SET dependentCount = 0, unionMember = 1 WHERE employeeCode = 'EMP003';
UPDATE Employees SET dependentCount = 0, unionMember = 0 WHERE employeeCode = 'EMP005';
UPDATE Employees SET dependentCount = 3, unionMember = 1 WHERE employeeCode = 'EMP006';
UPDATE Employees SET dependentCount = 0, unionMember = 0 WHERE employeeCode = 'EMP007';

INSERT INTO Employment_Contracts
    (contractCode, employeeId, contractType, signedDate, effectiveDate, endDate,
     salary, status, note, createdBy)
VALUES
    ('TEST_CONTRACT_FULL_MONTH', @emp_full, 'INDEFINITE', '2026-01-01', '2026-01-01', NULL,
     30000000, 'ACTIVE', 'Full month: attendance bonus + union fee + overtime', @hr_user),
    ('TEST_CONTRACT_MID_START', @emp_mid_start, 'INDEFINITE', '2026-06-12', '2026-06-12', NULL,
     24000000, 'ACTIVE', 'Starts mid-month: days before start must not be deducted', @hr_user),
    ('TEST_CONTRACT_MID_END', @emp_mid_end, 'FIXED_TERM', '2026-01-01', '2026-01-01', '2026-06-18',
     26000000, 'ACTIVE', 'Ends mid-month: days after end must not be deducted', @hr_user),
    ('TEST_CONTRACT_NO_INSURANCE', @emp_no_insurance, 'INDEFINITE', '2026-01-01', '2026-01-01', NULL,
     22000000, 'ACTIVE', 'Works only 5 days: unpaid/not-worked >= insurance threshold', @hr_user),
    ('TEST_CONTRACT_PAID_LEAVE', @emp_leave, 'INDEFINITE', '2026-01-01', '2026-01-01', NULL,
     20000000, 'ACTIVE', 'Includes paid leave, unpaid absence, and late arrival', @hr_user),
    ('TEST_CONTRACT_NO_ATTENDANCE', @emp_no_attendance, 'INDEFINITE', '2026-01-01', '2026-01-01', NULL,
     18000000, 'ACTIVE', 'No attendance rows: should produce generation warning', @hr_user),
    ('TEST_CONTRACT_PENDING_UI', @emp_hr_manager, 'FIXED_TERM', '2026-07-01', '2026-08-01', '2027-07-31',
     35000000, 'PENDING_APPROVAL', 'Pending contract for contract approval screen', @hr_user)
ON DUPLICATE KEY UPDATE
    employeeId = VALUES(employeeId),
    contractType = VALUES(contractType),
    signedDate = VALUES(signedDate),
    effectiveDate = VALUES(effectiveDate),
    endDate = VALUES(endDate),
    salary = VALUES(salary),
    status = VALUES(status),
    note = VALUES(note),
    createdBy = VALUES(createdBy);

INSERT INTO Contract_Audit_Log (contractId, oldStatus, newStatus, changedBy, actionReason)
SELECT ec.contractId, NULL, ec.status, @hr_user, 'TEST_PAYROLL_SEED'
FROM Employment_Contracts ec
WHERE ec.contractCode LIKE 'TEST_CONTRACT_%'
  AND NOT EXISTS (
      SELECT 1
      FROM Contract_Audit_Log cal
      WHERE cal.contractId = ec.contractId
        AND cal.actionReason = 'TEST_PAYROLL_SEED'
  );

INSERT INTO Attendance_Period_Status
    (periodYear, periodMonth, departmentId, status, managerConfirmedBy, managerConfirmedAt,
     submittedToBaBy, submittedToBaAt, baApprovedBy, baApprovedAt, note)
SELECT @payroll_year, @payroll_month, d.departmentId, 4, @hr_user, NOW(),
       @hr_user, NOW(), @hr_user, NOW(), 'TEST_PAYROLL_SEED'
FROM Departments d
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    managerConfirmedBy = VALUES(managerConfirmedBy),
    managerConfirmedAt = VALUES(managerConfirmedAt),
    submittedToBaBy = VALUES(submittedToBaBy),
    submittedToBaAt = VALUES(submittedToBaAt),
    baApprovedBy = VALUES(baApprovedBy),
    baApprovedAt = VALUES(baApprovedAt),
    note = VALUES(note);

INSERT INTO Attendance
    (attendanceCode, employeeId, employeeCode, fullName, positionId, positionName,
     departmentId, departmentName, workDate, timeIn, timeOut, hoursWorked,
     isOvertime, otHoursWorked, attendanceStatus)
SELECT CONCAT('TEST_ATT_', e.employeeCode, '_', DATE_FORMAT(d.workDate, '%Y%m%d')),
       e.employeeId, e.employeeCode, u.fullName, e.positionId, p.positionName,
       e.departmentId, dept.departmentName, d.workDate,
       CASE
           WHEN e.employeeCode = 'EMP001' AND d.workDate = '2026-06-05' THEN '08:45:00'
           WHEN e.employeeCode = 'EMP006' AND d.workDate = '2026-06-10' THEN '08:40:00'
           WHEN e.employeeCode = 'EMP006' AND d.workDate = '2026-06-09' THEN NULL
           ELSE '08:00:00'
       END,
       CASE
           WHEN e.employeeCode = 'EMP001' AND d.workDate = '2026-06-10' THEN '20:00:00'
           WHEN e.employeeCode = 'EMP006' AND d.workDate = '2026-06-09' THEN NULL
           ELSE '17:00:00'
       END,
       CASE
           WHEN e.employeeCode = 'EMP001' AND d.workDate = '2026-06-10' THEN 11.00
           WHEN e.employeeCode = 'EMP006' AND d.workDate = '2026-06-09' THEN 0.00
           ELSE 8.00
       END,
       CASE WHEN e.employeeCode = 'EMP001' AND d.workDate = '2026-06-10' THEN 1 ELSE 0 END,
       CASE WHEN e.employeeCode = 'EMP001' AND d.workDate = '2026-06-10' THEN 3.00 ELSE 0.00 END,
       CASE
           WHEN e.employeeCode = 'EMP001' AND d.workDate = '2026-06-05' THEN 1
           WHEN e.employeeCode = 'EMP006' AND d.workDate = '2026-06-08' THEN 4
           WHEN e.employeeCode = 'EMP006' AND d.workDate = '2026-06-09' THEN 2
           WHEN e.employeeCode = 'EMP006' AND d.workDate = '2026-06-10' THEN 1
           ELSE 0
       END
FROM Employees e
JOIN Users u ON u.userId = e.userId
LEFT JOIN Positions p ON p.positionId = e.positionId
LEFT JOIN Departments dept ON dept.departmentId = e.departmentId
JOIN (
    SELECT DATE('2026-06-01') workDate UNION ALL SELECT '2026-06-02' UNION ALL SELECT '2026-06-03'
    UNION ALL SELECT '2026-06-04' UNION ALL SELECT '2026-06-05' UNION ALL SELECT '2026-06-08'
    UNION ALL SELECT '2026-06-09' UNION ALL SELECT '2026-06-10' UNION ALL SELECT '2026-06-11'
    UNION ALL SELECT '2026-06-12' UNION ALL SELECT '2026-06-15' UNION ALL SELECT '2026-06-16'
    UNION ALL SELECT '2026-06-17' UNION ALL SELECT '2026-06-18' UNION ALL SELECT '2026-06-19'
    UNION ALL SELECT '2026-06-22' UNION ALL SELECT '2026-06-23' UNION ALL SELECT '2026-06-24'
    UNION ALL SELECT '2026-06-25' UNION ALL SELECT '2026-06-26' UNION ALL SELECT '2026-06-29'
    UNION ALL SELECT '2026-06-30'
) d
WHERE e.employeeCode IN ('EMP001', 'EMP002', 'EMP003', 'EMP005', 'EMP006')
  AND (e.employeeCode <> 'EMP002' OR d.workDate >= '2026-06-12')
  AND (e.employeeCode <> 'EMP003' OR d.workDate <= '2026-06-18')
  AND (e.employeeCode <> 'EMP005' OR d.workDate <= '2026-06-05')
ON DUPLICATE KEY UPDATE
    attendanceCode = VALUES(attendanceCode),
    employeeCode = VALUES(employeeCode),
    fullName = VALUES(fullName),
    positionId = VALUES(positionId),
    positionName = VALUES(positionName),
    departmentId = VALUES(departmentId),
    departmentName = VALUES(departmentName),
    timeIn = VALUES(timeIn),
    timeOut = VALUES(timeOut),
    hoursWorked = VALUES(hoursWorked),
    isOvertime = VALUES(isOvertime),
    otHoursWorked = VALUES(otHoursWorked),
    attendanceStatus = VALUES(attendanceStatus);

INSERT INTO Form_Requests
    (formCode, employeeId, formTypeId, reason, startDate, endDate,
     startTime, endTime, status, approverId, approverNote, approvedAt)
VALUES
    ('TEST_OT_EMP001_20260610', @emp_full, @overtime_type, 'TEST_PAYROLL_SEED overtime workday',
     '2026-06-10', '2026-06-10', '18:00:00', '20:00:00', 1,
     @emp_hr_manager, 'Approved test overtime', NOW())
ON DUPLICATE KEY UPDATE
    employeeId = VALUES(employeeId),
    formTypeId = VALUES(formTypeId),
    reason = VALUES(reason),
    startDate = VALUES(startDate),
    endDate = VALUES(endDate),
    startTime = VALUES(startTime),
    endTime = VALUES(endTime),
    status = VALUES(status),
    approverId = VALUES(approverId),
    approverNote = VALUES(approverNote),
    approvedAt = VALUES(approvedAt);

SET @ot_form_emp001 = (SELECT formId FROM Form_Requests WHERE formCode = 'TEST_OT_EMP001_20260610');

INSERT INTO Overtime_Details (formId, otDate, startTime, endTime, dayType)
VALUES (@ot_form_emp001, '2026-06-10', '18:00:00', '20:00:00', 1)
ON DUPLICATE KEY UPDATE
    otDate = VALUES(otDate),
    startTime = VALUES(startTime),
    endTime = VALUES(endTime),
    dayType = VALUES(dayType);

INSERT IGNORE INTO Overtime_Assignees (formId, employeeId)
VALUES (@ot_form_emp001, @emp_full);

COMMIT;

SELECT 'TEST DATA READY' AS result,
       @period_start AS periodStart,
       @period_end AS periodEnd,
       'Run payroll for 06/2026.' AS note;
