package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum NotificationStatus implements PgEnum {
    QUEUED("queued"),
    SENT("sent"),
    FAILED("failed"),
    READ("read");

    private final String dbValue;

    NotificationStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class NotificationStatusConverter extends AbstractPgEnumConverter<NotificationStatus> {
    NotificationStatusConverter() {
        super(NotificationStatus.class);
    }
}
