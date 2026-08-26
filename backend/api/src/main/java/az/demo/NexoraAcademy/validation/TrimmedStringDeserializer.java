package az.demo.NexoraAcademy.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

/**
 * Trims leading/trailing whitespace off a String field *before* Bean Validation runs,
 * so the constraints (@NotBlank/@Size) apply to exactly the value that gets persisted.
 *
 * Without this, padded input slips through: {@code "  "} satisfies @Size(min = 2) on a
 * PATCH DTO (where @NotBlank is inactive by design — see {@link ValidationGroups}) and
 * would be stored as a whitespace-only name, and {@code " A "} would pass min-length 2
 * only to be stored as a single character.
 *
 * Deliberately opt-in per field rather than registered globally: fields where whitespace
 * is significant (passwords above all, plus free-form content bodies) must NOT be trimmed.
 */
public class TrimmedStringDeserializer extends StdScalarDeserializer<String> {

    public TrimmedStringDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        return value == null ? null : value.trim();
    }
}
