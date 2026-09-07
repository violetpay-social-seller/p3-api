-- Dev seed for local PostgreSQL.
-- Scenario 1: completed purchase, picked up.
-- Scenario 2: order flow in progress, confirmation sent, not paid.
--
-- Run:
-- psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f scripts/dev-seed-order-flow.sql

BEGIN;

-- Clean only this deterministic seed data.
DELETE FROM refunds
WHERE order_id IN ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1');

DELETE FROM order_status_histories
WHERE order_id IN ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1');

DELETE FROM orders
WHERE id IN ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1');

DELETE FROM payment_attempts
WHERE id IN ('99999999-9999-4999-8999-000000000001');

DELETE FROM notifications
WHERE user_id IN (
  'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
  '2a450e66-f7d9-4853-8620-64c2909553fa',
  '4c8a3c2d-50e9-4ef6-9b8c-e8859b42f011',
  '6bdb3a43-2d33-492d-9e35-6f1c2674f7e7'
);

DELETE FROM chat_timeline_items
WHERE inquiry_id IN (
  '66666666-6666-4666-8666-000000000001',
  '66666666-6666-4666-8666-000000000002'
);

DELETE FROM chat_message_assets
WHERE message_id IN (
  SELECT id
  FROM chat_messages
  WHERE inquiry_id IN (
    '66666666-6666-4666-8666-000000000001',
    '66666666-6666-4666-8666-000000000002'
  )
);

DELETE FROM chat_messages
WHERE inquiry_id IN (
  '66666666-6666-4666-8666-000000000001',
  '66666666-6666-4666-8666-000000000002'
);

DELETE FROM order_start_reference_assets
WHERE inquiry_id IN (
  '66666666-6666-4666-8666-000000000001',
  '66666666-6666-4666-8666-000000000002'
);

DELETE FROM order_confirmations
WHERE id IN (
  '88888888-8888-4888-8888-000000000001',
  '88888888-8888-4888-8888-000000000002'
);

DELETE FROM order_form_submissions
WHERE id IN (
  '77777777-7777-4777-8777-000000000001',
  '77777777-7777-4777-8777-000000000002'
);

DELETE FROM inquiries
WHERE id IN (
  '66666666-6666-4666-8666-000000000001',
  '66666666-6666-4666-8666-000000000002'
);

DELETE FROM store_notices
WHERE store_id = '11111111-1111-4111-8111-111111111111';

DELETE FROM store_weekly_pickup_settings
WHERE store_id = '11111111-1111-4111-8111-111111111111';

DELETE FROM store_operation_settings
WHERE store_id = '11111111-1111-4111-8111-111111111111';

DELETE FROM order_form_options
WHERE option_group_id IN (
  '44444444-4444-4444-8444-000000000001',
  '44444444-4444-4444-8444-000000000002',
  '44444444-4444-4444-8444-000000000003'
);

DELETE FROM order_form_option_groups
WHERE category_group_id = '33333333-3333-4333-8333-000000000001';

DELETE FROM order_form_category_groups
WHERE template_id = '22222222-2222-4222-8222-000000000001';

DELETE FROM order_form_templates
WHERE id = '22222222-2222-4222-8222-000000000001';

DELETE FROM store_gallery_items
WHERE store_id = '11111111-1111-4111-8111-111111111111';

DELETE FROM store_representative_images
WHERE store_id = '11111111-1111-4111-8111-111111111111';

UPDATE stores
SET profile_asset_id = NULL
WHERE id = '11111111-1111-4111-8111-111111111111';

DELETE FROM stores
WHERE id = '11111111-1111-4111-8111-111111111111';

DELETE FROM asset_variants
WHERE asset_id IN (
  '11111111-1111-4111-8111-000000000001',
  '11111111-1111-4111-8111-000000000002',
  '11111111-1111-4111-8111-000000000003',
  '11111111-1111-4111-8111-000000000004'
);

DELETE FROM assets
WHERE id IN (
  '11111111-1111-4111-8111-000000000001',
  '11111111-1111-4111-8111-000000000002',
  '11111111-1111-4111-8111-000000000003',
  '11111111-1111-4111-8111-000000000004'
);

DELETE FROM seller_onboardings
WHERE id = 'f1111111-1111-4111-8111-111111111111';

DELETE FROM users
WHERE id IN (
  'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
  '2a450e66-f7d9-4853-8620-64c2909553fa',
  '4c8a3c2d-50e9-4ef6-9b8c-e8859b42f011',
  '6bdb3a43-2d33-492d-9e35-6f1c2674f7e7'
);

