/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.zk.crud.ui;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.A;
import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Span;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewCustomizer;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.crud.CrudView;
import tools.dynamia.zk.crud.actions.FastCrudAction;
import tools.dynamia.zk.ui.CanBeReadonly;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ZKWrapperView;
import tools.dynamia.zk.viewers.table.TableView;
import tools.dynamia.zk.viewers.table.TableViewRowRenderer;
import tools.dynamia.zk.viewers.ui.Viewer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.function.Predicate.not;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings("rawtypes")
public class EntityPickerBox extends Span implements CanBeReadonly {

    /**
     *
     */
    private static final long serialVersionUID = 6708107320014456649L;

    public static final String ENTITYPICKER = "entitypicker";

    static {
        BindingComponentIndex.getInstance().put("selected", EntityPickerBox.class);
        ComponentAliasIndex.getInstance().add(ENTITYPICKER, EntityPickerBox.class);
    }

    private static final LoggingService logger = new SLF4JLoggingService(EntityPickerBox.class);

    private final QueryParameters defaultParameters = new QueryParameters();
    private final CrudService crudService = Containers.get().findObject(CrudService.class);

    private String[] fields;

    private Class entityClass;
    private String entityProperty;
    private String entityName;

    private Object selected;
    private boolean autoboxed;
    private Bandbox inputField;
    private TableView resultTable;
    private A tableButton;
    private boolean autoselect;
    private long lastSearchTimestamp;
    private String lastSearchText;
    private List result;
    private String defaultItemLabel;
    private int maxResults = 20;
    private boolean autosearch = true;
    private String emptyMessage;

    private int requiredTextSize = 0;

    public EntityPickerBox() {
        init();
    }

    public EntityPickerBox(Class entityClass) {
        setEntityClass(entityClass);
        init();
    }

    public EntityPickerBox(Class entityClass, Object selected) {
        setEntityClass(entityClass);
        setSelected(selected);
        init();
    }

    private static boolean isSearcheable(Field f) {
        return !Boolean.FALSE.equals(f.getParams().get("searcheable"));
    }

    private void init() {

        String writeMoreText = Messages.get(EntityPickerBox.class, "writeMore");
        String selectText = Messages.get(EntityPickerBox.class, "select");
        setTooltiptext(writeMoreText);
        setZclass("entitypickerbox");
        initInputField();
        initCrudView(selectText);
    }

    private void initInputField() {
        inputField = new Bandbox();
        tableButton = new A();

        appendChild(inputField);
        appendChild(tableButton);

        tableButton.setZclass("entitypickerbox-button");
        tableButton.setIconSclass("z-icon-search");
        inputField.setAutodrop(true);
        Bandpopup popup = new Bandpopup();
        inputField.appendChild(popup);
        inputField.setPopupWidth("110%");
        inputField.setSclass("entitypickerbox-popup");

        inputField.addEventListener(Events.ON_CHANGING, evt -> {
            InputEvent event = (InputEvent) evt;
            String newValue = event.getValue();
            if (newValue != null && newValue.length() >= requiredTextSize) {
                search(newValue);
            }
        });

        inputField.addEventListener(Events.ON_OPEN, evt -> {
            if (resultTable.getModel() == null || resultTable.getModel().getSize() == 0) {
                if (isAutosearch()) {
                    search("%");
                }
            }
        });

        inputField.addEventListener(Events.ON_OK, evt -> {
            if (result != null && result.size() == 1) {
                setSelected(result.get(0));
                Events.postEvent(Events.ON_SELECT, this, getSelected());
                close();
            }
        });

    }

    private void initTableView() {
        String noneText = Messages.get(EntityPickerBox.class, "none");
        ViewDescriptor descriptor = Viewers.findViewDescriptor(entityClass, HttpUtils.detectDevice(), ENTITYPICKER);
        if (descriptor == null || HttpUtils.isSmartphone()) {
            resultTable = new TableView();
            resultTable.setItemRenderer((ListitemRenderer<Object>) (item, data, index) -> {
                String noneLabel = defaultItemLabel != null ? defaultItemLabel : noneText;
                item.setValue(data);
                item.setLabel(data != null ? BeanUtils.getInstanceName(data) : noneLabel);
            });
        } else {
            DefaultViewDescriptor resultDescriptor = new DefaultViewDescriptor(entityClass, "table", false);
            resultDescriptor.addParam("sizedByContent", true);

            resultDescriptor.merge(descriptor);
            resultTable = (TableView) Viewers.getView(resultDescriptor);
            resultTable.setItemRenderer(new TableViewRowRenderer() {
                @Override
                public void render(Listitem item, Object data, int index) {
                    String noneLabel = defaultItemLabel != null ? defaultItemLabel : noneText;
                    if (data == null) {
                        Listcell noneCell = new Listcell(noneLabel);
                        noneCell.setParent(item);
                        int fields = descriptor.getFields().size();
                        noneCell.setSpan(fields);

                    } else {
                        super.render(item, data, index);
                    }
                }
            });

            resultTable.setHeight(null);
            resultTable.setVflex(null);
            resultTable.setHflex(null);
        }
        resultTable.setMold("default");
        resultTable.setAutopaging(false);
        resultTable.setHeight("250px");
        resultTable.setHflex("1");
        resultTable.setEmptyMessage(emptyMessage);

        inputField.getDropdown().appendChild(resultTable);
        resultTable.addEventListener(Events.ON_SELECT, evt -> {
            Events.postEvent(this, evt);
            if (HttpUtils.isSmartphone()) {
                close();
            }
        });

        resultTable.addEventListener(Events.ON_OK, evt -> close());

        inputField.setWidgetListener("onKeyDown", "event.keyCode==40?this.lastChild.lastChild.firstItem.focus():''");

    }

