package tools.dynamia.ui;

public interface ButtonComponent extends UIComponent {

    void setLabel(String label);

    String getLabel();

    void setIcon(String icon);

    String getIcon();

    void onClick(EventCallback onClick);
}
