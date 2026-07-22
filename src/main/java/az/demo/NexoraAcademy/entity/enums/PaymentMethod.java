package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum PaymentMethod implements PgEnum {
    CARD("card"),
    BANK_TRANSFER("bank_transfer"),
    INSTALLMENT("installment"),
    SCHOLARSHIP_COVERED("scholarship_covered");

    private final String dbValue;

    PaymentMethod(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class PaymentMethodConverter extends AbstractPgEnumConverter<PaymentMethod> {
    PaymentMethodConverter() {
        super(PaymentMethod.class);
    }
}
