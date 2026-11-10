package tools.dynamia.modules.email.ui;

import org.springframework.core.env.Environment;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Window;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.UINavigation;
import tools.dynamia.zk.util.ZKUtil;

/**
 * Shows the block editor inside a ZK modal window with an {@code iframe}, so the user designs a template without
 * leaving the application. The frame is same-origin, so the editor keeps using the session of the user.
 * <p>
 * Set {@code dynamia.email.editor.open-in=tab} to open the editor in a new browser tab instead (the default is
 * {@code window}).
 */
public final class EmailEditorWindow {

    /** Property that chooses where the editor opens: {@code window} (default) or {@code tab}. */
    public static final String OPEN_IN_PROPERTY = "dynamia.email.editor.open-in";

    private EmailEditorWindow() {
    }

    /**
     * Opens the editor on a template, in a window or a new tab depending on {@link #OPEN_IN_PROPERTY}.
     *
     * @param templateId the id of a saved template
     * @param title      the title of the window
     */
    public static void open(Long templateId, String title) {
        var url = EmailEditorLocation.url(contextPath(), templateId, Locales.getCurrent().getLanguage());
        if (opensInTab(property())) {
            UINavigation.openInNewWindow(url);
        } else {
            show(url, title);
        }
    }

    /**
     * @param url   the editor URL
     * @param title the title of the window
     * @return the modal window that holds the editor, nearly full screen
     */
    public static Window show(String url, String title) {
        var window = ZKUtil.createWindow(null, title, null);
        window.setWidth("96%");
        window.setHeight("96%");
        window.setSizable(true);
        window.setMaximizable(true);
        window.appendChild(frame(url));
        window.doModal();
        return window;
    }

    /**
     * @param url the page to show
     * @return a frame that fills its parent
     */
    static Iframe frame(String url) {
        var frame = new Iframe(url);
        frame.setHflex("1");
        frame.setVflex("1");
        return frame;
    }

    /**
     * @param openIn value of {@link #OPEN_IN_PROPERTY}, may be null
     * @return true when the editor must open in a new browser tab
     */
    static boolean opensInTab(String openIn) {
        return openIn != null && openIn.trim().equalsIgnoreCase("tab");
    }

    private static String property() {
        var env = Containers.get().findObject(Environment.class);
        return env == null ? null : env.getProperty(OPEN_IN_PROPERTY);
    }

    private static String contextPath() {
        var execution = Executions.getCurrent();
        return execution == null ? "" : execution.getContextPath();
    }
}
