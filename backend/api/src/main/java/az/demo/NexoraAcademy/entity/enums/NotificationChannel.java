package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum NotificationChannel implements PgEnum {
    EMAIL("email"),
    SMS("sms"),
    IN_APP("in_app"),
    PUSH("push");

    private final String dbValue;

    NotificationChannel(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class NotificationChannelConverter extends AbstractPgEnumConverter<NotificationChannel> {
    NotificationChannelConverter() {
        super(NotificationChannel.class);
    }
}
