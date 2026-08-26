# Nexora Academy

Nexora Academy-nin ictimai saytı, CMS admin paneli və chatbot servislərini bir repoda saxlayan tətbiqdir. İctimai saytda istifadəçi hesabı, qeydiyyat/login, tələbə kabineti, ödəniş və enrollment sistemi yoxdur. İdarəetmə üçün yalnız bir `ADMIN` rolu var.

## Memarlıq

```text
İctimai sayt ── /api/v1/public/** ──> Java / Spring Boot ──> PostgreSQL
             └─ /api/chat ─────────> Python / FastAPI ───> Java kataloqu

Gizli admin URL ──> React admin UI ──> C# / ASP.NET BFF ──> Java ──> PostgreSQL
                                      HttpOnly cookie       Bearer token
```

- `frontend/main-site`: HTML/CSS/JS ictimai sayt və əlçatan chatbot widgeti.
- `frontend/admin-ui`: React 19 + TypeScript CMS interfeysi.
- `backend/api`: Java 21/Spring Boot əsas API, biznes qaydaları və Flyway migrasiyaları.
- `backend/api/legacy-src`: yalnız baxış üçün deprecated arxiv; build və runtime-a daxil deyil.
- `backend/admin-bff`: admin SPA, HttpOnly sessiya, token refresh və Java proxy funksiyalı .NET 10 BFF. CRUD controller-ləri qəsdən Java-da saxlanır.
- `backend/chatbot-api`: Python 3.12/FastAPI, Redis, RAG və OpenRouter inteqrasiyası.
- `nginx.conf`: TLS, public API, upload, chatbot və gizli admin marşrutlarını ayıran gateway.
- `deploy.sh`: preflight, TLS, backup, build, migration, health və smoke yoxlamalarını idarə edir.

Admin panel bunları idarə edir:

- dashboard statistikası;
- ana səhifə, komanda və ümumi sayt məlumatları;
- xəbərlər, FAQ və vakansiyalar;
- kurslar, kateqoriyalar və təlimçilər;
- karyera müraciətləri/CV-lər, əlaqə mesajları və newsletter abunəçiləri;
- adminin öz profili və parolu.

## Production deploy — düzgün sıra

Serverdə yalnız bunlar lazımdır:

- Linux server və `amd64` və ya `arm64` CPU;
- Docker Engine;
- Docker Compose v2 plugin;
- `curl` və `gzip`;
- minimum 4 GB RAM və təxminən 20 GB boş disk;
- ilk build zamanı Docker Hub, MCR, Maven, npm, PyPI və Hugging Face üçün açıq çıxış HTTPS-i;
- açıq TCP `80` və `443` portları;
- `nexoracademy.az` və `www.nexoracademy.az` DNS qeydlərinin serverə yönəlməsi.

Node, Java, .NET və Python host sistemə ayrıca qurulmur; production build-lər Docker daxilində aparılır.

### 1. Production dəyişənləri

```bash
cp .env.example .env
nano .env
```

`.env` daxilində bütün `replace-with-...` dəyərləri real dəyərlərlə əvəz edilməlidir. Xüsusilə:

- `TLS_EMAIL` — sertifikat xəbərdarlıqları üçün real e-poçt;
- `DB_PASSWORD` — unikal və ən azı 16 simvol;
- `JWT_SECRET` — təsadüfi və ən azı 32 simvol;
- `REDIS_PASSWORD` — unikal və ən azı 16 simvol;
- `ADMIN_SEED_EMAIL` və `ADMIN_SEED_PASSWORD` — ilk admin hesabı;
- `OPENROUTER_API_KEY` — chatbotun tam AI cavabları üçün real açar.

`NEXORA_SUBNET` və `NEXORA_GATEWAY_IP` BFF-in yalnız `web-gateway`-dən gələn
forwarded header-lərə etibar etməsi üçün daxili Docker şəbəkəsini sabitləşdirir.
Default ünvanlar hostdakı başqa Docker/VPN şəbəkəsi ilə toqquşursa, hər ikisini
eyni boş private subnet daxilində dəyişin.

Secret yaratmaq üçün nümunə:

```bash
openssl rand -base64 48
```

### 2. Preflight

```bash
bash deploy.sh preflight
```

Preflight keçmədən növbəti mərhələyə keçməyin. Script nümunə parollarını, yanlış domaini, zəif secretləri, Docker daemon-u və Compose konfiqurasiyasını yoxlayır; secretlərin özünü ekrana çıxarmır.

### 3. İlk TLS sertifikatı

Port `80` başqa Nginx/Apache tərəfindən tutulmamalıdır. DNS yayıldıqdan sonra:

```bash
bash deploy.sh tls
```

Sertifikat Docker volume-da saxlanır. Normal deploy zamanı Certbot servisi başlamır.

### 4. Tam deploy

```bash
bash deploy.sh up
```

Bu tək əmr:

1. PostgreSQL və Redis-i başladır;
2. migration-dan əvvəl database backup yaradır;
3. Java, Python, React/C# və gateway image-lərini build edir;
4. Flyway migrasiyalarını işlədir;
5. bütün health-check-ləri gözləyir;
6. public sayt, Java public API, chatbot və admin login üçün smoke test keçirir;
7. Java admin API-sinin public gateway-dən bağlı olduğunu yoxlayır.

Uğurlu nəticədən sonra:

- sayt: `https://nexoracademy.az/`
- admin: `https://nexoracademy.az/sys-control-9912/`

### 5. İlk admin girişindən sonra

Əvvəlcə admin panelə real daxil olun və profil bölməsindən parolu dəyişin. Bundan sonra:

