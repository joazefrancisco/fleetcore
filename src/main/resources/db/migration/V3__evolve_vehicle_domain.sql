CREATE TABLE brand (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE model (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    brand_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_model_brand
        FOREIGN KEY (brand_id)
        REFERENCES brand (id),

     CONSTRAINT uk_model_brand_name
        UNIQUE (brand_id, name)
);

CREATE TABLE vehicle_configuration (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_vehicle_configuration_type_name
        UNIQUE (vehicle_type, name),

    CONSTRAINT uk_vehicle_configuration_id_type
        UNIQUE (id, vehicle_type)
);

INSERT INTO vehicle_configuration (name, vehicle_type) VALUES
    ('4X2', 'TRUCK'),
    ('6X2', 'TRUCK'),
    ('6X4', 'TRUCK'),
    ('8X2', 'TRUCK'),
    ('8X4', 'TRUCK'),

    ('4X2', 'TRACTOR'),
    ('6X2', 'TRACTOR'),
    ('6X4', 'TRACTOR'),
    ('8X2', 'TRACTOR'),
    ('8X4', 'TRACTOR'),

    ('1 AXLE', 'SEMI_TRAILER'),
    ('2 AXLES TANDEM', 'SEMI_TRAILER'),
    ('3 AXLES TANDEM', 'SEMI_TRAILER'),
    ('3 AXLES SPACED (VANDERLEIA)', 'SEMI_TRAILER'),
    ('4 AXLES', 'SEMI_TRAILER'),

    ('2 AXLES', 'TRAILER'),
    ('3 AXLES', 'TRAILER'),
    ('4 AXLES', 'TRAILER'),

    ('1 AXLE', 'DOLLY'),
    ('2 AXLES TANDEM', 'DOLLY'),

    ('4X2', 'VAN'),

    ('4X2 FWD', 'CAR'),
    ('4X2 RWD', 'CAR'),
    ('4X4', 'CAR'),
    ('AWD', 'CAR'),

    ('4X2', 'BUS'),
    ('6X2', 'BUS'),
    ('8X2', 'BUS'),
    ('ARTICULATED 3 AXLES', 'BUS'),
    ('ARTICULATED 4 AXLES', 'BUS'),

    ('2X1', 'MOTORCYCLE'),
    ('2X2', 'MOTORCYCLE');

ALTER TABLE vehicle
    ADD COLUMN brand_id BIGINT NOT NULL,
    ADD COLUMN model_id BIGINT NOT NULL,
    ADD COLUMN vehicle_type VARCHAR(30) NOT NULL,
    ADD COLUMN fuel_type VARCHAR(30) NOT NULL,
    ADD COLUMN configuration_id BIGINT NOT NULL,
    ADD COLUMN manufacture_year INTEGER NOT NULL,
    ADD COLUMN model_year INTEGER NOT NULL,
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PRE_REGISTERED';

ALTER TABLE vehicle
    ALTER COLUMN pre_registration_code SET NOT NULL;

ALTER TABLE vehicle
    ADD CONSTRAINT fk_vehicle_brand
        FOREIGN KEY (brand_id)
        REFERENCES brand (id),

    ADD CONSTRAINT fk_vehicle_model_brand
        FOREIGN KEY (model_id, brand_id)
        REFERENCES model (id, brand_id),

    ADD CONSTRAINT fk_vehicle_configuration_type
        FOREIGN KEY (configuration_id, vehicle_type)
        REFERENCES vehicle_configuration (id, vehicle_type),

    ADD CONSTRAINT ck_vehicle_type
        CHECK (vehicle_type IN (
            'TRUCK', 'TRACTOR', 'SEMI_TRAILER', 'TRAILER',
            'DOLLY', 'VAN', 'BUS', 'CAR', 'MOTORCYCLE'
        )),

    ADD CONSTRAINT ck_vehicle_fuel_type
        CHECK (fuel_type IN (
            'DIESEL', 'GASOLINE', 'ETHANOL', 'FLEX',
            'HYBRID', 'ELECTRIC', 'CNG', 'LPG'
        )),

    ADD CONSTRAINT ck_vehicle_status
        CHECK (status IN (
            'PRE_REGISTERED', 'REGISTERED', 'ACTIVE',
            'IN_MAINTENANCE', 'INACTIVE', 'DISPOSED'
        ));

ALTER TABLE vehicle
    DROP COLUMN brand,
    DROP COLUMN model,
    DROP COLUMN active;

CREATE TABLE vehicle_acquisition (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    acquisition_date DATE NOT NULL,
    acquisition_value NUMERIC(15, 2) NOT NULL,
    invoice_number VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vehicle_acquisition_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicle (id),

    CONSTRAINT ck_vehicle_acquisition_type
        CHECK (type IN ('NEW', 'USED'))
);