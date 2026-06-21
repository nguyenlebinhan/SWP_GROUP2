-- Demo data for payroll feature review.
-- Do not insert base data already seeded by DBInitializer:
-- Roles, Permissions, Positions, Departments, Users, Employees, Department_Roles, Form_Types.
-- This file only inserts payroll-related demo rows that DBInitializer does not seed.
-- Target period: June 2026.

SET NAMES utf8mb4;

-- 1) Active employment contracts.
-- DBInitializer creates Employment_Contracts table but does not seed contract rows.
-- Existing employeeId seed mapping from DBInitializer:
-- EMP001=1 IT Manager, EMP002=2 IT Employee, EMP003=3 Intern,
-- EMP004=4 HR Manager, EMP005=5 HR Employee, EMP006=6 FI Manager, EMP007=7 FI Employee.
INSERT INTO Employment_Contracts
    (contractCode, employeeId, contractType, startDate, endDate, salary, status, note, createdBy)
VALUES
    ('CTR-EMP001-2026', 1, 'FULL_TIME', '2026-01-01', NULL, 35000000.00, 1, N'Hop dong demo payroll - IT Manager', 1),
    ('CTR-EMP002-2026', 2, 'FULL_TIME', '2026-01-01', NULL, 20000000.00, 1, N'Hop dong demo payroll - IT Employee', 1),
    ('CTR-EMP003-2026', 3, 'INTERN',    '2026-01-01', NULL,  6000000.00, 1, N'Hop dong demo payroll - Intern', 1),
    ('CTR-EMP004-2026', 4, 'FULL_TIME', '2026-01-01', NULL, 32000000.00, 1, N'Hop dong demo payroll - HR Manager', 1),
    ('CTR-EMP005-2026', 5, 'FULL_TIME', '2026-01-01', NULL, 18000000.00, 1, N'Hop dong demo payroll - HR Employee', 1),
    ('CTR-EMP006-2026', 6, 'FULL_TIME', '2026-01-01', NULL, 33000000.00, 1, N'Hop dong demo payroll - FI Manager', 1),
    ('CTR-EMP007-2026', 7, 'FULL_TIME', '2026-01-01', NULL, 19000000.00, 1, N'Hop dong demo payroll - FI Employee', 1)
ON DUPLICATE KEY UPDATE
    salary = VALUES(salary),
    status = VALUES(status),
    note = VALUES(note),
    updatedAt = CURRENT_TIMESTAMP;

-- 2) Holiday sample inside June payroll period.
INSERT INTO Holiday (holidayName, startDate, endDate, isActive)
SELECT N'Ngay nghi cong ty demo', '2026-06-18', '2026-06-18', 1
WHERE NOT EXISTS (
    SELECT 1 FROM Holiday
    WHERE holidayName = N'Ngay nghi cong ty demo'
      AND startDate = '2026-06-18'
      AND endDate = '2026-06-18'
);

-- 3) Attendance sample.
-- attendanceStatus:
-- 0=On time, 1=Late, 2=Absent, 3=Unauthorized absent, 4=Paid leave, 5=Holiday, 6=Weekend.
-- penalty is left 0 for late rows so PayrollService can calculate by late minutes.
INSERT INTO Attendance
    (attendanceCode, employeeId, employeeCode, fullName, departmentId, departmentName,
     workDate, timeIn, timeOut, hoursWorked, isOvertime, otHoursWorked, attendanceStatus, penalty)
