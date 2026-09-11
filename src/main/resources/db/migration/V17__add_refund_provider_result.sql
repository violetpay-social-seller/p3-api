ALTER TABLE refunds
    ADD COLUMN outcome VARCHAR(30),
    ADD COLUMN provider_refund_id VARCHAR(128),
    ADD COLUMN failure_code VARCHAR(100),
    ADD COLUMN failure_message TEXT,
    ADD COLUMN failure_details JSONB,
    ADD COLUMN failed_at TIMESTAMPTZ;

ALTER TABLE orders
    RENAME COLUMN cancel_requested_at TO refund_requested_at;

ALTER TABLE orders
    RENAME COLUMN cancel_reason TO refund_reason;

UPDATE orders
SET status = 'REFUND_REQUESTED'
WHERE status IN ('CANCEL_REQUESTED', 'REFUND_PROCESSING');

UPDATE orders
SET status = 'REFUNDED'
WHERE status = 'CANCELED';

UPDATE refunds
SET outcome = CASE
    WHEN status = 'COMPLETED' THEN 'COMPLETED'
    WHEN status = 'FAILED' THEN 'FAILED'
    ELSE 'PROCESSING'
END
WHERE outcome IS NULL;

UPDATE refunds
SET failed_at = COALESCE(completed_at, created_at)
WHERE status = 'FAILED' AND failed_at IS NULL;

ALTER TABLE refunds
    ALTER COLUMN outcome SET NOT NULL;
