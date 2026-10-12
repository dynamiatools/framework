package tools.dynamia.modules.reports.core.api;

import tools.dynamia.commons.StringUtils;
import tools.dynamia.modules.reports.api.v2.ReportColumn;
import tools.dynamia.modules.reports.core.ReportData;
import tools.dynamia.modules.reports.core.ReportDataEntry;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportField;
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.domain.enums.TextAlign;

import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Builds the columns of a report: the declared fields, or the columns of the query when the report has automatic
 * fields (labels and types are then taken from the matching field if there is one, or inferred).
 */
public final class ReportColumns {

    private ReportColumns() {
    }

    /**
     * Declared columns, without running the report.
     */
    public static List<ReportColumn> declared(Report report) {
        if (report.isAutofields()) {
            return List.of();
        }
        return report.getFields().stream().sorted(Comparator.comparingInt(ReportField::getOrder))
                .map(ReportColumns::of).toList();
    }

    /**
     * Columns of a result.
     */
    public static List<ReportColumn> of(Report report, ReportData data) {
        if (!report.isAutofields()) {
            return declared(report);
        }
        List<ReportColumn> columns = new ArrayList<>();
        if (data.getFieldNames() == null) {
            return columns;
        }
        for (String name : data.getFieldNames()) {
            ReportField field = report.findField(name);
            if (field != null) {
                columns.add(of(field));
            } else {
                DataType type = infer(name, data);
                columns.add(new ReportColumn(name, label(name),
                        type.name(), (type == DataType.NUMBER || type == DataType.CURRENCY ? TextAlign.RIGHT : TextAlign.LEFT).name(),
                        null, null, false));
            }
        }
        return columns;
    }

    /**
     * Readable label for a column name: {@code TOTAL_SALES}, {@code total_sales} and {@code totalSales} all become
     * {@code Total Sales}; dots separate words too.
     */
    static String label(String name) {
        String text = name.replace('_', ' ').replace('.', ' ').trim();
        boolean hasLower = text.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = text.chars().anyMatch(Character::isUpperCase);
        if (hasUpper && !hasLower) {
            text = text.toLowerCase();
        } else if (hasLower && hasUpper) {
            text = StringUtils.addSpaceBetweenWords(text);
        }
        return StringUtils.capitalizeAllWords(text.replaceAll("\\s+", " "));
    }

    private static ReportColumn of(ReportField field) {
        DataType type = field.getDataType() != null ? field.getDataType() : DataType.TEXT;
        TextAlign align = field.getAlign() != null ? field.getAlign() : TextAlign.LEFT;
        return new ReportColumn(field.getName(), field.getLabel(), type.name(), align.name(), field.getFormat(),
                field.getWidth(), field.isUpperCase());
    }

    private static DataType infer(String name, ReportData data) {
        for (ReportDataEntry entry : data.getEntries()) {
            Object value = entry.isSingleValue() ? entry.getValue() : entry.getValues().get(name);
            if (value == null) {
                continue;
            }
            if (value instanceof Boolean) {
                return DataType.BOOLEAN;
            } else if (value instanceof Number) {
                return DataType.NUMBER;
            } else if (value instanceof java.sql.Timestamp) {
                return DataType.DATE_TIME;
            } else if (value instanceof java.sql.Time) {
                return DataType.TIME;
            } else if (value instanceof Date || value instanceof TemporalAccessor) {
                return DataType.DATE;
            }
            return DataType.TEXT;
        }
        return DataType.TEXT;
    }
}
