# syntax=docker/dockerfile:1

# --- Build stage -------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Dependency layer cached separately from source so `docker build` doesn't
# re-download the internet every time only src/ changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw \
    && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B \
    && mv target/*.jar target/app.jar

# --- Runtime stage -------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    # logback-spring.xml logs/create|read|update|delete|error/... altına yazır (bax LOG_DIR) —
    # /app WORKDIR-i default olaraq root-a məxsusdur, spring istifadəçisi ora yaza bilmir.
    # Qovluğu əvvəlcədən yaradıb ona sahib olmasa, ilk logback appender açılışında
    # "Failed to create parent directories" ilə app boot-da çökür.
    && mkdir -p /app/logs && chown -R spring:spring /app

USER spring:spring

COPY --from=build --chown=spring:spring /app/target/app.jar app.jar

# Real value must come from the deploy environment (see application-prod.yml) —
# this default only picks the profile, it does not supply any secret.
ENV SPRING_PROFILES_ACTIVE=prod
# Konteyner limitinin ~75%-i heap-ə. Açıq yazılmasa JVM özü təxmin edir və
# `docker run -m 1g` kimi limitlərdə OOM-kill riski artır.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof"

# 8081 = tətbiq API-si (reverse proxy bura yönləndirir)
# 9091 = management/actuator (health + prometheus) — bax application-prod.yml.
#        Bu portu HEÇ VAXT internetə açmayın: actuator zənciri ayrı kontekstdədir.
EXPOSE 8081 9091

# DİQQƏT: prod profilində actuator 8081-də DEYİL, 9091-dədir. Əvvəllər bu yoxlama
# 8081/actuator/health-ə vururdu və prod-da 404 alırdı — yəni konteyner həmişə
# "unhealthy" görünür, `depends_on: service_healthy` şərtləri isə heç vaxt keçmirdi.
HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
    CMD wget -qO- http://localhost:9091/actuator/health/readiness | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
