package az.demo.NexoraAcademy.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Throwaway PostgreSQL for the {@code @SpringBootTest} slices.
 *
 * <p>Before this existed, every integration test connected to whatever database
 * {@code .env} happened to point at — the developer's own dev instance. That made the
 * suite unrunnable on CI (no {@code .env}, no Postgres on localhost:5432) and, worse,
 * let tests write rows (administrators, courses and CMS content) straight into a
 * database someone was also using by hand.
 *
 * <p>{@code @ServiceConnection} feeds the container's JDBC url/credentials to Spring as
 * {@code JdbcConnectionDetails}, which outranks any {@code spring.datasource.*} property —
 * so the {@code DB_*} values in {@code .env} are ignored during tests without needing
 * a separate {@code application-test.yml}. Flyway then runs V1..V13 against the empty
 * container, which also means the complete migration chain is exercised on every build.
 *
 * <p>The image is {@code pgvector/pgvector:pg16}, not plain {@code postgres} —
 * {@code V1__init_extensions_and_schemas.sql} does {@code CREATE EXTENSION vector}, which
 * only exists in that image (same image as docker-compose and the k8s StatefulSet).
 *
 * <p>The container is a singleton bean in a cached Spring context, so all test classes
 * that import this configuration share one container for the whole build.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    // Testcontainers 2.x: org.testcontainers.postgresql.PostgreSQLContainer artıq
    // generic deyil (köhnə org.testcontainers.containers.* variantından fərqli olaraq).
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(
                DockerImageName.parse("pgvector/pgvector:pg16")
                        .asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("nexora_academy_test")
                .withUsername("nexora_test")
                .withPassword("nexora_test")
                // MÜTLƏQ: application.yml/-prod.yml-dəki JDBC url-də olan
                // "?stringtype=unspecified" parametrinin eynisi. @ServiceConnection url-i
                // konteynerdən özü qurur və həmin parametri bilmir — o olmadan Postgres-in
                // native ENUM sütunlarına (user_role, account_status, ...) String bind
                // edilməsi "column ... is of type platform.user_role but expression is of
                // type character varying" ilə çökür və tətbiq AdminSeeder-də açılmır.
                .withUrlParam("stringtype", "unspecified");
    }
}
