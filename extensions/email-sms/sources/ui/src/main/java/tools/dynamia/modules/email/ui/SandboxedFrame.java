package tools.dynamia.modules.email.ui;

/**
 * Builds the markup of an iframe that shows an email without running anything in it: {@code sandbox} with no
 * permissions, so scripts, forms and the origin of the application are all off. The email is passed in {@code srcdoc},
 * so no request is made for it.
 */
public final class SandboxedFrame {

    private SandboxedFrame() {
    }

    /**
     * @param html the full HTML of the email
     * @return an {@code iframe} element that fills its parent and shows the email
     */
    public static String markup(String html) {
        return "<iframe sandbox=\"\" style=\"width:100%;height:100%;min-height:480px;border:0;background:#fff\" srcdoc=\""
                + escapeAttribute(html == null ? "" : html) + "\"></iframe>";
    }

    /**
     * @param value text for a double quoted attribute
     * @return the text with {@code & " < >} replaced by entities
     */
    static String escapeAttribute(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