    private void initCrudView(String selectText) {
        tableButton.addEventListener(Events.ON_CLICK, e -> {
            Viewer viewer = new Viewer("crud", entityClass);
            viewer.setVflex("1");
            viewer.setContentVflex("1");

            CrudView crudView = (CrudView) viewer.getView();

            FastCrudAction selectAction = new FastCrudAction(selectText + " " + entityName, "check", ce -> {
                setSelected(ce.getData());
                viewer.getParent().detach();
                Set selecteds = new HashSet();
                //noinspection unchecked
                selecteds.add(selected);
                //noinspection unchecked
                Events.postEvent(new SelectEvent(Events.ON_SELECT, this, selecteds));
            });
            selectAction.setColor("white");
            selectAction.setBackground("#00C851");

            crudView.addAction(selectAction);
            ZKUtil.showDialog(entityName, viewer, "80%", "80%");
            crudView.getController().setDefaultParameters(defaultParameters);
            if (defaultParameters != null && !defaultParameters.isEmpty()) {
                crudView.getController().doQuery();
            }
        });
    }

    private void search(String param) {

        //ms
        long DELAY = 500;
        if (lastSearchTimestamp > 0 && (System.currentTimeMillis() - lastSearchTimestamp) < DELAY) {
            return;
        }

        if (entityClass != null && !Objects.equals(lastSearchText, param) || lastSearchTimestamp == 0) {


            defaultParameters.setMaxResults(maxResults);
            //noinspection unchecked
            this.result = crudService.findByFields(entityClass, param, defaultParameters, getFields());
            if (result != null && !result.isEmpty()) {
                autoboxed = false;
                inputField.open();
            }

            //noinspection unchecked
            result.add(0, null);

            ZKUtil.fillListbox(resultTable, result, true);
            if ((param == null || param.isEmpty()) && selected != null) {
                setSelected(null);
                Events.postEvent(Events.ON_SELECT, this, null);
            }
            lastSearchTimestamp = System.currentTimeMillis();
            lastSearchText = param;
        }

    }

    public Object getSelected() {
        if (resultTable.getSelectedItem() != null) {
            selected = resultTable.getSelectedItem().getValue();
        } else if (entityClass == null && entityProperty != null) {
            selected = inputField.getValue();
        }
        autoboxSelected();

        return selected;
    }

    public void setSelected(Object object) {

        this.selected = object;

        if (object != null) {
            inputField.setValue(object.toString());
        } else {
            inputField.setValue(null);
        }

        if (resultTable.getModel() instanceof AbstractListModel model) {
            if (object != null) {
                //noinspection unchecked
                model.addToSelection(object);
            } else {
                model.clearSelection();
            }
        }
    }

    public final void setEntityClass(String entityClass) {
        try {
            Class clazz = Class.forName(entityClass);
            setEntityClass(clazz);
        } catch (ClassNotFoundException e) {
            logger.warn("EntityPickerBox: [" + entityClass + "] class not found. " + e.getMessage() + " - COMPONENT AUTODISABLED");
            setDisabled(true);
            resultTable = new TableView();
        }
    }

