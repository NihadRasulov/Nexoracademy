# Nexora Academy — Task 2: PostgreSQL + Flyway Quraşdırılması

## Qovluq strukturu

```
src/main/resources/db/migration/
├── V1__init_extensions_and_schemas.sql   (extension-lar + 11 schema)
├── V2__create_enum_types.sql               (auth, RBAC, sessions)
├── V3__create_identity_schema.sql                (kurslar, kateqoriyalar, təlimatçılar)
├── V4__create_catalog_schema.sql              (qruplar, cədvəl, qeydiyyat)
├── V5__create_academics_schema.sql                (ödəniş, təqsit, təqaüd, kampaniya)
├── V6__create_billing_schema.sql               (rəylər, məzun nəticələri)
├── V7__create_crm_schema.sql                    (lead-lər, formlar, newsletter)
├── V8__create_outcomes_schema.sql                    (səhifələr, bloq, media)
├── V9__create_cms_schema.sql                     (chatbot, RAG, tövsiyələr, AI safety)
├── V10__create_ai_schema.sql                (bildirişlər)
├── V11__create_notify_schema.sql              (UI ayarları, feature flag, audit log)
└── V12__create_platform_schema.sql             (event tracking, materialized view)
```

Flyway faylları default olaraq `src/main/resources/db/migration/` qovluğunda axtarır
(bax `application.yml` → `spring.flyway.locations`). Yuxarıdakı 12 faylı elə bu yolla
layihəyə köçürün.

## Niyə bu sıra ilə?

Miqrasiyalar bir-birindən asılıdır (FK-lar):
`identity` → `catalog` → `academics` → `billing` / `outcomes` → `crm` → `cms` → `ai` → `notify` → `platform` → `analytics`.
`V6`-da həmçinin `V3`-də yaradılan `catalog.instructor_ratings.course_review_id`
sütununa geriyə dönük FK əlavə olunur (dairəvi asılılığı aradan qaldırmaq üçün).

## Quraşdırma addımları

1. **Lokal Postgres qaldır** (pgvector image ilə, `ai.kb_embeddings` üçün lazımdır):
   ```bash
   docker compose -f docker-compose.yml up -d
   ```

2. **Asılılıqları əlavə et** — `pom-dependencies-snippet.xml` faylındakı
   `flyway-core`, `flyway-database-postgresql` və `postgresql` dependency-lərini
   `pom.xml`-ə (və ya Gradle ekvivalentini `build.gradle`-ə) əlavə et.

3. **application.yml-i konfiqurasiya et** — verilən `application.yml` nümunəsini
   layihənin `src/main/resources/` qovluğuna qoy, `.env` və ya environment
   variable-larla (`DB_HOST`, `DB_USER`, `DB_PASSWORD` və s.) həqiqi dəyərləri ver.

4. **Migration-ları çalışdır:**
    - Spring Boot tətbiqi işə düşərkən Flyway avtomatik çalışacaq (`spring.flyway.enabled: true`), və ya
    - Manual: `mvn flyway:migrate` / `./gradlew flywayMigrate`
    - CLI ilə: `flyway -url=jdbc:postgresql://localhost:5432/nexora_academy -user=nexora_app -password=*** migrate`

5. **Yoxla:**
   ```sql
   SELECT * FROM flyway_schema_history ORDER BY installed_rank;
   ```
   12 sətir "Success" statuslu görünməlidir.

## Diqqət ediləcək məqamlar

- **`CREATE EXTENSION vector`** (V1) server üzərində superuser icazəsi tələb edir.
  Əgər managed Postgres (RDS/Cloud SQL) istifadə olunursa və `pgvector` dəstəklənmirsə,
  bu sətri şərh halına salıb, `V9`-da `embedding VECTOR(1536)` sütununu
  `external_vector_id TEXT` ilə əvəzləyən düzəliş migration-u (`V13__...`) əlavə et —
  **artıq tətbiq olunmuş migration faylını heç vaxt dəyişmə**, yalnız yeni versiya əlavə et.
- **Flyway checksum qaydası**: `V1`–`V12` production-a tətbiq olunduqdan sonra onların
  içindəkilərini redaktə etmə — hər dəyişiklik `V13`, `V14`... kimi yeni fayl olmalıdır.
- `spring.jpa.hibernate.ddl-auto: validate` seçilib ki, Hibernate heç vaxt sxemi özü
  yaratmasın/dəyişməsin — bütün DDL nəzarəti Flyway-dədir (Task 2-nin tələbi).
- `platform.scope_exclusions` və `ai.safety_incidents` kimi cədvəllər SRS-in
  Modul 32 (scope-dan kənar) tələbini qoruma məqsədilə saxlanılıb — silinməsin.