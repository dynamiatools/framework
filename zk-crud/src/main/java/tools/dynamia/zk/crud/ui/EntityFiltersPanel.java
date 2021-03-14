/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import org.zkoss.bind.Binder;
import org.zkoss.zhtml.Text;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.crud.FilterCondition;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.EntityReference;
import tools.dynamia.domain.Reference;
import tools.dynamia.domain.query.QueryCondition;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.crud.EntityFilterCustomizer;
import tools.dynamia.zk.ui.Booleanbox;
import tools.dynamia.zk.ui.EnumListbox;
import tools.dynamia.zk.ui.model.FilterField;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.DefaultFieldCustomizer;
import tools.dynamia.zk.viewers.form.FormFieldComponent;

import java.util.*;

public class EntityFiltersPanel extends Borderlayout implements View {

    /**
     *
     */
    public static final String ON_SEARCH = "onSearch";
    private static final long serialVersionUID = 6522069747991047688L;
    private static final String PATH = "path";

    private Class<?> entityClass;

    private Button searchButton;

    private List<FilterField> filters = new ArrayList<>();
    private Map<FilterField, EntityFilterCustomizer> filterCustomizers = new HashMap<>();
    private Vlayout filtersPanel;
    private LoggingService logger = new SLF4JLoggingService(EntityFiltersPanel.class);
    private ViewDescriptor viewDescriptor;
    private Map<String, FormFieldComponent> componentsFieldsMap = new HashMap<>();
    private View parentView;
    private Object value;


    public EntityFiltersPanel(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
        if (getChildren().isEmpty()) {
            buildLayout();
        }
    }

    @Override
    public void setParent(Component parent) {
        super.setParent(parent); // To change body of generated methods, choose
        // Tools | Templates.
        if (getChildren().isEmpty()) {
            buildLayout();
        }
    }

    private void buildLayout() {
        setSclass("filterPanel");
        getChildren().clear();
        Center center = new Center();
        center.setAutoscroll(true);

        appendChild(center);

        filtersPanel = new Vlayout();
        filtersPanel.setHflex("1");
        filtersPanel.setStyle("padding: 4px");
        center.appendChild(filtersPanel);

        South south = new South();
        appendChild(south);

        buildButtons();
        south.appendChild(searchButton);

        buildFilters();
    }

    private void buildFilters() {
        if (viewDescriptor == null) {
            viewDescriptor = Viewers.findViewDescriptor(entityClass, "entityfilters");
        }
        if (viewDescriptor == null) {
            viewDescriptor = Viewers.getViewDescriptor(entityClass, "table");
        }
        List<Field> fields = Viewers.getFields(viewDescriptor);
        for (Field field : fields) {
            EntityFilterCustomizer filterCustomizer = null;
            try {
                String customizerClass = (String) field.getParams().get(Viewers.PARAM_FILTER_CUSTOMIZER);
                filterCustomizer = BeanUtils.newInstance(customizerClass);
                filterCustomizer.init(entityClass, field);
            } catch (Exception e) {
            }

            if (field.isVisible() && !field.isCollection() && (field.getPropertyInfo() != null || filterCustomizer != null)) {

                FilterCondition condition = null;

                try {
                    if (field.getParams().containsKey(Viewers.PARAM_CONDITION)) {
                        String conditionName = (String) field.getParams().get(Viewers.PARAM_CONDITION);
                        condition = FilterCondition.valueOf(conditionName.toUpperCase().trim());
                    }
                } catch (Exception e) {
                    logger.warn("Cannot instance condition for field " + field.getName() + ". Using default condition");
                }

                if (filterCustomizer != null) {
                    condition = filterCustomizer.getFilterCondition();
                }

                if (condition == null && isEntityReference(field)) {
                    condition = FilterCondition.EQUALS;
                }

                if (condition == null) {
                    condition = getFilterCondition(field.getFieldClass());
                }

                addFilter(condition, field, filterCustomizer);
            }
        }

    }

