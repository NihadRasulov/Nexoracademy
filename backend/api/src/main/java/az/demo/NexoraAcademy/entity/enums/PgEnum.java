package az.demo.NexoraAcademy.entity.enums;

/**
 * Implemented by enums backed by a native PostgreSQL ENUM type (defined in
 * V2__create_enum_types.sql, "platform" schema). dbValue() is the exact
 * lower_snake_case label stored in Postgres — decoupled from the Java
 * constant name so labels like "new" (a Java keyword) stay representable.
 */
public interface PgEnum {
    String dbValue();
}
