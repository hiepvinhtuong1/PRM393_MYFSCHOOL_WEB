-- Add subject coefficient for semester average calculation (TT22/2021)
-- Toán and Ngữ Văn have coefficient 2; all others default to 1
ALTER TABLE subjects ADD COLUMN coefficient SMALLINT NOT NULL DEFAULT 1;
