package tools.dynamia.zk;

import org.zkoss.util.resource.impl.LabelLoader;
import org.zkoss.util.resource.impl.LabelLoaderImpl;
import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.integration.sterotypes.Provider;

import java.util.Locale;

@Provider
public class ZKLocalizedMessageProvider implements LocalizedMessagesProvider {

    private final LabelLoader loader;

    public ZKLocalizedMessageProvider() {
        this.loader = new LabelLoaderImpl();
    }

    @Override
    public String getMessage(String key, String classifier, Locale locale, String defaultValue) {
        var label = loader.getLabel(locale, key);
        if (label == null && defaultValue != null) {
            label = defaultValue;
        }
        return label;
    }
}
