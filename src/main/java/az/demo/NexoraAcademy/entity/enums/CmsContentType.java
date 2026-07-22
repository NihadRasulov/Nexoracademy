package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum CmsContentType implements PgEnum {
    PAGE("page"),
    FAQ("faq"),
    SOCIAL_LINK("social_link"),
    BANNER("banner");

    private final String dbValue;

    CmsContentType(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class CmsContentTypeConverter extends AbstractPgEnumConverter<CmsContentType> {
    CmsContentTypeConverter() {
        super(CmsContentType.class);
    }
}
