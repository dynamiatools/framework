package mybookstore.listeners;

import mybookstore.domain.Invoice;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.sterotypes.Listener;

@Listener
public class InvoiceCrudListener extends CrudServiceListenerAdapter<Invoice> {

    @Override
    public void beforeUpdate(Invoice entity) {
        entity.calcTotal();
    }

    @Override
    public void beforeCreate(Invoice entity) {
        entity.calcTotal();
    }
}
