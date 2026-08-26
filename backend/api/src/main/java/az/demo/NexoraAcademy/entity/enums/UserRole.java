package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum UserRole implements PgEnum {
    ADMIN("admin");

    private final String dbValue;

    UserRole(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class UserRoleConverter extends AbstractPgEnumConverter<UserRole> {
    UserRoleConverter() {
        super(UserRole.class);
    }
}
