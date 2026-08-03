# NexoraAcademy — Production Handover (Docker deploy)

**Tarix:** 2026-07-29
**Kim üçün:** DevOps (və backend sahibi)
**Deploy üsulu:** **Docker Compose** — Kubernetes istifadə olunmur
**Sistem:** Spring Boot 4.1 · Java 21 · PostgreSQL 16 (pgvector) · Flyway · JWT auth
**Ölçü:** 223 Java sinfi · 23 controller · 13 Flyway migration · 38 test

> Bu sənəd `PRODUCTION_READINESS.md`-i **əvəz etmir, davam etdirir**. Orada kod
> səviyyəsindəki audit var (nə düzəldilib, niyə). Burada isə **deploy-a qədər qalan
> iş** var.
>
> `k8s/` qovluğu **istifadə olunmur** — gələcəkdə Kubernetes-ə keçilsə deyə saxlanılıb.
> Aktual infrastruktur: `docker-compose.prod.yml`, `deploy/nginx.conf`, `Dockerfile`.
>
> Oxuma ardıcıllığı: `§0 Xülasə` → `§1 Mən nə etdim` → `§2 Backend sahibinin işləri`
> → `§3 DevOps addımları` → `§4 Testlər` → `§5 Deploy-dan sonra`.

---

## §0 Xülasə — hazırlıq vəziyyəti

| Sahə | Vəziyyət | Qeyd |
|------|----------|------|
| Build & testlər | 🟢 Hazır | 38/38 keçir, təmiz konteyner bazasında |
| Migration-lar (V1–V13) | 🟢 Hazır | Boş bazada sıfırdan tətbiq olunur — yoxlandı |
| Auth / JWT / RBAC | 🟢 Hazır | Rotasiya olunmuş secret, rol əsaslı qaydalar |
| Docker image | 🟢 Hazır və sınanıb | Multi-stage, non-root (uid 100), healthcheck `healthy` oldu |
| Compose stack (nginx+app+db+backup) | 🟢 Yeni yazıldı | `docker-compose.prod.yml` |
| CI (build + test + scan) | 🟢 Yeni əlavə olundu | `.github/workflows/ci.yml` |
| Metrikalar | 🟡 Hazır, izlənmir | `:9091/actuator/prometheus` işləyir; **heç kim baxmır, alert yoxdur** |
| Mərkəzi loglama | 🟡 Qismən | Docker log rotasiyası quruldu; audit logları hələ yalnız fayldadır |
| Backup | 🟡 Servis var | Gündəlik `pg_dump` quruldu; **serverdən kənara köçürülmür, bərpa sınanmayıb** |
| TLS / domen | 🔴 Sənin tərəfdə | Sertifikat, DNS, real domen yoxdur |
| Ödəniş gateway-i | 🔴 Seçilməyib | Webhook **imzasız** qəbul olunur — §2.1 |
| Yük testi | 🔴 Aparılmayıb | Performans göstəricisi yoxdur |
| Rate limiting | 🟢 Bu quruluşda kifayət | Tək `app` konteyneri + nginx limiti |

**Bir cümlə ilə:** kod, image və compose stack-i hazırdır və sınanıb; qalan iş
**server, domen, TLS, sirlər və monitorinqdir**.

---

## §1 Bu sessiyada mən nə etdim (hamısı yoxlanılıb)

### 1.1 Sınmış test bazası bərpa olundu 🔴→🟢

`./mvnw test` **build-i sındırırdı**: 38 testdən 5-i düşürdü. Üç ayrı səbəb:

