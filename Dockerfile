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
ENV JAVA_OPTS=""

EXPOSE 8185

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8185/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
