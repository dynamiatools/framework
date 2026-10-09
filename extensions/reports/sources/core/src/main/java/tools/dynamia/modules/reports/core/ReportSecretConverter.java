package tools.dynamia.modules.reports.core;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter that stores secrets encrypted, see {@link ReportSecrets}.
 */
@Converter
public class ReportSecretConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return ReportSecrets.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return ReportSecrets.decrypt(dbData);
    }
}