| Problem | Kök səbəb | Düzəliş |
|---|---|---|
| `InvalidUseOfMatchers` / `UnfinishedStubbing` — və bu, **sonrakı bütün test siniflərini** zəncirvari sındırırdı | `EmailService.send()` 2026-07-25-də `@Async` oldu. `doNothing().when(spy).send(...)` arxa-fon thread-inə göndərilir, cari thread-də stubbing tamamlanmır → Mockito vəziyyəti bütün JVM üçün korlanır | Həmin stub silindi (lazımsız idi — `EmailService` xətanı onsuz da udur) |
| `zero interactions` (AuthFlow) | Eyni `@Async`: `verify()` dərhal işləyir, məktub hələ göndərilməyib — yarış | `verify(emailService, timeout(10_000))` |
| `zero interactions` (CourseCrud, EnrollmentPayment) | **Testlər köhnə login axınına görə yazılmışdı.** `AuthService.login()` → `isAdminPanelStaff()`: ADMIN/SYSTEM_ADMIN/SALES_CRM/CONTENT_MANAGER **OTP-ni keçir**, token birbaşa `/login` cavabında gəlir | `/login/verify-otp` addımı silindi |

### 1.2 Testlər artıq CI-da işləyə bilir (Testcontainers) 🔴→🟢

Əvvəl bütün `@SpringBootTest` testləri `.env`-dəki **real dev bazasına** qoşulurdu:
(a) CI-da işləmirdi, (b) lokalda canlı bazaya sətir yazırdı.

İndi: `src/test/.../integration/TestcontainersConfiguration.java` — hər build üçün
birdəfəlik `pgvector/pgvector:pg16` konteyneri. **Hər build-də 13 migration sıfırdan
icra olunur** (əvvəl heç vaxt yoxlanılmırdı).

> **Bu yolla tapılan real zəiflik:** tətbiq JDBC URL-indəki `?stringtype=unspecified`
> parametrindən asılıdır. O olmadan boş bazada `AdminSeeder` açılışda çökür:
> `column "role" is of type platform.user_role but expression is of type character varying`.
> Compose-da bu parametr `application-prod.yml`-dan gəlir — **DB URL-i əl ilə
> qurulsa bu parametr İTİRİLMƏMƏLİDİR.**

### 1.3 Actuator / metrikalar 🟡→🟢

- `micrometer-registry-prometheus` əlavə olundu → `/actuator/prometheus`
  (JVM, HTTP latency, Hikari pool, Tomcat thread-ləri).
- Prod-da actuator **ayrıca daxili porta (9091)** köçürüldü, ictimai səthdən çıxarıldı.
- `exposure.include: health,info,prometheus` (env/heapdump/configprops bağlıdır).
- `SecurityConfig`-də `/actuator/health` → `/actuator/health/**` düzəldildi —
  **bu bir bug idi:** prod profili liveness/readiness probe-larını aktivləşdirir, amma
  onların path-i qorunan siyahıya düşürdü.

**Prod profili ilə yerində yoxlandı:**
```
/actuator/health[/liveness|/readiness] → UP
/actuator/prometheus                    → 200 (hikaricp_*, jvm_*, http_server_requests_*)
/actuator/env|beans|heapdump|configprops→ 401
8081/actuator/**                        → 404
8081/v3/api-docs                        → 404 (Swagger prod-da bağlı)
```

> Yoxlama zamanı tapıldı: yalnız portu ayırmaq **kifayət etmir** — əsas security
> zənciri həmin porta da tətbiq olunur və Prometheus 401 alırdı. `/actuator/prometheus`
> üçün ayrıca `permitAll()` qaydası əlavə edildi (endpoint yalnız prod-da mövcuddur
> və yalnız daxili portdadır).

### 1.4 Dockerfile — sınıq healthcheck düzəldildi 🔴→🟢

Actuator 9091-ə köçdükdən sonra `HEALTHCHECK` hələ də `8081/actuator/health`-ə
vururdu — prod-da **404**. Nəticə: konteyner **həmişə "unhealthy"** görünürdü,
`depends_on: service_healthy` şərtləri heç vaxt keçmirdi, restart siyasətləri səhv
işləyirdi.

Digər düzəlişlər:
- `JAVA_OPTS="-XX:MaxRAMPercentage=75"` — konteyner yaddaş limitində OOM-kill riski
  azalır (əvvəl JVM özü təxmin edirdi).