VALUES
    -- EMP002: normal employee with late, paid leave, unauthorized absence and overtime.
    ('ATT-EMP002-20260601', 2, 'EMP002', N'Le Thi IT',       1, N'Phong Cong nghe thong tin', '2026-06-01', '08:00:00', '17:00:00', 8.00, 0, 0.00, 0, 0),
    ('ATT-EMP002-20260602', 2, 'EMP002', N'Le Thi IT',       1, N'Phong Cong nghe thong tin', '2026-06-02', '08:30:00', '17:00:00', 7.50, 0, 0.00, 1, 0),
    ('ATT-EMP002-20260603', 2, 'EMP002', N'Le Thi IT',       1, N'Phong Cong nghe thong tin', '2026-06-03', '09:20:00', '17:30:00', 7.20, 0, 0.00, 1, 0),
    ('ATT-EMP002-20260604', 2, 'EMP002', N'Le Thi IT',       1, N'Phong Cong nghe thong tin', '2026-06-04', NULL,       NULL,       0.00, 0, 0.00, 4, 0),
    ('ATT-EMP002-20260605', 2, 'EMP002', N'Le Thi IT',       1, N'Phong Cong nghe thong tin', '2026-06-05', NULL,       NULL,       0.00, 0, 0.00, 3, 0),
    ('ATT-EMP002-20260608', 2, 'EMP002', N'Le Thi IT',       1, N'Phong Cong nghe thong tin', '2026-06-08', '08:00:00', '19:00:00', 8.00, 1, 2.00, 0, 0),
    ('ATT-EMP002-20260618', 2, 'EMP002', N'Le Thi IT',       1, N'Phong Cong nghe thong tin', '2026-06-18', NULL,       NULL,       0.00, 0, 0.00, 5, 0),

    -- EMP003: intern with one late day and one unpaid/unauthorized absence.
    ('ATT-EMP003-20260601', 3, 'EMP003', N'Pham Van Dev',    1, N'Phong Cong nghe thong tin', '2026-06-01', '08:05:00', '17:00:00', 8.00, 0, 0.00, 0, 0),
    ('ATT-EMP003-20260602', 3, 'EMP003', N'Pham Van Dev',    1, N'Phong Cong nghe thong tin', '2026-06-02', '08:45:00', '17:00:00', 7.25, 0, 0.00, 1, 0),
    ('ATT-EMP003-20260603', 3, 'EMP003', N'Pham Van Dev',    1, N'Phong Cong nghe thong tin', '2026-06-03', NULL,       NULL,       0.00, 0, 0.00, 3, 0),
    ('ATT-EMP003-20260604', 3, 'EMP003', N'Pham Van Dev',    1, N'Phong Cong nghe thong tin', '2026-06-04', '08:00:00', '17:00:00', 8.00, 0, 0.00, 0, 0),

    -- EMP005: HR employee with paid leave and weekend overtime sample.
    ('ATT-EMP005-20260601', 5, 'EMP005', N'Vu Thi Nhan Su',  2, N'Phong Nhan su',             '2026-06-01', '08:00:00', '17:00:00', 8.00, 0, 0.00, 0, 0),
    ('ATT-EMP005-20260602', 5, 'EMP005', N'Vu Thi Nhan Su',  2, N'Phong Nhan su',             '2026-06-02', NULL,       NULL,       0.00, 0, 0.00, 4, 0),
    ('ATT-EMP005-20260606', 5, 'EMP005', N'Vu Thi Nhan Su',  2, N'Phong Nhan su',             '2026-06-06', '09:00:00', '13:00:00', 0.00, 1, 4.00, 6, 0)
ON DUPLICATE KEY UPDATE
    timeIn = VALUES(timeIn),
    timeOut = VALUES(timeOut),
    hoursWorked = VALUES(hoursWorked),
    isOvertime = VALUES(isOvertime),
    otHoursWorked = VALUES(otHoursWorked),
    attendanceStatus = VALUES(attendanceStatus),
    penalty = VALUES(penalty),
    updatedAt = CURRENT_TIMESTAMP;

-- 4) Approved leave requests.
INSERT INTO Form_Requests
    (formCode, employeeId, formTypeId, reason, startDate, endDate, totalDays, usedDays,
     status, approverId, approverNote, approvedAt)
SELECT 'LEAVE-EMP002-20260604', 2, ft.formTypeId, N'Nghi phep nam demo', '2026-06-04', '2026-06-04', 1.0, 1.0,
       1, 1, N'Da duyet nghi phep demo', '2026-06-03 10:00:00'
FROM Form_Types ft
WHERE ft.formTypeCode = 'LEAVE'
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    approverId = VALUES(approverId),
    approverNote = VALUES(approverNote),
    approvedAt = VALUES(approvedAt);

INSERT INTO Form_Requests
    (formCode, employeeId, formTypeId, reason, startDate, endDate, totalDays, usedDays,
     status, approverId, approverNote, approvedAt)
SELECT 'LEAVE-EMP005-20260602', 5, ft.formTypeId, N'Nghi phep nam demo', '2026-06-02', '2026-06-02', 1.0, 1.0,
       1, 4, N'Da duyet nghi phep demo', '2026-06-01 16:00:00'
FROM Form_Types ft
WHERE ft.formTypeCode = 'LEAVE'
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    approverId = VALUES(approverId),
    approverNote = VALUES(approverNote),
    approvedAt = VALUES(approvedAt);

INSERT INTO Leave_Form
    (employeeId, formTypeId, formId, startDate, endDate, totalDays, usedDays)
