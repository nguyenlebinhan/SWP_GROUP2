-- Migration: Add duration columns to Employment_Contracts
-- Purpose: Store durationValue + durationUnit for End Date auto calculation
-- Author: AI Assistant

ALTER TABLE Employment_Contracts
  ADD COLUMN durationValue INT NULL AFTER positionName,
  ADD COLUMN durationUnit VARCHAR(10) NULL AFTER durationValue;