package tools.dynamia.navigation;

/**
 * Page that should load from external source or path
 */
public class ExternalPage extends Page {

    public ExternalPage() {
    }

    public ExternalPage(String id, String name, String path) {
        super(id, name, path);
    }

    public ExternalPage(String id, String name, String path, boolean closeable) {
        super(id, name, path, closeable);
    }


    @Override
    public boolean isHtml() {
        return true;
    }
}
