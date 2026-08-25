ALTER TABLE inquiries
    ADD COLUMN buyer_deleted_at TIMESTAMPTZ,
    ADD COLUMN seller_deleted_at TIMESTAMPTZ,
    ADD COLUMN buyer_purged_at TIMESTAMPTZ,
    ADD COLUMN seller_purged_at TIMESTAMPTZ;

CREATE INDEX ix_inquiries_buyer_trash ON inquiries (buyer_user_id, buyer_deleted_at, buyer_purged_at);
CREATE INDEX ix_inquiries_seller_trash ON inquiries (store_id, seller_deleted_at, seller_purged_at);
