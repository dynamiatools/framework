package tools.dynamia.ui;

import tools.dynamia.integration.sterotypes.Component;

import java.util.HashMap;

@Component("_lc")
public class UILabelsMap extends HashMap<String, String> {


    public static final String CLASSIER = "* UI Labels";

    @Override
    public String get(Object key) {
        return UIMessages.getLocalizedMessage(key.toString(), CLASSIER);
    }
}