-- Users. The first seller and buyer match the documented dev Cognito accounts.
INSERT INTO users (
  id,
  cognito_sub,
  email,
  phone_number,
  signup_provider,
  payer_id,
  name,
  role,
  status,
  created_at,
  updated_at
) VALUES
  (
    'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
    '8408dd1c-a001-70a0-7be3-3a52d3298847',
    'p3.cloud.project@gmail.com',
    '010-1111-2222',
    'GOOGLE',
    NULL,
    'Minseo Seller',
    'SELLER',
    'ACTIVE',
    '2026-09-03 09:00:00+09',
    '2026-09-03 09:00:00+09'
  ),
  (
    '2a450e66-f7d9-4853-8620-64c2909553fa',
    'c428ddbc-f001-7049-8d1b-bada2b18ac8b',
    'kms.dev.data@gmail.com',
    '010-3333-4444',
    'KAKAO',
    'payer-seed-completed',
    'Minseo Buyer Completed',
    'BUYER',
    'ACTIVE',
    '2026-09-03 09:00:00+09',
    '2026-09-03 09:00:00+09'
  ),
  (
    '4c8a3c2d-50e9-4ef6-9b8c-e8859b42f011',
    'seed-buyer-pending-cognito-sub',
    'buyer.pending.seed@example.com',
    '010-5555-6666',
    'KAKAO',
    NULL,
    'Seed Buyer Pending',
    'BUYER',
    'ACTIVE',
    '2026-09-03 09:00:00+09',
    '2026-09-03 09:00:00+09'
  ),
  (
    '6bdb3a43-2d33-492d-9e35-6f1c2674f7e7',
    'seed-operator-cognito-sub',
    'operator.seed@example.com',
    NULL,
    NULL,
    NULL,
    'Seed Operator',
    'OPERATOR',
    'ACTIVE',
    '2026-09-03 09:00:00+09',
    '2026-09-03 09:00:00+09'
  );

INSERT INTO seller_onboardings (
  id,
  applicant_user_id,
  store_name,
  phone_number,
  address,
  sns_link,
  status,
  rejection_reason,
  reviewed_by,
  reviewed_at,
  created_at,
  updated_at
) VALUES (
  'f1111111-1111-4111-8111-111111111111',
  'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
  'P3 Seed Cake',
  '010-1111-2222',
  'Seoul Jung-gu Seed-ro 3',
  'https://instagram.com/p3.seed.cakes',
  'APPROVED',
  NULL,
  '6bdb3a43-2d33-492d-9e35-6f1c2674f7e7',
  '2026-09-03 09:05:00+09',
  '2026-09-03 09:03:00+09',
  '2026-09-03 09:05:00+09'
);

INSERT INTO assets (
  id,
  uploaded_by,
  original_filename,
  content_type,
  size,
  object_key,
  status,
  created_at,
  updated_at
) VALUES
  (
    '11111111-1111-4111-8111-000000000001',
    'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
    'seed-profile-cake.jpg',
    'image/jpeg',
    12345,
    'dev-seed/stores/p3-seed-cake/profile.jpg',
    'READY',
    '2026-09-03 09:06:00+09',
    '2026-09-03 09:06:00+09'
  ),
  (
    '11111111-1111-4111-8111-000000000002',
    'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
    'seed-representative-1.jpg',
    'image/jpeg',
    12345,
    'dev-seed/stores/p3-seed-cake/representative-1.jpg',
    'READY',
    '2026-09-03 09:06:00+09',
    '2026-09-03 09:06:00+09'
  ),
  (
    '11111111-1111-4111-8111-000000000003',
    'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
    'seed-representative-2.jpg',
    'image/jpeg',
    12345,
    'dev-seed/stores/p3-seed-cake/representative-2.jpg',
    'READY',
    '2026-09-03 09:06:00+09',
    '2026-09-03 09:06:00+09'
  ),
  (
    '11111111-1111-4111-8111-000000000004',
    'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
    'seed-representative-3.jpg',
    'image/jpeg',
    12345,
    'dev-seed/stores/p3-seed-cake/representative-3.jpg',
    'READY',
    '2026-09-03 09:06:00+09',
    '2026-09-03 09:06:00+09'
  );

