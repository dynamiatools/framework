package tools.dynamia.commons;


import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom deserializer to handle legacy date formats in JSON. It attempts to parse the date string using two patterns:
 * 1. "yyyy-MM-dd HH:mm:ss" - for date-time strings that include both date and time.
 * 2. "yyyy-MM-dd" - for date-only strings.
 * @author Mario A. Serrano Leones
 */
public class LegacyDateDeserializer extends StdDeserializer<LocalDate> {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public LegacyDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) {

        String value = p.getString();

        if (value == null || value.isBlank()) {
            return null;
        }


        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)).toLocalDate();
        } catch (Exception e) {

        }


        return LocalDate.parse(value, DateTimeFormatter.ofPattern(DATE_PATTERN));


    }
}
