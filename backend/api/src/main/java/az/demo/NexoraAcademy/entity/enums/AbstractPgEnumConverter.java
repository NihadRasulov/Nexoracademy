package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.AttributeConverter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractPgEnumConverter<E extends Enum<E> & PgEnum> implements AttributeConverter<E, String> {

    private final Map<String, E> byDbValue;

    protected AbstractPgEnumConverter(Class<E> enumType) {
        this.byDbValue = Arrays.stream(enumType.getEnumConstants())
                .collect(Collectors.toMap(PgEnum::dbValue, Function.identity()));
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.dbValue();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return dbData == null ? null : byDbValue.get(dbData);
    }
}
