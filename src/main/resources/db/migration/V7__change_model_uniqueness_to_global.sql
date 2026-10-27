-- Remove the old constraint that enforces uniqueness per (brand_id, name)
ALTER TABLE model DROP CONSTRAINT IF EXISTS uk_model_brand_name;

-- Create a new unique index for global case-insensitive uniqueness with trim normalization
-- This enforces that model names are unique globally, regardless of brand
-- Case differences are ignored (LOWER)
-- Leading/trailing spaces are ignored (BTRIM)
-- Internal consecutive spaces are preserved
CREATE UNIQUE INDEX uk_model_name_lower ON model (LOWER(BTRIM(name)));
