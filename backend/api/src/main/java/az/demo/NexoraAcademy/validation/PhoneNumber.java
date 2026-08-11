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
 * A phone number in the shape the platform actually stores it.
 *
 * <p><b>Max length 20 is not cosmetic</b> — every phone column in the schema is
 * {@code VARCHAR(20)} (identity.users, crm.leads, crm.contact_submissions). The previous
 * rule, {@code ^\+?[0-9 ()-]{6,20}$}, allowed the leading "+" *on top of* 20 characters,
 * so a 21-character number passed validation and then failed at INSERT with a
 * DataIntegrityViolationException instead of a clean 400.
 *
 * <p><b>Digit count 7–15.</b> 15 is the E.164 maximum for a full international number
 * (an Azerbaijani mobile is 12: +994 50 123 45 67); 7 is the shortest realistic national
 * subscriber number. Counting digits with a lookahead — rather than counting characters —
 * is what stops "((((((" and "------" from being accepted as phone numbers, which the
 * old character-class-only rule allowed.
 *
 * <p>Formatting characters (space, parentheses, hyphen) stay permitted so users can paste
 * a number the way their contacts app shows it: "+994 50 123 45 67" and the parenthesised
 * landline form "(012) 345-67-89" both pass. A number may open with "+" and/or "(" but
 * must end with a digit, so a stray separator cannot trail the value.
 *
 * <p>Presence is intentionally not enforced here — phone is optional everywhere it is used.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.METHOD})
@Constraint(validatedBy = {})
@Size(max = PhoneNumber.MAX_LENGTH)
@Pattern(regexp = PhoneNumber.PATTERN, message = PhoneNumber.PATTERN_MESSAGE)
public @interface PhoneNumber {

    /** Must match VARCHAR(20) — the widest phone column in the schema. */
    int MAX_LENGTH = 20;

    String PATTERN = "^(?=(?:\\D*\\d){7,15}\\D*$)\\+?\\(?\\d[\\d ()\\-]*\\d$";

    String PATTERN_MESSAGE = "must be a valid phone number: 7-15 digits, optionally starting with '+' "
            + "and separated by spaces, parentheses or hyphens";

    String message() default "is not a valid phone number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
