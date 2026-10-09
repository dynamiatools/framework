package tools.dynamia.crud.headless;

import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.crud.CrudControllerAPI;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.domain.query.DataSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link CrudControllerAPI} without a user interface, for the actions a REST client runs (see
 * {@link tools.dynamia.actions.replay.ReplayExecutor}). It does what the ZK controller does around saving and
 * deleting (confirmations and messages go through {@link UIMessages}, the work through the {@link CrudService}) and
 * nothing that needs a screen: querying, example forms and paging are not available.
 * <p>
 * The save and delete flows are written here as in {@code zk.crud.CrudController}; moving that shared logic into one
 * class both controllers use is the next step, so the two cannot drift.
 *
 * @param <E> entity type
 */
public class HeadlessCrudController<E> implements CrudControllerAPI<E> {

    private final java.util.Map<String, Object> attributes = new java.util.HashMap<>();
    private static final ClassMessages MESSAGES = ClassMessages.get(HeadlessCrudController.class);

    private CrudService crudService;
    private Class<E> entityClass;
    private E entity;
    private E selected;
    private E savedEntity;
    private List<E> selection = List.of();
    private boolean saved;
    private boolean deleted;
    private boolean confirmBeforeSave;
    private Callback onSaveCallback;
    private final Map<String, Object> parameters = new HashMap<>();

    public HeadlessCrudController(Class<E> entityClass, CrudService crudService) {
        this.entityClass = entityClass;
        this.crudService = crudService;
    }

    /**
     * Records the user selected, for an action that works on several (bulk delete). The action itself still gets one
     * entity in its event, as in ZK; {@link #doDelete()} asks once and deletes all of them.
     *
     * @param selection the selected records, in order
     */
    public void setSelection(List<E> selection) {
        this.selection = selection != null ? List.copyOf(selection) : List.of();
    }

    /** @return what the last successful save returned (the persisted entity), or {@code null} */
    public E getSavedEntity() {
        return savedEntity;
    }

    private String entityName() {
        return entityClass.getSimpleName();
    }

    @Override
    public void save() {
        savedEntity = crudService.save(entity, DomainUtils.findEntityId(entity));
    }

    @Override
    public void doSave() {
        Callback saveCallback = () -> {
            saved = false;
            try {
                save();
                UIMessages.showMessage(MESSAGES.get("savedSuccessfully", entityName()));
                saved = true;
                if (onSaveCallback != null) {
                    onSaveCallback.doSomething();
                }
            } catch (ValidationError e) {
                throw e; // the REST layer turns it into a 422
            } catch (Exception e) {
                UIMessages.showMessage(MESSAGES.get("saveErrorMessage", entityName()), MessageType.ERROR);
            }
        };

        if (isConfirmBeforeSave()) {
            UIMessages.showQuestion(MESSAGES.get("saveConfirmMessage", entityName()), saveCallback);
        } else {
            saveCallback.doSomething();
        }
    }

    @Override
    public void delete() {
        for (E target : targets()) {
            if (DomainUtils.isEntity(target)) {
                crudService.delete(target.getClass(), DomainUtils.findEntityId(target));
            } else {
                crudService.delete(target);
            }
        }
    }

    /** What a delete acts on: the whole selection when there is one, else the selected record. */
    private List<E> targets() {
        return selection.size() > 1 ? selection : (selected != null ? List.of(selected) : List.of());
    }

    @Override
    public void delete(E entity) {
        setSelected(entity);
        doDelete();
    }

