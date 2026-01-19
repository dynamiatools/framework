package tools.dynamia.zk.actions;

import org.jspecify.annotations.NonNull;
import org.zkoss.zul.Datebox;

public class LocalDateTimeboxActionRenderer extends DateboxActionRenderer {

    @Override
    protected @NonNull String getFormat() {
        return "yyyy/MM/dd HH:mm:ss";
    }

    @Override
    protected Object getValue(Datebox box) {
        return box.getValueInLocalDateTime();
    }
}
