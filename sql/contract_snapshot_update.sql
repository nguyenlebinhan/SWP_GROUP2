Sao chép
-- Migration: Add snapshot columns to Employment_Contracts
-- Purpose: Store departmentName and positionName at signing time

ALTER TABLE Employment_Contracts
  ADD COLUMN departmentName NVARCHAR(150) NULL AFTER salary,
  ADD COLUMN positionName NVARCHAR(150) NULL AFTER departmentName;

UPDATE Employment_Contracts ec
JOIN Employees e ON ec.employeeId = e.employeeId
JOIN Departments d ON e.departmentId = d.departmentId
JOIN Positions p ON e.positionId = p.positionId
SET ec.departmentName = d.departmentName,
    ec.positionName = p.positionName