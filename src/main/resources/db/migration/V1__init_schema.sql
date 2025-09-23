-- V1__init_schema.sql
-- Consolidated, clean, "first install" schema for Sanayi.
-- Includes: extensions, tables, constraints, indexes and rating aggregates triggers.
-- Aligns with current entities (User.enabled, User.role; Rating without deleted_at soft-delete).

-- 0) Extensions
CREATE EXTENSION IF NOT EXISTS postgis;

-- 1) USERS
CREATE TABLE IF NOT EXISTS users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255),
    password_hash VARCHAR(255),
    display_name  VARCHAR(100),
    role          VARCHAR(16) NOT NULL DEFAULT 'USER',
    enabled       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Case-insensitive unique email (ignore NULLs)
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_lower
    ON users ((lower(email))) WHERE email IS NOT NULL;

-- 2) PROVIDERS
CREATE TABLE IF NOT EXISTS providers
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    address      TEXT,
    city         VARCHAR(100),
    district     VARCHAR(100),
    phone        VARCHAR(50),
    location     GEOMETRY(Point, 4326),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    rating_count INT          NOT NULL DEFAULT 0,
    rating_sum   INT          NOT NULL DEFAULT 0,
    avg_score    NUMERIC(4, 2) GENERATED ALWAYS AS (
                     CASE
                         WHEN rating_count > 0
                             THEN round(rating_sum::numeric / rating_count, 2)
                         ELSE NULL END
                     ) STORED
);

-- Provider indexes (filters/sorts/geo)
CREATE INDEX IF NOT EXISTS idx_providers_city_lower ON providers ((lower(city)));
CREATE INDEX IF NOT EXISTS idx_providers_district_lower ON providers ((lower(district)));
CREATE INDEX IF NOT EXISTS idx_providers_city_district ON providers (lower(city), lower(district));
CREATE INDEX IF NOT EXISTS idx_providers_location_gist ON providers USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_providers_avg_score ON providers (avg_score);
CREATE INDEX IF NOT EXISTS idx_providers_rating_count ON providers (rating_count);

-- Prefix arama için pattern_ops + expression index
CREATE INDEX IF NOT EXISTS idx_providers_name_lower_pattern ON providers ((lower(name)) varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_providers_address_lower_pattern ON providers ((lower(address)) varchar_pattern_ops);

-- 3) BRANDS & CATEGORIES
CREATE TABLE IF NOT EXISTS brands
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS categories
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 4) M:N Provider relations
CREATE TABLE IF NOT EXISTS provider_brands
(
    provider_id INT NOT NULL REFERENCES providers (id) ON DELETE CASCADE,
    brand_id    INT NOT NULL REFERENCES brands (id) ON DELETE CASCADE,
    PRIMARY KEY (provider_id, brand_id)
);
CREATE INDEX IF NOT EXISTS idx_provider_brands ON provider_brands (brand_id, provider_id);

CREATE TABLE IF NOT EXISTS provider_categories
(
    provider_id INT NOT NULL REFERENCES providers (id) ON DELETE CASCADE,
    category_id INT NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    PRIMARY KEY (provider_id, category_id)
);
CREATE INDEX IF NOT EXISTS idx_provider_categories ON provider_categories (category_id, provider_id);

-- 5) RATINGS
CREATE TABLE IF NOT EXISTS ratings
(
    id           SERIAL PRIMARY KEY,
    provider_id  INT         NOT NULL REFERENCES providers (id) ON DELETE CASCADE,
    score        SMALLINT    NOT NULL CHECK (score BETWEEN -5 AND 5),
    comment_text VARCHAR(500),
    user_id      BIGINT      REFERENCES users (id) ON DELETE SET NULL,
    anonymous_id VARCHAR(100),
    ip_address   INET,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMPTZ NULL
);

-- XOR identity: user_id XOR anonymous_id
ALTER TABLE ratings
    DROP CONSTRAINT IF EXISTS ratings_identity_ck;
ALTER TABLE ratings
    ADD CONSTRAINT ratings_identity_ck CHECK (
        (user_id IS NOT NULL AND anonymous_id IS NULL) OR
        (user_id IS NULL AND anonymous_id IS NOT NULL)
        );

