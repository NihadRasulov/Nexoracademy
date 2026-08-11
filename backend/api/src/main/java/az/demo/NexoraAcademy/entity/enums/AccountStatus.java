package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum AccountStatus implements PgEnum {
    PENDING_VERIFICATION("pending_verification"),
    ACTIVE("active"),
    SUSPENDED("suspended"),
    DEACTIVATED("deactivated"),
    BANNED("banned");

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
