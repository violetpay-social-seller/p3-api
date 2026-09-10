ALTER TABLE seller_onboardings
    ADD COLUMN detail_address VARCHAR(100);

ALTER TABLE stores
    ADD COLUMN detail_address VARCHAR(100);
