/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package tools.dynamia.ui.files;

import java.util.Locale;
import java.util.Set;

/**
 * Decides which content types a server may ask the browser to display inline instead of saving them.
 *
 * <p>Only types that cannot run script are allowed: {@code text/html}, {@code image/svg+xml} and anything unknown are
 * always served as an attachment. The same policy is applied when a file is published (replay), when it is served
 * ({@code TransfersController}) and by the front end handlers, so a client can never turn an active document into an
 * inline one.
 *
 * <pre>{@code
 * InlinePolicy.allows("application/pdf; charset=binary"); // true
 * InlinePolicy.allows("text/html");                         // false
 * }</pre>
 */
public final class InlinePolicy {

    private static final Set<String> ALLOWED = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp",
            "text/plain");

    private InlinePolicy() {
    }

    /**
     * Tells whether a content type may be displayed inline.
     *
     * @param contentType the MIME type, with or without parameters; may be {@code null}
     * @return {@code true} only for the allow listed types
     */
    public static boolean allows(String contentType) {
        return ALLOWED.contains(baseType(contentType));
    }

    /**
     * Returns the MIME type without parameters, lower case and trimmed.
     *
     * @param contentType a content type; may be {@code null}
     * @return the base type, or an empty string when {@code contentType} is {@code null}
     */
    public static String baseType(String contentType) {
        if (contentType == null) {
            return "";
        }
        int semicolon = contentType.indexOf(';');
        String base = semicolon < 0 ? contentType : contentType.substring(0, semicolon);
        return base.trim().toLowerCase(Locale.ROOT);
    }
}
