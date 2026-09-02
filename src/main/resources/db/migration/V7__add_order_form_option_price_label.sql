ALTER TABLE order_form_options
    ADD COLUMN price_label VARCHAR(100);

ALTER TABLE order_form_options
    DROP CONSTRAINT ck_order_form_options_price;

ALTER TABLE order_form_options
    ADD CONSTRAINT ck_order_form_options_price CHECK (
        (
            input_type IN ('SELECT', 'SELECT_WITH_TEXT', 'TEXT')
            AND (
                (price IS NOT NULL AND price >= 0 AND price_label IS NULL)
                OR (price IS NULL AND price_label IS NOT NULL AND btrim(price_label) <> '')
            )
        )
        OR (
            input_type = 'IMAGE'
            AND (
                (price IS NOT NULL AND price >= 0 AND price_label IS NULL)
                OR (price IS NULL AND price_label IS NOT NULL AND btrim(price_label) <> '')
                OR (price IS NULL AND price_label IS NULL)
            )
        )
        OR (
            input_type = 'TEXTAREA'
            AND price IS NULL
            AND price_label IS NULL
        )
    );
