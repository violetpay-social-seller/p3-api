CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cognito_sub VARCHAR(128) NOT NULL,
    email VARCHAR(320) NOT NULL,
    payer_id VARCHAR(128),
    name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_users_cognito_sub UNIQUE (cognito_sub)
);

CREATE UNIQUE INDEX uk_users_email_lower ON users (lower(email));

CREATE TABLE assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    uploaded_by UUID NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    object_key VARCHAR(1024) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_assets_object_key UNIQUE (object_key),
    CONSTRAINT fk_assets_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id),
    CONSTRAINT ck_assets_size CHECK (size >= 0)
);

CREATE TABLE stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID NOT NULL,
    profile_asset_id UUID,
    banner_asset_id UUID,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    description TEXT,
    contact VARCHAR(100),
    contact_visible BOOLEAN NOT NULL DEFAULT FALSE,
    sns_links JSONB,
    business_hours JSONB,
    address VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_stores_owner_user_id UNIQUE (owner_user_id),
    CONSTRAINT uk_stores_slug UNIQUE (slug),
    CONSTRAINT fk_stores_owner_user_id FOREIGN KEY (owner_user_id) REFERENCES users (id),
    CONSTRAINT fk_stores_profile_asset_id FOREIGN KEY (profile_asset_id) REFERENCES assets (id) ON DELETE SET NULL,
    CONSTRAINT fk_stores_banner_asset_id FOREIGN KEY (banner_asset_id) REFERENCES assets (id) ON DELETE SET NULL
);

CREATE TABLE asset_variants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    object_key VARCHAR(1024) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    size BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_asset_variants_object_key UNIQUE (object_key),
    CONSTRAINT fk_asset_variants_asset_id FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT ck_asset_variants_width CHECK (width > 0),
    CONSTRAINT ck_asset_variants_height CHECK (height > 0),
    CONSTRAINT ck_asset_variants_size CHECK (size >= 0)
);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    base_price BIGINT,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_products_store_id FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE,
    CONSTRAINT ck_products_base_price CHECK (base_price IS NULL OR base_price >= 0)
);

CREATE TABLE product_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    sort_order INTEGER NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_product_assets_product_sort_order UNIQUE (product_id, sort_order),
    CONSTRAINT fk_product_assets_product_id FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_assets_asset_id FOREIGN KEY (asset_id) REFERENCES assets (id),
    CONSTRAINT ck_product_assets_sort_order CHECK (sort_order >= 0)
);

CREATE UNIQUE INDEX uk_product_assets_primary
    ON product_assets (product_id)
    WHERE is_primary;

CREATE TABLE product_option_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    selection_type VARCHAR(30) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL,
    CONSTRAINT uk_product_option_groups_product_sort_order UNIQUE (product_id, sort_order),
    CONSTRAINT fk_product_option_groups_product_id FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT ck_product_option_groups_sort_order CHECK (sort_order >= 0)
);

CREATE TABLE product_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    option_group_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    additional_price BIGINT NOT NULL DEFAULT 0,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_product_options_group_sort_order UNIQUE (option_group_id, sort_order),
    CONSTRAINT fk_product_options_option_group_id FOREIGN KEY (option_group_id) REFERENCES product_option_groups (id) ON DELETE CASCADE,
    CONSTRAINT ck_product_options_additional_price CHECK (additional_price >= 0),
    CONSTRAINT ck_product_options_sort_order CHECK (sort_order >= 0)
);

CREATE TABLE order_form_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_order_form_templates_store_id FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE
);

CREATE TABLE order_form_fields (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID NOT NULL,
    label VARCHAR(150) NOT NULL,
    field_type VARCHAR(30) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    settings JSONB,
    sort_order INTEGER NOT NULL,
    CONSTRAINT uk_order_form_fields_template_sort_order UNIQUE (template_id, sort_order),
    CONSTRAINT fk_order_form_fields_template_id FOREIGN KEY (template_id) REFERENCES order_form_templates (id) ON DELETE CASCADE,
    CONSTRAINT ck_order_form_fields_sort_order CHECK (sort_order >= 0)
);

