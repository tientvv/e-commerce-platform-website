-- Add google_id column to accounts table if it doesn't exist
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'accounts' AND COLUMN_NAME = 'google_id')
BEGIN
    ALTER TABLE accounts ADD google_id VARCHAR(255);
END
