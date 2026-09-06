ALTER TABLE store_weekly_pickup_settings
    ADD COLUMN break_start_time TIME NULL,
    ADD COLUMN break_end_time TIME NULL;

ALTER TABLE store_weekly_pickup_settings
    ALTER COLUMN daily_order_capacity DROP NOT NULL;

ALTER TABLE store_weekly_pickup_settings
    DROP CONSTRAINT ck_store_weekly_pickup_settings_capacity,
    ADD CONSTRAINT ck_store_weekly_pickup_settings_capacity
        CHECK (daily_order_capacity IS NULL OR daily_order_capacity > 0);
