package tools.dynamia.viewers;

/**
 * Interface that represent a generic label
 */
public interface GenericLabel {

    String getValue();

    void setValue(String value);

    void setTooltiptext(String tooltiptext);

    String getTooltiptext();

    void setSclass(String sclass);

    String getSclass();

    void setVisible(boolean visible);

    boolean isVisible();
}
