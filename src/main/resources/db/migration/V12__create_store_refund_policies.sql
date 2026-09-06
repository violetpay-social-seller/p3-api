CREATE TABLE store_refund_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    days_before_pickup INTEGER NOT NULL,
    refund_rate INTEGER NOT NULL,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_store_refund_policies_store_id
        FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE,
    CONSTRAINT uk_store_refund_policies_store_sort_order
        UNIQUE (store_id, sort_order),
    CONSTRAINT uk_store_refund_policies_store_days_before
        UNIQUE (store_id, days_before_pickup),
    CONSTRAINT ck_store_refund_policies_days_before
        CHECK (days_before_pickup >= 0),
    CONSTRAINT ck_store_refund_policies_refund_rate
        CHECK (refund_rate BETWEEN 0 AND 100 AND MOD(refund_rate, 10) = 0),
    CONSTRAINT ck_store_refund_policies_sort_order
        CHECK (sort_order >= 0)
);
