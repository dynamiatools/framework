package tools.dynamia.actions;

public interface ActionFilter {

    void beforeActionPerformed(ActionEvent evt);

    void afterActionPerformed(ActionEvent evt);
}
