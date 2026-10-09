package tools.dynamia.modules.reports.core.api;

import tools.dynamia.modules.reports.core.ReportData;
import tools.dynamia.modules.reports.core.ReportDataEntry;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns the values of a result into plain JSON-friendly values: numbers, booleans and text. Dates become ISO strings,
 * enums their name, and any other object its {@code toString()}, so the API never serializes entities.
 */
public final class ReportValues {

    private ReportValues() {
    }

    public static Object simplify(Object value) {
        if (value == null || value instanceof Number || value instanceof Boolean || value instanceof String) {
            return value;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().toString();
        }
        if (value instanceof java.sql.Time time) {
            return time.toLocalTime().toString();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant().toString();
        }
        if (value instanceof LocalDate || value instanceof LocalDateTime || value instanceof LocalTime
                || value instanceof Instant || value instanceof OffsetDateTime || value instanceof ZonedDateTime) {
            return value.toString();
        }
        if (value instanceof Enum<?> e) {
            return e.name();
        }
        return value.toString();
    }

    /**
     * The values of an entry by column name, with simplified values and the order of the columns.
     */
    public static Map<String, Object> row(ReportDataEntry entry, ReportData data) {
        Map<String, Object> row = new LinkedHashMap<>();
        List<String> names = data.getFieldNames();
        if (entry.isSingleValue()) {
            row.put(names != null && !names.isEmpty() ? names.get(0) : "Result", simplify(entry.getValue()));
            return row;
        }
        if (names == null) {
            entry.getValues().forEach((k, v) -> row.put(k, simplify(v)));
            return row;
        }
        for (String name : names) {
            row.put(name, simplify(entry.getValues().get(name)));
        }
        return row;
    }
}
