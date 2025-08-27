CREATE INDEX IF NOT EXISTS idx_providers_city_district ON providers (lower(city), lower(district));
CREATE INDEX IF NOT EXISTS idx_provider_categories ON provider_categories (category_id, provider_id);
CREATE INDEX IF NOT EXISTS idx_provider_brands ON provider_brands (brand_id, provider_id);

-- 1) Kolonlar
ALTER TABLE providers
    ADD COLUMN IF NOT EXISTS rating_count INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS rating_sum   INT NOT NULL DEFAULT 0;

-- PostgreSQL 12+ için generated stored column (yalnızca DB oluşturur; JPA dokunmasın)
ALTER TABLE providers
    ADD COLUMN IF NOT EXISTS avg_score NUMERIC(4, 2)
        GENERATED ALWAYS AS (
            CASE
                WHEN rating_count > 0
                    THEN round(rating_sum::numeric / rating_count, 2)
                ELSE NULL
                END
            ) STORED;

-- 2) Backfill (mevcut veriyi doldur)
WITH agg AS (SELECT provider_id, COUNT(*) AS cnt, COALESCE(SUM(score), 0) AS sum
             FROM ratings
             GROUP BY provider_id)
UPDATE providers p
SET rating_count = agg.cnt,
    rating_sum   = agg.sum
FROM agg
WHERE p.id = agg.provider_id;

-- 3) Trigger fonksiyonları
CREATE OR REPLACE FUNCTION ratings_after_insert() RETURNS trigger AS
$$
BEGIN
    UPDATE providers
    SET rating_count = rating_count + 1,
        rating_sum   = rating_sum + NEW.score
    WHERE id = NEW.provider_id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ratings_after_update() RETURNS trigger AS
$$
BEGIN
    -- provider değişmişse önce eskiden düş, sonra yeniye ekle
    IF NEW.provider_id <> OLD.provider_id THEN
        UPDATE providers
        SET rating_count = rating_count - 1,
            rating_sum   = rating_sum - OLD.score
        WHERE id = OLD.provider_id;

        UPDATE providers
        SET rating_count = rating_count + 1,
            rating_sum   = rating_sum + NEW.score
        WHERE id = NEW.provider_id;

    ELSE
        -- sadece skor değiştiyse delta kadar düzelt
        IF NEW.score <> OLD.score THEN
            UPDATE providers
            SET rating_sum = rating_sum + (NEW.score - OLD.score)
            WHERE id = NEW.provider_id;
        END IF;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ratings_after_delete() RETURNS trigger AS
$$
BEGIN
    UPDATE providers
    SET rating_count = rating_count - 1,
        rating_sum   = rating_sum - OLD.score
    WHERE id = OLD.provider_id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 4) Trigger’lar
DROP TRIGGER IF EXISTS trg_ratings_ai ON ratings;
CREATE TRIGGER trg_ratings_ai
    AFTER INSERT
    ON ratings
    FOR EACH ROW
EXECUTE FUNCTION ratings_after_insert();

DROP TRIGGER IF EXISTS trg_ratings_au ON ratings;
CREATE TRIGGER trg_ratings_au
    AFTER UPDATE OF provider_id, score
    ON ratings
    FOR EACH ROW
EXECUTE FUNCTION ratings_after_update();

DROP TRIGGER IF EXISTS trg_ratings_ad ON ratings;
CREATE TRIGGER trg_ratings_ad
    AFTER DELETE
    ON ratings
    FOR EACH ROW
EXECUTE FUNCTION ratings_after_delete();

-- 5) Yardımcı index (zaten vardır ama garanti edelim)
CREATE INDEX IF NOT EXISTS idx_ratings_provider ON ratings (provider_id);

-- Hızlı coğrafi arama/sort için GIST index
CREATE INDEX IF NOT EXISTS idx_providers_location_gist ON providers USING GIST (location);
