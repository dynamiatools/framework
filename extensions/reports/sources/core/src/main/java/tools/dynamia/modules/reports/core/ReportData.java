package tools.dynamia.modules.reports.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.dynamia.domain.jdbc.JdbcDataSet;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportData {

    private static final Pattern AS_ALIAS = Pattern.compile("(?is).+\\s+as\\s+([a-z_][a-z0-9_]*)\\s*$");
    private static final Pattern IMPLICIT_ALIAS = Pattern.compile("(?is).+[\\s)]([a-z_][a-z0-9_]*)\\s*$");
    private static final Pattern PATH = Pattern.compile("(?is)^[a-z_][a-z0-9_]*(\\.[a-z_][a-z0-9_]*)+$");

    @JsonIgnore
    private Report report;
    private List<ReportDataEntry> entries = new ArrayList<>();
    @JsonIgnore
    private List<String> fieldNames;
    private boolean truncated;

    public static ReportData build(Report report, JdbcDataSet dataSet) {
        ReportData data = new ReportData();
        data.report = report;
        List<String> fields = report.isAutofields() ? dataSet.getColumnsLabels() : report.getFields().stream().map(ReportField::getName).toList();
        data.fieldNames = fields;
        dataSet.getRows().forEach(row -> {
            data.entries.add(ReportDataEntry.build(fields, row));
        });
        dataSet.close();

        return data;
    }

    @SuppressWarnings("rawtypes")
    public static ReportData build(Report report, Collection collection) {
        ReportData data = new ReportData();
        data.report = report;
        if (report.isAutofields()) {
            Object first = collection.isEmpty() ? null : collection.iterator().next();
            if (first instanceof Object[] firstRow) {
                List<String> names = selectAliases(report.getQueryScript(), firstRow.length);
                data.fieldNames = names;
                collection.forEach(obj -> data.entries.add(ReportDataEntry.build(names, (Object[]) obj)));
            } else {
                data.fieldNames = Collections.singletonList("Result");
                collection.forEach(obj -> data.entries.add(new ReportDataEntry(String.valueOf(obj), obj, true)));
            }
        } else if (!report.getFields().isEmpty()) {
            List<String> fields = report.getFields().stream().map(ReportField::getName).toList();
            data.fieldNames = fields;
            collection.forEach(obj -> {
                if (obj instanceof Object[] row) {
                    data.entries.add(ReportDataEntry.build(fields, row));
                } else {
                    data.entries.add(ReportDataEntry.build(fields, obj));
                }
            });
        }

        return data;
    }

    /**
     * Derives the column names of a JPQL select with several items, from the aliases (<code>as total</code> or
     * <code>count(o) total</code>) or the last segment of a path (<code>o.name</code> becomes <code>name</code>).
     * Items without a usable name are called <code>col1</code>, <code>col2</code>... Names are always unique.
     *
     * @param jpql  the JPQL query
     * @param count the number of items in each result row
     * @return exactly {@code count} column names
     */
    static List<String> selectAliases(String jpql, int count) {
        List<String> items = selectItems(jpql);
        List<String> names = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (int i = 0; i < count; i++) {
            String name = items.size() == count ? aliasOf(items.get(i)) : null;
            if (name == null || !used.add(name.toLowerCase())) {
                name = "col" + (i + 1);
                used.add(name);
            }
            names.add(name);
        }
        return names;
    }

    private static String aliasOf(String item) {
        String text = item.strip();
        Matcher matcher = AS_ALIAS.matcher(text);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        if (PATH.matcher(text).matches()) {
            return text.substring(text.lastIndexOf('.') + 1);
        }
        matcher = IMPLICIT_ALIAS.matcher(text);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private static List<String> selectItems(String jpql) {
        List<String> items = new ArrayList<>();
        if (jpql == null) {
            return items;
        }
        String text = jpql.replaceAll("\\s+", " ").strip();
        if (!text.regionMatches(true, 0, "select ", 0, 7)) {
            return items;
        }
        int start = 7;
        if (text.regionMatches(true, start, "distinct ", 0, 9)) {
            start += 9;
        }
        int depth = 0;
        int itemStart = start;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && c == ',') {
                items.add(text.substring(itemStart, i));
                itemStart = i + 1;
            } else if (depth == 0 && c == ' ' && text.regionMatches(true, i, " from ", 0, 6)) {
                items.add(text.substring(itemStart, i));
                return items;
            }
        }
        items.add(text.substring(itemStart));
        return items;
    }

    public void sort(String field, boolean ascending) {
        entries.sort((e1, e2) -> ascending ? e1.compareTo(field, e2) : e2.compareTo(field, e1));
    }

    public Report getReport() {
        return report;
    }

    public List<ReportDataEntry> getEntries() {
        return entries;
    }

    public int getSize() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    /**
     * Tells if the result was cut because it reached the maximum number of rows
     * ({@code dynamia.reports.max-rows}).
     *
     * @return true if there are more rows than the returned entries
     */
    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }
}
