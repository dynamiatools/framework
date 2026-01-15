package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.commons.DurationUnit;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Basic duration input selector for {@link Duration} values.
 */
public class DurationSelector extends Div {

    static {
        BindingComponentIndex.getInstance().put("selected", DurationSelector.class);
        ComponentAliasIndex.getInstance().add("durationselector", DurationSelector.class);
    }

    private Combobox unitsbox;
    private Longbox valuebox;

    private Duration selected;

    public DurationSelector() {
        initUI();
    }

    private void initUI() {
        Layout container = new Hlayout();
        if (HttpUtils.isSmartphone()) {
            container = new Vlayout();
        }

        container.setHflex("1");
        appendChild(container);

        valuebox = new Longbox();
        unitsbox = new Combobox();
        unitsbox.setReadonly(true);
        ZKUtil.fillCombobox(unitsbox, List.of(DurationUnit.MILLIS, DurationUnit.SECONDS, DurationUnit.MINUTES, DurationUnit.HOURS), false);

        container.appendChild(valuebox);
        container.appendChild(unitsbox);

        valuebox.addEventListener(Events.ON_OK, this::notifySelection);
        valuebox.addEventListener(Events.ON_BLUR, this::notifySelection);
        unitsbox.addEventListener(Events.ON_SELECT, this::notifySelection);
    }

    private void updateUI() {
        if (selected != null) {
            DurationUnit unit = DurationUnit.MILLIS;
            String format = selected.toString();
            long value = selected.toMillis();
            if (!format.contains("0.") && format.indexOf("S") > 0) {
                unit = DurationUnit.SECONDS;
                value = selected.toSeconds();
            } else if (format.indexOf("M") > 0) {
                unit = DurationUnit.MINUTES;
                value = selected.toMinutes();
            } else if (format.indexOf("H") > 0) {
                unit = DurationUnit.HOURS;
                value = selected.toHours();
            }

            ((ListModelList) unitsbox.getModel()).addToSelection(unit);
            valuebox.setValue(value);


        }
    }

    private void notifySelection(Event evt) {
        DurationUnit selectedUnit = unitsbox.getSelectedItem() != null ? unitsbox.getSelectedItem().getValue() : null;
        if (selectedUnit != null && valuebox.getValue() != null) {
            Duration selection = Duration.of(valuebox.getValue(), selectedUnit.getTemporalUnit());
            if (!Objects.equals(selection, this.selected)) {
                this.selected = selection;
                Events.postEvent(new Event(Events.ON_SELECT, this, getSelected()));
            }
        }
    }

    public void setSelected(Duration selected) {
        if (!Objects.equals(this.selected, selected)) {
            this.selected = selected;
            updateUI();
        }
    }

    public Duration getSelected() {
        return selected;
    }

    public Combobox getUnitsbox() {
        return unitsbox;
    }

    public Longbox getValuebox() {
        return valuebox;
    }
}