    private void buildButtons() {

        searchButton = new Button();
        searchButton.setStyle("margin:5px;float:right");
        searchButton.setZclass("btn btn-primary");
        ZKUtil.configureComponentIcon("filter", searchButton, IconSize.SMALL);

        searchButton.setLabel(Messages.get(EntityFiltersPanel.class, "search"));
        searchButton.addEventListener(Events.ON_CLICK, event -> Events.postEvent(ON_SEARCH, EntityFiltersPanel.this, getQueryParameters()));

    }

    @SuppressWarnings("rawtypes")
    private void addFilter(FilterCondition filterCondition, Field field, EntityFilterCustomizer filterCustomizer) {
        PropertyInfo prop = field.getPropertyInfo();
        String path = field.getName();
        if (field.getParams().containsKey(PATH)) {
            path = (String) field.getParams().get(PATH);
        }
        String label = field.getLocalizedLabel(Messages.getDefaultLocale());


        if (prop != null) {
            if (prop.is(Boolean.class) || prop.is(boolean.class)) {
                label += "?";
            }
        }


        QueryCondition qc = BeanUtils.newInstance(filterCondition.getConditionClass());
        Vlayout filterGroup = new Vlayout();

        Binder binder = ZKBindingUtil.createBinder();
        ZKBindingUtil.initBinder(binder, filterGroup, filterGroup);
        FilterField filterField = new FilterField(path, prop, field.getLabel(), qc, binder);
        filters.add(filterField);
        filterCustomizers.put(filterField, filterCustomizer);

        Label labelComp = new Label();
        if (field.isRequired()) {
            labelComp.addSclass("form-view-lbl");
            labelComp.addSclass("required");
        }
        labelComp.setValue(label);

        filterGroup.setStyle("padding-top: 10px");
        filterGroup.appendChild(labelComp);


        if (filterCondition == FilterCondition.BETWEEN) {

            Component comp = buildComponent(field, prop, filterCondition);
            Component comp2 = buildComponent(field, prop, filterCondition);
            if (comp instanceof InputElement) {
                InputElement input = (InputElement) comp;
                input.setHflex("1");
                input.setPlaceholder("Desde");
            }
            if (comp2 instanceof InputElement) {
                InputElement input = (InputElement) comp2;
                input.setHflex("1");
                input.setPlaceholder("Hasta");
            }


            comp.setParent(filterGroup);
            comp2.setParent(filterGroup);

            bindComponentToBetween(binder, qc, comp, comp2);
            componentsFieldsMap.put(field.getName(), new FormFieldComponent(field.getName(), labelComp, comp, comp2));
        } else {
            Component comp = null;
            if (filterCustomizer != null) {
                comp = filterCustomizer.buildComponent();
            } else {
                comp = buildComponent(field, prop, filterCondition);
            }
            bindComponentToCondition(binder, qc, comp);

            if (comp instanceof InputElement) {
                InputElement ie = (InputElement) comp;
                ie.setStyle("text-align:left");
            }
            comp.setParent(filterGroup);
            if (comp instanceof HtmlBasedComponent) {
                HtmlBasedComponent hcomp = (HtmlBasedComponent) comp;
                hcomp.setHflex("1");
                if (field.getDescription() != null) {
                    hcomp.setTooltiptext(field.getDescription());
                }
            }
            componentsFieldsMap.put(field.getName(), new FormFieldComponent(field.getName(), labelComp, comp));

        }

        filtersPanel.appendChild(filterGroup);

        binder.loadComponent(filterGroup, false);

    }