- `EXPOSE 8081 9091`, `start-period` 40s → 90s (13 migration soyuq startda uzun çəkir).

**Image real olaraq build edilib işə salındı:** `healthy` statusuna keçdi, API 200,
prometheus 200, konteyner `uid=100(spring)` — root deyil ✅

### 1.5 `docker-compose.prod.yml` production üçün yenidən yazıldı

| Əvvəl | İndi | Niyə |
|---|---|---|
| `app` portu host-a açıq (`8081:8081`) | Açıq deyil, yalnız nginx daxildən çıxır | TLS-siz və limitsiz birbaşa giriş bağlandı |
| TLS yoxdur, reverse proxy yoxdur | **nginx servisi** + `deploy/nginx.conf` | Şifrələnməmiş trafik = JWT və şifrələr açıq gedir |
| Log rotasiyası yoxdur | Hər servisdə `max-size: 10m, max-file: 5` | Docker json-file logları **sonsuz böyüyür** və host diskini doldurur |
| Yaddaş limiti yoxdur | `app` və `postgres` üçün 1G | Bir servis bütün maşını yeyə bilirdi |
| Backup yoxdur | `backup` servisi — gündəlik `pg_dump`, 14 gün saxlama | Volume itsə bütün məlumat geri dönməz idi |
| `PGDATA` yoxdur | `/var/lib/postgresql/data/pgdata` | Volume kökündəki `lost+found` üzündən initdb çökürdü |
| Postgres healthcheck-də ad hardcode | `$$POSTGRES_USER` / `$$POSTGRES_DB` | `.env`-də ad dəyişəndə healthcheck həmişə uğursuz olurdu |

`deploy/nginx.conf` — TLS terminasiyası, HSTS + təhlükəsizlik başlıqları,
`X-Forwarded-*` (tətbiq bunlara güvənir), auth endpoint-ləri üçün **əlavə rate limit**,
`/actuator` **qəsdən proxy edilmir**, qalan hər şey 404.

### 1.6 Digər

- `.github/workflows/ci.yml` — build + 38 test + image build + Trivy CVE skanı.
- `.gitignore` təmizliyi.
- `k8s/` manifestləri də düzəldildi (NetworkPolicy egress SMTP/443 bağlı idi, probe-lar,
  securityContext, backup CronJob) — **hazırda istifadə olunmur**, gələcək üçün saxlanılıb.

---

## §2 Backend sahibinin işləri

Tam siyahı və dəqiq sətir nömrələri: **`PRODUCTION_TODO_MINE.md`**

Ən kritik üçü:

### 2.1 🔴 Ödəniş webhook secret-i — **ƏN BÖYÜK AÇIQ RİSK**
`.env:72` → `PAYMENT_GATEWAY_WEBHOOK_SECRET=` **boşdur**. Bu halda
`PaymentGatewaySignatureVerifier` imza yoxlanışını **bypass edir** (hər çağırışda WARN).
Yəni `/api/v1/payments/callback` ünvanını bilən istənilən kəs saxta "ödəniş uğurlu"
göndərib pulsuz qeydiyyat aça bilər.
*Gateway hazır olana qədər alternativ: bu endpoint-i `deploy/nginx.conf`-da bağlayın.*

### 2.2 🔴 Real domen + TLS
`CORS_ALLOWED_ORIGINS` və `FRONTEND_BASE_URL` hələ `localhost`-dur;
`deploy/nginx.conf`-da `api.example.com`. Domen olmadan brauzer bütün sorğuları
CORS-da bloklayır və email linkləri işləmir.

### 2.3 🟠 Gmail SMTP limiti
Gündə **~500 məktub**. OTP + email doğrulama + şifrə bərpası hamısı məktuba bağlıdır —
limit dolan gün **qeydiyyat və login tam dayanır**. SES/SendGrid/Postmark-a keçid
yalnız `.env` dəyişikliyidir, kod dəyişmir.

---

## §3 DevOps addımları (Docker)

### 3.0 Serverin hazırlanması