SELECT fr.employeeId, fr.formTypeId, fr.formId, fr.startDate, fr.endDate, fr.totalDays, fr.usedDays
FROM Form_Requests fr
WHERE fr.formCode IN ('LEAVE-EMP002-20260604', 'LEAVE-EMP005-20260602')
  AND NOT EXISTS (
      SELECT 1 FROM Leave_Form lf WHERE lf.formId = fr.formId
  );

-- 5) Approved overtime requests.
-- Manager creates request, assignees are employees who receive overtime pay.
INSERT INTO Form_Requests
    (formCode, employeeId, formTypeId, reason, startDate, endDate, totalDays, usedDays,
     status, approverId, approverNote, approvedAt)
SELECT 'OT-IT-20260608', 1, ft.formTypeId, N'Tang ca release he thong demo', '2026-06-08', '2026-06-08', NULL, 0,
       1, 1, N'Da duyet OT demo', '2026-06-07 17:00:00'
FROM Form_Types ft
WHERE ft.formTypeCode = 'OVERTIME'
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    approverId = VALUES(approverId),
    approverNote = VALUES(approverNote),
    approvedAt = VALUES(approvedAt);

INSERT INTO Overtime_Details (formId, otDate, startTime, endTime, dayType)
SELECT fr.formId, '2026-06-08', '17:00:00', '19:00:00', 1
FROM Form_Requests fr
WHERE fr.formCode = 'OT-IT-20260608'
ON DUPLICATE KEY UPDATE
    otDate = VALUES(otDate),
    startTime = VALUES(startTime),
    endTime = VALUES(endTime),
    dayType = VALUES(dayType);

INSERT IGNORE INTO Overtime_Assignees (formId, employeeId)
SELECT fr.formId, 2
FROM Form_Requests fr
WHERE fr.formCode = 'OT-IT-20260608';

INSERT INTO Form_Requests
    (formCode, employeeId, formTypeId, reason, startDate, endDate, totalDays, usedDays,
     status, approverId, approverNote, approvedAt)
SELECT 'OT-HR-20260606', 4, ft.formTypeId, N'Tang ca cuoi tuan xu ly ho so demo', '2026-06-06', '2026-06-06', NULL, 0,
       1, 4, N'Da duyet OT cuoi tuan demo', '2026-06-05 17:00:00'
FROM Form_Types ft
WHERE ft.formTypeCode = 'OVERTIME'
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    approverId = VALUES(approverId),
    approverNote = VALUES(approverNote),
    approvedAt = VALUES(approvedAt);

INSERT INTO Overtime_Details (formId, otDate, startTime, endTime, dayType)
SELECT fr.formId, '2026-06-06', '09:00:00', '13:00:00', 2
FROM Form_Requests fr
WHERE fr.formCode = 'OT-HR-20260606'
ON DUPLICATE KEY UPDATE
    otDate = VALUES(otDate),
    startTime = VALUES(startTime),
    endTime = VALUES(endTime),
    dayType = VALUES(dayType);

INSERT IGNORE INTO Overtime_Assignees (formId, employeeId)
SELECT fr.formId, 5
FROM Form_Requests fr
WHERE fr.formCode = 'OT-HR-20260606';

-- 6) Performance records in June 2026.
INSERT INTO Performance
    (departmentId, employeeId, positionId, evaluatorId, evaluationDate, content, result)
SELECT 1, 2, 2, 1, '2026-06-20', N'Demo payroll: hoan thanh tot cong viec thang 6', 'GOOD'
WHERE NOT EXISTS (
    SELECT 1 FROM Performance
    WHERE employeeId = 2 AND evaluationDate = '2026-06-20'
);

INSERT INTO Performance
    (departmentId, employeeId, positionId, evaluatorId, evaluationDate, content, result)
SELECT 1, 3, 1, 1, '2026-06-20', N'Demo payroll: can cai thien ky luat gio giac', 'AVERAGE'
WHERE NOT EXISTS (
    SELECT 1 FROM Performance
    WHERE employeeId = 3 AND evaluationDate = '2026-06-20'
);

INSERT INTO Performance
    (departmentId, employeeId, positionId, evaluatorId, evaluationDate, content, result)
SELECT 2, 5, 2, 4, '2026-06-20', N'Demo payroll: xu ly ho so tot', 'EXCELLENT'
WHERE NOT EXISTS (
    SELECT 1 FROM Performance
    WHERE employeeId = 5 AND evaluationDate = '2026-06-20'
);