CREATE TABLE inquiries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    buyer_user_id UUID NOT NULL,
    context_product_id UUID,
    buyer_last_read_at TIMESTAMPTZ,
    seller_last_read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_inquiries_store_buyer UNIQUE (store_id, buyer_user_id),
    CONSTRAINT fk_inquiries_store_id FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE,
    CONSTRAINT fk_inquiries_buyer_user_id FOREIGN KEY (buyer_user_id) REFERENCES users (id),
    CONSTRAINT fk_inquiries_context_product_id FOREIGN KEY (context_product_id) REFERENCES products (id) ON DELETE SET NULL
);

CREATE TABLE order_form_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_id UUID NOT NULL,
    template_id UUID NOT NULL,
    submitted_by UUID NOT NULL,
    answers JSONB NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_order_form_submissions_inquiry_id FOREIGN KEY (inquiry_id) REFERENCES inquiries (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_form_submissions_template_id FOREIGN KEY (template_id) REFERENCES order_form_templates (id),
    CONSTRAINT fk_order_form_submissions_submitted_by FOREIGN KEY (submitted_by) REFERENCES users (id)
);

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_id UUID NOT NULL,
    sender_user_id UUID NOT NULL,
    content TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_chat_messages_inquiry_id FOREIGN KEY (inquiry_id) REFERENCES inquiries (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_sender_user_id FOREIGN KEY (sender_user_id) REFERENCES users (id)
);

CREATE TABLE chat_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_id UUID NOT NULL,
    sender_user_id UUID,
    type VARCHAR(30) NOT NULL,
    reference_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_chat_events_inquiry_id FOREIGN KEY (inquiry_id) REFERENCES inquiries (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_events_sender_user_id FOREIGN KEY (sender_user_id) REFERENCES users (id)
);

CREATE TABLE chat_message_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    sort_order INTEGER NOT NULL,
    CONSTRAINT uk_chat_message_assets_message_sort_order UNIQUE (message_id, sort_order),
    CONSTRAINT fk_chat_message_assets_message_id FOREIGN KEY (message_id) REFERENCES chat_messages (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_assets_asset_id FOREIGN KEY (asset_id) REFERENCES assets (id),
    CONSTRAINT ck_chat_message_assets_sort_order CHECK (sort_order >= 0)
);

CREATE TABLE order_confirmations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_id UUID NOT NULL,
    created_by UUID NOT NULL,
    menu_name VARCHAR(150) NOT NULL,
    option_summary TEXT NOT NULL,
    amount BIGINT NOT NULL,
    pickup_at TIMESTAMPTZ NOT NULL,
    store_name_snapshot VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_order_confirmations_inquiry_id FOREIGN KEY (inquiry_id) REFERENCES inquiries (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_confirmations_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_order_confirmations_amount CHECK (amount >= 0)
);

CREATE TABLE payment_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_id UUID NOT NULL,
    confirmation_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ,
    CONSTRAINT fk_payment_requests_inquiry_id FOREIGN KEY (inquiry_id) REFERENCES inquiries (id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_requests_confirmation_id FOREIGN KEY (confirmation_id) REFERENCES order_confirmations (id),
    CONSTRAINT fk_payment_requests_requested_by FOREIGN KEY (requested_by) REFERENCES users (id)
);

