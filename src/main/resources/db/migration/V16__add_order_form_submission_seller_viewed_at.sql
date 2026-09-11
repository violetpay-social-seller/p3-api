ALTER TABLE order_form_submissions
    ADD COLUMN seller_viewed_at TIMESTAMPTZ;

UPDATE order_form_submissions submission
SET seller_viewed_at = confirmation.seller_viewed_at
FROM (
    SELECT
        order_form_submission_id,
        MIN(COALESCE(sent_at, created_at)) AS seller_viewed_at
    FROM order_confirmations
    WHERE order_form_submission_id IS NOT NULL
    GROUP BY order_form_submission_id
) confirmation
WHERE submission.id = confirmation.order_form_submission_id
  AND submission.seller_viewed_at IS NULL;