**Minimum:** 4 GB RAM · 2 vCPU · 40 GB SSD · Ubuntu 22.04+
*(Tətbiq + Postgres + nginx eyni maşındadır. 2 GB-da JVM OOM olur.)*

```bash
# Docker + Compose plugin
curl -fsSL https://get.docker.com | sh

# Firewall — yalnız SSH + HTTP + HTTPS
ufw allow 22/tcp && ufw allow 80/tcp && ufw allow 443/tcp && ufw enable

# Sistem limitləri (Postgres üçün)
sysctl -w vm.overcommit_memory=1
```

⚠️ **5432 və 8081 portlarını HEÇ VAXT açmayın.** Compose onları onsuz da host-a
çıxarmır — `ports:` sətri əlavə etməyin.

### 3.1 Kodun və konfiqurasiyanın yerləşdirilməsi

```bash
git clone <repo> /opt/nexora && cd /opt/nexora
scp .env user@server:/opt/nexora/.env      # .env git-də YOXDUR — əl ilə köçürülür
chmod 600 .env
mkdir -p deploy/certs backups
```

### 3.2 TLS sertifikatı

```bash
# nginx hələ işləmədiyi üçün standalone rejim
certbot certonly --standalone -d api.<domen>
cp /etc/letsencrypt/live/api.<domen>/{fullchain.pem,privkey.pem} deploy/certs/

# Avtoyenilənmə — BU OLMASA 90 gündən sonra sayt tam düşür
echo '0 3 * * 1 cd /opt/nexora && certbot renew --quiet && cp /etc/letsencrypt/live/api.<domen>/*.pem deploy/certs/ && docker compose -f docker-compose.prod.yml restart nginx' | crontab -
```

`deploy/nginx.conf`-da `api.example.com`-u real domenlə əvəz edin (**3 yerdə**).

### 3.3 İlk deploy

```bash
cd /opt/nexora
docker compose -f docker-compose.prod.yml up -d --build

# Gözləyin (ilk build 5-10 dəqiqə, migration-lar 1-2 dəqiqə)
docker compose -f docker-compose.prod.yml ps          # hamısı healthy olmalıdır
docker compose -f docker-compose.prod.yml logs -f app
```

Açılışda log-da axtarılacaq sətir: `Successfully applied 13 migrations`.

**Servislərin qalxma ardıcıllığı avtomatikdir:**
`postgres` (healthy) → `flyway` (migrate edib çıxır) → `app` (healthy) → `nginx`

### 3.4 Migration strategiyası

Migration-ları **`flyway` servisi** `app`-dan əvvəl tətbiq edir
(`depends_on: service_completed_successfully`). Tətbiqin özündə də Flyway aktivdir —
tək instans olduğu üçün münaqişə yoxdur.

> **Rollback barədə:** Flyway-də `undo` yoxdur. Image geri qaytarılsa **schema geri
> qayıtmır**. Migration-lar həmişə geriyə uyğun olmalıdır: əvvəlcə sütun əlavə et →
> kodu yay → yalnız sonra köhnə sütunu sil.

**Deploy-dan əvvəl həmişə backup alın** (§3.6) — `up -d` migration işlədir.

#### 3.4.1 V15/V16 — istifadəçi adının bölünməsi (iki mərhələli, indiki relizdə aktual)

`identity.users.full_name` → `first_name` + `last_name`. Yuxarıdakı qaydaya uyğun olaraq
**iki migration**-a bölünüb:

| Migration | Nə edir | Nə vaxt işləyir |
|---|---|---|
| `V15__split_user_full_name.sql` | Yeni sütunları əlavə edir, backfill edir, `full_name`-i **nullable** edir və iki tərəfi sinxron saxlayan trigger qurur | **İndi** — geriyə uyğundur, köhnə və yeni kod eyni anda işləyə bilir |
| `V16__drop_user_full_name.sql` | Trigger-i silir, `NOT NULL` qoyur, `full_name`-i **DROP** edir | **Növbəti relizdə** — yalnız köhnə konteyner/pod tam söndükdən sonra |

