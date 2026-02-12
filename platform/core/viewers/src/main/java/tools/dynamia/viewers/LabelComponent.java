package tools.dynamia.viewers;

import java.io.Serializable;

/**
 * Interface that represent a generic label
 */
public interface LabelComponent extends Serializable {

    String getValue();

    void setValue(String value);

    void setTooltiptext(String tooltiptext);

    String getTooltiptext();

    void setSclass(String sclass);

    String getSclass();

    void setVisible(boolean visible);

    boolean isVisible();
}
