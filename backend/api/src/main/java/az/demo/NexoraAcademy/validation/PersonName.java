package az.demo.NexoraAcademy.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A human first/last name as it may appear on an Azerbaijani ID document.
 *
 * <p><b>Length (2–40).</b> The longest surnames in real use here run ~13–16 characters
 * ("Məmmədəliyeva", "Hacıbababəyov", "Nəcəfquliyeva") and given names ~11
 * ("Nurməhəmməd", "Məhəmmədəli"), so a single part never approaches the 100 the column
 * previously allowed. 40 is deliberately not tightened to ~20: hyphenated double
 * surnames ("Məmmədəliyeva-Nəcəfquliyeva" is 27) and foreign-locale administrator
 * names would otherwise be rejected. 40 also sits just above the 39
 * characters ICAO 9303 allots to the full name field of a machine-readable passport,
 * which is the practical ceiling for a name that has to fit on an ID document at all.
 *
 * <p><b>Alphabet.</b> {@code \p{L}} covers the Azerbaijani alphabet (ə, ğ, ı, ö, ş, ü, ç)
 * along with Latin/Cyrillic, so no letter is excluded. Digits, punctuation, emoji and
 * control characters are rejected. Space, hyphen and apostrophe are allowed *between*
 * letters — "Anna-Maria", "O'Brien", "Van der Berg" are valid — but a name cannot start
 * or end with a separator, nor contain two in a row.
 *
 * <p>Pair with {@code @JsonDeserialize(using = TrimmedStringDeserializer.class)} so the
 * value is trimmed before these constraints run; otherwise " A " passes min-length 2 and
 * is then stored as a single character. Presence (@NotBlank) is intentionally NOT part of
 * this annotation — PATCH DTOs need the field to stay optional (see {@link ValidationGroups}).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.METHOD})
@Constraint(validatedBy = {})
@Size(min = PersonName.MIN_LENGTH, max = PersonName.MAX_LENGTH)
@Pattern(regexp = PersonName.PATTERN, message = PersonName.PATTERN_MESSAGE)
public @interface PersonName {

    int MIN_LENGTH = 2;
    int MAX_LENGTH = 40;

    /** Letter, then any run of letters separated by single spaces, hyphens or apostrophes. */
    String PATTERN = "^\\p{L}[\\p{L}\\p{M}]*(?:[ '’\\-][\\p{L}\\p{M}]+)*$";

    String PATTERN_MESSAGE = "must contain letters only, optionally separated by a single space, hyphen or apostrophe";

    String message() default "is not a valid name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
