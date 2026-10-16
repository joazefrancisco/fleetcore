ALTER TABLE brand DROP CONSTRAINT brand_name_key;

CREATE UNIQUE INDEX uk_brand_name_lower ON brand (LOWER(name));
