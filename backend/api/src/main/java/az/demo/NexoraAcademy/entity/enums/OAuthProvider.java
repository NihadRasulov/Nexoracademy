package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum OAuthProvider implements PgEnum {
    GOOGLE("google"),
    GITHUB("github"),
    LINKEDIN("linkedin");

    private final String dbValue;

    OAuthProvider(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class OAuthProviderConverter extends AbstractPgEnumConverter<OAuthProvider> {
    OAuthProviderConverter() {
        super(OAuthProvider.class);
    }
}