    @Override
    public void doDelete() {
        deleted = false;
        if (selected == null) {
            UIMessages.showMessage(MESSAGES.get("deleteSelectItemMessage", entityName()), MessageType.WARNING);
            return;
        }
        UIMessages.getDisplayer().showCustomQuestion(
                targets().size() > 1
                        ? MESSAGES.get("deleteManyConfirmMessage", targets().size(), entityName())
                        : MESSAGES.get("deleteConfirmMessage", entityName(), selected),
                MESSAGES.get("deleteConfirmTitle"),
                MESSAGES.get("deleteYesLabel"), MESSAGES.get("deleteNoLabel"), MessageType.CRITICAL, () -> {
                    try {
                        delete();
                        deleted = true;
                        UIMessages.showMessage(MESSAGES.get("deletedSuccessfully", entityName()), MessageType.NORMAL);
                        selected = null;
                    } catch (ValidationError e) {
                        UIMessages.showMessage(e.getMessage(), MessageType.WARNING);
                    } catch (Exception e) {
                        if (e.getMessage() != null && e.getMessage().contains("ConstraintViolationException")) {
                            UIMessages.showMessage(MESSAGES.get("deleteErrorMessageConstraint", entityName()), MessageType.WARNING);
                        } else {
                            UIMessages.showMessage(MESSAGES.get("deleteErrorMessage", entityName()), MessageType.ERROR);
                        }
                    }
                }, Callback.DO_NOTHING);
    }

    @Override
    public void setCrudService(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public CrudService getCrudService() {
        return crudService;
    }

    @Override
    public E getEntity() {
        return entity;
    }

    @Override
    public void setEntity(E entity) {
        this.entity = entity;
    }

    @Override
    public E getSelected() {
        return selected;
    }

    @Override
    public void setSelected(E selected) {
        this.selected = selected;
    }

    @Override
    public boolean isSaved() {
        return saved;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public Class<E> getEntityClass() {
        return entityClass;
    }

    @Override
    public void setEntityClass(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public boolean isConfirmBeforeSave() {
        return confirmBeforeSave;
    }

    @Override
    public void setConfirmBeforeSave(boolean confirm) {
        this.confirmBeforeSave = confirm;
    }

    @Override
    public void onSave(Callback onSave) {
        this.onSaveCallback = onSave;
    }

    @Override
    public Object getParameter(String param) {
        return parameters.get(param);
    }

    @Override
    public void setParemeter(String key, Object value) {
        parameters.put(key, value);
    }

    @Override
    public void newEntity() {
        // the REST client keeps its own form: nothing to reset
    }

    @Override
    public void reloadEntity() {
        if (entity != null && DomainUtils.findEntityId(entity) != null) {
            entity = crudService.reload(entity);
        }
    }

    @Override
    public void clear() {
        entity = null;
        selected = null;
    }

    // -- screen oriented operations: not available without a view ---------------------------------------

    private static UnsupportedOperationException notHeadless(String operation) {
        return new UnsupportedOperationException(operation + " needs a user interface and is not available when the action runs headless");
    }

    @Override
    public void query() {
        throw notHeadless("query");
    }

    @Override
    public void doQuery() {
        throw notHeadless("doQuery");
    }

    @Override
    public void edit(E entity) {
        throw notHeadless("edit");
    }

    @Override
    public void doEdit() {
        throw notHeadless("doEdit");
    }

    @Override
    public void doCreate() {
        throw notHeadless("doCreate");
    }

    @Override
    public void doSaveAndEdit() {
        throw notHeadless("doSaveAndEdit");
    }

    @Override
    public void newExample() {
        throw notHeadless("newExample");
    }

    @Override
    public E getExample() {
        throw notHeadless("getExample");
    }

    @Override
    public QueryParameters getParams() {
        throw notHeadless("getParams");
    }

    @Override
    public void setParams(QueryParameters params) {
        throw notHeadless("setParams");
    }

    @Override
    public DataSet getQueryResult() {
        throw notHeadless("getQueryResult");
    }

    @Override
    public void setQueryResult(DataSet queryResult) {
        throw notHeadless("setQueryResult");
    }

    @Override
    public boolean isQueryResultEmpty() {
        throw notHeadless("isQueryResultEmpty");
    }

    @Override
    public DataPaginator getDataPaginator() {
        throw notHeadless("getDataPaginator");
    }

    @Override
    public BeanSorter getSorter() {
        throw notHeadless("getSorter");
    }

    @Override
    public Map<String, Object> getDefaultEntityValues() {
        return Map.of();
    }

    @Override
    public java.util.Map<String, Object> getAttributes() {
        return attributes;
    }
}
