ALTER TABLE order_form_category_groups
    DROP CONSTRAINT ck_order_form_category_groups_category;

UPDATE order_form_category_groups
SET category = 'SIZE',
    title = '사이즈'
WHERE category = 'DESIGN';

ALTER TABLE order_form_category_groups
    ADD CONSTRAINT ck_order_form_category_groups_category
        CHECK (category IN ('SIZE', 'SHAPE', 'CAKE_FLAVOR', 'CAKE_DESIGN', 'PACKAGING', 'OTHER_REQUEST'));
