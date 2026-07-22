package az.demo.NexoraAcademy.validation;

import jakarta.validation.groups.Default;

/**
 * Distinguishes full-payload validation (POST create / PUT replace, where every
 * required field must be present) from partial-update validation (PATCH, where
 * only the fields the client actually sent should be format-checked).
 *
 * Presence constraints (@NotBlank/@NotNull) are declared against {@link OnCreate}.
 * Format/range constraints (@Email, @Pattern, @Size, @DecimalMin, ...) are left
 * in the implicit Default group, so they still run against whichever fields a
 * PATCH request actually sends — Bean Validation only checks a format constraint
 * when the value is non-null, so omitted fields are left untouched.
 *
 * {@link OnCreate} extends {@link Default}, so controllers validating against
 * OnCreate (create/update) get both groups; controllers validating against the
 * plain Default group (patch) get format checks only, skipping presence.
 */
public final class ValidationGroups {

    private ValidationGroups() {
    }

    public interface OnCreate extends Default {
    }
}
