package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum SessionType implements PgEnum {
    SESSION("session"),
    PASSWORD_RESET("password_reset"),
    EMAIL_VERIFY("email_verify");

    private final String dbValue;

    SessionType(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class SessionTypeConverter extends AbstractPgEnumConverter<SessionType> {
    SessionTypeConverter() {
        super(SessionType.class);
    }
}
