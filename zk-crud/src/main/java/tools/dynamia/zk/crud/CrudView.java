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
package tools.dynamia.zk.crud;

import org.zkoss.bind.Binder;
import org.zkoss.bind.impl.BinderUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.XulElement;
import tools.dynamia.actions.*;
import tools.dynamia.commons.*;
import tools.dynamia.commons.collect.ArrayListMultiMap;
import tools.dynamia.commons.collect.ListMultiMap;
import tools.dynamia.crud.*;
import tools.dynamia.crud.actions.DeleteAction;
import tools.dynamia.crud.actions.EditAction;
import tools.dynamia.crud.actions.NewAction;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.ObjectMatcher;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.navigation.Page;
import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.viewers.*;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.actions.ActionToolbar;
import tools.dynamia.zk.actions.MenuitemActionRenderer;
import tools.dynamia.zk.actions.ToolbarbuttonActionRenderer;
import tools.dynamia.zk.navigation.ComponentPage;
import tools.dynamia.zk.ui.CanBeReadonly;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.form.FormFieldComponent;
import tools.dynamia.zk.viewers.form.FormView;
import tools.dynamia.zk.viewers.mv.MultiView;
import tools.dynamia.zk.viewers.mv.MultiViewListener;
import tools.dynamia.zk.viewers.ui.Viewer;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CrudView<T> extends Div implements GenericCrudView<T>, ActionEventBuilder, IdSpace, CanBeReadonly {

    private static final String DEFAULT_FORM_VIEW_TITLE = "defaultFormViewTitle";

    static {
        BindingComponentIndex.getInstance().put("value", CrudView.class);
        ComponentAliasIndex.getInstance().add(CrudView.class);
    }

    /**
     *
     */
    private static final long serialVersionUID = 7256261472260522851L;
    protected static final String ACTION = "ACTION";
    private Class objectClass;
    private View parentView;
    private String dataSetViewType = "table";
    protected ActionToolbar toolbarLeft;
    protected ActionToolbar toolbarRight;

    protected FormView<T> formView;
    protected MultiView<T> formViewContainer;
    protected DataSetView dataSetView;
    protected Component layout;
    protected Component activeView;
    protected Component toolbarContainer;

    private boolean readOnly;
    private Map<String, Object> actionsParams;
    // ------------------------
    private Class<? extends CrudController> controllerClass;
    private CrudController controller;
    private CrudState state;
    private ViewDescriptor viewDescriptor;
    private final List<CrudStateChangedListener> localListeners = new ArrayList<>();
    // -----------------------------------
    private Menupopup contextMenu;
    private List<CrudAction> applicableActions;
    private final List<CrudAction> actions = new ArrayList<>();
    private String crudServiceName;
    private String formViewDescriptorId;

    private Object source;
    private Consumer onSourceChange;
    private boolean queryProjection;
    private LocalizedMessagesProvider messagesProvider;


    public CrudView() {
        buildGeneralView();
        buildToolbars();
        buildToolbarContainer();
        buildContextMenu();
    }

    protected void buildToolbars() {
        ActionToolbar toolbar = new ActionToolbar(this);
        toolbar.setAlign("start");
        toolbar.setActionRenderer(getDefaultActionRenderer());
        toolbarLeft = toolbar;

        toolbar = new ActionToolbar(this);
        toolbar.setAlign("end");
        toolbar.setActionRenderer(getDefaultActionRenderer());
        toolbarRight = toolbar;
    }

    protected void buildToolbarContainer() {
        Box boxToolbarContainer = new Box(new Component[]{toolbarLeft, toolbarRight});
        boxToolbarContainer.setOrient("horizontal");
        boxToolbarContainer.setPack("stretch");
        boxToolbarContainer.setStyle("width:100%; padding:0px");
        boxToolbarContainer.setSclass(ActionToolbar.CONTAINER_SCLASS);
        if (layout instanceof Borderlayout) {
            Borderlayout borderlayout = (Borderlayout) layout;
            boxToolbarContainer.setParent(borderlayout.getNorth());
            toolbarContainer = boxToolbarContainer;
        }
    }

    protected void buildGeneralView() {
        setHeight("100%");
        setZclass("crudview");
        Borderlayout borderLayout = new Borderlayout();
        borderLayout.setVflex("1");
        {
            North north = new North();
            north.setSclass("crudview-header");
            north.setParent(borderLayout);
            north.setBorder("none");

            Center center = new Center();
            center.setSclass("crudview-body");
            center.setParent(borderLayout);
            center.setBorder("none");
        }
        borderLayout.setParent(this);
        layout = borderLayout;
    }

    protected void buildDataSetView() {

        CrudDataSetViewBuilder viewBuilder = getDataSetViewBuilder(getDataSetViewType());
        if (viewBuilder != null) {
            dataSetView = viewBuilder.build(this);
        }

        if (dataSetView == null) {
            throw new ViewRendererException(
                    "Unable to build DataSetViewType [" + dataSetViewType + "] for CrudView " + getClass());
        }

        if (dataSetView.getViewDescriptor().getParams().get("useProjection") == Boolean.TRUE) {
            setQueryProjection(true);
        }

        ((Component) dataSetView).addEventListener(Events.ON_SELECT,
                event -> getController().setSelected(dataSetView.getSelected()));

    }

    protected void buildFormView() {
        final String device = HttpUtils.detectDevice();
        if (formViewDescriptorId != null && !formViewDescriptorId.isBlank()) {
            formView = (FormView) Viewers.getView(formViewDescriptorId);
        }

        if (formView == null) {
            formView = (FormView<T>) Viewers.getView(getViewDescriptor().getBeanClass(), "form", device, getValue());
        }
    }

    protected void buildFormViewContainer() {
        String formViewTitle = (String) formView.getViewDescriptor().getParams().get(Viewers.PARAM_TITLE);
        if (formViewTitle == null) {
            formViewTitle = Messages.get(CrudView.class, DEFAULT_FORM_VIEW_TITLE);
        }

        if (messagesProvider != null) {
            formViewTitle = messagesProvider.getMessage("Form Title", Viewers.buildMessageClasffier(formView.getViewDescriptor()),
                    Messages.getDefaultLocale(),
                    formViewTitle);
        }

        formViewContainer = new MultiView();
        formViewContainer.setVflex("1");
        var multiViewParams = formView.getViewDescriptor().getParams().get(Viewers.PARAM_MULTIVIEW);
        if (multiViewParams instanceof Map) {
            BeanUtils.setupBean(formViewContainer, multiViewParams);
        }
        addFormViewToContainer(formViewTitle);
        formViewContainer.setParentView(this);

        // Find collection and viewers fields and add Subviews
        for (Field field : formView.getViewDescriptor().getFields()) {
            String label = field.getLocalizedLabel(Messages.getDefaultLocale());
            if (messagesProvider != null) {
                label = messagesProvider.getMessage(field.getName(), Viewers.buildMessageClasffier(field.getViewDescriptor()), Messages.getDefaultLocale(), label);
            }


            if (field.isCollection() && field.getComponentClass() == CrudView.class) {
                addSubCrudView(formView, field, label);
            } else if (field.getComponentClass() == Viewer.class) {
                addSubGenericView(field);
            }
        }
    }

    protected void addFormViewToContainer(String formViewTitle) {
        formViewContainer.addView(formViewTitle, formView);
    }

    protected void buildContextMenu() {
        contextMenu = new Menupopup();
        contextMenu.setParent(this);
        contextMenu.addEventListener(Events.ON_OPEN, event -> loadMenuActions());
    }

    @Override
    public void setState(CrudState crudState) {
        CrudState oldState = this.state;
        Component activeViewParent = getActiveViewParent();
        activeView = null;
        this.state = crudState;
        switch (crudState) {
            case CREATE:
            case UPDATE:
                activeView = getFormView();
                if (activeViewParent instanceof Center) {
                    ((Center) activeViewParent).setAutoscroll(true);
                }
                setTitle(null);
                break;
            case READ:
                activeView = (Component) getDataSetView();
                if (activeViewParent instanceof Center) {
                    ((Center) activeViewParent).setAutoscroll(false);
                }
                break;
            default:
                break;
        }

        if (activeView != null) {

            activeViewParent.getChildren().clear();
            showActiveView();
            loadActions(getState());
            fireChangedStateListeners(this.state, oldState);
        }
    }

    @Override
    public CrudState getState() {
        return state;
    }

    @Override
    public T getValue() {
        if (activeView != null) {
            View view = (View) activeView;
            return (T) view.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void setValue(T value) {
        if (activeView != null) {
            View view = (View) activeView;
            view.setValue(value);
            if (view instanceof FormView && formViewContainer != null) {
                formViewContainer.setValue(value);
            }

        }
    }

    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    public FormView<T> getFormView() {
        if (formView == null) {
            buildFormView();
            if (formView.getCustomView() == null) {
                buildFormViewContainer();
            }
        }
        return formView;
    }

    public DataSetView getDataSetView() {
        if (dataSetView == null) {
            buildDataSetView();
        }
        return dataSetView;
    }

    public String getDataSetViewType() {
        return dataSetViewType;
    }

    public void setDataSetViewType(String dataSetViewType) {
        this.dataSetViewType = dataSetViewType;
    }

    public Class getBeanClass() {
        return objectClass;
    }

    public void setBeanClass(Class objectClass) {
        this.objectClass = objectClass;
    }

    public Class<? extends CrudController> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<? extends CrudController> controller) {
        this.controllerClass = controller;
    }

    public void setControllerClass(String className) throws ClassNotFoundException {
        Class clazz = Class.forName(className.trim());
        if (BeanUtils.isAssignable(clazz, CrudController.class)) {
            setControllerClass(clazz);
        } else {
            throw new ViewRendererException("CrudView controllers class is not a CrudController");
        }
    }

    @Override
    public CrudController getController() {
        return controller;
    }

    @Override
    public void setController(CrudControllerAPI controller) {
        if (controller == null) {
            throw new NullPointerException("You can't asign a null CrudController to CrudView... :(");
        }
        this.controller = (CrudController) controller;
        this.controller.setDataSetView(getDataSetView());
        this.controller.setQueryProjection(isQueryProjection());

        if (crudServiceName != null && !crudServiceName.isEmpty()) {
            CrudService customCrudService = Containers.get().findObject(crudServiceName, CrudService.class);
            if (customCrudService == null) {
                throw new ViewRendererException(
                        "Cant find crudservice instace with name:" + crudServiceName + ". CrudView: " + this);
            }

            this.controller.setCrudService(customCrudService);
        }

        try {
            this.controller.doAfterCompose(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isReadonly() {
        return readOnly;
    }

    @Override
    public void setReadonly(boolean readOnly) {
        this.readOnly = readOnly;

    }

    public void clearActions() {
        if (applicableActions != null) {
            for (Action action : applicableActions) {
                if (action instanceof ActionLifecycleAware) {
                    ActionLifecycleAware ala = (ActionLifecycleAware) action;
                    ala.onDestroy();
                }
            }
        }

        if (toolbarLeft != null) {
            toolbarLeft.getChildren().clear();
        }
        if (toolbarRight != null) {
            toolbarRight.getChildren().clear();
        }

        applicableActions = null;
    }

    public void addAction(CrudAction action) {
        this.actions.add(action);
        loadActions(getState());
    }

    protected ActionRenderer getDefaultActionRenderer() {
        return new ToolbarbuttonActionRenderer();
    }

    private void renderMenuActions(Class valueClass) {

        List<CrudAction> menuActions = findApplicableActions(valueClass, CrudState.READ);

        MenuitemActionRenderer renderer = new MenuitemActionRenderer();
        ListMultiMap<ActionGroup, Component> menuItems = new ArrayListMultiMap<>();
        for (CrudAction crudAction : menuActions) {

            if (crudAction.isEnabled() && crudAction.isMenuSupported()) {
                menuItems.put(crudAction.getGroup(), renderer.render(crudAction, this));
            }
        }

        if (!menuItems.entrySet().isEmpty()) {
            Component lastSeparator = null;

            for (Entry<ActionGroup, List<Component>> entry : menuItems.entrySet()) {

                List<Component> groupActions = entry.getValue();
                groupActions.sort(new ComponentComparator());
                for (Component menuitem : groupActions) {
                    contextMenu.appendChild(menuitem);
                }
                lastSeparator = new Menuseparator();
                contextMenu.appendChild(new Menuseparator());
            }
            lastSeparator.setParent(null);
        }
    }

    public void addCrudStateChangedListener(CrudStateChangedListener listener) {
        if (listener != null && !localListeners.contains(listener)) {
            localListeners.add(listener);
        }
    }

    public void removeCrudStateChangedListener(CrudStateChangedListener listener) {
        localListeners.remove(listener);
    }

    private void showActiveView() {
        if (activeView == dataSetView) {
            destroyFormView();
            showDataSetView();
        } else {
            showFormView();
        }
    }

    private void showFormView() {
        Component activeViewParent = getActiveViewParent();

        if (formViewContainer != null) {
            formViewContainer.setParent(activeViewParent);
        } else {
            activeView.setParent(activeViewParent);
        }

        if (formView != null && formView.getCustomView() != null) {
            clearActions();


            Binder binder = BinderUtil.getBinder(formView.getFirstChild());
            if (binder != null) {
                Object viewModel = binder.getViewModel();
                if (viewModel instanceof FormCrudViewModel) {
                    ((FormCrudViewModel) viewModel).initForm(this, formView);
                }
            }

        }

    }

    protected Component getActiveViewParent() {
        Component activeViewParent = layout;

        if (layout instanceof Borderlayout) {
            Borderlayout borderlayout = (Borderlayout) layout;
            activeViewParent = borderlayout.getCenter();
        }
        return activeViewParent;
    }

    private void destroyFormView() {
        if (formView != null) {
            formView.detach();
            formView = null;
        }

        if (formViewContainer != null) {
            formViewContainer.detach();
            formViewContainer = null;
        }
    }

    private void showDataSetView() {
        ((Component) dataSetView).setParent(getActiveViewParent());
    }

    protected void addSubCrudView(FormView<T> formView, final Field field, final String label) {
        if (field.getParams().get(Viewers.PARAM_INPLACE) == Boolean.TRUE) {
            View view = loadSubview(field);
            Component parent = null;
            FormFieldComponent parentField = formView.getFieldComponent(field.getName());
            if (parentField != null) {
                parent = parentField.getInputComponent().getParent();
                parentField.getInputComponent().detach();
            }
            if (view instanceof XulElement && field.getParams().containsKey(Viewers.PARAM_HEIGHT)) {
                ((XulElement) view).setHeight((String) field.getParams().get(Viewers.PARAM_HEIGHT));
            }
            if (parent != null) {
                ((Component) view).setParent(parent);
            } else {
                this.formView.addSubview(label, view);
            }
            if (view instanceof CrudView) {
                ((CrudView) view).getController().doQuery();
            }
        } else {
            formViewContainer.addView(label, p -> CrudView.this.loadSubview(field));
        }
    }

    protected void addSubGenericView(final Field field) {

        if (field.getParams().get(Viewers.PARAM_INPLACE) != Boolean.TRUE) {

            MultiViewListener listener = null;
            if (field.getParams().get(Viewers.PARAM_MULTIVIEW_LISTENER) != null) {
                listener = BeanUtils.newInstance(field.getParams().get(Viewers.PARAM_MULTIVIEW_LISTENER).toString());
                if (listener != null) {
                    BeanUtils.setupBean(listener, field.getParams());
                }
            }

            formViewContainer.addView(field.getLabel(), listener, parentView -> {
                Viewer viewer = new Viewer();
                BeanUtils.setupBean(viewer, field.getParams());
                return viewer.getView();
            });
        }
    }

    private boolean isWritableAction(Action action) {
        return action instanceof NewAction || action instanceof EditAction || action instanceof DeleteAction;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public void setSource(Object source) {
        this.source = source;
        if (onSourceChange != null) {
            onSourceChange.accept(source);
        }
    }

    public boolean isQueryProjection() {
        return queryProjection;
    }

    public void setQueryProjection(boolean queryProjection) {
        this.queryProjection = queryProjection;
        if (controller != null) {
            controller.setQueryProjection(queryProjection);
        }

    }

    public String getFormViewDescriptorId() {
        return formViewDescriptorId;
    }

    public void setFormViewDescriptorId(String formViewDescriptorId) {
        this.formViewDescriptorId = formViewDescriptorId;
    }

    public LocalizedMessagesProvider getMessagesProvider() {
        return messagesProvider;
    }

    public void setMessagesProvider(LocalizedMessagesProvider messagesProvider) {
        this.messagesProvider = messagesProvider;
    }

    private static class ComponentComparator implements Comparator<Component> {

        @Override
        public int compare(Component o1, Component o2) {
            Action a1 = (Action) o1.getAttribute(ACTION);
            Action a2 = (Action) o2.getAttribute(ACTION);
            return a1.compareTo(a2);
        }
    }

    private View loadSubview(Field field) {

        Class subentityClass = field.getPropertyInfo().getGenericType();
        if (subentityClass == null && field.getParams().containsKey("genericType")) {
            try {
                subentityClass = Class.forName((String) field.getParams().get("genericType"));
            } catch (ClassNotFoundException e) {
                throw new ViewRendererException(
                        "Cannot load subcrud view " + field.getLabel() + " not class found for subentity", e);
            }
        }

        if (subentityClass == null) {
            throw new ViewRendererException(
                    "Field " + field.getName() + " dont have a generic type, i cannot build a crudview");
        }

        Object parent = formView.getValue();
        String parentName = BeanUtils.findParentPropertyName(getViewDescriptor().getBeanClass(), subentityClass);
        if (field.getParams().get("parentName") != null) {
            parentName = field.getParams().get("parentName").toString();
        }
        SubcrudController subcrudController = new SubcrudController(subentityClass, parent, parentName,
                field.getName());

        try {
            String subcontrollerClass = (String) field.getParams().get(Viewers.PARAM_CONTROLLER);
            if (subcontrollerClass != null) {
                Constructor constructor = Class.forName(subcontrollerClass).getConstructor(Class.class, Object.class,
                        String.class, String.class);
                if (constructor != null) {
                    subcrudController = (SubcrudController) constructor.newInstance(subentityClass, parent, parentName,
                            field.getName());
                } else {
                    throw new ViewRendererException("No valid constructor found in custom SubcrudController class");
                }
            }

        } catch (Exception e) {
            throw new ViewRendererException("Cannot instanciate custom SubcrudController", e);
        }

        ViewDescriptor descriptor = Viewers.getViewDescriptor(subentityClass, "crud");
        CrudViewRenderer crudViewRenderer = getCrudViewRenderer();
        CrudView subView = crudViewRenderer.render(descriptor, null, subcrudController);
        getController().addSubcrudController(subcrudController);

        return subView;
    }

    protected CrudViewRenderer getCrudViewRenderer() {
        return new CrudViewRenderer<>();
    }


    private void loadMenuActions() {
        if (contextMenu != null && dataSetView != null && dataSetView.getSelected() != null) {
            Object value = dataSetView.getSelected();
            Class valueClass = value.getClass();
            Class lastClass = (Class) contextMenu.getAttribute("LastClass");
            if (valueClass != lastClass) {
                lastClass = valueClass;
                contextMenu.getChildren().clear();
                contextMenu.getAttributes().put("LastClass", lastClass);

                renderMenuActions(valueClass);
            }
        }
    }

    protected void loadActions(final CrudState state) {
        clearActions();

        this.applicableActions = findApplicableActions(getBeanClass(), state);
        Map<String, ActionGroup> groups = new TreeMap<>();
        for (final Action action : applicableActions) {
            if (action.isEnabled()) {
                String groupName = "default";
                if (action.getGroup() != null) {
                    groupName = action.getGroup().getName();
                }
                ActionGroup group = groups.get(groupName);
                if (group == null) {
                    group = new ActionGroup(groupName);
                    if (action.getGroup() != null) {
                        group.setAlign(action.getGroup().getAlign());
                    }
                    groups.put(groupName, group);
                }
                group.getActions().add(action);

                if (action instanceof CrudControllerAware) {
                    ((CrudControllerAware) action).setCrudController(getController());
                }
            }
        }
        for (ActionGroup group : groups.values()) {
            showActionGroup(group);
        }

    }

    protected void showActionGroup(final ActionGroup actionGroup) {
        for (Action action : actionGroup.getActions()) {
            showAction(actionGroup, action);
        }
        if (actionGroup.getAlign().equals("right")) {
            toolbarRight.addSeparator();
        } else {
            toolbarLeft.addSeparator();
        }
    }

    protected void showAction(final ActionGroup actionGroup, Action action) {
        if (isReadonly() && isWritableAction(action)) {
            return;
        }

        if ("right".equals(actionGroup.getAlign())) {
            toolbarRight.addAction(action);
        } else {
            toolbarLeft.addAction(action);
        }
    }

    protected List<CrudAction> findApplicableActions(final Class targetClass, final CrudState state) {
        ActionLoader loader = new ActionLoader(CrudAction.class);
        loader.setActionAttributes(actionsParams);
        List<CrudAction> allowedActions = loader.load((ObjectMatcher<CrudAction>) crudAction -> {
            if (isReadonly()) {
                return isApplicable(targetClass, state, crudAction) && crudAction instanceof ReadableOnly;
            } else {
                return isApplicable(targetClass, state, crudAction);
            }
        });

        for (CrudAction action : this.actions) {
            if (isApplicable(targetClass, state, action)) {
                allowedActions.add(action);
            }
        }
        allowedActions.sort(new ActionComparator());

        return allowedActions;
    }

    private boolean isApplicable(final Class targetClass, final CrudState state, CrudAction crudAction) {
        boolean applicableState = CrudState.isApplicable(state, crudAction.getApplicableStates());
        boolean applicableClass = ApplicableClass.isApplicable(targetClass, crudAction.getApplicableClasses(), true);
        return applicableClass && applicableState;
    }

    @Override
    public ActionEvent buildActionEvent(Object source, Map<String, Object> params) {
        Object data = null;

        switch (getState()) {
            case CREATE:
            case UPDATE:
                data = getValue();
                break;
            case READ:
            case DELETE:
                data = getDataSetView().getSelected();
                break;
        }

        if (data instanceof BeanMap && ((BeanMap) data).getId() != null) {
            CrudService crudService = crudServiceName != null ? Containers.get().findObject(crudServiceName, CrudService.class) : Containers.get().findObject(CrudService.class);
            if (crudService != null) {
                BeanMap beanMap = (BeanMap) data;
                data = crudService.find(beanMap.getBeanClass(), (Serializable) beanMap.getId());
            }
        }

        return new CrudActionEvent(data, source, params, this, this.getController());
    }

    private void fireChangedStateListeners(CrudState newState, CrudState oldState) {
        ChangedStateEvent evt = new ChangedStateEvent(newState, oldState, this);
        // FIRST FIRE LOCAL LISTENERS
        for (CrudStateChangedListener localListener : localListeners) {
            localListener.changedState(evt);
        }

        // THEN FIRE GLOBAL LISTENERS
        Collection<CrudStateChangedListener> listeners = Containers.get().findObjects(CrudStateChangedListener.class);
        if (listeners != null && !listeners.isEmpty()) {
            for (CrudStateChangedListener listener : listeners) {
                listener.changedState(evt);
            }
        }
    }

    @Override
    public View getParentView() {
        return parentView;
    }

    @Override
    public void setParentView(View parentView) {
        this.parentView = parentView;
    }

    public Menupopup getContextMenu() {
        return contextMenu;
    }

    public void setActionsParams(Map actionsParams) {
        this.actionsParams = actionsParams;
    }

    public static Window showUpdateView(String title, Class clazz, Object value) {
        return showUpdateView(title, clazz, value, null);
    }

    public static Window showUpdateView(String title, Class clazz, Object value, Callback callback) {

        final Viewer viewer = new Viewer("crud", clazz);
        final Window window = ZKUtil.showDialog(title, viewer, "90%", "98%");

        CrudView crudView = (CrudView) viewer.getView();
        crudView.setState(CrudState.UPDATE);
        crudView.setValue(value);
        crudView.getController().setEntity(value);
        crudView.addCrudStateChangedListener(evt -> {
            if (evt.getNewState() != CrudState.UPDATE) {
                window.detach();
                if (callback != null) {
                    callback.doSomething();
                }
            }
        });
        return window;
    }


    public static void showUpdateViewPage(String title, Class clazz, Object value) {

        final Viewer viewer = new Viewer("crud", clazz);

        CrudView crudView = (CrudView) viewer.getView();
        crudView.setState(CrudState.UPDATE);
        crudView.setValue(value);
        crudView.getController().setEntity(value);
        crudView.addCrudStateChangedListener(evt -> {
            if (evt.getNewState() != CrudState.UPDATE) {
                NavigationManager.getCurrent().closeCurrentPage();
            }

        });

        Page page = new ComponentPage("edit" + clazz.getName(), title, viewer);
        page.setTemporal(true);
        page.setAlwaysAllowed(true);
        NavigationManager.getCurrent().setCurrentPage(page);
    }

    public static void showCreateViewPage(String title, Class clazz) {

        final Viewer viewer = new Viewer("crud", clazz);
        CrudView crudView = (CrudView) viewer.getView();
        crudView.setState(CrudState.CREATE);
        crudView.getController().newEntity();
        crudView.addCrudStateChangedListener(evt -> {
            if (evt.getNewState() == CrudState.READ) {
                NavigationManager.getCurrent().closeCurrentPage();
            }
        });

        Page page = new ComponentPage("create" + clazz.getName(), title, viewer);
        page.setAlwaysAllowed(true);
        page.setTemporal(true);
        NavigationManager.getCurrent().setCurrentPage(page);
    }

    public static Window showCreateView(String title, Class clazz) {
        return showCreateView(title, clazz, null);
    }

    public static Window showCreateView(String title, Class clazz, Callback callback) {

        final Viewer viewer = new Viewer("crud", clazz);
        CrudView crudView = (CrudView) viewer.getView();
        crudView.setState(CrudState.CREATE);
        crudView.getController().newEntity();
        crudView.addCrudStateChangedListener(evt -> {
            if (evt.getNewState() == CrudState.READ) {
                Window window = (Window) viewer.getParent();
                window.detach();
                if (callback != null) {
                    callback.doSomething();
                }
            }
        });
        return ZKUtil.showDialog(title, viewer, "90%", "98%");
    }

    public static void showCrudView(String title, Class clazz) {

        final Viewer viewer = new Viewer("crud", clazz);

        ZKUtil.showDialog(title, viewer, "90%", "98%");

    }

    public static CrudDataSetViewBuilder getDataSetViewBuilder(String typeName) {
        for (CrudDataSetViewBuilder viewBuilder : Containers.get().findObjects(CrudDataSetViewBuilder.class)) {
            if (viewBuilder.getViewTypeName().equals(typeName)) {
                return viewBuilder;
            }
        }
        return null;
    }

    public Component getLayout() {
        return layout;
    }

    public String getCrudServiceName() {
        return crudServiceName;
    }

    public void setCrudServiceName(String crudServiceName) {
        this.crudServiceName = crudServiceName;
    }

    @Override
    public void handleValidationError(ValidationError error) {
        if (error != null && error.getInvalidProperty() != null && formView != null) {
            FormFieldComponent fieldComponent = formView.getFieldComponent(error.getInvalidProperty());
            if (fieldComponent != null) {
                throw new WrongValueException(fieldComponent.getInputComponent(), error.getMessage());
            }
        }
    }

    @Override
    public void setTitle(String title) {
        if (title != null && messagesProvider != null) {
            title = messagesProvider.getMessage(title, Viewers.buildMessageClasffier(getFormView().getViewDescriptor()), Messages.getDefaultLocale(), title);
        }
        getFormView().setTitle(title);
    }

    @Override
    public List<CrudAction> getActions() {
        return applicableActions;
    }

    @Override
    public String toString() {
        return super.toString() + ". " + getViewDescriptor();
    }

    public void onSourceChanged(Consumer onSourceChange) {
        this.onSourceChange = onSourceChange;
    }

}
