package az.demo.NexoraAcademy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins down the intermediate schema state that makes the name split deployable without
 * downtime — the half the rest of the suite never sees.
 *
 * <p>The suite normally migrates all the way to V15, where {@code full_name} is gone. In
 * production the rollout stops at V14 (see {@code application-prod.yml →
 * spring.flyway.target}) precisely so that old pods, which still SELECT and INSERT
 * {@code full_name}, keep working while new pods write {@code first_name}/{@code last_name}.
 * A regression in the V14 trigger would not fail any other test, but it would corrupt names
 * — or drop them entirely — during every rolling deploy.
 *
 * <p>{@code spring.flyway.target=14} gives this class its own context and its own container,
 * migrated only as far as the expand step.
 */
@SpringBootTest(properties = "spring.flyway.target=14")
@Import(TestcontainersConfiguration.class)
class UserNameExpandMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void fullNameColumnSurvivesTheExpandStepSoOldInstancesCanStillUseIt() {
        Integer nullable = jdbc.queryForObject("""
                SELECT CASE WHEN is_nullable = 'YES' THEN 1 ELSE 0 END
                FROM information_schema.columns
                WHERE table_schema = 'identity' AND table_name = 'users' AND column_name = 'full_name'
                """, Integer.class);

        assertThat(nullable)
                .as("full_name must still exist after V14, and be nullable so new code need not write it")
                .isEqualTo(1);
    }

    /** Old pod: knows only full_name. The trigger has to derive the split columns. */
    @Test
    void insertWritingOnlyFullNameFillsFirstAndLastName() {
        String email = insertWithFullName("Aysel Məmmədova");

        Map<String, Object> row = nameRowOf(email);
        assertThat(row.get("first_name")).isEqualTo("Aysel");
        assertThat(row.get("last_name")).isEqualTo("Məmmədova");
    }

    /** New pod: knows only first/last. The trigger has to keep full_name populated. */
    @Test
    void insertWritingOnlyFirstAndLastNameFillsFullName() {
        String email = insertWithSplitName("Nihad", "Rəsulov");

        Map<String, Object> row = nameRowOf(email);
        assertThat(row.get("full_name")).isEqualTo("Nihad Rəsulov");
    }

    @Test
    void updateFromAnOldInstanceRederivesFirstAndLastName() {
        String email = insertWithFullName("Aysel Məmmədova");

        jdbc.update("UPDATE identity.users SET full_name = ? WHERE email = ?", "Leyla İsmayılova", email);

        Map<String, Object> row = nameRowOf(email);
        assertThat(row.get("first_name")).isEqualTo("Leyla");
        assertThat(row.get("last_name")).isEqualTo("İsmayılova");
    }

    @Test
    void updateFromANewInstanceRebuildsFullName() {
        String email = insertWithSplitName("Nihad", "Rəsulov");

        jdbc.update("UPDATE identity.users SET first_name = ?, last_name = ? WHERE email = ?",
                "Günel", "Səfərova", email);

        Map<String, Object> row = nameRowOf(email);
        assertThat(row.get("full_name")).isEqualTo("Günel Səfərova");
        assertThat(row.get("first_name")).isEqualTo("Günel");
    }

    /** Everything after the first space is the surname — "Məmmədova Qızı" must not be truncated to one word. */
    @Test
    void multiWordSurnameKeepsEveryWordAfterTheFirstSpace() {
        String email = insertWithFullName("Aysel Məmmədova Qızı");

        Map<String, Object> row = nameRowOf(email);
        assertThat(row.get("first_name")).isEqualTo("Aysel");
        assertThat(row.get("last_name")).isEqualTo("Məmmədova Qızı");
    }

    /** A single-word legacy name has no surname to split off; the placeholder keeps NOT NULL reachable in V15. */
    @Test
    void singleWordLegacyNameGetsThePlaceholderSurname() {
        String email = insertWithFullName("Madonna");

        Map<String, Object> row = nameRowOf(email);
        assertThat(row.get("first_name")).isEqualTo("Madonna");
        assertThat(row.get("last_name")).isEqualTo("Yoxdur");
    }

    @Test
    void surroundingWhitespaceInALegacyNameIsNotCarriedIntoTheSplitColumns() {
        String email = insertWithFullName("  Tural   Bayramov  ");

        Map<String, Object> row = nameRowOf(email);
        assertThat(row.get("first_name")).isEqualTo("Tural");
        assertThat(row.get("last_name")).isEqualTo("Bayramov");
    }

    // --- helpers ---------------------------------------------------------------

    private String insertWithFullName(String fullName) {
        String email = uniqueEmail("legacy");
        jdbc.update("INSERT INTO identity.users (email, full_name) VALUES (?, ?)", email, fullName);
        return email;
    }

    private String insertWithSplitName(String firstName, String lastName) {
        String email = uniqueEmail("current");
        jdbc.update("INSERT INTO identity.users (email, first_name, last_name) VALUES (?, ?, ?)",
                email, firstName, lastName);
        return email;
    }

    private Map<String, Object> nameRowOf(String email) {
        return jdbc.queryForMap(
                "SELECT first_name, last_name, full_name FROM identity.users WHERE email = ?", email);
    }

    private static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }
}
