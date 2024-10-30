package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listitem;
import tools.dynamia.actions.FastAction;
import tools.dynamia.ui.SelectEventCallback;
import tools.dynamia.ui.SelectionEventCallback;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptorBuilder;
import tools.dynamia.zk.viewers.table.TableView;
import tools.dynamia.zk.viewers.ui.Viewer;

import java.util.List;

public class ZKTableSelector<T> extends Div {

    private List<String> columns;
    private List<T> data;
    private SelectEventCallback<T> onSelect;
    private SelectionEventCallback<T> onSelection;
    private Viewer viewer;
    private Class<T> type;
    private boolean multiple;
    private String label = "Select";


    /**
     * Default constructor
     */
    public ZKTableSelector() {
    }


    public ZKTableSelector(List<String> columns, List<T> data) {
        this.columns = columns;
        this.data = data;
        init();
    }

    public ZKTableSelector(String label, List<String> columns, List<T> data, boolean multiple) {
        this.label = label;
        this.columns = columns;
        this.data = data;
        this.multiple = multiple;
        init();
    }

    public ZKTableSelector(String label, List<String> columns, List<T> data) {
        this.label = label;
        this.columns = columns;
        this.data = data;
        init();
    }

    public ZKTableSelector(List<String> columns, List<T> data, boolean multiple) {
        this.columns = columns;
        this.data = data;
        this.multiple = multiple;
        init();
    }

    /**
     * Init view and table data
     */
    public void init() {
        setHflex("1");
        setVflex("1");
        if (data != null && !data.isEmpty() && columns != null && !columns.isEmpty()) {
            data.stream().findFirst().ifPresent(t -> type = (Class<T>) t.getClass());
            if (type != null) {
                var descriptor = ViewDescriptorBuilder.viewDescriptor("table", type)
                        .autofields(false)
                        .fields(() -> columns.stream().map(Field::new).toList())
                        .addParam("checkmark", true)
                        .addParam("multiple", multiple)
                        .build();


                viewer = new Viewer(descriptor);
                viewer.setValue(data);
                viewer.setVflex("1");
                var tableView = (TableView) viewer.getView();
                viewer.addAction(new FastAction(label, event -> {
                    if (tableView.getSelectedCount() > 0) {
                        var selected = tableView.getSelectedItems().stream().map(Listitem::getValue).toList();
                        if (onSelect != null) {
                            onSelect.onSelect((T) selected.get(0));
                        }

                        if (onSelection != null) {
                            onSelection.onSelect((List<T>) selected);
                        }
                    }
                }));
                getChildren().clear();
                appendChild(viewer);
            }
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
        init();
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setData(List<T> data) {
        this.data = data;
        if (viewer != null) {
            viewer.setValue(data);
        } else {
            init();
        }
    }

    public List<T> getData() {
        return data;
    }

    public void onSelect(SelectEventCallback<T> onSelect) {
        this.onSelect = onSelect;
    }

    public void onSelection(SelectionEventCallback<T> onSelection) {
        this.onSelection = onSelection;
    }

}
