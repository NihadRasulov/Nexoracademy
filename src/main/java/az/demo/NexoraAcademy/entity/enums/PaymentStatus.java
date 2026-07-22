package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum PaymentStatus implements PgEnum {
    INITIATED("initiated"),
    AUTHORIZED("authorized"),
    CAPTURED("captured"),
    FAILED("failed"),
    CANCELLED("cancelled"),
    REFUNDED("refunded"),
    PARTIALLY_REFUNDED("partially_refunded");

    private final String dbValue;

    PaymentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class PaymentStatusConverter extends AbstractPgEnumConverter<PaymentStatus> {
    PaymentStatusConverter() {
        super(PaymentStatus.class);
    }
}
