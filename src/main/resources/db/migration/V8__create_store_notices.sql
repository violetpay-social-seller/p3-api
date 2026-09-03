CREATE TABLE store_notices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_store_notices_store_type UNIQUE (store_id, type),
    CONSTRAINT fk_store_notices_store_id
        FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE
);
