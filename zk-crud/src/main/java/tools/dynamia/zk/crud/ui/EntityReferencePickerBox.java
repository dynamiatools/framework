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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.AbstractListModel;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import tools.dynamia.commons.Messages;
import tools.dynamia.domain.EntityReference;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.ui.CanBeReadonly;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.table.TableView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings("rawtypes")
public class EntityReferencePickerBox extends Bandbox implements CanBeReadonly {

    /**
     *
     */
    private static final long serialVersionUID = 6708107320014456649L;

    static {
        BindingComponentIndex.getInstance().put("selectedId", EntityReferencePickerBox.class);
        ComponentAliasIndex.getInstance().add("entityrefpicker", EntityReferencePickerBox.class);
    }

    private final QueryParameters defaultParameters = new QueryParameters();
    private String[] fields;
    private String entityClassName;
    private String entityAlias;
    private EntityReference selected;
    private EntityReferenceRepository repository;
    private TableView resultTable;
    private String lastSearchText;
    private String defaultItemLabel;

    public EntityReferencePickerBox() {
        init();
    }

    public EntityReferencePickerBox(String entityClass) {
        setEntityClassName(entityClass);
        init();
    }

    public EntityReferencePickerBox(String entityClass, EntityReference selected) {
        setEntityClassName(entityClass);
        setSelected(selected);
        init();
    }

    private void init() {
        setSclass("entitypickerbox-popup");
        Bandpopup bandpopup = new Bandpopup();
        bandpopup.setStyle("padding: 0");
        resultTable = new TableView();
        resultTable.setMold("default");
        resultTable.setAutopaging(false);
        resultTable.setHeight("250px");
        resultTable.setHflex("1");


        appendChild(bandpopup);

        bandpopup.appendChild(resultTable);

        String none = Messages.get(EntityPickerBox.class, "none");
        String writeMore = Messages.get(EntityPickerBox.class, "writeMore");
        setTooltiptext(writeMore);

        setAutodrop(true);
        setButtonVisible(true);
        setPopupWidth("100%");
        setSclass("entitypickerbox");


        addEventListener(Events.ON_CHANGING, evt -> {
            InputEvent event = (InputEvent) evt;
            String newValue = event.getValue();
            if (newValue != null) {
                search(newValue);
            }
        });

        addEventListener(Events.ON_OPEN, evt -> {
            if (resultTable.getModel() == null || resultTable.getModel().getSize() == 0) {
                search("%");
            }
        });

        resultTable.setItemRenderer((item, data, index) -> {
            item.setValue(data);
            if (data == null) {
                item.setLabel(defaultItemLabel != null ? defaultItemLabel : none);
            } else {
                item.setLabel(data.toString());
            }
        });

        resultTable.addEventListener(Events.ON_SELECT, evt -> {
            Events.postEvent(this, evt);
            if (HttpUtils.isSmartphone()) {
                close();
            }
        });
        resultTable.addEventListener(Events.ON_OK, evt -> close());
        setWidgetListener("onKeyDown", "event.keyCode==40?this.lastChild.lastChild.firstItem.focus():''");
    }

    @SuppressWarnings("unchecked")
    private void search(String param) {
        if (repository != null && !Objects.equals(lastSearchText, param)) {
            List<Object> result = repository.find(param, defaultParameters);
            if (result != null) {
                open();
            }

            if (result == null) {
                result = new ArrayList<>();
            }
            result.add(0, null);
            this.lastSearchText = param;
            ZKUtil.fillListbox(resultTable, result, true);

            if ((param == null || param.isEmpty()) && selected != null) {
                setSelected(null);
                Events.postEvent(Events.ON_SELECT, this, null);
            }
        }

    }

    public EntityReference getSelected() {
        if (resultTable.getSelectedItem() != null) {
            selected = resultTable.getSelectedItem().getValue();
        }

        return selected;
    }

    public void setSelected(EntityReference reference) {
        this.selected = reference;
        if (entityClassName == null && reference != null) {
            setEntityClassName(reference.getClassName());
        }

        if (reference != null) {
            setValue(selected.getName());
        } else {
            setValue(null);
        }


        if (resultTable.getModel() instanceof AbstractListModel) {
            AbstractListModel model = (AbstractListModel) resultTable.getModel();
            model.addToSelection(reference);
        }
    }

    public Serializable getSelectedId() {
        EntityReference ref = getSelected();
        return ref != null ? ref.getId() : null;
    }

    public void setSelectedId(Serializable id) {
        if (repository == null) {
            initRepository();
        }

        if (repository != null) {
            EntityReference reference = repository.load(id);
            setSelected(reference);
        }
    }

    public final void setEntityClassName(String entityClass) {
        this.entityClassName = entityClass;
        initRepository();
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public String getEntityAlias() {
        return entityAlias;
    }

    public void setEntityAlias(String entityAlias) {
        this.entityAlias = entityAlias;
        initRepository();
    }

    private void initRepository() {
        this.repository = DomainUtils.getEntityReferenceRepository(entityClassName);

        if (repository == null) {
            repository = DomainUtils.getEntityReferenceRepositoryByAlias(entityAlias);
        }

        setDisabled(repository == null);
        setTooltiptext(repository == null ? Messages.get(EntityReferencePickerBox.class, "noservice") : "");
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

        return fields;
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

    @Override
    public void setParent(Component parent) {
        initRepository();
        super.setParent(parent);
    }

    @Override
    public boolean isReadonly() {
        return isReadonly();
    }

    @Override
    public void setReadonly(boolean readonly) {
        super.setReadonly(readonly);
        super.setButtonVisible(!readonly);
    }

    public static EntityReferencePickerBox showDialog(String title, String alias, Consumer<EntityReference> selectedRef) {
        EntityReferencePickerBox picker = new EntityReferencePickerBox();
        picker.setEntityAlias(alias);
        picker.setWidth("100%");


        var win = ZKUtil.showDialog(title, picker, "400px", null);
        picker.addEventListener(Events.ON_SELECT, e -> {
            selectedRef.accept(picker.getSelected());
            win.detach();
        });

        return picker;
    }

    public String getDefaultItemLabel() {
        return defaultItemLabel;
    }

    public void setDefaultItemLabel(String defaultItemLabel) {
        this.defaultItemLabel = defaultItemLabel;
    }
}
