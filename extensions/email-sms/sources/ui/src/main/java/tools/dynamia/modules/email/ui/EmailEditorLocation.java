package tools.dynamia.modules.email.ui;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Where the block editor page is served from and how to open it for a template. The page is built from the
 * {@code email-editor-vue} package by the {@code editor} Maven profile of this module and ends up under
 * {@code META-INF/resources/dynamia-email/editor/}, which the web application serves at
 * {@code /dynamia-email/editor/}.
 */
public final class EmailEditorLocation {

    /** URL path of the editor page, relative to the application context path. */
    public static final String PATH = "/dynamia-email/editor/index.html";
    private static final String RESOURCE = "/META-INF/resources/dynamia-email/editor/index.html";

    private EmailEditorLocation() {
    }

    /**
     * @return true when this build contains the editor page (it is absent unless built with {@code -Peditor})
     */
    public static boolean isInstalled() {
        return EmailEditorLocation.class.getResource(RESOURCE) != null;
    }

    /**
     * @param contextPath the context path of the application, may be null or empty
     * @param templateId  the id of a saved template
     * @param language    the language of the user, for example {@code es}; null for the browser language
     * @return the URL that opens the editor on that template
     */
    public static String url(String contextPath, Long templateId, String language) {
        var base = contextPath == null || contextPath.equals("/") ? "" : contextPath;
        var url = new StringBuilder(base).append(PATH).append("?id=").append(templateId);
        if (language != null && !language.isBlank()) {
            url.append("&locale=").append(URLEncoder.encode(language, StandardCharsets.UTF_8));
        }
        return url.toString();
    }
}
