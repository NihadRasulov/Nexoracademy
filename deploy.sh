#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"
DOMAIN="nexoracademy.az"
WWW_DOMAIN="www.nexoracademy.az"
ADMIN_PATH="sys-control-9912"
COMPOSE=(docker compose)

info() { printf '\033[1;34m[INFO]\033[0m %s\n' "$*"; }
success() { printf '\033[1;32m[OK]\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m[WARN]\033[0m %s\n' "$*" >&2; }
fail() { printf '\033[1;31m[ERROR]\033[0m %s\n' "$*" >&2; exit 1; }

usage() {
  cat <<'USAGE'
Nexora Academy production helper

İstifadə:
  bash deploy.sh preflight       Server, .env və Compose yoxlaması
  bash deploy.sh tls             İlk Let's Encrypt sertifikatını al (gateway bağlı olmalıdır)
  bash deploy.sh up              Backup + build + migration + deploy + smoke test
  bash deploy.sh smoke           Canlı public/admin/chatbot yoxlamaları
  bash deploy.sh status          Konteyner vəziyyətləri
  bash deploy.sh logs [service]  Son loglar (məs. app, web-gateway)
  bash deploy.sh backup          PostgreSQL və upload fayllarının backup-ı
  bash deploy.sh renew-tls       Sertifikatı yenilə və Nginx-i reload et
  bash deploy.sh lock-admin      İlk login-dən sonra admin seeder-i söndür

Bu script qəsdən down/reset/purge əmri vermir və volume silmir.
USAGE
}

need_command() {
  command -v "$1" >/dev/null 2>&1 || fail "Tələb olunan əmr tapılmadı: $1"
}

env_value() {
  local key="$1"
  local value
  value="$(awk -F= -v wanted="$key" '
    $0 !~ /^[[:space:]]*#/ && $1 == wanted {
      sub(/^[^=]*=/, ""); found=$0
    }
    END { if (found != "") print found }
  ' "$ENV_FILE")"
  value="${value%$'\r'}"
  value="${value#\"}"
  value="${value%\"}"
  printf '%s' "$value"
}

require_real_value() {
  local key="$1"
  local value
  value="$(env_value "$key")"
  [[ -n "$value" ]] || fail ".env daxilində $key boşdur."
  case "$value" in
    *replace-with*|*change-me*|*example.com*)
      fail ".env daxilində $key hələ nümunə dəyəridir."
      ;;
  esac
}

