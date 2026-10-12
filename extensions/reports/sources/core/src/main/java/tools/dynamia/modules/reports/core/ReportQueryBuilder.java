package tools.dynamia.modules.reports.core;

import tools.dynamia.modules.reports.core.domain.ReportFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adds the conditions of the report filters to a SQL or JPQL query.
 * <p>
 * Conditions are inserted at the {@value #FILTERS_MARKER} marker when the query has one. Otherwise they are inserted
 * before the first top level {@code GROUP BY}, {@code HAVING}, {@code ORDER BY}, {@code LIMIT}, set operator or
 * {@code FOR} clause, or at the end of the query. A {@code WHERE} keyword is detected case-insensitively and only at
 * the level where the conditions are inserted, so subqueries, strings and identifiers like {@code somewhere} do not
 * confuse the builder.
 */
public final class ReportQueryBuilder {

    /**
     * Marker that sets explicitly the position of the filter conditions.
     */
    public static final String FILTERS_MARKER = "<FILTERS>";

    private static final Set<String> TAIL_CLAUSES = Set.of("group", "having", "order", "limit", "offset", "fetch",
            "union", "intersect", "except", "for", "window");

    private ReportQueryBuilder() {
    }

    /**
     * Builds the final query.
     *
     * @param query   the query stored in the report
     * @param filters the filters with value used in this execution
     * @return the query with the filter conditions applied
     */
    public static String build(String query, ReportFilters filters) {
        String sql = query.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
        List<String> conditions = conditions(filters);
        List<Marker> markers = new ArrayList<>();
        int[] tail = {-1};
        boolean[] topWhere = {false};
        scan(sql, markers, tail, topWhere);

        if (!markers.isEmpty()) {
            StringBuilder result = new StringBuilder(sql);
            for (int i = markers.size() - 1; i >= 0; i--) {
                Marker marker = markers.get(i);
                result.replace(marker.start, marker.end, render(conditions, marker.whereSeen));
            }
            return result.toString();
        }

        if (conditions.isEmpty()) {
            return sql;
        }

        String trimmed = stripTrailingSemicolons(sql);
        int position = tail[0] >= 0 && tail[0] <= trimmed.length() ? tail[0] : trimmed.length();
        return trimmed.substring(0, position) + render(conditions, topWhere[0]) + " " + trimmed.substring(position);
    }

    private static List<String> conditions(ReportFilters filters) {
        List<String> conditions = new ArrayList<>();
        if (filters == null) {
            return conditions;
        }
        for (var option : filters.getOptions()) {
            ReportFilter filter = option.getFilter() != null ? option.getFilter() : filters.getFilter(option.getName());
            if (filter != null && filter.getCondition() != null && !filter.getCondition().isBlank()) {
                conditions.add(filter.getCondition().trim());
            }
        }
        return conditions;
    }

    private static String render(List<String> conditions, boolean whereSeen) {
        if (conditions.isEmpty()) {
            return " ";
        }
        StringBuilder sb = new StringBuilder(whereSeen ? " " : " where 1=1 ");
        for (String condition : conditions) {
            sb.append(" and ").append(condition);
        }
        return sb.toString();
    }

    private static String stripTrailingSemicolons(String sql) {
        String result = sql.stripTrailing();
        while (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1).stripTrailing();
        }
        return result;
    }

    private record Marker(int start, int end, boolean whereSeen) {
    }

    /**
     * Walks the query once, ignoring literals and comments, collecting the markers, whether a top level WHERE exists
     * and the position of the first top level tail clause.
     */
    private static void scan(String sql, List<Marker> markers, int[] tail, boolean[] topWhere) {
        int depth = 0;
        boolean[] whereAtDepth = new boolean[sql.length() + 2];
        int length = sql.length();
        int i = 0;
        while (i < length) {
            char c = sql.charAt(i);
            char next = i + 1 < length ? sql.charAt(i + 1) : '\0';
            if (c == '\'' || c == '"' || c == '`') {
                i = skipQuoted(sql, i, c);
            } else if (c == '/' && next == '*') {
                int end = sql.indexOf("*/", i + 2);
                i = end < 0 ? length : end + 2;
            } else if (c == '-' && next == '-') {
                int end = sql.indexOf('\n', i);
                i = end < 0 ? length : end;
            } else if (sql.startsWith(FILTERS_MARKER, i)) {
                markers.add(new Marker(i, i + FILTERS_MARKER.length(), whereAtDepth[depth]));
                i += FILTERS_MARKER.length();
            } else if (c == '(') {
                depth++;
                whereAtDepth[depth] = false;
                i++;
            } else if (c == ')') {
                depth = Math.max(0, depth - 1);
                i++;
            } else if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < length && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_')) {
                    i++;
                }
                String word = sql.substring(start, i).toLowerCase();
                if ("where".equals(word)) {
                    whereAtDepth[depth] = true;
                    if (depth == 0) {
                        topWhere[0] = true;
                    }
                } else if (depth == 0 && tail[0] < 0 && isTailClause(word, sql, i)) {
                    tail[0] = start;
                }
            } else {
                i++;
            }
        }
    }

    /**
     * Group and order only count when followed by BY (an entity may be called Order) and for only when followed by
     * UPDATE or SHARE.
     */
    private static boolean isTailClause(String word, String sql, int afterWord) {
        if (!TAIL_CLAUSES.contains(word)) {
            return false;
        }
        return switch (word) {
            case "group", "order" -> "by".equals(nextWord(sql, afterWord));
            case "for" -> Set.of("update", "share", "no").contains(nextWord(sql, afterWord));
            default -> true;
        };
    }

    private static String nextWord(String sql, int from) {
        int i = from;
        while (i < sql.length() && Character.isWhitespace(sql.charAt(i))) {
            i++;
        }
        int start = i;
        while (i < sql.length() && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_')) {
            i++;
        }
        return sql.substring(start, i).toLowerCase();
    }

    private static int skipQuoted(String text, int start, char quote) {
        int i = start + 1;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == quote) {
                if (i + 1 < text.length() && text.charAt(i + 1) == quote) {
                    i += 2;
                } else {
                    return i + 1;
                }
            } else {
                i++;
            }
        }
        return text.length();
    }
}