INSERT INTO stores (
  id,
  owner_user_id,
  profile_asset_id,
  name,
  slug,
  description,
  contact,
  contact_visible,
  sns_links,
  business_hours,
  pickup_settings,
  order_notice,
  cancellation_refund_policy,
  address,
  settlement_account_status,
  settlement_account_registered_at,
  status,
  created_at,
  updated_at
) VALUES (
  '11111111-1111-4111-8111-111111111111',
  'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
  '11111111-1111-4111-8111-000000000001',
  'P3 Seed Cake',
  'p3-seed-cake',
  'Custom cake seed store for local flow tests.',
  '010-1111-2222',
  TRUE,
  '["https://instagram.com/p3.seed.cakes"]'::jsonb,
  '{"mon":"10:00-18:00","tue":"10:00-18:00","wed":"10:00-18:00","thu":"10:00-18:00","fri":"10:00-18:00","sat":"11:00-16:00"}'::jsonb,
  '{"leadTimeDays":2,"pickupNotice":"Please arrive on time."}'::jsonb,
  'Confirm pickup date, size, lettering, and cancellation policy before submitting.',
  'Cancellation is available before production starts. Refunds follow seller confirmation.',
  'Seoul Jung-gu Seed-ro 3',
  'INPUT_COMPLETED',
  '2026-09-03 09:07:00+09',
  'ACTIVE',
  '2026-09-03 09:06:00+09',
  '2026-09-03 09:07:00+09'
);

INSERT INTO store_representative_images (
  id,
  store_id,
  asset_id,
  sort_order,
  status,
  created_at,
  updated_at
) VALUES
  (
    '12111111-1111-4111-8111-000000000001',
    '11111111-1111-4111-8111-111111111111',
    '11111111-1111-4111-8111-000000000002',
    0,
    'ACTIVE',
    '2026-09-03 09:08:00+09',
    '2026-09-03 09:08:00+09'
  ),
  (
    '12111111-1111-4111-8111-000000000002',
    '11111111-1111-4111-8111-111111111111',
    '11111111-1111-4111-8111-000000000003',
    1,
    'ACTIVE',
    '2026-09-03 09:08:00+09',
    '2026-09-03 09:08:00+09'
  ),
  (
    '12111111-1111-4111-8111-000000000003',
    '11111111-1111-4111-8111-111111111111',
    '11111111-1111-4111-8111-000000000004',
    2,
    'ACTIVE',
    '2026-09-03 09:08:00+09',
    '2026-09-03 09:08:00+09'
  );

INSERT INTO store_gallery_items (
  id,
  store_id,
  asset_id,
  sort_order,
  featured,
  status,
  created_at,
  updated_at
) VALUES (
  '13111111-1111-4111-8111-000000000001',
  '11111111-1111-4111-8111-111111111111',
  '11111111-1111-4111-8111-000000000002',
  0,
  TRUE,
  'VISIBLE',
  '2026-09-03 09:08:00+09',
  '2026-09-03 09:08:00+09'
);

INSERT INTO store_operation_settings (
  store_id,
  lead_time_minutes,
  pre_order_notice,
  cancellation_cutoff_days,
  created_at,
  updated_at
) VALUES (
  '11111111-1111-4111-8111-111111111111',
  2880,
  'Submit after checking pickup time and design details.',
  2,
  '2026-09-03 09:09:00+09',
  '2026-09-03 09:09:00+09'
);

INSERT INTO store_weekly_pickup_settings (
  id,
  store_id,
  day_of_week,
  start_time,
  end_time,
  daily_order_capacity,
  enabled,
  created_at,
  updated_at
) VALUES
  ('14111111-1111-4111-8111-000000000001', '11111111-1111-4111-8111-111111111111', 'MONDAY', '10:00:00', '18:00:00', 10, TRUE, '2026-09-03 09:09:00+09', '2026-09-03 09:09:00+09'),
  ('14111111-1111-4111-8111-000000000002', '11111111-1111-4111-8111-111111111111', 'TUESDAY', '10:00:00', '18:00:00', 10, TRUE, '2026-09-03 09:09:00+09', '2026-09-03 09:09:00+09'),
  ('14111111-1111-4111-8111-000000000003', '11111111-1111-4111-8111-111111111111', 'WEDNESDAY', '10:00:00', '18:00:00', 10, TRUE, '2026-09-03 09:09:00+09', '2026-09-03 09:09:00+09'),
  ('14111111-1111-4111-8111-000000000004', '11111111-1111-4111-8111-111111111111', 'THURSDAY', '10:00:00', '18:00:00', 10, TRUE, '2026-09-03 09:09:00+09', '2026-09-03 09:09:00+09'),
  ('14111111-1111-4111-8111-000000000005', '11111111-1111-4111-8111-111111111111', 'FRIDAY', '10:00:00', '18:00:00', 10, TRUE, '2026-09-03 09:09:00+09', '2026-09-03 09:09:00+09'),
  ('14111111-1111-4111-8111-000000000006', '11111111-1111-4111-8111-111111111111', 'SATURDAY', '11:00:00', '16:00:00', 6, TRUE, '2026-09-03 09:09:00+09', '2026-09-03 09:09:00+09'),
  ('14111111-1111-4111-8111-000000000007', '11111111-1111-4111-8111-111111111111', 'SUNDAY', '11:00:00', '16:00:00', 3, FALSE, '2026-09-03 09:09:00+09', '2026-09-03 09:09:00+09');