assert_env() {
  [[ -f "$ENV_FILE" ]] || fail ".env yoxdur. Əvvəlcə: cp .env.example .env"
  chmod 600 "$ENV_FILE"

  # These keys replace Spring Boot's normal config search and can make a valid
  # JAR ignore application.yml/application-prod.yml entirely.
  local forbidden_key
  for forbidden_key in SPRING_CONFIG_LOCATION SPRING_CONFIG_ADDITIONAL_LOCATION \
    SPRING_CONFIG_NAME SPRING_CONFIG_IMPORT; do
    if grep -Eq "^[[:space:]]*${forbidden_key}[[:space:]]*=" "$ENV_FILE"; then
      fail ".env daxilində ${forbidden_key} olmamalıdır; bu parametr application.yml yüklənməsini poza bilər."
    fi
  done

  local key
  for key in TLS_EMAIL DB_NAME DB_USER DB_PASSWORD JWT_SECRET REDIS_PASSWORD \
    ADMIN_SEED_EMAIL ADMIN_SEED_PASSWORD OPENROUTER_API_KEY; do
    require_real_value "$key"
  done

  [[ "$(env_value TLS_DOMAIN)" == "$DOMAIN" ]] \
    || fail "TLS_DOMAIN dəqiq $DOMAIN olmalıdır."
  [[ "$(env_value TLS_WWW_DOMAIN)" == "$WWW_DOMAIN" ]] \
    || fail "TLS_WWW_DOMAIN dəqiq $WWW_DOMAIN olmalıdır."
  [[ "$(env_value TLS_EMAIL)" == *@* ]] || fail "TLS_EMAIL düzgün e-poçt deyil."
  [[ "$(env_value ADMIN_SEED_EMAIL)" == *@* ]] || fail "ADMIN_SEED_EMAIL düzgün e-poçt deyil."
  local jwt_secret db_password redis_password admin_password
  jwt_secret="$(env_value JWT_SECRET)"
  db_password="$(env_value DB_PASSWORD)"
  redis_password="$(env_value REDIS_PASSWORD)"
  admin_password="$(env_value ADMIN_SEED_PASSWORD)"
  [[ ${#jwt_secret} -ge 32 ]] || fail "JWT_SECRET ən azı 32 simvol olmalıdır."
  [[ ${#db_password} -ge 16 ]] || fail "DB_PASSWORD ən azı 16 simvol olmalıdır."
  [[ ${#redis_password} -ge 16 ]] || fail "REDIS_PASSWORD ən azı 16 simvol olmalıdır."
  [[ ${#admin_password} -ge 12 ]] \
    || fail "ADMIN_SEED_PASSWORD ən azı 12 simvol olmalıdır."

  case "$(env_value ADMIN_SEED_ENABLED)" in
    true|false) ;;
    *) fail "ADMIN_SEED_ENABLED yalnız true və ya false ola bilər." ;;
  esac

  local origins
  origins="$(env_value CORS_ALLOWED_ORIGINS)"
  [[ "$origins" == *"https://${DOMAIN}"* ]] \
    || fail "CORS_ALLOWED_ORIGINS daxilində https://${DOMAIN} yoxdur."
}

preflight() {
  need_command docker
  need_command curl
  need_command gzip
  assert_env
  docker info >/dev/null 2>&1 || fail "Docker daemon işləmir və ya cari istifadəçinin icazəsi yoxdur."
  "${COMPOSE[@]}" version >/dev/null
  "${COMPOSE[@]}" config --quiet
  success "Preflight keçdi: Docker, .env və Compose konfiqurasiyası düzgündür."
}

service_state() {
  local service="$1"
  local id
  id="$("${COMPOSE[@]}" ps -q "$service")"
  [[ -n "$id" ]] || { printf 'missing'; return; }
  docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$id"
}

wait_for_service() {
  local service="$1"
  local timeout="${2:-240}"
  local started=$SECONDS
  local state
  while (( SECONDS - started < timeout )); do
    state="$(service_state "$service")"
    case "$state" in
      healthy|running)
        success "$service hazırdır ($state)."
        return
        ;;
      unhealthy|exited|dead)
        "${COMPOSE[@]}" logs --tail=120 "$service" >&2 || true
        fail "$service uğursuz vəziyyətə keçdi: $state"
        ;;
    esac
    sleep 3
  done
  "${COMPOSE[@]}" logs --tail=120 "$service" >&2 || true
  fail "$service ${timeout} saniyədə hazır olmadı."
}

certificate_exists() {
  "${COMPOSE[@]}" --profile tls run --rm --no-deps --entrypoint sh certbot -c \
    "test -s /etc/letsencrypt/live/${DOMAIN}/fullchain.pem && test -s /etc/letsencrypt/live/${DOMAIN}/privkey.pem" \
    >/dev/null 2>&1
}

issue_tls() {
  preflight
  if [[ "$(service_state web-gateway)" == "healthy" || "$(service_state web-gateway)" == "running" ]]; then
    fail "web-gateway işləyir. Mövcud sertifikat üçün 'bash deploy.sh renew-tls' istifadə edin."
  fi
  if certificate_exists; then
    success "TLS sertifikatı artıq mövcuddur."
    return
  fi

  info "Let's Encrypt sertifikatı alınır. DNS A/AAAA qeydləri bu serverə baxmalıdır."
  "${COMPOSE[@]}" --profile tls run --rm --service-ports certbot \
    certonly --standalone --non-interactive --agree-tos --no-eff-email \
    --email "$(env_value TLS_EMAIL)" \
    -d "$DOMAIN" -d "$WWW_DOMAIN"
  certificate_exists || fail "Certbot tamamlandı, amma sertifikat volume-da tapılmadı."
  success "TLS sertifikatı hazırdır."
}

backup() {
  assert_env
  need_command docker
  need_command gzip
  [[ "$(service_state postgres)" == "healthy" ]] || fail "Backup üçün postgres healthy olmalıdır."

  local stamp backup_dir db_file uploads_file db_user db_name db_password
  stamp="$(date -u +%Y%m%dT%H%M%SZ)"
  backup_dir="${SCRIPT_DIR}/backups"
  db_file="${backup_dir}/postgres-${stamp}.sql.gz"
  uploads_file="${backup_dir}/uploads-${stamp}.tar.gz"
  db_user="$(env_value DB_USER)"
  db_name="$(env_value DB_NAME)"
  db_password="$(env_value DB_PASSWORD)"

  umask 077
  mkdir -p "$backup_dir"
  info "PostgreSQL backup yaradılır..."
  "${COMPOSE[@]}" exec -T -e PGPASSWORD="$db_password" postgres \
    pg_dump -U "$db_user" -d "$db_name" --clean --if-exists --no-owner --no-privileges \
    | gzip -9 > "$db_file"
  [[ -s "$db_file" ]] || fail "PostgreSQL backup boş yarandı."

  if [[ "$(service_state app)" == "healthy" ]]; then
    info "Upload/CV backup yaradılır..."
    "${COMPOSE[@]}" exec -T app tar -C /app -cf - uploads | gzip -9 > "$uploads_file"
    [[ -s "$uploads_file" ]] || fail "Upload backup boş yarandı."
  else
    warn "Java app hələ işləmədiyi üçün upload backup ötürüldü (ilk deploy üçün normaldır)."
  fi
  success "Backup hazırdır: $backup_dir"
}

smoke() {
  preflight
  certificate_exists || fail "TLS sertifikatı yoxdur. Əvvəlcə 'bash deploy.sh tls'."

  local resolve=(--resolve "${DOMAIN}:443:127.0.0.1")
  local base="https://${DOMAIN}"
  info "Gateway və public səhifələr yoxlanılır..."
  curl -fsS --max-time 20 "${resolve[@]}" "${base}/health" >/dev/null
  curl -fsS --max-time 20 "${resolve[@]}" "${base}/" >/dev/null
  curl -fsS --max-time 20 "${resolve[@]}" "${base}/api/v1/public/catalog/categories" >/dev/null
  curl -fsS --max-time 20 "${resolve[@]}" "${base}/${ADMIN_PATH}/login" >/dev/null

  local admin_backend_code
  admin_backend_code="$(curl -sS -o /dev/null -w '%{http_code}' --max-time 20 \
    "${resolve[@]}" -H 'Content-Type: application/json' \
    -d '{"email":"nexora-smoke-probe@invalid.example","password":"not-a-real-password"}' \
    "${base}/${ADMIN_PATH}/api/v1/auth/login")"
  [[ "$admin_backend_code" == "401" ]] \
    || fail "Admin BFF -> Java login probe uğursuz oldu (HTTP $admin_backend_code)."

  curl -fsS --max-time 40 "${resolve[@]}" \
    -H 'Content-Type: application/json' -d '{"message":"/start"}' \
    "${base}/api/chat" | grep -q '"reply"' \
    || fail "Chatbot smoke testi cavab qaytarmadı."

  local blocked_code
  blocked_code="$(curl -sS -o /dev/null -w '%{http_code}' --max-time 20 \
    "${resolve[@]}" "${base}/api/v1/users")"
  [[ "$blocked_code" == "404" ]] || fail "Java admin API gateway-də açıq qalıb (HTTP $blocked_code)."

  success "Smoke test keçdi: public UI/API, chatbot və Admin BFF -> Java login yolu işləyir."
  printf '\nSayt:  %s\nAdmin: %s/%s/\n' "$base" "$base" "$ADMIN_PATH"
}

deploy_up() {
  preflight
  certificate_exists || fail "TLS sertifikatı yoxdur. Əvvəlcə 'bash deploy.sh tls' işlədin."

  info "PostgreSQL və Redis başladılır..."
  "${COMPOSE[@]}" up -d postgres redis
  wait_for_service postgres 180
  wait_for_service redis 120
  backup

  info "Production image-lər build olunur və stack başladılır..."
  "${COMPOSE[@]}" build --pull app
  "${COMPOSE[@]}" up -d --build --force-recreate

  local service
  for service in app chatbot-api admin-bff main-ui web-gateway; do
    wait_for_service "$service" 360
  done

  local flyway_id flyway_exit
  flyway_id="$("${COMPOSE[@]}" ps -aq flyway)"
  [[ -n "$flyway_id" ]] || fail "Flyway konteyneri tapılmadı."
  flyway_exit="$(docker inspect --format '{{.State.ExitCode}}' "$flyway_id")"
  [[ "$flyway_exit" == "0" ]] || {
    "${COMPOSE[@]}" logs --tail=160 flyway >&2 || true
    fail "Flyway migration uğursuz oldu (exit $flyway_exit)."
  }
  success "Flyway migration uğurla tamamlandı."
  smoke
}

renew_tls() {
  preflight
  [[ "$(service_state web-gateway)" == "healthy" ]] \
    || fail "Webroot renewal üçün web-gateway healthy olmalıdır."
  "${COMPOSE[@]}" --profile tls run --rm --no-deps certbot \
    renew --webroot --webroot-path /var/www/certbot --quiet
  "${COMPOSE[@]}" exec -T web-gateway nginx -s reload
  success "TLS renewal yoxlanıldı və Nginx reload edildi."
}

lock_admin_seed() {
  preflight
  [[ "$(env_value ADMIN_SEED_ENABLED)" == "true" ]] || {
    success "Admin seeder artıq söndürülüb."
    return
  }
  printf 'Admin panelə real giriş etdiyinizi təsdiqləyin. Davam etmək üçün YES yazın: '
  local confirmation
  read -r confirmation
  [[ "$confirmation" == "YES" ]] || fail "Təsdiq alınmadı; heç nə dəyişmədi."

  local temp_file
  temp_file="$(mktemp "${ENV_FILE}.tmp.XXXXXX")"
  awk '
    BEGIN { changed=0 }
    /^ADMIN_SEED_ENABLED=/ { print "ADMIN_SEED_ENABLED=false"; changed=1; next }
    { print }
    END { if (!changed) print "ADMIN_SEED_ENABLED=false" }
  ' "$ENV_FILE" > "$temp_file"
  chmod 600 "$temp_file"
  mv "$temp_file" "$ENV_FILE"
  "${COMPOSE[@]}" up -d --force-recreate app
  wait_for_service app 240
  "${COMPOSE[@]}" restart web-gateway >/dev/null
  wait_for_service web-gateway 120
  success "Admin seeder söndürüldü və servis təhlükəsiz yeniləndi."
}

show_status() {
  [[ -f "$ENV_FILE" ]] || fail ".env yoxdur."
  "${COMPOSE[@]}" ps -a
}

show_logs() {
  [[ -f "$ENV_FILE" ]] || fail ".env yoxdur."
  local service="${1:-}"
  if [[ -n "$service" ]]; then
    "${COMPOSE[@]}" logs --tail=200 "$service"
  else
    "${COMPOSE[@]}" logs --tail=120
  fi
}

cd "$SCRIPT_DIR"
case "${1:-}" in
  preflight) preflight ;;
  tls) issue_tls ;;
  up) deploy_up ;;
  smoke) smoke ;;
  status) show_status ;;
  logs) show_logs "${2:-}" ;;
  backup) backup ;;
  renew-tls) renew_tls ;;
  lock-admin) lock_admin_seed ;;
  help|-h|--help|"") usage ;;
  down|reset|purge|destroy)
    fail "Bu təhlükəli əməliyyat deploy.sh daxilində qəsdən bloklanıb."
    ;;
  *) usage; fail "Naməlum əməliyyat: $1" ;;
esac
