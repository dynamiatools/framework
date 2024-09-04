package tools.dynamia.viewers;

import java.io.Serializable;

public interface IFormFieldComponent<C>  extends Serializable {

    String getFieldName();

    C getInputComponent();

    LabelComponent getLabel();

    C getInputComponentAlt();

    void updateLabel(String label);

    void remove();

    void hide();

    void show();


}
