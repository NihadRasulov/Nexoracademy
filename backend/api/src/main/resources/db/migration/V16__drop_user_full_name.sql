-- V16__drop_user_full_name.sql
-- identity.users.full_name -> first_name + last_name  (CONTRACT mərhələsi)
--
-- ⚠️ BUNU YALNIZ V15-İ DAŞIYAN RELİZ TAM YAYILANDAN VƏ KÖHNƏ PODLAR
--    SÖNDÜKDƏN SONRA İŞƏ SAL. Əks halda hələ full_name-i NOT NULL bilən köhnə
--    instansiya INSERT zamanı çökər.
--
-- Prod-da bu migration `spring.flyway.target: 15` ilə bloklanıb — aktivləşdirmək üçün
-- application-prod.yml-də target dəyərini 16 et (və ya sətri tamamilə sil), sonra deploy et.
-- Bu addım geri qaytarıla bilməz: full_name DROP olunur, köhnə dəyəri yalnız
-- backup-dan bərpa etmək olar. İcra etməzdən əvvəl backup al.

-- 1) Keçid dövrünün sinxronizasiya trigger-i artıq lazım deyil.
DROP TRIGGER IF EXISTS trg_users_sync_name ON identity.users;
DROP FUNCTION IF EXISTS identity.users_sync_name();

-- 2) Ehtiyat: V15-dən sonra hər hansı sətir boş qalıbsa (məs. trigger-dən yan keçən
--    birbaşa SQL yazısı), NOT NULL-a keçməzdən əvvəl doldurulur.
UPDATE identity.users SET first_name = 'Yoxdur' WHERE first_name IS NULL OR btrim(first_name) = '';
UPDATE identity.users SET last_name = 'Yoxdur' WHERE last_name IS NULL OR btrim(last_name) = '';

ALTER TABLE identity.users ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE identity.users ALTER COLUMN last_name SET NOT NULL;

-- 3) Köhnə sütun silinir.
ALTER TABLE identity.users DROP COLUMN full_name;