INSERT INTO store_notices (
  id,
  store_id,
  type,
  content,
  sort_order,
  created_at,
  updated_at
) VALUES
  ('15111111-1111-4111-8111-000000000001', '11111111-1111-4111-8111-111111111111', 'PICKUP_DELIVERY', 'Pickup is available only during selected pickup hours.', 0, '2026-09-03 09:10:00+09', '2026-09-03 09:10:00+09'),
  ('15111111-1111-4111-8111-000000000002', '11111111-1111-4111-8111-111111111111', 'DESIGN_PRODUCTION', 'Design details are confirmed through the order confirmation.', 0, '2026-09-03 09:10:00+09', '2026-09-03 09:10:00+09'),
  ('15111111-1111-4111-8111-000000000003', '11111111-1111-4111-8111-111111111111', 'PAYMENT', 'Payment starts after the seller sends the order confirmation.', 0, '2026-09-03 09:10:00+09', '2026-09-03 09:10:00+09'),
  ('15111111-1111-4111-8111-000000000004', '11111111-1111-4111-8111-111111111111', 'CAKE_CARE', 'Keep the cake refrigerated after pickup.', 0, '2026-09-03 09:10:00+09', '2026-09-03 09:10:00+09'),
  ('15111111-1111-4111-8111-000000000005', '11111111-1111-4111-8111-111111111111', 'BUSINESS_HOURS', 'Business hours may change on holidays.', 0, '2026-09-03 09:10:00+09', '2026-09-03 09:10:00+09');

INSERT INTO order_form_templates (
  id,
  store_id,
  name,
  active,
  created_at,
  updated_at
) VALUES (
  '22222222-2222-4222-8222-000000000001',
  '11111111-1111-4111-8111-111111111111',
  'Seed Cake Order Form',
  TRUE,
  '2026-09-03 09:11:00+09',
  '2026-09-03 09:11:00+09'
);

INSERT INTO order_form_category_groups (
  id,
  template_id,
  category,
  title,
  description,
  sort_order
) VALUES (
  '33333333-3333-4333-8333-000000000001',
  '22222222-2222-4222-8222-000000000001',
  'SIZE',
  'Order details',
  'Menu, size, and lettering options.',
  0
);

INSERT INTO order_form_option_groups (
  id,
  category_group_id,
  label,
  selection_type,
  required,
  sort_order
) VALUES
  ('44444444-4444-4444-8444-000000000001', '33333333-3333-4333-8333-000000000001', 'Menu name', 'SINGLE', TRUE, 0),
  ('44444444-4444-4444-8444-000000000002', '33333333-3333-4333-8333-000000000001', 'Size', 'SINGLE', TRUE, 1),
  ('44444444-4444-4444-8444-000000000003', '33333333-3333-4333-8333-000000000001', 'Lettering', 'SINGLE', FALSE, 2);

INSERT INTO order_form_options (
  id,
  option_group_id,
  label,
  value,
  input_type,
  price,
  price_label,
  settings,
  sort_order,
  active
) VALUES
  ('55555555-5555-4555-8555-000000000001', '44444444-4444-4444-8444-000000000001', 'Menu name', 'menu', 'TEXT', 0, NULL, NULL, 0, TRUE),
  ('55555555-5555-4555-8555-000000000002', '44444444-4444-4444-8444-000000000002', '1号', 'size-1', 'SELECT', 38000, NULL, NULL, 0, TRUE),
  ('55555555-5555-4555-8555-000000000003', '44444444-4444-4444-8444-000000000002', '2号', 'size-2', 'SELECT', 45000, NULL, NULL, 1, TRUE),
  ('55555555-5555-4555-8555-000000000004', '44444444-4444-4444-8444-000000000003', 'Lettering text', 'lettering', 'TEXT', 0, NULL, '{"maxLength":30}'::jsonb, 0, TRUE);