-- One active rating per provider per identity (soft-delete aware, uses deleted_at IS NULL)
-- Keep names to match GlobalExceptionHandler mapping
CREATE UNIQUE INDEX uniq_rating_user_active
    ON ratings (provider_id, user_id)
    WHERE user_id IS NOT NULL AND deleted_at IS NULL;

CREATE UNIQUE INDEX uniq_rating_anon_active
    ON ratings (provider_id, anonymous_id)
    WHERE anonymous_id IS NOT NULL AND deleted_at IS NULL;

-- Helper indexes
CREATE INDEX IF NOT EXISTS idx_ratings_provider ON ratings (provider_id);

-- 6) Aggregate triggers for providers (rating_count, rating_sum)
-- Note: No soft-delete column; we react to INSERT/UPDATE/DELETE only.

-- INSERT
CREATE OR REPLACE FUNCTION ratings_after_insert() RETURNS trigger AS
$$
BEGIN
    IF NEW.deleted_at IS NULL THEN
        UPDATE providers
        SET rating_count = rating_count + 1,
            rating_sum   = rating_sum + NEW.score
        WHERE id = NEW.provider_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- UPDATE: provider değişimi, score değişimi ve soft-delete geçişleri
CREATE OR REPLACE FUNCTION ratings_after_update() RETURNS trigger AS
$$
DECLARE
    old_active BOOLEAN := (OLD.deleted_at IS NULL);
    new_active BOOLEAN := (NEW.deleted_at IS NULL);
BEGIN
    -- Provider değiştiyse, eskisinden düş, yenisine ekle (aktif olanlar için)
    IF NEW.provider_id <> OLD.provider_id THEN
        IF old_active THEN
            UPDATE providers
            SET rating_count = rating_count - 1,
                rating_sum   = rating_sum - OLD.score
            WHERE id = OLD.provider_id;
        END IF;
        IF new_active THEN
            UPDATE providers
            SET rating_count = rating_count + 1,
                rating_sum   = rating_sum + NEW.score
            WHERE id = NEW.provider_id;
        END IF;
        RETURN NULL;
    END IF;

    -- Aktiflik durumu değiştiyse (soft-delete ya da geri alma)
    IF old_active AND NOT new_active THEN
        UPDATE providers
        SET rating_count = rating_count - 1,
            rating_sum   = rating_sum - OLD.score
        WHERE id = NEW.provider_id;
        RETURN NULL;
    ELSIF NOT old_active AND new_active THEN
        UPDATE providers
        SET rating_count = rating_count + 1,
            rating_sum   = rating_sum + NEW.score
        WHERE id = NEW.provider_id;
        RETURN NULL;
    END IF;

    -- Her ikisi de aktif ve sadece skor değiştiyse delta uygula
    IF new_active AND OLD.score <> NEW.score THEN
        UPDATE providers
        SET rating_sum = rating_sum + (NEW.score - OLD.score)
        WHERE id = NEW.provider_id;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- DELETE: sadece aktif satır silinmişse düş
CREATE OR REPLACE FUNCTION ratings_after_delete() RETURNS trigger AS
$$
BEGIN
    IF OLD.deleted_at IS NULL THEN
        UPDATE providers
        SET rating_count = rating_count - 1,
            rating_sum   = rating_sum - OLD.score
        WHERE id = OLD.provider_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_ratings_ai ON ratings;
CREATE TRIGGER trg_ratings_ai
    AFTER INSERT
    ON ratings
    FOR EACH ROW
EXECUTE FUNCTION ratings_after_insert();

DROP TRIGGER IF EXISTS trg_ratings_au ON ratings;
CREATE TRIGGER trg_ratings_au
    AFTER UPDATE
    ON ratings
    FOR EACH ROW
EXECUTE FUNCTION ratings_after_update();

DROP TRIGGER IF EXISTS trg_ratings_ad ON ratings;
CREATE TRIGGER trg_ratings_ad
    AFTER DELETE
    ON ratings
    FOR EACH ROW
EXECUTE FUNCTION ratings_after_delete();
