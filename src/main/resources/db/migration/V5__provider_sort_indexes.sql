-- Indexes to speed up common sorts/filters on providers

-- Sorting by avg_score and rating_count
CREATE INDEX IF NOT EXISTS idx_providers_avg_score ON providers (avg_score);
CREATE INDEX IF NOT EXISTS idx_providers_rating_count ON providers (rating_count);

-- Case-insensitive city/district filtering used by queries (lower(city) = lower(:city), etc.)
CREATE INDEX IF NOT EXISTS idx_providers_city_lower ON providers ((lower(city)));
CREATE INDEX IF NOT EXISTS idx_providers_district_lower ON providers ((lower(district)));

-- Optional combined index for frequent city+district lookups
-- CREATE INDEX IF NOT EXISTS idx_providers_city_district_lower ON providers((lower(city)), (lower(district)));