```bash
bash deploy.sh lock-admin
```

Script `YES` təsdiqi almadan heç nə dəyişmir. Təsdiqdən sonra `ADMIN_SEED_ENABLED=false` edir, yalnız lazım olan servisi təhlükəsiz yeniləyir və gateway-i reload edir.

### 6. Gündəlik əməliyyatlar

```bash
bash deploy.sh status
bash deploy.sh logs app
bash deploy.sh logs chatbot-api
bash deploy.sh logs admin-bff
bash deploy.sh logs web-gateway
bash deploy.sh smoke
bash deploy.sh backup
```

TLS renewal yoxlaması:

```bash
bash deploy.sh renew-tls
```

Server cron nümunəsi:

```cron
17 3 * * * cd /opt/nexora-academy && bash deploy.sh renew-tls >> /var/log/nexora-tls.log 2>&1
```

Repo başqa qovluqdadırsa cron-dakı yolu ona uyğun dəyişin.

## Deploy edən şəxs üçün qırmızı xətlər

Aşağıdakı əmrləri işlətməyin:

```text
docker compose down -v
docker volume rm ...
docker system prune --volumes
git reset --hard
```

Həmçinin:

- tətbiq olunmuş Flyway migration fayllarını dəyişməyin və adını dəyişməyin;
- PostgreSQL və Java portlarını internetə açmayın;
- Java admin endpoint-lərini C# BFF-ni keçərək public etməyin;
- ilk admin loginini yoxlamadan seeder-i söndürməyin;
- `.env` faylını Git-ə commit etməyin və messencerə göndərməyin;
- deploy scripti xəta verəndə yoxlamanı keçmək üçün scripti dəyişməyin;
- backup olmadan mövcud serverdə migration işlətməyin.

Deploy dayanarsa, yeni “düzəliş” etməyin. Bu ardıcıllıqla yalnız diaqnostika aparın:

```bash
bash deploy.sh status
bash deploy.sh logs flyway
bash deploy.sh logs app
bash deploy.sh logs web-gateway
```

Sonra çıxışı layihə sahibinə/developerə göndərin. Script təhlükəli `down`, `reset`, `purge` və `destroy` əmrlərini qəsdən qəbul etmir.

## İlk açılışda hansı məlumatlar hazırdır?

Fresh database-də Flyway avtomatik olaraq:

- ana səhifə başlıqlarını və göstəricilərini;
- “Biz kimik?” komanda adlarını, vəzifələrini və 19 portret yolunu;
- xidmətlər, roadmap və bölmə başlıqlarını;
- ünvan, telefon, e-poçt və sosial media linklərini

CMS database-inə əlavə edir. Bunları deploy edən şəxs əl ilə daxil etmir. Admin sonradan həmin məlumatları redaktə edə, sıralaya, silə və yeni şəkil yükləyə bilər. `ON CONFLICT DO NOTHING` mövcud admin məzmununu qoruyur.

Kurslar, kateqoriyalar, təlimçilər, xəbərlər, FAQ və vakansiyalar təşkilata məxsus real məlumat olduğuna görə admin paneldən daxil edilir. Kursun public görünməsi üçün kateqoriya aktiv, kurs isə `Yayımda + Aktiv + Arxivlənməyib` vəziyyətində olmalıdır.

Kurs və CMS şəkilləri admin paneldən JPG/PNG/WebP formatında, maksimum 5 MB yüklənir. Hero videosu MP4/WebM və maksimum 25 MB-dır. Fayllar `nexora_uploads` Docker volume-da saxlanır; CV public URL ilə verilmir.

## Backup və saxlanılan data

`bash deploy.sh backup` nəticələri Git-dən kənar `backups/` qovluğuna yazır:

- PostgreSQL sıxılmış SQL dump;
- kurs/CMS şəkilləri, videolar və CV-lərin sıxılmış arxivi.

Əsas persistent volume-lar:

- `nexora_pg_data` — PostgreSQL;
- `nexora_uploads` — şəkil, video və CV;
- `nexora_chroma_data` — chatbotun RAG indeksi;
- `nexora_tls_data` — TLS sertifikatı.

Server səviyyəsində bu backup-ları ayrıca şifrəli storage-a köçürmək tövsiyə olunur.

## Lokal inkişaf və test

- Node.js 22+ və npm;
- JDK 21;
- .NET 10 SDK;
- Docker Desktop/Testcontainers.

Java:

```bash
cd backend/api
./mvnw test
```

Admin UI:

```bash
cd frontend/admin-ui
npm ci
npm run lint
npm run build
```

C# BFF:

```bash
dotnet build backend/admin-bff/NexoraAdminPanel/NexoraAdminPanel/NexoraAdminPanel.sln
```

Chatbot testləri production Python 3.12 image-i daxilində işlədilir:

```bash
docker build -t nexora-chatbot-test backend/chatbot-api
docker run --rm --entrypoint python \
  -v "$PWD/backend/chatbot-api:/workspace:ro" \
  -w /workspace nexora-chatbot-test \
  -m unittest discover -s tests -v
docker image rm nexora-chatbot-test
```

## CMS nədir?

CMS saytdakı məzmunu kod dəyişmədən admin paneldən idarə etməyə imkan verir. Bu layihədə ana səhifə, komanda, ümumi sayt məlumatları, Haqqımızda, xəbərlər, FAQ və vakansiyalar CMS-dən gəlir. Naviqasiya, hüquqi səhifələrin skeleti, forma sahələri və təhlükəsizlik qaydaları kodda statik saxlanır.