INSERT INTO inquiries (
  id,
  store_id,
  buyer_user_id,
  status,
  buyer_last_read_at,
  seller_last_read_at,
  created_at,
  buyer_deleted_at,
  seller_deleted_at,
  buyer_purged_at,
  seller_purged_at
) VALUES
  (
    '66666666-6666-4666-8666-000000000001',
    '11111111-1111-4111-8111-111111111111',
    '2a450e66-f7d9-4853-8620-64c2909553fa',
    'PICKED_UP',
    '2026-09-03 13:50:00+09',
    '2026-09-03 13:55:00+09',
    '2026-09-03 10:00:00+09',
    NULL,
    NULL,
    NULL,
    NULL
  ),
  (
    '66666666-6666-4666-8666-000000000002',
    '11111111-1111-4111-8111-111111111111',
    '4c8a3c2d-50e9-4ef6-9b8c-e8859b42f011',
    'IN_PROGRESS',
    '2026-09-03 15:05:00+09',
    '2026-09-03 15:10:00+09',
    '2026-09-03 14:00:00+09',
    NULL,
    NULL,
    NULL,
    NULL
  );

INSERT INTO order_form_submissions (
  id,
  inquiry_id,
  template_id,
  submitted_by,
  answers,
  reference_assets,
  submitted_at,
  cancellation_refund_agreed,
  pickup_date,
  pickup_time
) VALUES
  (
    '77777777-7777-4777-8777-000000000001',
    '66666666-6666-4666-8666-000000000001',
    '22222222-2222-4222-8222-000000000001',
    '2a450e66-f7d9-4853-8620-64c2909553fa',
    '[
      {
        "optionGroupId": "44444444-4444-4444-8444-000000000001",
        "label": "Menu name",
        "selectionType": "SINGLE",
        "required": true,
        "sortOrder": 0,
        "value": [{"optionValue": "menu", "text": "Chocolate cake"}],
        "selectedOptions": [
          {
            "label": "Menu name",
            "value": "menu",
            "inputType": "TEXT",
            "price": 0,
            "priceLabel": null,
            "settings": null,
            "text": "Chocolate cake",
            "assetIds": []
          }
        ]
      },
      {
        "optionGroupId": "44444444-4444-4444-8444-000000000002",
        "label": "Size",
        "selectionType": "SINGLE",
        "required": true,
        "sortOrder": 1,
        "value": [{"optionValue": "size-1"}],
        "selectedOptions": [
          {
            "label": "1号",
            "value": "size-1",
            "inputType": "SELECT",
            "price": 38000,
            "priceLabel": null,
            "settings": null,
            "text": null,
            "assetIds": []
          }
        ]
      },
      {
        "optionGroupId": "44444444-4444-4444-8444-000000000003",
        "label": "Lettering",
        "selectionType": "SINGLE",
        "required": false,
        "sortOrder": 2,
        "value": [{"optionValue": "lettering", "text": "Happy day"}],
        "selectedOptions": [
          {
            "label": "Lettering text",
            "value": "lettering",
            "inputType": "TEXT",
            "price": 0,
            "priceLabel": null,
            "settings": "{\"maxLength\":30}",
            "text": "Happy day",
            "assetIds": []
          }
        ]
      }
    ]'::jsonb,
    NULL,
    '2026-09-03 10:10:00+09',
    TRUE,
    '2026-09-03',
    '13:30:00'
  ),
  (
    '77777777-7777-4777-8777-000000000002',
    '66666666-6666-4666-8666-000000000002',
    '22222222-2222-4222-8222-000000000001',
    '4c8a3c2d-50e9-4ef6-9b8c-e8859b42f011',
    '[
      {
        "optionGroupId": "44444444-4444-4444-8444-000000000001",
        "label": "Menu name",
        "selectionType": "SINGLE",
        "required": true,
        "sortOrder": 0,
        "value": [{"optionValue": "menu", "text": "Strawberry cake"}],
        "selectedOptions": [
          {
            "label": "Menu name",
            "value": "menu",
            "inputType": "TEXT",
            "price": 0,
            "priceLabel": null,
            "settings": null,
            "text": "Strawberry cake",
            "assetIds": []
          }
        ]
      },
      {
        "optionGroupId": "44444444-4444-4444-8444-000000000002",
        "label": "Size",
        "selectionType": "SINGLE",
        "required": true,
        "sortOrder": 1,
        "value": [{"optionValue": "size-2"}],
        "selectedOptions": [
          {
            "label": "2号",
            "value": "size-2",
            "inputType": "SELECT",
            "price": 45000,
            "priceLabel": null,
            "settings": null,
            "text": null,
            "assetIds": []
          }
        ]
      },
      {
        "optionGroupId": "44444444-4444-4444-8444-000000000003",
        "label": "Lettering",
        "selectionType": "SINGLE",
        "required": false,
        "sortOrder": 2,
        "value": [{"optionValue": "lettering", "text": "Congrats"}],
        "selectedOptions": [
          {
            "label": "Lettering text",
            "value": "lettering",
            "inputType": "TEXT",
            "price": 0,
            "priceLabel": null,
            "settings": "{\"maxLength\":30}",
            "text": "Congrats",
            "assetIds": []
          }
        ]
      }
    ]'::jsonb,
    NULL,
    '2026-09-03 14:10:00+09',
    TRUE,
    '2026-09-05',
    '14:00:00'
  );

