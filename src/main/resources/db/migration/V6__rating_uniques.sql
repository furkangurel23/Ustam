-- Enforce "one rating per provider per user/anon" at the database level
-- A user can rate a provider at most once
CREATE UNIQUE INDEX IF NOT EXISTS uq_rating_user ON ratings (provider_id, user_id) WHERE user_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_rating_anon ON ratings (provider_id, anonymous_id) WHERE anonymous_id IS NOT NULL;