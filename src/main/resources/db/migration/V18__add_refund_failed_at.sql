ALTER TABLE refunds
    ADD COLUMN failed_at TIMESTAMPTZ;

UPDATE refunds
SET failed_at = COALESCE(completed_at, created_at)
WHERE status = 'FAILED' AND failed_at IS NULL;
