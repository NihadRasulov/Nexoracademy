package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum EnrollmentStatus implements PgEnum {
    WAITLISTED("waitlisted"),
    HELD("held"),
    PENDING_PAYMENT("pending_payment"),
    CONFIRMED("confirmed"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    REFUNDED("refunded");

    private final String dbValue;

    EnrollmentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class EnrollmentStatusConverter extends AbstractPgEnumConverter<EnrollmentStatus> {
    EnrollmentStatusConverter() {
        super(EnrollmentStatus.class);
    }
}
