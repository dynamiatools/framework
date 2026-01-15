package mybookstore.customizers;

import mybookstore.domain.Book;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import tools.dynamia.viewers.ViewCustomizer;
import tools.dynamia.zk.viewers.form.FormFieldComponent;
import tools.dynamia.zk.viewers.form.FormView;

public class BookFormViewCustomizer implements ViewCustomizer<FormView<Book>> {

    @Override
    public void customize(FormView<Book> view) {


        FormFieldComponent onSale = view.getFieldComponent("onSale");
        FormFieldComponent salePrice = view.getFieldComponent("salePrice");
        salePrice.hide();



        view.addEventListener(FormView.ON_VALUE_CHANGED, event -> {
            if (view.getValue()!=null && view.getValue().isOnSale()) {
                salePrice.show();
            }
        });


        if (onSale != null && onSale.getInputComponent() instanceof Checkbox checkbox) {

            checkbox.addEventListener(Events.ON_CHECK, event -> {
                if (checkbox.isChecked()) {
                    salePrice.show();
                } else {
                    salePrice.hide();
                }
            });
        }


    }
}