INSERT INTO order_confirmations (
  id,
  inquiry_id,
  order_form_submission_id,
  created_by,
  menu_name,
  option_summary,
  amount,
  pickup_at,
  store_name_snapshot,
  order_summary,
  additional_items,
  seller_note,
  status,
  sent_at,
  revision_requested_at,
  buyer_viewed_at,
  replaced_by_confirmation_id,
  created_at
) VALUES
  (
    '88888888-8888-4888-8888-000000000001',
    '66666666-6666-4666-8666-000000000001',
    '77777777-7777-4777-8777-000000000001',
    'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
    'Chocolate cake',
    'Chocolate cake / Size 1 / Happy day / Number candle',
    41000,
    '2026-09-03 13:30:00+09',
    'P3 Seed Cake',
    '{
      "orderFormSubmissionId": "77777777-7777-4777-8777-000000000001",
      "answers": [
        {
          "optionGroupId": "44444444-4444-4444-8444-000000000001",
          "label": "Menu name",
          "selectionType": "SINGLE",
          "required": true,
          "sortOrder": 0,
          "value": [{"optionValue": "menu", "text": "Chocolate cake"}],
          "selectedOptions": [
            {
              "label": "Menu name",
              "value": "menu",
              "inputType": "TEXT",
              "price": 0,
              "priceLabel": null,
              "settings": null,
              "text": "Chocolate cake",
              "assetIds": []
            }
          ]
        },
        {
          "optionGroupId": "44444444-4444-4444-8444-000000000002",
          "label": "Size",
          "selectionType": "SINGLE",
          "required": true,
          "sortOrder": 1,
          "value": [{"optionValue": "size-1"}],
          "selectedOptions": [
            {
              "label": "1号",
              "value": "size-1",
              "inputType": "SELECT",
              "price": 38000,
              "priceLabel": null,
              "settings": null,
              "text": null,
              "assetIds": []
            }
          ]
        },
        {
          "optionGroupId": "44444444-4444-4444-8444-000000000003",
          "label": "Lettering",
          "selectionType": "SINGLE",
          "required": false,
          "sortOrder": 2,
          "value": [{"optionValue": "lettering", "text": "Happy day"}],
          "selectedOptions": [
            {
              "label": "Lettering text",
              "value": "lettering",
              "inputType": "TEXT",
              "price": 0,
              "priceLabel": null,
              "settings": "{\"maxLength\":30}",
              "text": "Happy day",
              "assetIds": []
            }
          ]
        }
      ],
      "referenceAssets": null
    }'::jsonb,
    '[{"label":"Candle","value":"Number candle","amount":3000}]'::jsonb,
    'Pickup is complete. Thank you.',
    'PAID',
    '2026-09-03 10:30:00+09',
    NULL,
    '2026-09-03 10:35:00+09',
    NULL,
    '2026-09-03 10:30:00+09'
  ),
  (
    '88888888-8888-4888-8888-000000000002',
    '66666666-6666-4666-8666-000000000002',
    '77777777-7777-4777-8777-000000000002',
    'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
    'Strawberry cake',
    'Strawberry cake / Size 2 / Congrats / Number candle',
    48000,
    '2026-09-05 14:00:00+09',
    'P3 Seed Cake',
    '{
      "orderFormSubmissionId": "77777777-7777-4777-8777-000000000002",
      "answers": [
        {
          "optionGroupId": "44444444-4444-4444-8444-000000000001",
          "label": "Menu name",
          "selectionType": "SINGLE",
          "required": true,
          "sortOrder": 0,
          "value": [{"optionValue": "menu", "text": "Strawberry cake"}],
          "selectedOptions": [
            {
              "label": "Menu name",
              "value": "menu",
              "inputType": "TEXT",
              "price": 0,
              "priceLabel": null,
              "settings": null,
              "text": "Strawberry cake",
              "assetIds": []
            }
          ]
        },
        {
          "optionGroupId": "44444444-4444-4444-8444-000000000002",
          "label": "Size",
          "selectionType": "SINGLE",
          "required": true,
          "sortOrder": 1,
          "value": [{"optionValue": "size-2"}],
          "selectedOptions": [
            {
              "label": "2号",
              "value": "size-2",
              "inputType": "SELECT",
              "price": 45000,
              "priceLabel": null,
              "settings": null,
              "text": null,
              "assetIds": []
            }
          ]
        },
        {
          "optionGroupId": "44444444-4444-4444-8444-000000000003",
          "label": "Lettering",
          "selectionType": "SINGLE",
          "required": false,
          "sortOrder": 2,
          "value": [{"optionValue": "lettering", "text": "Congrats"}],
          "selectedOptions": [
            {
              "label": "Lettering text",
              "value": "lettering",
              "inputType": "TEXT",
              "price": 0,
              "priceLabel": null,
              "settings": "{\"maxLength\":30}",
              "text": "Congrats",
              "assetIds": []
            }
          ]
        }
      ],
      "referenceAssets": null
    }'::jsonb,
    '[{"label":"Candle","value":"Number candle","amount":3000}]'::jsonb,
    'Please check the confirmation and proceed with payment.',
    'SENT',
    '2026-09-03 14:30:00+09',
    NULL,
    NULL,
    NULL,
    '2026-09-03 14:30:00+09'
  );

