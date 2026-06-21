-- Run this only on an existing database that was created before payroll import was removed.
-- If you recreate the database through DBInitializer, this file is not needed.

ALTER TABLE Payroll
    DROP COLUMN socialInsurance,
    DROP COLUMN healthInsurance,
    DROP COLUMN unemploymentInsurance,
    DROP COLUMN taxDeduction;

DELETE rp
FROM Role_Permissions rp
JOIN Permissions p ON p.permissionId = rp.permissionId
WHERE p.permissionCode = 'IMPORT_PAYROLL';

DELETE FROM Permissions
WHERE permissionCode = 'IMPORT_PAYROLL';
