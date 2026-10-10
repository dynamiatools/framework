package tools.dynamia.crud.headless;

import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.ActionRuntimes;
import tools.dynamia.actions.HeadlessCapable;
import tools.dynamia.actions.RunsOn;
import tools.dynamia.actions.replay.ReplayExecutor;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.crud.AbstractCrudRemoteAction;
import tools.dynamia.crud.CrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.crud.actions.remote.SaveSupport;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.viewers.JsonView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serves a {@link CrudAction} (the same class ZK runs) to REST clients: this is the single piece of code that adapts
 * a local action to the {@code /api/app/metadata/entities/{id}/actions/{action}} endpoint.
 * <p>
 * The delegate must be declared {@code @RunsOn(HEADLESS)}. For every request the adapter builds what the action expects in its
 * {@link CrudActionEvent}: the entity (loaded by {@code dataId}, or built from the JSON of {@code data}), a
 * {@link HeadlessCrudController} and a {@link HeadlessCrudView}; and runs {@code actionPerformed} through the
 * {@link ReplayExecutor}, which turns the questions of the action into steps of the flow protocol.
 */
@RunsOn(ActionRuntime.HEADLESS)
public final class HeadlessCrudRemoteAction extends AbstractCrudRemoteAction {

    private final CrudAction delegate;

    /**
     * @param delegate the action to serve; it must be declared {@code HEADLESS}
     */
    public HeadlessCrudRemoteAction(CrudAction delegate) {
        if (ActionRuntimes.of(delegate) != ActionRuntime.HEADLESS) {
            throw new IllegalArgumentException(delegate.getClass().getName() + " is not declared HEADLESS");
        }
        this.delegate = delegate;
        setId(delegate instanceof HeadlessCapable capable ? capable.headlessId() : ActionRuntimes.headlessId(delegate.getClass()));
        setName(delegate.getName());
        setDescription(delegate.getDescription());
        setImage(delegate.getImage());
        setGroup(delegate.getGroup());
        setPosition(delegate.getPosition());
        setApplicableStates(delegate.getApplicableStates());
        setApplicableClasses(delegate.getApplicableClasses());
    }

    /** @return the action this one serves */
    public CrudAction getDelegate() {
        return delegate;
    }

    @Override
    public ActionExecutionResponse execute(ActionExecutionRequest request) {
        return ReplayExecutor.execute(getId(), request, original -> run(original));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object run(ActionExecutionRequest request) {
        CrudService crudService = DomainUtils.lookupCrudService();
        Class entityClass = SaveSupport.resolveEntityClass(request.getDataType());
        CrudState state = stateOf(request);

        var controller = new HeadlessCrudController(entityClass, crudService);
        var view = new HeadlessCrudView(controller, state);
        List<Object> many = entitiesOf(request, entityClass, crudService);
        Object entity = many.isEmpty() ? entityOf(request, entityClass, crudService) : many.get(0);
        if (many.size() > 1) {
            controller.setSelection(many);
        }
        view.setValue(entity);

        Map<String, Object> params = request.getParams() != null ? new HashMap<>(request.getParams()) : new HashMap<>();
        delegate.actionPerformed(new CrudActionEvent(entity, null, params, view, controller));

        Map<String, Object> result = new HashMap<>();
        result.put("saved", controller.isSaved());
        result.put("deleted", controller.isDeleted());
        result.put("state", view.getState());
        if (controller.getSavedEntity() != null) {
            result.put("entity", toJson(controller.getSavedEntity(), entityClass));
        }
        return result;
    }

    /**
     * The saved entity as the REST API shows it (its {@code json-form} descriptor): serializing the entity itself would
     * touch lazy associations once the transaction is over.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object toJson(Object entity, Class entityClass) {
        var descriptor = SaveSupport.jsonFormDescriptor(entityClass);
        return StringPojoParser.parseJsonToMap(new JsonView<>(entity, descriptor).renderJson());
    }

    /**
     * {@code data: {"ids": [...]}} or a list body: the records of a bulk action (delete several). Empty for the usual
     * single record.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Object> entitiesOf(ActionExecutionRequest request, Class entityClass, CrudService crudService) {
        if (request.getDataId() != null && !request.getDataId().isBlank()) {
            return List.of();
        }
        Object ids = request.getData() instanceof Map<?, ?> map ? map.get("ids") : request.getData();
        if (!(ids instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        List<Object> entities = new ArrayList<>();
        for (Object id : list) {
            Object entity = crudService.find(entityClass, (Serializable) id);
            if (entity == null) {
                throw new IllegalArgumentException(entityClass.getSimpleName() + " with id " + id + " not found");
            }
            entities.add(entity);
        }
        return entities;
    }

    /** An action that applies to existing records works on the one in {@code dataId}; one that saves, on the {@code data} map. */
    private CrudState stateOf(ActionExecutionRequest request) {
        var states = getApplicableStates();
        boolean bulk = request.getData() instanceof Map<?, ?> m && m.get("ids") instanceof List<?>
                || request.getData() instanceof List<?>;
        if (CrudState.isApplicable(CrudState.READ, states) && (request.getDataId() != null || bulk)) {
            return CrudState.READ;
        }
        if (request.getData() instanceof Map<?, ?> map && map.get("id") != null) {
            return CrudState.UPDATE;
        }
        return CrudState.CREATE;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object entityOf(ActionExecutionRequest request, Class entityClass, CrudService crudService) {
        if (request.getDataId() != null && !request.getDataId().isBlank()) {
            Object entity = crudService.find(entityClass, (Serializable) request.getDataId());
            if (entity == null) {
                throw new IllegalArgumentException(entityClass.getSimpleName() + " with id " + request.getDataId() + " not found");
            }
            return entity;
        }
        if (request.getData() instanceof Map<?, ?> data) {
            var map = (Map<String, Object>) data;
            Object id = map.get("id");
            Object entity = id != null ? crudService.find(entityClass, (Serializable) id) : ObjectOperations.newInstance(entityClass);
            if (entity == null) {
                throw new IllegalArgumentException(entityClass.getSimpleName() + " with id " + id + " not found");
            }
            SaveSupport.applyPatch(entity, SaveSupport.jsonFormDescriptor(entityClass), map);
            return entity;
        }
        throw new IllegalArgumentException("The action needs \"dataId\" or an object in \"data\"");
    }
}