V16 prod-da `application-prod.yml → spring.flyway.target: "15"` ilə **bloklanıb**.

**V16-nı aktivləşdirmə ardıcıllığı:**
```bash
# 1) V15-li reliz tam yayılıb və köhnə konteyner söndürülüb?
docker compose -f docker-compose.prod.yml ps

# 2) Backup — V16 geri qaytarıla bilməz (DROP COLUMN)
docker compose -f docker-compose.prod.yml exec -T backup \
  sh -c 'pg_dump -Fc -f /backups/pre-v15-$(date -u +%Y%m%dT%H%M%SZ).dump'

# 3) Backfill-in nəticəsini yoxla — soyadı təyin edilə bilməyən sətirlər
docker compose -f docker-compose.prod.yml exec -T postgres \
  psql -U "$DB_USER" -d "$DB_NAME" \
  -c "SELECT id, email, first_name, last_name FROM identity.users WHERE last_name = 'Yoxdur';"

# 4) application-prod.yml-də `target: "15"` sətrini 16 et və ya tamamilə sil, sonra deploy
docker compose -f docker-compose.prod.yml up -d --build app
```

> ⚠️ **`target` açarını sonra silməyi unutmayın.** `target: "15"` qaldığı müddətdə V16 **və
> bütün gələcək migration-lar** (V17, V18 …) səssizcə tətbiq olunmayacaq — Flyway sadəcə
> onları atlayır, xəta vermir. Bu, ən asan gözdən qaçan risklərdəndir.

> **K8s qeydi:** `k8s/04-app-deployment.yaml` `RollingUpdate` + `maxUnavailable: 0` ilə
> işləyir, yəni yeni pod hazır olana qədər köhnə pod trafik alır. V15 məhz buna görə
> geriyə uyğun yazılıb — trigger sayəsində köhnə pod `full_name` yazsa da yeni sütunlar,
> yeni pod `first_name`/`last_name` yazsa da `full_name` avtomatik dolur. V16-nı isə
> rollout bitmədən işlətmək köhnə podu dərhal çökdürər.

### 3.5 Yenilənmə (yeni versiya yayımı)

```bash
cd /opt/nexora
docker compose -f docker-compose.prod.yml exec -T backup \
  sh -c 'pg_dump -Fc -f /backups/pre-deploy-$(date -u +%Y%m%dT%H%M%SZ).dump'   # 1) backup
git pull                                                                       # 2) kod
docker compose -f docker-compose.prod.yml up -d --build app                    # 3) yalnız app
docker compose -f docker-compose.prod.yml ps                                   # 4) healthy?
```