INSERT INTO payment_attempts (
  id,
  confirmation_id,
  payer_user_id,
  point3_session_id,
  payer_id,
  amount,
  status,
  failure_code,
  created_at,
  completed_at,
  expires_at
) VALUES (
  '99999999-9999-4999-8999-000000000001',
  '88888888-8888-4888-8888-000000000001',
  '2a450e66-f7d9-4853-8620-64c2909553fa',
  'seed-session-completed-001',
  'payer-seed-completed',
  41000,
  'SUCCEEDED',
  NULL,
  '2026-09-03 10:40:00+09',
  '2026-09-03 10:45:00+09',
  '2026-09-04 10:40:00+09'
);

INSERT INTO orders (
  id,
  store_id,
  buyer_user_id,
  inquiry_id,
  confirmation_id,
  payment_attempt_id,
  order_number,
  menu_name_snapshot,
  option_summary_snapshot,
  start_reference_assets,
  paid_amount,
  pickup_at,
  status,
  cancel_requested_at,
  cancel_reason,
  created_at,
  updated_at
) VALUES (
  'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1',
  '11111111-1111-4111-8111-111111111111',
  '2a450e66-f7d9-4853-8620-64c2909553fa',
  '66666666-6666-4666-8666-000000000001',
  '88888888-8888-4888-8888-000000000001',
  '99999999-9999-4999-8999-000000000001',
  'P3-20260903-seed-picked-up-001',
  'Chocolate cake',
  'Chocolate cake / Size 1 / Happy day / Number candle',
  '[]'::jsonb,
  41000,
  '2026-09-03 13:30:00+09',
  'PICKED_UP',
  NULL,
  NULL,
  '2026-09-03 10:45:00+09',
  '2026-09-03 13:55:00+09'
);

INSERT INTO order_status_histories (
  id,
  order_id,
  previous_status,
  new_status,
  changed_by,
  reason,
  created_at
) VALUES
  (
    'abababab-abab-4bab-8bab-000000000001',
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1',
    NULL,
    'PAID',
    '2a450e66-f7d9-4853-8620-64c2909553fa',
    'PAYMENT_SUCCEEDED',
    '2026-09-03 10:45:00+09'
  ),
  (
    'abababab-abab-4bab-8bab-000000000002',
    'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1',
    'PAID',
    'PICKED_UP',
    'c822ed91-47a1-452f-90fe-ab92c64f3dbb',
    'PICKUP_COMPLETED',
    '2026-09-03 13:55:00+09'
  );

