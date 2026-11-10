package tools.dynamia.modules.email.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Serves the block editor page at {@link EmailEditorLocation#PATH}. The page is the {@code index.html} packaged in this
 * module; its scripts and styles are relative ({@code ./assets/...}), so the response gets a {@code <base>} that points
 * at the folder they are served from and the clean URL (no {@code index.html}) works.
 */
@Controller
public class EmailEditorController {

    private static final String RESOURCE = "/META-INF/resources/dynamia-email/editor/index.html";
    private static final String ASSETS_FOLDER = "/dynamia-email/editor/";

    @GetMapping(value = {EmailEditorLocation.PATH, EmailEditorLocation.PATH + "/", EmailEditorLocation.PATH + "/index.html"},
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> editor(HttpServletRequest request) throws IOException {
        if (!EmailEditorLocation.isInstalled()) {
            return ResponseEntity.notFound().build();
        }
        String html;
        try (var in = EmailEditorController.class.getResourceAsStream(RESOURCE)) {
            html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.TEXT_HTML)
                .body(withBase(html, request.getContextPath()));
    }

    /**
     * @param html        the editor page
     * @param contextPath the context path of the application, may be null or empty
     * @return the page with a {@code <base>} for its relative assets, right after {@code <head>}
     */
    static String withBase(String html, String contextPath) {
        var base = contextPath == null || contextPath.equals("/") ? "" : contextPath;
        var tag = "<base href=\"" + base + ASSETS_FOLDER + "\" />";
        var head = html.indexOf("<head>");
        return head < 0 ? html : html.substring(0, head + 6) + tag + html.substring(head + 6);
    }
}
