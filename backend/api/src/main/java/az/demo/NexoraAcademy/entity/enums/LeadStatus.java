package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum LeadStatus implements PgEnum {
    NEW("new"),
    CONTACTED("contacted"),
    QUALIFIED("qualified"),
    CONVERTED("converted"),
    LOST("lost"),
    DISQUALIFIED("disqualified");

    private final String dbValue;

    LeadStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class LeadStatusConverter extends AbstractPgEnumConverter<LeadStatus> {
    LeadStatusConverter() {
        super(LeadStatus.class);
    }
}
