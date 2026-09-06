ALTER TABLE stores
    ALTER COLUMN business_hours TYPE text
    USING business_hours #>> '{}';
