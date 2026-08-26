package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum DeliveryFormat implements PgEnum {
    ONLINE("online"),
    OFFLINE("offline"),
    HYBRID("hybrid");

    private final String dbValue;

    DeliveryFormat(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class DeliveryFormatConverter extends AbstractPgEnumConverter<DeliveryFormat> {
    DeliveryFormatConverter() {
        super(DeliveryFormat.class);
    }
}
