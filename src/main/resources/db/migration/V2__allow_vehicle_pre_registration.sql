ALTER TABLE vehicle
    ALTER COLUMN plate DROP NOT NULL;

ALTER TABLE vehicle
    ADD COLUMN pre_registration_code VARCHAR(10);

ALTER TABLE vehicle
    ADD CONSTRAINT uk_vehicle_pre_registration_code
    UNIQUE (pre_registration_code);