package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum AccountStatus implements PgEnum {
    ACTIVE("active"),
    DEACTIVATED("deactivated");

    private final String dbValue;

    AccountStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class AccountStatusConverter extends AbstractPgEnumConverter<AccountStatus> {
    AccountStatusConverter() {
        super(AccountStatus.class);
    }
}
