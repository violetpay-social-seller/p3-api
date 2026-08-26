ALTER TABLE order_form_submissions
    ADD COLUMN cancellation_refund_agreed BOOLEAN NOT NULL DEFAULT FALSE;