    private Component buildComponent(Field field, PropertyInfo prop, FilterCondition filterCondition) {
        Component comp = null;

        List<String> labelsComponents = List.of("label", "entityreflabel", "enumlabel");
        if (field.getComponent() != null && !labelsComponents.contains(field.getComponent())) {
            comp = buildFieldComponent(field);
        } else if (prop.isEnum() && filterCondition == FilterCondition.INLIST) {
            comp = buildEnumChecks(field, prop);
        } else if (prop.isEnum() && filterCondition == FilterCondition.EQUALS) {
            comp = buildEnumCombobox(field, prop);
        } else if (prop.is(AbstractEntity.class)) {
            comp = buildEntityPicker(prop, filterCondition);
        } else if (isEntityReference(field)) {
            comp = buildEntityReferencePicker(field, prop, filterCondition);
        }

        if (prop.is(Boolean.class) || prop.is(boolean.class)) {
            comp = new Booleanbox();
        }

        if (comp == null) {
            comp = buildDefaultComponent(field, prop);
        }

        if (comp instanceof InputElement) {
            comp.addEventListener(Events.ON_OK, e -> Events.postEvent(ON_SEARCH, EntityFiltersPanel.this, getQueryParameters()));
        }

        return comp;
    }

    private Component buildEnumCombobox(Field field, PropertyInfo prop) {
        Combobox combobox = new Combobox();
        combobox.setReadonly(true);
        List enumValues = getEnumValues(field, prop);
        ZKUtil.fillCombobox(combobox, enumValues);
        return combobox;

    }

    private List getEnumValues(Field field, PropertyInfo prop) {
        List enumValues = Arrays.asList(prop.getType().getEnumConstants());
        List values = (List) field.getParams().get("enumValues");
        if (values != null) {
            enumValues = new ArrayList();

            for (Object enumVal : values) {
                if (enumVal instanceof String) {
                    enumValues.add(Enum.valueOf((Class<Enum>) prop.getType(), enumVal.toString()));
                } else if (BeanUtils.isAssignable(enumVal.getClass(), prop.getType())) {
                    enumValues.add(enumVal);
                }
            }
        }
        return enumValues;
    }

    private Component buildFieldComponent(Field field) {
        Class<Component> compClass = (Class<Component>) ComponentAliasIndex.getInstance().get(field.getComponent());
        if (compClass != null) {
            Component comp = BeanUtils.newInstance(compClass);
            if (comp != null) {
                BeanUtils.setupBean(comp, field.getParams());
                return comp;
            }
        }

        return null;
    }

    private Component buildEntityReferencePicker(Field field, PropertyInfo prop, FilterCondition filterCondition) {
        Reference reference = getReferenceField(field);
        EntityReferencePickerBox entityReferencePickerBox = new EntityReferencePickerBox();
        entityReferencePickerBox.setEntityAlias(reference.value());
        return entityReferencePickerBox;
    }

    private boolean isEntityReference(Field field) {
        Reference reference = getReferenceField(field);
        return BeanUtils.isAssignable(field.getFieldClass(), EntityReference.class) || reference != null;
    }

    private Reference getReferenceField(Field field) {
        if (field != null && field.getViewDescriptor() != null) {
            Class beanClass = field.getViewDescriptor().getBeanClass();
            if (beanClass != null) {
                try {
                    java.lang.reflect.Field classField = BeanUtils.getField(beanClass, field.getName());
                    return classField.getAnnotation(Reference.class);
                } catch (NoSuchFieldException e) {

                }
            }
        }
        return null;

    }

    private void bindComponentToCondition(Binder binder, QueryCondition qc, Component comp) {
        String beanId = "QC";
        ZKBindingUtil.bindBean(binder.getView(), beanId, qc);
        ZKBindingUtil.bindComponent(binder, comp, beanId + ".value", null);
    }

    private void bindComponentToBetween(Binder binder, QueryCondition qc, Component comp, Component comp2) {
        String beanId = "QC";
        ZKBindingUtil.bindBean(binder.getView(), beanId, qc);
        ZKBindingUtil.bindComponent(binder, comp, beanId + ".valueLo", null);
        ZKBindingUtil.bindComponent(binder, comp2, beanId + ".valueHi", null);
    }

