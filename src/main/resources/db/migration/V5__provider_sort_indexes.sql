-- Indexes to speed up common sorts/filters on providers

-- Sorting by avg_score and rating_count
CREATE INDEX IF NOT EXISTS idx_providers_avg_score ON providers (avg_score);
CREATE INDEX IF NOT EXISTS idx_providers_rating_count ON providers (rating_count);

-- Optional combined index for frequent city+district lookups
-- CREATE INDEX IF NOT EXISTS idx_providers_city_district_lower ON providers((lower(city)), (lower(district)));
