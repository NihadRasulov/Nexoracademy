package az.demo.NexoraAcademy.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint asserting that {@code endField} comes after
 * {@code startField} on a record (or bean). Either side may be null —
 * presence is the job of @NotNull on the individual fields.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface DateRange {

    String message() default "endField must be after startField";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String startField();

    String endField();

    /**
     * If true, end == start is accepted (end >= start). If false (default),
     * end must be strictly after start (end > start).
     */
    boolean inclusive() default false;
}
