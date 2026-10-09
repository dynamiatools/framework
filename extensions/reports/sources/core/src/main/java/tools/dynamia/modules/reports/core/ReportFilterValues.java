package tools.dynamia.modules.reports.core;

import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportFilter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Converts the raw filter values received by the REST API (strings or JSON values) into the types the report queries
 * expect, and validates required filters. Shared by every REST endpoint of the module.
 */
public final class ReportFilterValues {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";

    private ReportFilterValues() {
    }

    /**
     * Converts the raw values of the filters defined by the report. Values of names the report does not define are
     * ignored, and so are null or blank values.
     *
     * @param report the report
     * @param raw    raw values by filter name
     * @return the filters with converted values
     * @throws ValidationError naming the filter when a value cannot be converted
     */
    public static ReportFilters load(Report report, Map<String, ?> raw) {
        ReportFilters loaded = new ReportFilters();
        if (raw == null) {
            return loaded;
        }
        for (ReportFilter filter : report.getFilters()) {
            Object value = raw.get(filter.getName());
            if (value == null || value.toString().isBlank()) {
                continue;
            }
            loaded.add(filter, convert(filter, value));
        }
        return loaded;
    }

    /**
     * @throws ValidationError if a required filter has no value
     */
    public static void validateRequired(Report report, ReportFilters loaded) {
        for (ReportFilter filter : report.getRequiredFilters()) {
            if (!loaded.exists(filter.getName())) {
                throw new ValidationError("Filter Required [" + filter.getName() + "] of type [" + filter.getDataType() + "] "
                        + expectedFormat(filter));
            }
        }
    }

    /**
     * Converts one raw value to the type of the filter.
     *
     * @param filter the filter definition
     * @param value  the raw value
     * @return the converted value, never null
     * @throws ValidationError if the value cannot be converted
     */
    public static Object convert(ReportFilter filter, Object value) {
        try {
            Object converted = switch (filter.getDataType()) {
                case BOOLEAN -> parseBoolean(value);
                case ENUM -> convertToEnum(filter.getEnumClassName(), value);
                case NUMBER, CURRENCY -> new BigDecimal(value.toString().trim());
                case ENTITY -> convertToEntity(value);
                case DATE -> DateTimeUtils.parse(value.toString(), DATE_FORMAT);
                case DATE_TIME -> DateTimeUtils.parse(value.toString(), DATE_TIME_FORMAT);
                case TIME -> DateTimeUtils.parse(value.toString(), TIME_FORMAT);
                case TEXT -> value.toString();
            };
            if (converted == null) {
                throw new IllegalArgumentException("Cannot convert value");
            }
            return converted;
        } catch (Exception e) {
            throw new ValidationError("Invalid value for filter [" + filter.getName() + "] of type [" + filter.getDataType() + "] "
                    + expectedFormat(filter));
        }
    }

    /**
     * Describes the value a filter expects, for error messages.
     *
     * @param f the filter
     * @return a short description such as {@code (a number)}
     */
    public static String expectedFormat(ReportFilter f) {
        return switch (f.getDataType()) {
            case BOOLEAN -> "(true or false)";
            case CURRENCY, NUMBER -> "(a number)";
            case ENUM -> "(one of " + Arrays.toString(enumValues(f.getEnumClassName())) + ")";
            case ENTITY -> "(id)";
            case DATE -> "(with format " + DATE_FORMAT + ")";
            case DATE_TIME -> "(with format " + DATE_TIME_FORMAT + ")";
            case TIME -> "(with format " + TIME_FORMAT + ")";
            case TEXT -> "";
        };
    }

    private static Boolean parseBoolean(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if ("true".equalsIgnoreCase(value.toString())) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(value.toString())) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Not a boolean");
    }

    private static Object convertToEntity(Object value) {
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return value;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convertToEnum(String enumClassName, Object value) throws ClassNotFoundException {
        return Enum.valueOf((Class<Enum>) Class.forName(enumClassName), value.toString());
    }

    private static Enum<?>[] enumValues(String enumClassName) {
        try {
            return (Enum<?>[]) Class.forName(enumClassName).getEnumConstants();
        } catch (Exception e) {
            return new Enum[]{};
        }
    }

    /**
     * @return the names of the enum constants, empty when the class is not an enum
     */
    public static List<String> enumNames(String enumClassName) {
        return Arrays.stream(enumValues(enumClassName)).map(Enum::name).toList();
    }
}
