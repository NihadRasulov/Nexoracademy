package az.demo.NexoraAcademy.validation;

import az.demo.NexoraAcademy.dto.auth.RegisterRequest;
import az.demo.NexoraAcademy.dto.identity.UpdateProfileRequest;
import az.demo.NexoraAcademy.dto.identity.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the firstName/lastName split (migration V14): the two name fields replaced the
 * old single fullName, so their constraints — and the trimming that happens before those
 * constraints run — are exercised here against the real Jackson + Bean Validation stack.
 */
class UserNameValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        factory.close();
    }

    // --- RegisterRequest (public self-registration) ----------------------------

    @Test
    void registerAcceptsValidFirstAndLastName() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Nihad", "Rəsulov", null, "pass1234");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void registerRejectsMissingNameFields() {
        RegisterRequest request = new RegisterRequest("user@example.com", null, null, null, "pass1234");

        assertThat(fieldsOf(validator.validate(request))).contains("firstName", "lastName");
    }

    @Test
    void registerRejectsSingleCharacterAndOverlongNames() {
        RegisterRequest tooShort = new RegisterRequest("user@example.com", "A", "Rəsulov", null, "pass1234");
        RegisterRequest tooLong = new RegisterRequest(
                "user@example.com", "Nihad", "R".repeat(PersonName.MAX_LENGTH + 1), null, "pass1234");

        assertThat(fieldsOf(validator.validate(tooShort))).contains("firstName");
        assertThat(fieldsOf(validator.validate(tooLong))).contains("lastName");
    }

    /** The Azerbaijani alphabet must pass unharmed — that is the whole point of \p{L}. */
    @Test
    void registerAcceptsAzerbaijaniLettersAndRealisticLongNames() {
        // ə ğ ı ö ş ü ç + the longest surnames actually in use here
        for (String name : new String[]{"Əli", "Şəhriyar", "Nurməhəmməd", "Məmmədəliyeva",
                "Hacıbababəyov", "Nəcəfquliyeva", "Çingiz", "Gülnar"}) {
            RegisterRequest request = new RegisterRequest("user@example.com", name, name, null, "pass1234");
            assertThat(validator.validate(request))
                    .as("valid Azerbaijani name: %s", name)
                    .isEmpty();
        }
    }

    /** Hyphenated double surnames and apostrophes are the reason the cap is 40, not ~20. */
    @Test
    void registerAcceptsHyphenatedApostrophedAndMultiWordNames() {
        for (String name : new String[]{"Məmmədəliyeva-Nəcəfquliyeva", "Anna-Maria", "O'Brien",
                "Van der Berg", "Məhəmməd Əli"}) {
            RegisterRequest request = new RegisterRequest("user@example.com", "Nihad", name, null, "pass1234");
            assertThat(validator.validate(request))
                    .as("valid compound name: %s", name)
                    .isEmpty();
        }
    }

    @Test
    void registerRejectsDigitsSymbolsAndStraySeparatorsInNames() {
        for (String name : new String[]{"Nihad1", "N1had", "Nihad!", "<script>", "Nihad_Rəsulov",
                "-Nihad", "Nihad-", "Nihad--Rəsul", "Nihad  Rəsul", "'Nihad", "😀"}) {
            RegisterRequest request = new RegisterRequest("user@example.com", name, "Rəsulov", null, "pass1234");
            assertThat(fieldsOf(validator.validate(request)))
                    .as("invalid name must be rejected: %s", name)
                    .contains("firstName");
        }
    }

    /** Padding must be stripped before @Size runs, otherwise " A " would be stored as "A". */
    @Test
    void registerTrimsNamesBeforeValidationSoPaddedInputCannotSlipThrough() throws Exception {
        String json = """
                {"email":"user@example.com","firstName":" A ","lastName":"  ","password":"pass1234"}
                """;

        RegisterRequest request = objectMapper.readValue(json, RegisterRequest.class);

        assertThat(request.firstName()).isEqualTo("A");
        assertThat(request.lastName()).isEmpty();
        assertThat(fieldsOf(validator.validate(request))).contains("firstName", "lastName");
    }

    @Test
    void registerKeepsSurroundingWhitespaceOutOfStoredNames() throws Exception {
        String json = """
                {"email":"user@example.com","firstName":"  Nihad  ","lastName":" Rəsulov ","password":"pass1234"}
                """;

        RegisterRequest request = objectMapper.readValue(json, RegisterRequest.class);

        assertThat(request.firstName()).isEqualTo("Nihad");
        assertThat(request.lastName()).isEqualTo("Rəsulov");
        assertThat(validator.validate(request)).isEmpty();
    }

    /** Passwords are deliberately NOT trimmed — leading/trailing spaces are legitimate characters. */
    @Test
    void registerDoesNotTrimPassword() throws Exception {
        String json = """
                {"email":"user@example.com","firstName":"Nihad","lastName":"Rəsulov","password":" pass1234 "}
                """;

        RegisterRequest request = objectMapper.readValue(json, RegisterRequest.class);

        assertThat(request.password()).isEqualTo(" pass1234 ");
    }

    // --- UserRequest (admin CRUD: OnCreate vs PATCH) ---------------------------

    @Test
    void adminCreateRequiresBothNamesAndStillChecksFormatConstraints() {
        UserRequest request = new UserRequest("not-an-email", null, null, null, "weak",
                null, null, null, null);

        Set<String> fields = fieldsOf(validator.validate(request, ValidationGroups.OnCreate.class));

        // presence constraints (OnCreate group)
        assertThat(fields).contains("firstName", "lastName");
        // format constraints live in the Default group — OnCreate extends Default, so they must run too
        assertThat(fields).contains("email", "password");
    }

    @Test
    void adminPatchAllowsOmittedNamesButStillValidatesTheOnesSent() {
        UserRequest untouched = new UserRequest(null, null, null, null, null, null, null, null, null);
        UserRequest badName = new UserRequest(null, null, "A", null, null, null, null, null, null);

        assertThat(validator.validate(untouched)).isEmpty();
        assertThat(fieldsOf(validator.validate(badName))).contains("firstName");
    }

    /** PATCH has no @NotBlank by design, so trimming is what stops a whitespace-only name. */
    @Test
    void adminPatchRejectsWhitespaceOnlyName() throws Exception {
        UserRequest request = objectMapper.readValue("{\"firstName\":\"   \"}", UserRequest.class);

        assertThat(request.firstName()).isEmpty();
        assertThat(fieldsOf(validator.validate(request))).contains("firstName");
    }

    // --- UpdateProfileRequest (self-service /me) -------------------------------

    @Test
    void selfServiceProfileUpdateRejectsWhitespaceOnlyNameAndAllowsOmission() throws Exception {
        UpdateProfileRequest blank = objectMapper.readValue("{\"lastName\":\" \"}", UpdateProfileRequest.class);
        UpdateProfileRequest omitted = objectMapper.readValue("{\"locale\":\"az-AZ\"}", UpdateProfileRequest.class);

        assertThat(fieldsOf(validator.validate(blank))).contains("lastName");
        assertThat(validator.validate(omitted)).isEmpty();
    }

    @Test
    void selfServiceProfileUpdateAcceptsUpdatingOnlyOneOfTheTwoNames() throws Exception {
        UpdateProfileRequest request = objectMapper.readValue("{\"firstName\":\"Aysel\"}", UpdateProfileRequest.class);

        assertThat(request.firstName()).isEqualTo("Aysel");
        assertThat(request.lastName()).isNull();
        assertThat(validator.validate(request)).isEmpty();
    }

    // --- phone: the pattern must not outrun the VARCHAR(20) column ------------

    @Test
    void phoneLongerThanTheDatabaseColumnIsRejectedInsteadOfFailingAtInsert() {
        // 21 chars: "+" plus 20 — accepted by the old ^\+?[0-9 ()-]{6,20}$ rule, then blew up
        // at INSERT because identity.users.phone is VARCHAR(20).
        String twentyOne = "+99450123456789012345";
        assertThat(twentyOne).hasSize(PhoneNumber.MAX_LENGTH + 1);

        RegisterRequest request = new RegisterRequest(
                "user@example.com", "Nihad", "Rəsulov", twentyOne, "pass1234");

        assertThat(fieldsOf(validator.validate(request))).contains("phone");
    }

    @Test
    void phoneAcceptsRealAzerbaijaniFormatsAndRejectsDigitlessJunk() {
        for (String phone : new String[]{"+994501234567", "+994 50 123 45 67", "0501234567",
                "(012) 345-67-89", null}) {
            RegisterRequest request = new RegisterRequest(
                    "user@example.com", "Nihad", "Rəsulov", phone, "pass1234");
            assertThat(validator.validate(request)).as("valid phone: %s", phone).isEmpty();
        }

        for (String phone : new String[]{"((((((", "------", "12345", "+", "abcdefg",
                "+9945012345678901"}) {
            RegisterRequest request = new RegisterRequest(
                    "user@example.com", "Nihad", "Rəsulov", phone, "pass1234");
            assertThat(fieldsOf(validator.validate(request)))
                    .as("invalid phone must be rejected: %s", phone)
                    .contains("phone");
        }
    }

    private static <T> Set<String> fieldsOf(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
