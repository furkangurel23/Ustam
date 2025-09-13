-- V4__seed_data.sql
-- Seed data for categories, brands, providers, relations, and sample ratings
-- Assumptions: 
--   - providers.location is geometry(Point,4326)
--   - join tables are provider_categories(provider_id, category_id) and provider_brands(provider_id, brand_id)

-- 1) Categories
INSERT INTO categories (name)
VALUES ('Mekanik'),
       ('Elektrik'),
       ('Kaporta'),
       ('Boyacı'),
       ('Lastik')
ON CONFLICT (name) DO NOTHING;

-- 2) Brands
INSERT INTO brands (name)
VALUES ('Honda'),
       ('Toyota'),
       ('Hyundai'),
       ('Renault'),
       ('Fiat'),
       ('BMW'),
       ('Mercedes'),
       ('Volkswagen')
ON CONFLICT (name) DO NOTHING;

-- Helper: get ids
WITH cat AS (SELECT id, name
             FROM categories),
     br AS (SELECT id, name
            FROM brands)
-- 3) Providers (Ankara-focused, sample coords)
INSERT
INTO providers (name, address, city, district, phone, location)
VALUES ('Ostim Honda Usta', '1003. Cad. No:10', 'Ankara', 'Yenimahalle', '+90 312 000 0001',
        ST_SetSRID(ST_MakePoint(32.7540, 39.9776), 4326)),
       ('İvedik Elektrik', '1473. Sok. No:3', 'Ankara', 'Yenimahalle', '+90 312 000 0002',
        ST_SetSRID(ST_MakePoint(32.7432, 39.9819), 4326)),
       ('Siteler Kaporta', 'Sanayi Cd. No:25', 'Ankara', 'Altındağ', '+90 312 000 0003',
        ST_SetSRID(ST_MakePoint(32.8859, 39.9477), 4326)),
       ('Keçiören Lastik', 'Fatih Cd. No:48', 'Ankara', 'Keçiören', '+90 312 000 0004',
        ST_SetSRID(ST_MakePoint(32.8670, 39.9921), 4326)),
       ('Çayyolu Boya', 'Konya Yolu Blv. 8', 'Ankara', 'Çankaya', '+90 312 000 0005',
        ST_SetSRID(ST_MakePoint(32.7306, 39.8865), 4326))
ON CONFLICT DO NOTHING;

-- Map names to ids for relations
WITH p AS (SELECT id, name FROM providers),
     c AS (SELECT id, name FROM categories),
     b AS (SELECT id, name FROM brands)
-- 4) Provider-Categories
INSERT
INTO provider_categories (provider_id, category_id)
SELECT p.id, c.id
FROM p
         JOIN c ON (
    (p.name = 'Ostim Honda Usta' AND c.name IN ('Mekanik')) OR
    (p.name = 'İvedik Elektrik' AND c.name IN ('Elektrik')) OR
    (p.name = 'Siteler Kaporta' AND c.name IN ('Kaporta')) OR
    (p.name = 'Keçiören Lastik' AND c.name IN ('Lastik')) OR
    (p.name = 'Çayyolu Boya' AND c.name IN ('Boyacı'))
    )
ON CONFLICT DO NOTHING;

-- 5) Provider-Brands
INSERT INTO provider_brands (provider_id, brand_id)
SELECT p.id, b.id
FROM (SELECT id, name FROM providers) p
         JOIN (SELECT id, name FROM brands) b ON (
    (p.name = 'Ostim Honda Usta' AND b.name IN ('Honda', 'Toyota', 'Hyundai')) OR
    (p.name = 'İvedik Elektrik' AND b.name IN ('Renault', 'Fiat')) OR
    (p.name = 'Siteler Kaporta' AND b.name IN ('Honda', 'Volkswagen')) OR
    (p.name = 'Keçiören Lastik' AND b.name IN ('BMW', 'Mercedes')) OR
    (p.name = 'Çayyolu Boya' AND b.name IN ('Toyota', 'Fiat'))
    )
ON CONFLICT DO NOTHING;

-- 6) Users (optional display names for non-anonymous ratings)
INSERT INTO users (email, password_hash, display_name)
VALUES ('ali@example.com', null, 'Ali'),
       ('ayşe@example.com', null, 'Ayşe'),
       ('mehmet@example.com', null, 'Mehmet')
ON CONFLICT DO NOTHING;

-- 7) Ratings (mix of user and anonymous)
-- NOTE: score range is [-5, 5] per schema/validation
WITH p AS (SELECT id, name FROM providers),
     u AS (SELECT id, display_name FROM users)
INSERT
INTO ratings (provider_id, score, comment_text, user_id, anonymous_id, ip_address)
VALUES ((SELECT id FROM p WHERE name = 'Ostim Honda Usta'), 5, 'Çok ilgili ve hızlı',
        (SELECT id FROM u WHERE display_name = 'Ali'), null, '1.2.3.4'),
       ((SELECT id FROM p WHERE name = 'Ostim Honda Usta'), 3, 'İdare eder', null, 'anon-123', '2.3.4.5'),
       ((SELECT id FROM p WHERE name = 'İvedik Elektrik'), -1, 'Teslimat gecikti',
        (SELECT id FROM u WHERE display_name = 'Ayşe'), null, '3.4.5.6'),
       ((SELECT id FROM p WHERE name = 'Siteler Kaporta'), 4, 'İşçilik temiz', null, 'anon-456', '4.5.6.7'),
       ((SELECT id FROM p WHERE name = 'Keçiören Lastik'), 2, 'Fiyat/performans iyi',
        (SELECT id FROM u WHERE display_name = 'Mehmet'), null, '5.6.7.8'),
       ((SELECT id FROM p WHERE name = 'Çayyolu Boya'), -2, 'Beklediğim gibi değil', null, 'anon-789', '6.7.8.9');

UPDATE providers
SET location = ST_SetSRID(location, 4326)
WHERE location IS NOT NULL
  AND ST_SRID(location) = 0;

-- Done.