INSERT INTO chat_timeline_items (
  id,
  inquiry_id,
  sender_user_id,
  type,
  reference_id,
  created_at
) VALUES
  ('bbbbbbbb-bbbb-4bbb-8bbb-000000000001', '66666666-6666-4666-8666-000000000001', '2a450e66-f7d9-4853-8620-64c2909553fa', 'ORDER_FORM_SUBMISSION', '77777777-7777-4777-8777-000000000001', '2026-09-03 10:10:00+09'),
  ('bbbbbbbb-bbbb-4bbb-8bbb-000000000002', '66666666-6666-4666-8666-000000000001', 'c822ed91-47a1-452f-90fe-ab92c64f3dbb', 'ORDER_CONFIRMATION', '88888888-8888-4888-8888-000000000001', '2026-09-03 10:30:00+09'),
  ('bbbbbbbb-bbbb-4bbb-8bbb-000000000003', '66666666-6666-4666-8666-000000000001', '2a450e66-f7d9-4853-8620-64c2909553fa', 'PAYMENT_COMPLETED', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1', '2026-09-03 10:45:00+09'),
  ('bbbbbbbb-bbbb-4bbb-8bbb-000000000004', '66666666-6666-4666-8666-000000000002', '4c8a3c2d-50e9-4ef6-9b8c-e8859b42f011', 'ORDER_FORM_SUBMISSION', '77777777-7777-4777-8777-000000000002', '2026-09-03 14:10:00+09'),
  ('bbbbbbbb-bbbb-4bbb-8bbb-000000000005', '66666666-6666-4666-8666-000000000002', 'c822ed91-47a1-452f-90fe-ab92c64f3dbb', 'ORDER_CONFIRMATION', '88888888-8888-4888-8888-000000000002', '2026-09-03 14:30:00+09');

INSERT INTO notifications (
  id,
  user_id,
  type,
  reference_type,
  reference_id,
  title,
  body,
  read_at,
  created_at
) VALUES
  ('cccccccc-cccc-4ccc-8ccc-000000000001', 'c822ed91-47a1-452f-90fe-ab92c64f3dbb', 'ORDER_FORM_SUBMITTED', 'INQUIRY', '66666666-6666-4666-8666-000000000001', 'Order form submitted.', 'Check submitted order form.', '2026-09-03 10:20:00+09', '2026-09-03 10:10:00+09'),
  ('cccccccc-cccc-4ccc-8ccc-000000000002', '2a450e66-f7d9-4853-8620-64c2909553fa', 'ORDER_CONFIRMATION_SENT', 'ORDER_CONFIRMATION', '88888888-8888-4888-8888-000000000001', 'Order confirmation sent.', 'Check the order confirmation.', '2026-09-03 10:35:00+09', '2026-09-03 10:30:00+09'),
  ('cccccccc-cccc-4ccc-8ccc-000000000003', '2a450e66-f7d9-4853-8620-64c2909553fa', 'PAYMENT_COMPLETED', 'ORDER', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1', 'Payment completed.', 'Check your order.', '2026-09-03 11:00:00+09', '2026-09-03 10:45:00+09'),
  ('cccccccc-cccc-4ccc-8ccc-000000000004', 'c822ed91-47a1-452f-90fe-ab92c64f3dbb', 'PAYMENT_COMPLETED', 'ORDER', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1', 'Payment completed.', 'Check the new order.', '2026-09-03 11:00:00+09', '2026-09-03 10:45:00+09'),
  ('cccccccc-cccc-4ccc-8ccc-000000000005', 'c822ed91-47a1-452f-90fe-ab92c64f3dbb', 'ORDER_FORM_SUBMITTED', 'INQUIRY', '66666666-6666-4666-8666-000000000002', 'Order form submitted.', 'Check submitted order form.', NULL, '2026-09-03 14:10:00+09'),
  ('cccccccc-cccc-4ccc-8ccc-000000000006', '4c8a3c2d-50e9-4ef6-9b8c-e8859b42f011', 'ORDER_CONFIRMATION_SENT', 'ORDER_CONFIRMATION', '88888888-8888-4888-8888-000000000002', 'Order confirmation sent.', 'Check the order confirmation.', NULL, '2026-09-03 14:30:00+09');

COMMIT;
