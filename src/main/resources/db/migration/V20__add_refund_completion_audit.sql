ALTER TABLE refunds
    ADD COLUMN completed_by UUID,
    ADD COLUMN completion_method VARCHAR(30);
