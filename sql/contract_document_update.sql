ALTER TABLE Employment_Contracts
  ADD COLUMN contractFilePath VARCHAR(255) NULL AFTER positionName,
  ADD COLUMN contractFileName VARCHAR(255) NULL AFTER contractFilePath,
  ADD COLUMN uploadedAt TIMESTAMP NULL AFTER contractFileName,
  ADD COLUMN uploadedBy INT NULL AFTER uploadedAt,
  ADD FOREIGN KEY (uploadedBy) REFERENCES Users(userId);