package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum GroupStatus implements PgEnum {
    PLANNED("planned"),
    OPEN("open"),
    FULL("full"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String dbValue;

    GroupStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class GroupStatusConverter extends AbstractPgEnumConverter<GroupStatus> {
    GroupStatusConverter() {
        super(GroupStatus.class);
    }
}
