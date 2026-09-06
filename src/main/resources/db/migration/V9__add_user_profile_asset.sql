ALTER TABLE users
    ADD COLUMN profile_asset_id UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_profile_asset_id
        FOREIGN KEY (profile_asset_id) REFERENCES assets (id) ON DELETE SET NULL;
