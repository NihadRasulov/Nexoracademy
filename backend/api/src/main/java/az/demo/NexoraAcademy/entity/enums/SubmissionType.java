package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum SubmissionType implements PgEnum {
    CONTACT("contact");

    private final String dbValue;

    SubmissionType(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class SubmissionTypeConverter extends AbstractPgEnumConverter<SubmissionType> {
    SubmissionTypeConverter() {
        super(SubmissionType.class);
    }
}