-- 7) Payroll result snapshots for June 2026.
-- These rows match the updated Payroll schema in DBInitializer:
-- periodEnd, overtimePay, penalty, grossSalary, socialInsurance, healthInsurance,
-- unemploymentInsurance, insuranceDeduction, personalIncomeTax, taxDeduction, note.
-- Values are demo snapshots so you can see the table shape before PayrollService is coded.
INSERT INTO Payroll
    (periodStart, periodEnd, employeeId, positionId, departmentId, workingDays, hoursWorked,
     baseSalary, allowance, bonus, overtimePay, penalty, grossSalary,
     socialInsurance, healthInsurance, unemploymentInsurance, insuranceDeduction,
     personalIncomeTax, taxDeduction, netSalary, note, status)
SELECT
    '2026-06-01', '2026-06-30', e.employeeId, e.positionId, e.departmentId, 21, 38.70,
    19090911.00, 0.00, 800000.00, 1022724.00, 511366.00, 20402269.00,
    1600000.00, 300000.00, 200000.00, 2100000.00,
    140113.00, 140113.00, 18162156.00,
    N'Demo payroll EMP002: 20 ngay lam, 1 ngay nghi phep co luong, 1 ngay khong phep, 2 lan di muon, 2h OT ngay thuong.',
    0
FROM Employees e
WHERE e.employeeCode = 'EMP002'
  AND NOT EXISTS (
      SELECT 1 FROM Payroll p
      WHERE p.employeeId = e.employeeId
        AND p.periodStart = '2026-06-01'
        AND p.periodEnd = '2026-06-30'
  );

INSERT INTO Payroll
    (periodStart, periodEnd, employeeId, positionId, departmentId, workingDays, hoursWorked,
     baseSalary, allowance, bonus, overtimePay, penalty, grossSalary,
     socialInsurance, healthInsurance, unemploymentInsurance, insuranceDeduction,
     personalIncomeTax, taxDeduction, netSalary, note, status)
SELECT
    '2026-06-01', '2026-06-30', e.employeeId, e.positionId, e.departmentId, 20, 160.00,
    5454545.00, 0.00, 0.00, 0.00, 232955.00, 5221590.00,
    480000.00, 90000.00, 60000.00, 630000.00,
    0.00, 0.00, 4591590.00,
    N'Demo payroll EMP003: intern, 20 ngay cong, 1 ngay khong phep, 1 lan di muon.',
    0
FROM Employees e
WHERE e.employeeCode = 'EMP003'
  AND NOT EXISTS (
      SELECT 1 FROM Payroll p
      WHERE p.employeeId = e.employeeId
        AND p.periodStart = '2026-06-01'
        AND p.periodEnd = '2026-06-30'
  );

INSERT INTO Payroll
    (periodStart, periodEnd, employeeId, positionId, departmentId, workingDays, hoursWorked,
     baseSalary, allowance, bonus, overtimePay, penalty, grossSalary,
     socialInsurance, healthInsurance, unemploymentInsurance, insuranceDeduction,
     personalIncomeTax, taxDeduction, netSalary, note, status)
SELECT
    '2026-06-01', '2026-06-30', e.employeeId, e.positionId, e.departmentId, 2, 8.00,
    1636364.00, 0.00, 1440000.00, 1636364.00, 0.00, 4712728.00,
    1440000.00, 270000.00, 180000.00, 1890000.00,
    0.00, 0.00, 2822728.00,
    N'Demo payroll EMP005: 1 ngay lam, 1 ngay nghi phep co luong, 4h OT cuoi tuan, performance EXCELLENT.',
    0
FROM Employees e
WHERE e.employeeCode = 'EMP005'
  AND NOT EXISTS (
      SELECT 1 FROM Payroll p
      WHERE p.employeeId = e.employeeId
        AND p.periodStart = '2026-06-01'
        AND p.periodEnd = '2026-06-30'
  );

-- 7.1) Default payroll snapshots for every remaining active employee with an active contract.
-- This fills managers, interns and normal employees not covered by the special demo cases above.
INSERT INTO Payroll
    (periodStart, periodEnd, employeeId, positionId, departmentId, workingDays, hoursWorked,
     baseSalary, allowance, bonus, overtimePay, penalty, grossSalary,
     socialInsurance, healthInsurance, unemploymentInsurance, insuranceDeduction,
     personalIncomeTax, taxDeduction, netSalary, note, status)
SELECT
    '2026-06-01',
    '2026-06-30',
    x.employeeId,
    x.positionId,
    x.departmentId,
    21,
    168.00,
    x.baseSalary,
    0.00,
    x.bonus,
    0.00,
    0.00,
    x.grossSalary,
    x.socialInsurance,
    x.healthInsurance,
    x.unemploymentInsurance,
    x.insuranceDeduction,
    x.personalIncomeTax,
    x.personalIncomeTax,
    ROUND(x.grossSalary - x.insuranceDeduction - x.personalIncomeTax, 2),
    CONCAT(N'Demo payroll default ', x.employeeCode, N': du cong, khong di muon, khong nghi khong phep, khong OT. Giam tru ca nhan 15.500.000 VND, nguoi phu thuoc 0.'),
    0
