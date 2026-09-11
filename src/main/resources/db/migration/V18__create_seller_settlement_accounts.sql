CREATE TABLE seller_settlement_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    bank_code VARCHAR(3) NOT NULL,
    encrypted_account_number TEXT NOT NULL,
    encrypted_account_holder_name TEXT NOT NULL,
    account_holder_type VARCHAR(20) NOT NULL,
    provider_transaction_id VARCHAR(40) NOT NULL,
    verified_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_seller_settlement_accounts_store_id
        FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE,
    CONSTRAINT uk_seller_settlement_accounts_store_id UNIQUE (store_id),
    CONSTRAINT ck_seller_settlement_accounts_bank_code
        CHECK (bank_code ~ '^[0-9]{3}$'),
    CONSTRAINT ck_seller_settlement_accounts_holder_type
        CHECK (account_holder_type IN ('PERSONAL', 'BUSINESS'))
);
