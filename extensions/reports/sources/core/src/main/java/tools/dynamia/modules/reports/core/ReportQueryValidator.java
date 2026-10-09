package tools.dynamia.modules.reports.core;

import tools.dynamia.domain.ValidationError;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates the queries and condition fragments stored in report definitions. Reports must only read data: a query is
 * accepted when it is a single {@code SELECT} (or {@code WITH}, or JPQL {@code FROM}) statement without data
 * modification, DDL or other dangerous keywords. This is a defense in depth check that gives early feedback when a
 * report is saved; execution additionally runs on a read-only connection with timeout and row limits.
 * <p>
 * String literals, quoted identifiers and comments are ignored when looking for keywords, so
 * {@code where status = 'deleted'} is accepted.
 */
public final class ReportQueryValidator {

    private static final Set<String> SQL_START = Set.of("select", "with");
    private static final Set<String> JPQL_START = Set.of("select", "from");

    private static final Pattern FORBIDDEN = Pattern.compile("\\b(insert|update|delete|drop|truncate|alter|create|grant|revoke|merge|"
            + "call|exec|execute|into|pragma|attach|detach|vacuum|shutdown|backup|restore|declare)\\b");
    private static final Pattern FIRST_WORD = Pattern.compile("^\\s*\\(*\\s*([a-z]+)");

    private ReportQueryValidator() {
    }

    /**
     * Validates a full report query.
     *
     * @param query the SQL or JPQL query
     * @param lang  the query language, {@code sql} or {@code jpql}
     * @return the same query
     * @throws ValidationError if the query is empty or not a read-only single statement
     */
    public static String validateQuery(String query, String lang) {
        if (query == null || query.isBlank()) {
            throw new ValidationError("Query is empty");
        }
        String clean = sanitize(query).strip();
        while (clean.endsWith(";")) {
            clean = clean.substring(0, clean.length() - 1).stripTrailing();
        }
        if (clean.contains(";")) {
            throw new ValidationError("Only one statement is allowed in a report query");
        }

        var matcher = FIRST_WORD.matcher(clean);
        Set<String> allowed = "jpql".equalsIgnoreCase(lang) ? JPQL_START : SQL_START;
        if (!matcher.find() || !allowed.contains(matcher.group(1))) {
            throw new ValidationError("Report queries must start with " + String.join(" or ", allowed).toUpperCase(Locale.ROOT));
        }
        checkForbidden(clean);
        return query;
    }

    /**
     * Validates a condition fragment, for example the condition of a report filter.
     *
     * @param fragment the SQL or JPQL fragment
     * @return the same fragment
     * @throws ValidationError if the fragment is not a read-only expression
     */
    public static String validateFragment(String fragment) {
        if (fragment == null || fragment.isBlank()) {
            return fragment;
        }
        String clean = sanitize(fragment);
        if (clean.contains(";")) {
            throw new ValidationError("Statement separators are not allowed in report conditions");
        }
        checkForbidden(clean);
        return fragment;
    }

    private static void checkForbidden(String clean) {
        var matcher = FORBIDDEN.matcher(clean);
        if (matcher.find()) {
            throw new ValidationError("Keyword not allowed in reports: " + matcher.group(1).toUpperCase(Locale.ROOT));
        }
    }

    /**
     * Lowercases the text and removes string literals, quoted identifiers and comments. Everything removed is replaced
     * by a space, so the remaining text only has code tokens.
     */
    static String sanitize(String text) {
        StringBuilder out = new StringBuilder(text.length());
        int length = text.length();
        int i = 0;
        while (i < length) {
            char c = text.charAt(i);
            char next = i + 1 < length ? text.charAt(i + 1) : '\0';
            if (c == '\'' || c == '"' || c == '`') {
                i = skipQuoted(text, i, c);
                out.append(' ');
            } else if (c == '/' && next == '*') {
                int end = text.indexOf("*/", i + 2);
                String body = end < 0 ? text.substring(i + 2) : text.substring(i + 2, end);
                if (body.startsWith("!") || body.startsWith("M!")) {
                    throw new ValidationError("Executable comments are not allowed in report queries");
                }
                i = end < 0 ? length : end + 2;
                out.append(' ');
            } else if (c == '-' && next == '-' && isLineComment(text, i)) {
                int end = text.indexOf('\n', i);
                i = end < 0 ? length : end;
                out.append(' ');
            } else {
                out.append(Character.toLowerCase(c));
                i++;
            }
        }
        return out.toString();
    }

    /**
     * {@code --} always starts a comment in most databases, but MySQL requires whitespace after it. When it is not
     * followed by whitespace the text is kept (and checked) because some databases would execute it.
     */
    private static boolean isLineComment(String text, int index) {
        int after = index + 2;
        return after >= text.length() || Character.isWhitespace(text.charAt(after));
    }

    private static int skipQuoted(String text, int start, char quote) {
        int i = start + 1;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\\' && quote == '\'' && i + 1 < text.length()) {
                i += 2;
            } else if (c == quote) {
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
