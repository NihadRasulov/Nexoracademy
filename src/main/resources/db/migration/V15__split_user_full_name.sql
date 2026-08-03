-- V15__split_user_full_name.sql
-- identity.users.full_name -> first_name + last_name  (EXPAND mərhələsi)
--
-- Qeydiyyat formasında ad və soyad ayrı-ayrı alındığı üçün tək sütun bölünür.
-- Bu migration "expand/contract" nümunəsinin BİRİNCİ yarısıdır və qəsdən
-- GERİYƏ UYĞUNDUR: icra olunandan sonra həm köhnə app instansiyası (yalnız
-- full_name bilir), həm də yeni instansiya (yalnız first_name/last_name bilir)
-- eyni anda problemsiz işləyə bilir. Buna görə k8s RollingUpdate (replicas: 2,
-- maxUnavailable: 0) zamanı downtime YOXDUR.
--
-- İki tərəfi sinxron saxlayan trigger aşağıda yaradılır:
--   köhnə pod full_name yazır  -> trigger first_name/last_name-i doldurur
--   yeni pod first/last yazır  -> trigger full_name-i doldurur
--
-- full_name sütununun silinməsi AYRI migration-dadır: V16__drop_user_full_name.sql.
-- V15 yalnız bütün köhnə podlar söndükdən sonra işə salınmalıdır — bax
-- application-prod.yml -> spring.flyway.target.

-- 1) Yeni sütunlar. NULL-a icazə verilir: köhnə pod INSERT edəndə onları doldurmur.
--    Uzunluq 40 — PersonName validasiyası ilə eyni hədd (bax @PersonName.MAX_LENGTH).
ALTER TABLE identity.users ADD COLUMN first_name VARCHAR(40);
ALTER TABLE identity.users ADD COLUMN last_name  VARCHAR(40);

-- 2) full_name artıq məcburi deyil: yeni pod onu heç yazmır (trigger dolduracaq).
ALTER TABLE identity.users ALTER COLUMN full_name DROP NOT NULL;

-- 3) Mövcud sətirlərin backfill-i: ilk söz = ad, qalan hissə = soyad.
--    Tək sözdən ibarət köhnə adlarda soyad yoxdur — 'Yoxdur' yer tutucusu yazılır.
--    Yer tutucu qəsdən yalnız hərflərdən və 2+ simvoldan ibarətdir: tətbiqin öz
--    @PersonName validasiyası (min 2 simvol, yalnız hərflər) belə sətirləri də keçərli
--    saydığı üçün admin panelindən problemsiz redaktə oluna bilir. '-' və ya 'N/A'
--    kimi variantlar bu qaydanı pozardı.
UPDATE identity.users
SET first_name = coalesce(nullif(left(split_part(btrim(full_name), ' ', 1), 40), ''), 'Yoxdur'),
    last_name = CASE
                    WHEN position(' ' IN btrim(full_name)) > 0
                        THEN coalesce(nullif(
                                left(btrim(substr(btrim(full_name), position(' ' IN btrim(full_name)) + 1)), 40),
                                ''), 'Yoxdur')
                    ELSE 'Yoxdur'
        END
WHERE full_name IS NOT NULL;

-- 4) Sinxronizasiya trigger-i — yalnız keçid dövrü üçün, V15-də silinir.
CREATE OR REPLACE FUNCTION identity.users_sync_name() RETURNS trigger AS $$
DECLARE
    trimmed text;
BEGIN
    -- full_name dəyişib, first/last isə dəyişməyibsə => yazan köhnə koddur: bölürük.
    IF (TG_OP = 'INSERT' AND NEW.first_name IS NULL AND NEW.last_name IS NULL AND NEW.full_name IS NOT NULL)
        OR (TG_OP = 'UPDATE'
            AND NEW.full_name IS DISTINCT FROM OLD.full_name
            AND NEW.first_name IS NOT DISTINCT FROM OLD.first_name
            AND NEW.last_name IS NOT DISTINCT FROM OLD.last_name)
    THEN
        trimmed := btrim(coalesce(NEW.full_name, ''));
        NEW.first_name := coalesce(nullif(left(split_part(trimmed, ' ', 1), 40), ''), 'Yoxdur');
        NEW.last_name := CASE
                             WHEN position(' ' IN trimmed) > 0
                                 THEN coalesce(nullif(
                                         left(btrim(substr(trimmed, position(' ' IN trimmed) + 1)), 40),
                                         ''), 'Yoxdur')
                             ELSE 'Yoxdur'
            END;
    ELSE
        -- Əks halda first/last həqiqətdir (yeni kod) => full_name-i onlardan qururuq.
        NEW.full_name := left(nullif(btrim(concat_ws(' ', NEW.first_name, NEW.last_name)), ''), 150);
        IF NEW.full_name IS NULL THEN
            NEW.full_name := 'Yoxdur';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_sync_name
    BEFORE INSERT OR UPDATE ON identity.users
    FOR EACH ROW
EXECUTE FUNCTION identity.users_sync_name();

-- 5) Backfill-dən sonra əl ilə düzəldilməli sətirləri tapmaq üçün:
--      SELECT id, email, first_name, last_name FROM identity.users WHERE last_name = 'Yoxdur';
