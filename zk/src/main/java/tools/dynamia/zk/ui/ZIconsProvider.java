package tools.dynamia.zk.ui;

import tools.dynamia.ui.icons.Icon;
import tools.dynamia.ui.icons.InstallIcons;

/**
 * Disabled by default. To enable, instance this in a Spring configuration class using @{@link org.springframework.context.annotation.Bean}
 * </pre>
 */
public class ZIconsProvider extends FontAwesomeIconsProvider {

    @Override
    protected String getIconsPath() {
        return "/META-INF/dynamia/zk-icons.properties";
    }

    @Override
    protected String getIconsPrefix() {
        return "z-icon-";
    }

    @Override
    protected Icon newIcon(String name, String internalName) {
        return new FAIcon(name, getIconsPrefix() + internalName);
    }


}
