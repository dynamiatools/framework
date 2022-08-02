package tools.dynamia.actions;

public interface ActionFilter {

    default void beforeActionPerformed(ActionEvent evt){}

    default void afterActionPerformed(ActionEvent evt){}
}
