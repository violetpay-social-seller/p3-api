ALTER TABLE refunds
    ADD COLUMN refund_rate INTEGER NOT NULL DEFAULT 100,
    ADD CONSTRAINT ck_refunds_refund_rate CHECK (refund_rate BETWEEN 0 AND 100);
