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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.WrongValuesException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import org.zkoss.zul.ext.Paginal;
import tools.dynamia.commons.*;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.crud.CrudControllerAPI;
import tools.dynamia.domain.CrudServiceException;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.jdbc.QueryInterruptedException;
import tools.dynamia.domain.query.*;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.table.TableView;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class works as crud controllers for ZK web applications
 *
 * @author Ing. Mario Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CrudController<E> extends SelectorComposer implements Serializable, CrudControllerAPI<E> {

    /**
     *
     */
    private static final long serialVersionUID = 5762960709271600367L;
    private E entity;
    private E example;
    private E selected;
    private DataSet queryResult;
    private QueryParameters params;
    private Class<E> entityClass;
    protected LoggingService logger;
    protected CrudService crudService;
    private boolean autoClearPage = false;
    private boolean autoReloadEntity = true;
    private final List<SubcrudController> subcontrollers = new ArrayList<>();
    // util
    private String name;
    private Window currentDialog;
    private final BeanSorter sorter = new BeanSorter();
    private DataPaginator dataPaginator;
    // auto wired zk components
    private Paginal paginator;
    protected DataSetView dataSetView;
    private boolean alwaysFindByExample = false;
    private boolean saved;
    private boolean deleted;
    private final Map<String, Object> defaultEntityValues = new HashMap<>();
    private boolean confirmBeforeSave;
    private Callback onSaveCallback;
    private boolean queryProjection;
    private QueryParameters defaultParameters;
    private Map<String, Object> attributes = new HashMap<>();
    private boolean saveWithNewTransaction = true;


    public CrudController() {
        this(null);
    }

    public CrudController(Class entityClass) {
        this.entityClass = entityClass;
        init();
    }

    public CrudController(Class entityClass, CrudService crudService) {
        this.entityClass = entityClass;
        this.crudService = crudService;
        init();
    }

    public void addSubcrudController(SubcrudController subController) {
        subController.setParentEntity(getEntity());
        subcontrollers.add(subController);
    }

    @Override
    public boolean isConfirmBeforeSave() {
        return confirmBeforeSave;
    }

    @Override
    public void setConfirmBeforeSave(boolean confirmBeforeSave) {
        this.confirmBeforeSave = confirmBeforeSave;
    }

    protected List<SubcrudController> getSubcontrollers() {
        return subcontrollers;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#setCrudService(com.dynamia
     * .tools.domain.services.CrudService)
     */
    @Override
    public void setCrudService(CrudService crudService) {
        this.crudService = crudService;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#getCrudService()
     */
    @Override
    public CrudService getCrudService() {
        return crudService;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        Object ent = getArg("entity");

        if (ent != null && ent.getClass().equals(entityClass)) {
            setEntity((E) ent);
            reloadEntity();
        }

        Object dialog = getArg("dialog");

        if (dialog != null && dialog instanceof Window) {
            this.currentDialog = (Window) dialog;
        }

        afterPageLoaded();
    }


    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#save()
     */
    @Override
    public void save() {
        logger.debug("Saving entity " + entityClass);
        if (isSaveWithNewTransaction()) {
            crudService.executeWithinTransaction(() -> crudService.save(entity, DomainUtils.findEntityId(entity)));
        } else {
            crudService.save(entity, DomainUtils.findEntityId(entity));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#delete()
     */
    @Override
    public void delete() {
        if (DomainUtils.isEntity(getSelected())) {
            crudService.delete(getSelected().getClass(), DomainUtils.findEntityId(getSelected()));
        } else {
            crudService.delete(getSelected());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#query()
     */
    @Override
    public void query() {

        try {
            if (BeanUtils.isAssignable(entityClass, QueryExecuter.class)) {
                QueryExecuter queryExecutor = (QueryExecuter) BeanUtils.newInstance(entityClass);
                setQueryResult(new ListDataSet(queryExecutor.executeQuery(crudService, getParams())));
            } else if (alwaysFindByExample) {
                setQueryResult(new ListDataSet(crudService.findByExample(getExample(), getParams())));
            } else if (queryProjection) {
                setQueryResult(new ListDataSet(crudService.executeQuery(createQueryProjection(), getParams())));
            } else {
                setQueryResult(new ListDataSet(crudService.find(entityClass, getParams())));
            }

        } catch (CrudServiceException e) {
            if (getQueryResult() == null || getQueryResult().getSize() <= 0) {
                UIMessages.showMessage("La consulta no arrojo resultados");
            }
        } catch (QueryInterruptedException e) {
            logger.error(e);
            Messagebox.show("La consulta demora mucho tiempo en procesarse, por favor utilice otros filtros" +
                            " o intente mas tarde. Por ejemplo, si esta usando un rango de fechas reduzca la diferencia. ", "Error al Consultar",
                    Messagebox.OK, Messagebox.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            UIMessages.showMessage("Error al consultar: " + e.getMessage(), MessageType.ERROR);
        }
    }

    private QueryBuilder createQueryProjection() {
        List<String> fields = Viewers.getFields(dataSetView.getViewDescriptor()).stream().map(Field::getName).collect(Collectors.toList());
        fields = new ArrayList<>(fields);
        fields.add(0, "id");
        return QueryBuilder.select(fields.toArray(new String[0])).from(entityClass, "e").where(getParams())
                .resultType(BeanMap.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#delete(E)
     */
    @Override
    public final void delete(E entity) {
        setSelected(entity);
        doDelete();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#edit(E)
     */
    @Override
    public final void edit(E entity) {
        setSelected(entity);
        doEdit();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#newEntity()
     */
    @Override
    public void newEntity() {
        if (entityClass != null) {
            try {
                entity = BeanUtils.newInstance(entityClass);
                BeanUtils.setupBean(entity, getDefaultEntityValues());
            } catch (Exception ex) {
                logger.error("Error creating new entity", ex);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#newExample()
     */
    @Override
    public void newExample() {
        if (entityClass != null) {
            try {
                example = BeanUtils.newInstance(entityClass);
            } catch (Exception ex) {
                logger.error("Error creating new example object", ex);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#reloadEntity()
     */
    @Override
    public void reloadEntity() {
        if (entity != null && DomainUtils.findEntityId(entity) != null && autoReloadEntity) {
            entity = crudService.findSingle(entityClass, "id", DomainUtils.findEntityId(entity));
            autoReloadEntity = true;
        }
    }

    public void closeCurrentDialog() {
        if (currentDialog != null) {
            Events.postEvent(Events.ON_CLOSE, currentDialog, this);
            currentDialog.detach();
            currentDialog.setVisible(false);
            currentDialog = null;
        }
    }

    protected void exceptionCaught(Exception e) {
        e.printStackTrace();
        if (e instanceof ValidationError) {
            UIMessages.showMessage(e.getMessage(), MessageType.ERROR);

            throw (ValidationError) e;

        } else {
            logger.error(e);

        }
    }

    // <editor-fold defaultstate="collapsed" desc="Before and After events">
    protected void afterPageLoaded() {
    }

    protected void beforeCreate() {
    }

    protected void afterCreate() {
    }

    protected void beforeSave() {
    }

    protected void afterSave() {
    }

    protected void beforeQuery() {
    }

    protected void afterQuery() {
    }

    protected void beforeEdit() {
    }

    protected void afterEdit() {
    }

    protected void beforeDelete() {
    }

    protected void afterDelete() {
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#getEntity()
     */
    @Override
    public E getEntity() {
        if (entity == null) {
            newEntity();
        }
        return entity;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#setEntity(E)
     */
    @Override
    public void setEntity(E entity) {
        this.entity = entity;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#getExample()
     */
    @Override
    public E getExample() {
        if (example == null) {
            newExample();
        }
        return example;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#getSelected()
     */
    @Override
    public E getSelected() {
        return selected;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#isSaved()
     */
    @Override
    public boolean isSaved() {
        return saved;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#isDeleted()
     */
    @Override
    public boolean isDeleted() {
        return deleted;
    }

    public void setAlwaysFindByExample(boolean alwaysFindByExample) {
        this.alwaysFindByExample = alwaysFindByExample;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#setSelected(E)
     */
    @Override
    public void setSelected(E selected) {
        this.selected = selected;
        if (dataSetView != null) {
            dataSetView.setSelected(selected);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#getParams()
     */
    @Override
    public QueryParameters getParams() {
        return params;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#setParams(com.dynamia.tools
     * .domain.query.QueryParameters)
     */
    @Override
    public void setParams(QueryParameters params) {
        this.params = params;
        if (this.getParams() == null) {
            this.params = new QueryParameters();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.web.crud.ICrudController#getParameter(java.lang.String)
     */
    @Override
    public Object getParameter(String param) {
        return getParams().get(param);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.web.crud.ICrudController#setParemeter(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public void setParemeter(String key, Object value) {
        if (value != null) {
            params.put(key, value);
        } else {
            params.remove(key);
        }
    }

    @Override
    public DataSet getQueryResult() {
        return queryResult;
    }

    public List<E> getQueryResultList() {
        if (queryResult instanceof ListDataSet) {
            return ((ListDataSet) queryResult).getData();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void setQueryResult(DataSet queryResult) {
        this.queryResult = queryResult;
        updateDataSetView();
        afterQuery();

        if (isQueryResultEmpty() && dataSetView.isEmpty()) {
            UIMessages.showMessage("La consulta no arrojo resultados", MessageType.WARNING);
        }
    }

    /**
     * Set result list
     *
     * @param queryResult
     */
    public final void setQueryResult(List queryResult) {
        setQueryResult(new ListDataSet(queryResult));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#isQueryResultEmpty()
     */
    @Override
    public boolean isQueryResultEmpty() {
        if (getQueryResult() == null) {
            return true;
        }
        return getQueryResult().getSize() == 0;
    }

    public boolean isAutoClearPage() {
        return autoClearPage;
    }

    public void setAutoClearPage(boolean autoClearPage) {
        this.autoClearPage = autoClearPage;
    }

    public void setPaginator(Paginal paginator) {
        this.paginator = paginator;
    }

    public void setDataSetView(DataSetView dataSetView) {
        this.dataSetView = dataSetView;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#getDataPaginator()
     */
    @Override
    public DataPaginator getDataPaginator() {
        return dataPaginator;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#getSorter()
     */
    @Override
    public BeanSorter getSorter() {
        return sorter;
    }

    public Window getCurrentDialog() {
        return currentDialog;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#getEntityClass()
     */
    @Override
    public Class<E> getEntityClass() {
        return entityClass;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.web.crud.ICrudController#setEntityClass(java.lang.Class )
     */
    @Override
    public void setEntityClass(Class<E> entityClass) {
        this.entityClass = entityClass;
        if (entityClass != null) {
            name = StringUtils.addSpaceBetweenWords(entityClass.getSimpleName());
        }
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Private Methods">
    private void init() {
        if (crudService == null) {
            crudService = Containers.get().findObject(CrudService.class);
        }

        if (logger == null) {
            logger = Containers.get().findObject(LoggingService.class);
        }

        if (logger == null) {
            logger = new SLF4JLoggingService(CrudController.class);
        }

        params = new QueryParameters();
        if (entityClass == null) {
            try {
                setEntityClass(BeanUtils.getGenericTypeClass(this));

            } catch (Exception e) {
                logger.warn(
                        "Cannot get generic class for EntityClass, you should invoke setEntityClass or use the constructor");
            }
        }

        newEntity();
        afterInit();
    }

    protected void updateDataSetView() {
        if (dataSetView != null) {
            dataSetView.setValue(getQueryResult());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#doQuery()
     */
    @Override
    public final void doQuery() {
        try {
            if (defaultParameters != null && !defaultParameters.isEmpty()) {
                defaultParameters.forEach(this::setParemeter);
            }
            configurePaginator();
            configureSorter();
            beforeQuery();
            query();

        } catch (Exception e) {
            exceptionCaught(e);
        }
    }

    private void configurePaginator() {
        if (paginator != null) {
            dataPaginator = new DataPaginator();
            dataPaginator.setPageSize(paginator.getPageSize());
            getParams().paginate(dataPaginator);

            if (dataSetView.getViewDescriptor().getParams().get("pagination") == Boolean.FALSE) {
                getParams().paginate(null);
            }
        }
    }

    private void configureSorter() {
        if (getParams().getSorter() == null) {
            getParams().sort(sorter);

            if (dataSetView instanceof TableView) {
                TableView tableView = (TableView) dataSetView;
                if (tableView.getOrderBy() != null) {
                    getParams().orderBy(tableView.getOrderBy(), true);
                }
                if (tableView.getMaxResults() > 0) {
                    getParams().setMaxResults(tableView.getMaxResults());
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#doSave()
     */
    @Override
    @Listen("onClick = #save")
    public final void doSave() {
        Callback saveCallbak = () -> {
            saved = false;
            try {
                doDeletesInSubcontrollers();
                beforeSave();
                save();
                doChangesInSubcontrollers();
                showMessageOnSaveSuccessfull();
                saved = true;
                afterSave();
                newEntity();
                closeCurrentDialog();
                if (isAutoClearPage()) {
                    ZKUtil.clearPage(getPage());
                }
                if (onSaveCallback != null) {
                    onSaveCallback.doSomething();
                }
            } catch (WrongValueException | WrongValuesException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Error al guardar " + entityClass, e);
                exceptionCaught(e);
            }
        };

        if (isConfirmBeforeSave()) {
            UIMessages.showQuestion("¿Esta seguro que desea guardar " + name + "?", saveCallbak);
        } else {
            saveCallbak.doSomething();
        }
    }

    @Override
    public void onSave(Callback onSave) {
        this.onSaveCallback = onSave;

    }

    public void showMessageOnSaveSuccessfull() {
        if (name != null) {
            UIMessages.showMessage(name + " guardado correctamente ");
        } else {
            UIMessages.showMessage(getEntity() + " guardado correctamente ");
        }
    }

    protected void doDeletesInSubcontrollers() {
        if (!(this instanceof SubcrudController)) {
            if (!subcontrollers.isEmpty()) {
                for (SubcrudController subcontroller : subcontrollers) {
                    try {
                        subcontroller.doDeletes();
                    } catch (Exception e) {
                        logger.warn("Exception running subcrud controller " + subcontroller + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    protected void doChangesInSubcontrollers() {
        if (!(this instanceof SubcrudController)) { // Parent controllers

            if (!subcontrollers.isEmpty()) {
                for (SubcrudController subcontroller : subcontrollers) {
                    try {
                        subcontroller.doCreates();
                        subcontroller.doUpdates();
                    } catch (Exception e) {
                        logger.warn("Exception running subcrud controller " + subcontroller + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#doSaveAndEdit()
     */
    @Override
    public final void doSaveAndEdit() {
        saved = false;
        try {
            beforeSave();
            save();
            UIMessages.showMessage(name + " guardado correctamente, puede continuar editando el registro ");
            afterSave();
            setSelected(getEntity());
            doEdit();
            saved = true;
        } catch (WrongValueException | WrongValuesException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al guardar " + entityClass, e);
            exceptionCaught(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#doEdit()
     */
    @Override
    @Listen("onClick = #edit")
    public final void doEdit() {
        saved = true;
        Object ent = getSelected();
        if (ent != null) {
            beforeEdit();
            var entityId = DomainUtils.findEntityId(ent);
            if (entityId != null) {
                setEntity(crudService.load(entityClass, entityId));
            } else {
                setEntity((E) ent);
            }
            afterEdit();
        } else {
            UIMessages.showMessage("Seleccione " + name + " para editar", MessageType.WARNING);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#doDelete()
     */
    @Override
    @Listen("onClick = #delete")
    public final void doDelete() {

        deleted = false;
        if (getSelected() != null) {
            beforeDelete();
            UIMessages.showQuestion(
                    "¿Esta seguro que desea borrar: " + name + "  " + getSelected().toString() + "?", () -> {
                        try {
                            delete();

                            afterDelete();
                            doQuery();
                            deleted = true;
                            UIMessages.showMessage(getSelected() + " borrado exitosamente");
                            setSelected(null);
                        } catch (ValidationError e) {
                            UIMessages.showMessage(e.getMessage(), MessageType.WARNING);
                        } catch (Exception e) {
                            logger.error(e);
                            if (e.getMessage() != null && e.getMessage().contains("ConstraintViolationException")) {
                                UIMessages.showMessage("No se puede eliminar " + name + " porque esta siendo usado o tiene registros asociados",
                                        MessageType.WARNING);
                            } else {
                                UIMessages.showMessage("Error al eliminar " + name + " contacte al administrador del sistema",
                                        MessageType.ERROR);

                            }

                        }
                    });
        } else {
            UIMessages.showMessage("Seleccione " + name + " para borrar", MessageType.WARNING);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.web.crud.ICrudController#doCreate()
     */
    @Override
    @Listen("onClick = #create")
    public final void doCreate() {
        saved = false;
        beforeCreate();
        newEntity();
        afterCreate();
    }

    // </editor-fold>
    @Override
    public Map<String, Object> getDefaultEntityValues() {
        return defaultEntityValues;
    }

    public Object getArg(Object name) {

        return Executions.getCurrent().getArg().get(name);
    }

    protected void afterInit() {
    }


    @Override
    public void clear() {
        queryResult = null;
        params = new QueryParameters();
    }

    protected void log(String message) {
        logger.info(message);
    }

    protected void log(String messsage, Throwable exception) {
        logger.error(messsage, exception);
    }

    public boolean isQueryProjection() {
        return queryProjection;
    }

    public void setQueryProjection(boolean queryProjection) {
        this.queryProjection = queryProjection;
    }

    public QueryParameters getDefaultParameters() {
        return defaultParameters;
    }

    public void setDefaultParameters(QueryParameters defaultParameters) {
        this.defaultParameters = defaultParameters;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public boolean isSaveWithNewTransaction() {
        return saveWithNewTransaction;
    }

    public void setSaveWithNewTransaction(boolean saveWithNewTransaction) {
        this.saveWithNewTransaction = saveWithNewTransaction;
    }
}