FROM (
    SELECT
        t.*,
        ROUND(
            (LEAST(t.taxableIncome, 10000000) * 0.05)
            + (GREATEST(LEAST(t.taxableIncome, 30000000) - 10000000, 0) * 0.10)
            + (GREATEST(LEAST(t.taxableIncome, 60000000) - 30000000, 0) * 0.20)
            + (GREATEST(LEAST(t.taxableIncome, 100000000) - 60000000, 0) * 0.30)
            + (GREATEST(t.taxableIncome - 100000000, 0) * 0.35),
            2
        ) AS personalIncomeTax
    FROM (
        SELECT
            b.*,
            GREATEST(b.grossSalary - b.insuranceDeduction - 15500000, 0) AS taxableIncome
        FROM (
            SELECT
                e.employeeId,
                e.employeeCode,
                e.positionId,
                e.departmentId,
                ROUND(ec.salary, 2) AS baseSalary,
                500000.00 AS bonus,
                ROUND(ec.salary + 500000, 2) AS grossSalary,
                ROUND(ec.salary * 0.08, 2) AS socialInsurance,
                ROUND(ec.salary * 0.015, 2) AS healthInsurance,
                ROUND(ec.salary * 0.01, 2) AS unemploymentInsurance,
                ROUND(ec.salary * 0.105, 2) AS insuranceDeduction
            FROM Employees e
            JOIN Employment_Contracts ec ON ec.employeeId = e.employeeId
            WHERE e.status = 1
              AND ec.status = 1
              AND ec.startDate <= '2026-06-30'
              AND (ec.endDate IS NULL OR ec.endDate >= '2026-06-01')
              AND ec.contractId = (
                  SELECT ec2.contractId
                  FROM Employment_Contracts ec2
                  WHERE ec2.employeeId = e.employeeId
                    AND ec2.status = 1
                    AND ec2.startDate <= '2026-06-30'
                    AND (ec2.endDate IS NULL OR ec2.endDate >= '2026-06-01')
                  ORDER BY ec2.contractId DESC
                  LIMIT 1
              )
              AND NOT EXISTS (
                  SELECT 1 FROM Payroll p
                  WHERE p.employeeId = e.employeeId
                    AND p.periodStart = '2026-06-01'
                    AND p.periodEnd = '2026-06-30'
              )
        ) b
    ) t
) x;

-- 8) Quick verification queries.
SELECT e.employeeCode, u.fullName, ec.salary, p.positionName, d.departmentName
FROM Employees e
JOIN Users u ON u.userId = e.userId
JOIN Employment_Contracts ec ON ec.employeeId = e.employeeId AND ec.status = 1
LEFT JOIN Positions p ON p.positionId = e.positionId
LEFT JOIN Departments d ON d.departmentId = e.departmentId
ORDER BY e.employeeId;

SELECT employeeCode, workDate, timeIn, timeOut, hoursWorked, attendanceStatus, isOvertime, otHoursWorked
FROM Attendance
WHERE workDate BETWEEN '2026-06-01' AND '2026-06-30'
ORDER BY employeeCode, workDate;

SELECT fr.formCode, fr.employeeId AS requesterId, ft.formTypeCode, fr.status,
       od.otDate, od.startTime, od.endTime, od.dayType, oa.employeeId AS assigneeId
FROM Form_Requests fr
JOIN Form_Types ft ON ft.formTypeId = fr.formTypeId
LEFT JOIN Overtime_Details od ON od.formId = fr.formId
LEFT JOIN Overtime_Assignees oa ON oa.formId = fr.formId
WHERE fr.formCode LIKE 'OT-%'
ORDER BY fr.formCode, oa.employeeId;

SELECT e.employeeCode, u.fullName, p.periodStart, p.periodEnd,
       p.workingDays, p.baseSalary, p.overtimePay, p.penalty,
       p.grossSalary, p.insuranceDeduction, p.personalIncomeTax, p.netSalary, p.status
FROM Payroll p
JOIN Employees e ON e.employeeId = p.employeeId
JOIN Users u ON u.userId = e.userId
WHERE p.periodStart = '2026-06-01'
  AND p.periodEnd = '2026-06-30'
ORDER BY e.employeeCode;
