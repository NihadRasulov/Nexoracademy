package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum UserRole implements PgEnum {
    GUEST("guest"),
    STUDENT("student"),
    SALES_CRM("sales_crm"),
    CONTENT_MANAGER("content_manager"),
    ADMIN("admin"),
    SYSTEM_ADMIN("system_admin");

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