    private Component buildDefaultComponent(Field field, PropertyInfo prop) {
        Field dummyField = new Field("thefield", prop.getType());
        DefaultFieldCustomizer dfc = new DefaultFieldCustomizer();
        dfc.customize("form", dummyField);
        if (dummyField.getComponentClass() == null) {
            dummyField.setComponentClass(Textbox.class);
        }
        Component comp = (Component) BeanUtils.newInstance(dummyField.getComponentClass());
        if (comp != null) {
            BeanUtils.setupBean(comp, field.getParams());

        }
        return comp;
    }

    private Component buildEntityPicker(PropertyInfo prop, FilterCondition filterCondition) {
        StringBuilder sb = new StringBuilder();

        ViewDescriptor viewDescriptor = Viewers.getViewDescriptor(prop.getType(), "entitypicker");

        for (Field field : Viewers.getFields(viewDescriptor)) {
            if (field.isVisible() && !field.isCollection()) {
                sb.append(field.getName()).append(",");
            }
        }

        Component comp = null;

        if (filterCondition == FilterCondition.INLIST) {
            MultiEntityPicker multiEntityPicker = new MultiEntityPicker();
            multiEntityPicker.setEntityClass(prop.getType().getName());
            multiEntityPicker.setHflex("1");
            multiEntityPicker.setFields(sb.toString());
            comp = multiEntityPicker;
        } else if (filterCondition == FilterCondition.EQUALS) {
            EntityPickerBox entityPicker = new EntityPickerBox();
            entityPicker.setEntityClass(prop.getType().getName());
            entityPicker.setHflex("1");
            entityPicker.setFields(sb.toString());
            entityPicker.setPopupWidth("200%");
            comp = entityPicker;
        }

        return comp;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Component buildEnumChecks(Field field, PropertyInfo prop) {
        return new EnumListbox(prop.getType(), getEnumValues(field, prop));
    }

    public QueryParameters getQueryParameters() {

        QueryParameters qp = new QueryParameters();

        for (FilterField filter : filters) {
            EntityFilterCustomizer customizer = filterCustomizers.get(filter);
            QueryCondition condition = filter.getCondition();
            if (hasValue(condition)) {
                if (customizer != null && customizer.isManualParameters()) {
                    customizer.setupParameters(qp, condition);
                } else {
                    qp.add(filter.getPath(), condition);
                }
            }
        }
        return qp;
    }

    private FilterCondition getFilterCondition(Class<?> fieldClass) {
        if (fieldClass == Date.class || BeanUtils.isAssignable(fieldClass, Number.class)
                || fieldClass == double.class
                || fieldClass == long.class
                || fieldClass == float.class
                || fieldClass == int.class) {
            return FilterCondition.BETWEEN;
        }

        if (fieldClass == String.class) {
            return FilterCondition.CONTAINS;
        }

        if (fieldClass.isEnum()) {
            return FilterCondition.INLIST;
        }
        return FilterCondition.EQUALS;
    }

    private boolean hasValue(QueryCondition condition) {
        if (condition.getValue() == null) {
            return false;
        }

        if (condition.getValue() instanceof String) {
            String text = (String) condition.getValue();
            if (text.trim().isEmpty()) {
                return false;
            }
        }

        if (condition.getValue() instanceof Collection) {
            Collection collection = (Collection) condition.getValue();
            if (collection.isEmpty()) {
                return false;
            }
        }

        if (condition.getValue().getClass().isArray()) {
            Object[] array = (Object[]) condition.getValue();
            return array.length != 0;
        }
        return true;
    }


    public FormFieldComponent getFieldComponent(String fieldName) {
        return componentsFieldsMap.get(fieldName);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
        buildLayout();
    }

    @Override
    public View getParentView() {
        return parentView;
    }

    @Override
    public void setParentView(View parentView) {
        this.parentView = parentView;
    }
}
