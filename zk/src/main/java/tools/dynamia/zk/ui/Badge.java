package tools.dynamia.zk.ui;

import org.zkoss.zhtml.Text;
import org.zkoss.zul.Span;
import tools.dynamia.commons.StringUtils;

public class Badge extends Span {

    private String value;

    public Badge() {
        init();
    }

    public Badge(String value) {
        this.value = value;
        init();
    }

    private void init() {
        getChildren().clear();
        setSclass(null);
        if (value != null && !value.isBlank()) {
            String sclass = StringUtils.simplifiedString(value.toLowerCase());
            setSclass("badge badge-" + sclass);
            appendChild(new Text(value));
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        init();
    }
}
