package tools.dynamia.viewers;

public interface GenericFormFieldComponent<C> {

    String getFieldName();

    C getInputComponent();

    GenericLabel getLabel();

    C getInputComponentAlt();

    void updateLabel(String label);

    void remove();

    void hide();

    void show();


}
