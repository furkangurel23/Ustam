-- 1) USERS (MVP: email/password_hash NULL olabilir)
CREATE TABLE IF NOT EXISTS users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255),
    password_hash VARCHAR(255),
    display_name  VARCHAR(100),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_lower ON users ((lower(email))) WHERE email IS NOT NULL;

-- 2) PROVIDERS (Usta / Servis)
CREATE TABLE IF NOT EXISTS providers
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    address    TEXT,
    city       VARCHAR(100),
    district   VARCHAR(100),
    phone      VARCHAR(50),
    location   GEOMETRY(Point, 4326), -- PostGIS açıksa
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 3) BRANDS (Araç Markaları)
CREATE TABLE IF NOT EXISTS brands
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 4) CATEGORIES (Hizmet Türleri)
CREATE TABLE IF NOT EXISTS categories
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 5) M:N Providers ↔ Brands
CREATE TABLE IF NOT EXISTS provider_brands
(
    provider_id INT NOT NULL REFERENCES providers (id) ON DELETE CASCADE,
    brand_id    INT NOT NULL REFERENCES brands (id) ON DELETE CASCADE,
    PRIMARY KEY (provider_id, brand_id)
);

-- 6) M:N Providers ↔ Categories
CREATE TABLE IF NOT EXISTS provider_categories
(
    provider_id INT NOT NULL REFERENCES providers (id) ON DELETE CASCADE,
    category_id INT NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    PRIMARY KEY (provider_id, category_id)
);

-- 7) RATINGS (yorum + puan tek tabloda)
CREATE TABLE IF NOT EXISTS ratings
(
    id           SERIAL PRIMARY KEY,
    provider_id  INT         NOT NULL REFERENCES providers (id) ON DELETE CASCADE,
    score        SMALLINT    NOT NULL CHECK (score BETWEEN -5 AND 5),
    comment_text VARCHAR(500),
    user_id      BIGINT      REFERENCES users (id) ON DELETE SET NULL,
    anonymous_id VARCHAR(100), -- cihaz/çerez hash (login yoksa dolu)
    ip_address   INET,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Kimlik bütünlüğü: user_id XOR anonymous_id (tam olarak biri dolu olmalı)
ALTER TABLE ratings
    DROP CONSTRAINT IF EXISTS ratings_identity_ck;
ALTER TABLE ratings
    ADD CONSTRAINT ratings_identity_ck CHECK (
        (user_id IS NOT NULL AND anonymous_id IS NULL)
            OR (user_id IS NULL AND anonymous_id IS NOT NULL)
        );

-- Üyeler için tekil oy (aynı provider’a 1 kez)
DROP INDEX IF EXISTS uniq_rating_member;
CREATE UNIQUE INDEX uniq_rating_member
    ON ratings (provider_id, user_id)
    WHERE user_id IS NOT NULL;

-- Anonimler için tekil oy (aynı provider’a 1 kez)
DROP INDEX IF EXISTS uniq_rating_anon;
CREATE UNIQUE INDEX uniq_rating_anon
    ON ratings (provider_id, anonymous_id)
    WHERE anonymous_id IS NOT NULL;

-- İsteğe bağlı yardımcı indexler (sorgu hızlandırma)
CREATE INDEX IF NOT EXISTS idx_ratings_provider ON ratings (provider_id);

CREATE INDEX IF NOT EXISTS idx_providers_city_district ON providers (lower(city), lower(district));
CREATE INDEX IF NOT EXISTS idx_providers_city_lower ON providers ((lower(city)));
CREATE INDEX IF NOT EXISTS idx_providers_district_lower ON providers ((lower(district)));