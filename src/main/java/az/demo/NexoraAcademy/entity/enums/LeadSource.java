package az.demo.NexoraAcademy.entity.enums;

import jakarta.persistence.Converter;

public enum LeadSource implements PgEnum {
    CONTACT_FORM("contact_form"),
    DEMO_REQUEST("demo_request"),
    SYLLABUS_DOWNLOAD("syllabus_download"),
    NEWSLETTER("newsletter"),
    CHATBOT("chatbot"),
    REFERRAL("referral");

    private final String dbValue;

    LeadSource(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String dbValue() {
        return dbValue;
    }
}

@Converter(autoApply = true)
class LeadSourceConverter extends AbstractPgEnumConverter<LeadSource> {
    LeadSourceConverter() {
        super(LeadSource.class);
    }
}
