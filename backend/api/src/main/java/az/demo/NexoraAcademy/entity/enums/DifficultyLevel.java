package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum DifficultyLevel implements PgEnum {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    private final String dbValue;

    DifficultyLevel(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class DifficultyLevelConverter extends AbstractPgEnumConverter<DifficultyLevel> {
    DifficultyLevelConverter() {
        super(DifficultyLevel.class);
    }
}
