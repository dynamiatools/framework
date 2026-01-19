package tools.dynamia.zk.actions;

import org.zkoss.zul.Datebox;

public class LocalDateboxActionRenderer extends DateboxActionRenderer {

    @Override
    protected Object getValue(Datebox box) {
        return box.getValueInLocalDate();
    }
}
