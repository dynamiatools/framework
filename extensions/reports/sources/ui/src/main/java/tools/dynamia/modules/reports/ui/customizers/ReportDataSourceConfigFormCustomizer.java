package tools.dynamia.modules.reports.ui.customizers;

import org.zkoss.zul.Combobox;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;
import tools.dynamia.viewers.ViewCustomizer;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.form.FormView;

import java.sql.DriverManager;
import java.util.ArrayList;

public class ReportDataSourceConfigFormCustomizer implements ViewCustomizer<FormView<ReportDataSourceConfig>> {

    @Override
    public void customize(FormView<ReportDataSourceConfig> view) {

        var driversCombo = (Combobox) view.getFieldComponent("driverClassName").getInputComponent();
        var drivers = new ArrayList<String>();
        DriverManager.getDrivers().asIterator().forEachRemaining(driver -> drivers.add(driver.getClass().getName()));

        view.addEventListener(FormView.ON_VALUE_CHANGED, event -> {
            ZKUtil.fillCombobox(driversCombo, drivers, view.getValue().getDriverClassName(), true);
        });

    }
}