⚠️ **Kəsintili deploy:** tək `app` konteyneri olduğu üçün restart zamanı ~30-60 saniyə
xidmət dayanır. Sıfır kəsinti lazımdırsa: `--scale app=2` + nginx upstream (o zaman
rate limiter replikaya bölünür — `PRODUCTION_TODO_MINE.md` #13).

### 3.6 Backup və bərpa (🟡 yarımçıq)

`backup` servisi hər 24 saatdan bir `./backups/nexora-<tarix>.dump` yaradır, 14 gündən
köhnələri silir. **Amma:**

- Backup-lar **eyni serverdədir** — server ölsə backup da ölür.
  **Xarici anbara köçürmə əlavə olunmalıdır** (S3 / rsync / başqa maşın).
- **Bərpa heç vaxt sınanmayıb.** Canlıya çıxmazdan əvvəl bir dəfə tam sınaq:

```bash
# 1) mövcud dump-a bax
ls -lh backups/
# 2) AYRI, boş bazaya bərpa et (prod bazasına YOX)
docker compose -f docker-compose.prod.yml exec postgres \
  psql -U $DB_USER -c "CREATE DATABASE restore_test;"
docker compose -f docker-compose.prod.yml exec -T postgres \
  pg_restore -U $DB_USER -d restore_test --clean --if-exists < backups/nexora-XXXX.dump
# 3) yoxla: istifadəçi sayı, ən son ödəniş tarixi
# 4) təmizlə: DROP DATABASE restore_test;
```

RPO hazırda **24 saatdır**. Ödəniş məlumatı üçün bu çox ola bilər — daha tez-tez
dump və ya WAL arxivləşdirmə nəzərdən keçirilməlidir.

### 3.7 Monitorinq (🟡 metrikalar var, izləyən yoxdur)

Metrikalar hazırdır: `http://app:9091/actuator/prometheus` (compose şəbəkəsi daxilində).

**Minimum (5 dəqiqəlik iş):** UptimeRobot / BetterStack — `https://api.<domen>/api/v1/courses`
ünvanını izləsin, düşəndə mail/telegram göndərsin.

**Tam variant:** eyni compose-a `prometheus` + `grafana` servisləri əlavə edin
(`app:9091` scrape target). İzləniləsi metrikalar:

| Metrik | Həddi | Niyə |
|---|---|---|
| `up` | 0 → alert | Tətbiq düşüb |
| `http_server_requests_seconds_count{status=~"5.."}` | >1% | Reqressiya |
| p99 latency | >2s | Degradasiya |
| `hikaricp_connections_pending` | >0 | Pool 10-dur, tükənir |
| `jvm_memory_used_bytes` (heap) | >85% | OOM yaxınlaşır |
| disk boş sahə | <20% | Loglar/backup-lar doldurur |

**Əlavə:** `PaymentGatewaySignatureVerifier` WARN loglarının sayı — §2.1 həll olunana
qədər hər callback bu xəbərdarlığı yazır.

### 3.8 Loglar

- **Docker logları:** rotasiya quruldu (10 MB × 5 fayl / servis).
  `docker compose -f docker-compose.prod.yml logs -f app`
- **Audit logları (CRUD):** `logback-spring.xml` bunları **yalnız fayla** yazır
  (`nexora_app_logs` volume-u, 30 gün) — `docker logs`-da **görünmür**.
  Mərkəzi loglama istəyirsinizsə audit appender-ləri stdout-a yönəldilməlidir
  (kod dəyişikliyi — backend sahibi ilə razılaşdırın).
- Appender-lərdə `totalSizeCap` yoxdur — volume-un ölçüsünü izləyin.

**Hüquqi qeyd:** audit logları hansı istifadəçinin nəyi yaratdığını/sildiyini saxlayır.
30 günlük saxlama müddəti sizin data-retention siyasətinizə uyğun olmalıdır.

### 3.9 Resurs ölçüləri

`app` və `postgres` üçün 1 GB limit qoyulub — **ölçülməyib, təxmindir**. Yük testindən
sonra dəqiqləşdirin. JVM heap konteyner limitinin 75%-ni götürür (`MaxRAMPercentage`),
yəni 1 GB limitdə heap ~768 MB.

---

## §4 Testlər

### 4.1 Mövcud vəziyyət (38 test, hamısı keçir)

```
GlobalExceptionHandlerTest             6   unit
UserServiceTest                        7   unit
EnrollmentServiceTest                  8   unit
PaymentServiceTest                     8   unit
AuthFlowIntegrationTest                4   integration (register→OTP→login→refresh→logout)
CourseCrudAndSearchIntegrationTest     3   integration (RBAC + axtarış/paginasiya)
EnrollmentPaymentEventIntegrationTest  1   integration (event → notification + audit log)
NexoraAcademyApplicationTests          1   context load
```

```bash
./mvnw test      # yalnız Docker lazımdır (Testcontainers), başqa heç nə
```

### 4.2 Nə örtülmür

| Boşluq | Risk | Kim |
|---|---|---|
| Rate limiting testi yoxdur | 429 məntiqi avtomatik yoxlanmır | Backend |
| Ödəniş callback imza testi yoxdur | §2.1 ilə birləşəndə ən yüksək risk | Backend |
| Yük/stress testi yoxdur | Resurs limitləri təxminidir | DevOps |
| 23 controller-in kiçik hissəsi test olunub | Reqressiya riski | Backend |

### 4.3 Deploy-dan sonra smoke test

```bash
BASE=https://api.<domen>

curl -sf $BASE/api/v1/courses                                     # 200 — public GET
curl -so /dev/null -w "%{http_code}\n" $BASE/api/v1/users          # 401 — qorunur
curl -so /dev/null -w "%{http_code}\n" $BASE/v3/api-docs           # 404 — Swagger bağlı
curl -so /dev/null -w "%{http_code}\n" $BASE/actuator/health       # 404 — actuator ictimai deyil
curl -sI $BASE/api/v1/courses | grep -i strict-transport           # HSTS var?
curl -sI http://api.<domen>/api/v1/courses | head -1               # 301 → https

# CORS: icazəsiz origin-dən Access-Control-Allow-Origin GƏLMƏMƏLİDİR
curl -si -H "Origin: https://evil.example" $BASE/api/v1/courses | grep -i access-control

# Rate limit: 25 ardıcıl login → sonuncular 429
for i in $(seq 1 25); do
  curl -so /dev/null -w "%{http_code} " -X POST $BASE/api/v1/auth/login \
    -H 'Content-Type: application/json' -d '{"email":"x@y.z","password":"wrong-pass"}'
done; echo

# Daxildən
docker compose -f docker-compose.prod.yml exec app wget -qO- localhost:9091/actuator/health
```

**Ən vacib əl testi:** real qeydiyyat → **məktubun həqiqətən gəlməsi** → OTP ilə
təsdiq → login. SMTP problemləri **yalnız bu addımda üzə çıxır**, çünki `EmailService`
xətanı udur və heç bir 5xx qaytarmır.

---

## §5 Deploy-dan sonra ilk 48 saat

1. `docker compose -f docker-compose.prod.yml ps` → hamısı **healthy**, restart sayı 0
2. Log-da `Successfully applied 13 migrations` sətrini təsdiqlə
3. **Bir real qeydiyyat** et və məktubun gəldiyini yoxla (§4.3)
4. Ertəsi gün `ls -lh backups/` — ilk avtomatik dump yarandımı?
5. `PaymentGatewaySignatureVerifier` WARN loglarını izlə (§2.1 həll olunana qədər)
6. `docker stats` ilə yaddaş/CPU-ya bax, limitləri dəqiqləşdir
7. Disk sahəsini yoxla (`df -h`) — loglar + backup-lar böyüyür

---

## §6 Sürətli istinad

| Nə | Harada |
|---|---|
| Compose faylı | `docker-compose.prod.yml` |
| nginx / TLS konfiqurasiyası | `deploy/nginx.conf`, `deploy/certs/` |
| Konfiqurasiya (yeganə mənbə) | `.env` (git-də yoxdur) |
| Tətbiq portu / management portu | 8081 / 9091 (ikisi də host-a açıq deyil) |
| Health | konteyner daxilində `:9091/actuator/health[/liveness|/readiness]` |
| Metrikalar | `app:9091/actuator/prometheus` (compose şəbəkəsindən) |
| Backup-lar | `./backups/nexora-<tarix>.dump` |
| Migration-lar | `src/main/resources/db/migration/V1..V13` |
| Prod konfiqurasiya | `src/main/resources/application-prod.yml` |
| Təhlükəsizlik qaydaları | `src/main/java/.../config/SecurityConfig.java` |
| Mənim (sahibin) TODO siyahım | `PRODUCTION_TODO_MINE.md` |
| Kod auditi (nə düzəldilib, niyə) | `PRODUCTION_READINESS.md` |
| API müqaviləsi | `API_CONTRACT.md` |
| Kubernetes manifestləri | `k8s/` — **hazırda istifadə olunmur** |

**Deploy-u bloklayan üç şey:** ① ödəniş webhook secret-i (§2.1) · ② real domen + TLS
(§2.2, §3.2) · ③ backup bərpasının sınaqdan keçirilməsi (§3.6).