    public final void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
        if (entityClass != null) {
            entityName = StringUtils.addSpaceBetweenWords(entityClass.getSimpleName());
            entityName = StringUtils.capitalizeAllWords(entityName);
            inputField.setPlaceholder(entityName);
            inputField.setTooltiptext(entityName);
            initTableView();
        }
    }

    public void setFields(String fields) {
        if (fields != null) {
            String[] fieldsNames = fields.trim().replace(" ", "").split(",");
            setFields(fieldsNames);
        }
    }

    public void setFields(String... fields) {
        if (fields != null) {
            this.fields = fields;
        }
    }

    public String[] getFields() {
        if (fields == null) {
            loadFields();
        }

        return fields;
    }

    private void loadFields() {
        ViewDescriptor descriptor = Viewers.getViewDescriptor(entityClass, ENTITYPICKER);
        DefaultViewDescriptor tableDescriptor = new DefaultViewDescriptor(entityClass, "table", false);
        tableDescriptor.merge(descriptor);

        if (descriptor.getViewCustomizerClass() != null) {
            try {
                ViewCustomizer customizer = BeanUtils.newInstance(descriptor.getViewCustomizerClass());
                //noinspection unchecked
                customizer.customize(new ZKWrapperView(this));
            } catch (Exception ignored) {
            }
        }

        if (fields != null && fields.length > 0) {
            setFields(fields);
        } else {
            fields = Viewers.getFields(descriptor).stream()
                    .filter(Field::isProperty)
                    .filter(Field::isVisible)
                    .filter(Field::isReadWrite)
                    .filter(not(Field::isCollection))
                    .filter(EntityPickerBox::isSearcheable)
                    .map(Field::getName)
                    .toArray(String[]::new);

        }

    }

    public void addDefaultParameter(String name, Object value) {
        defaultParameters.add(name, value);
    }

    public void removeDefaultParameter(String name) {
        defaultParameters.remove(name);
    }

    public void setOrderBy(String orderBy) {
        defaultParameters.orderBy(orderBy, true);
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public String getEntityProperty() {
        return entityProperty;
    }

    public void setEntityProperty(String entityProperty) {
        this.entityProperty = entityProperty;
    }

    private void autoboxSelected() {
        if (!autoboxed && entityProperty != null && selected != null) {
            selected = BeanUtils.invokeGetMethod(selected, BeanUtils.getPropertyInfo(entityClass, entityProperty));
            autoboxed = true;
        }
    }

    @Override
    public boolean isReadonly() {
        return inputField.isReadonly();
    }

    @Override
    public void setReadonly(boolean readonly) {
        inputField.setReadonly(readonly);
        tableButton.setVisible(!readonly);
        inputField.setButtonVisible(!readonly);
    }

    public <T> ListModel<T> getModel() {
        return resultTable.getModel();
    }

    public void setModel(ListModel<?> model) {
        resultTable.setModel(model);
    }

    public void setEmptySearchMessage(String msg) {
        resultTable.setEmptyMessage(msg);
    }

    public <T> ListitemRenderer<T> getItemRenderer() {
        return resultTable.getItemRenderer();
    }

    public void setItemRenderer(ListitemRenderer<?> renderer) {
        resultTable.setItemRenderer(renderer);
    }

    public void setItemRenderer(String clsnm) throws
            ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        resultTable.setItemRenderer(clsnm);
    }

    public boolean isAutodrop() {
        return inputField.isAutodrop();
    }

    public void setAutodrop(boolean autodrop) {
        inputField.setAutodrop(autodrop);
    }

    public boolean isAutocomplete() {
        return inputField.isAutodrop();
    }

    public void setAutocomplete(boolean autocomplete) {
        inputField.setAutodrop(autocomplete);
    }

    public boolean isOpen() {
        return inputField.isOpen();
    }

    public void setOpen(boolean open) {
        inputField.setOpen(open);
    }

    public void open() {
        inputField.open();
    }

    public void close() {
        inputField.close();
    }


    public void setMultiline(boolean multiline) {
        inputField.setMultiline(multiline);
    }

    public void setRows(int rows) {
        inputField.setRows(rows);
    }

    public boolean isSubmitByEnter() {
        return inputField.isSubmitByEnter();
    }

    public void setSubmitByEnter(boolean submitByEnter) {
        inputField.setSubmitByEnter(submitByEnter);
    }

    public String getPlaceholder() {
        return inputField.getPlaceholder();
    }

    public void setPlaceholder(String placeholder) {
        inputField.setPlaceholder(placeholder);
    }

    public void setInplace(boolean inplace) {
        inputField.setInplace(inplace);
    }

    public boolean isInplace() {
        return inputField.isInplace();
    }

    public void setDisabled(boolean disabled) {
        inputField.setDisabled(disabled);
        if (tableButton != null) {
            tableButton.setDisabled(disabled);
        }
    }


    public boolean isAutoselect() {
        return autoselect;
    }

    public void setAutoselect(boolean autoselect) {
        this.autoselect = autoselect;
    }

    public String getPopupWidth() {
        return inputField.getPopupWidth();
    }

    public void setPopupWidth(String popupWidth) {
        inputField.setPopupWidth(popupWidth);
    }

    public String getDefaultItemLabel() {
        return defaultItemLabel;
    }

    public void setDefaultItemLabel(String defaultItemLabel) {
        this.defaultItemLabel = defaultItemLabel;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public QueryParameters getDefaultParameters() {
        return defaultParameters;
    }

    public boolean isAutosearch() {
        return autosearch;
    }

    public void setAutosearch(boolean autosearch) {
        this.autosearch = autosearch;
    }

    public String getEmptyMessage() {
        return emptyMessage;
    }

    public void setEmptyMessage(String emptyMessage) {
        this.emptyMessage = emptyMessage;
        if (resultTable != null) {
            resultTable.setEmptyMessage(emptyMessage);
        }
    }

    public int getRequiredTextSize() {
        return requiredTextSize;
    }

    public void setRequiredTextSize(int requiredTextSize) {
        this.requiredTextSize = requiredTextSize;
    }
}