CREATE TABLE payment_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_request_id UUID NOT NULL,
    payer_user_id UUID NOT NULL,
    point3_session_id VARCHAR(128) NOT NULL,
    payer_id VARCHAR(128),
    amount BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    failure_code VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT uk_payment_attempts_point3_session_id UNIQUE (point3_session_id),
    CONSTRAINT fk_payment_attempts_payment_request_id FOREIGN KEY (payment_request_id) REFERENCES payment_requests (id),
    CONSTRAINT fk_payment_attempts_payer_user_id FOREIGN KEY (payer_user_id) REFERENCES users (id),
    CONSTRAINT ck_payment_attempts_amount CHECK (amount >= 0)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    buyer_user_id UUID NOT NULL,
    inquiry_id UUID NOT NULL,
    confirmation_id UUID NOT NULL,
    payment_attempt_id UUID NOT NULL,
    order_number VARCHAR(40) NOT NULL,
    menu_name_snapshot VARCHAR(150) NOT NULL,
    option_summary_snapshot TEXT NOT NULL,
    paid_amount BIGINT NOT NULL,
    pickup_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(30) NOT NULL,
    cancel_requested_at TIMESTAMPTZ,
    cancel_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_orders_payment_attempt_id UNIQUE (payment_attempt_id),
    CONSTRAINT uk_orders_order_number UNIQUE (order_number),
    CONSTRAINT fk_orders_store_id FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT fk_orders_buyer_user_id FOREIGN KEY (buyer_user_id) REFERENCES users (id),
    CONSTRAINT fk_orders_inquiry_id FOREIGN KEY (inquiry_id) REFERENCES inquiries (id),
    CONSTRAINT fk_orders_confirmation_id FOREIGN KEY (confirmation_id) REFERENCES order_confirmations (id),
    CONSTRAINT fk_orders_payment_attempt_id FOREIGN KEY (payment_attempt_id) REFERENCES payment_attempts (id),
    CONSTRAINT ck_orders_paid_amount CHECK (paid_amount >= 0)
);

CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    payment_attempt_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    amount BIGINT NOT NULL,
    reason TEXT,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_refunds_order_id FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_refunds_payment_attempt_id FOREIGN KEY (payment_attempt_id) REFERENCES payment_attempts (id),
    CONSTRAINT fk_refunds_requested_by FOREIGN KEY (requested_by) REFERENCES users (id),
    CONSTRAINT ck_refunds_amount CHECK (amount >= 0)
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50),
    reference_id UUID,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_notifications_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX ix_assets_uploaded_by ON assets (uploaded_by);
CREATE INDEX ix_asset_variants_asset_id ON asset_variants (asset_id);
CREATE INDEX ix_products_store_id_status ON products (store_id, status);
CREATE INDEX ix_product_assets_product_id ON product_assets (product_id);
CREATE INDEX ix_product_option_groups_product_id ON product_option_groups (product_id);
CREATE INDEX ix_product_options_option_group_id ON product_options (option_group_id);
CREATE INDEX ix_order_form_templates_store_id_active ON order_form_templates (store_id, active);
CREATE INDEX ix_order_form_fields_template_id ON order_form_fields (template_id);
CREATE INDEX ix_inquiries_buyer_user_id ON inquiries (buyer_user_id);
CREATE INDEX ix_inquiries_context_product_id ON inquiries (context_product_id);
CREATE INDEX ix_order_form_submissions_inquiry_id ON order_form_submissions (inquiry_id);
CREATE INDEX ix_chat_messages_inquiry_id_created_at ON chat_messages (inquiry_id, created_at);
CREATE INDEX ix_chat_events_inquiry_id_created_at_id ON chat_events (inquiry_id, created_at, id);
CREATE INDEX ix_chat_message_assets_message_id ON chat_message_assets (message_id);
CREATE INDEX ix_order_confirmations_inquiry_id_created_at ON order_confirmations (inquiry_id, created_at);
CREATE INDEX ix_payment_requests_inquiry_id_status ON payment_requests (inquiry_id, status);
CREATE INDEX ix_payment_attempts_payment_request_id ON payment_attempts (payment_request_id);
CREATE INDEX ix_orders_store_id_pickup_at ON orders (store_id, pickup_at);
CREATE INDEX ix_orders_store_id_status ON orders (store_id, status);
CREATE INDEX ix_orders_buyer_user_id_created_at ON orders (buyer_user_id, created_at);
CREATE INDEX ix_refunds_order_id ON refunds (order_id);
CREATE INDEX ix_notifications_user_id_created_at ON notifications (user_id, created_at);
