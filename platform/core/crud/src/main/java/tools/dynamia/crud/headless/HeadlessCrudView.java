package tools.dynamia.crud.headless;

import tools.dynamia.crud.CrudAction;
import tools.dynamia.crud.CrudControllerAPI;
import tools.dynamia.crud.CrudState;
import tools.dynamia.crud.CrudViewComponent;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;

import java.util.List;

/**
 * {@link CrudViewComponent} of a headless run: it only keeps the CRUD state the action leaves it in (an action that
 * saves moves the view to {@code READ}) and refuses what a screen would do. A validation error is not displayed, it
 * is thrown again so the REST layer answers with a {@code 422}.
 *
 * @param <E> entity type
 */
public class HeadlessCrudView<E> implements CrudViewComponent<E> {

    private CrudControllerAPI<E> controller;
    private CrudState state;
    private E value;
    private ViewDescriptor viewDescriptor;
    private String title;

    public HeadlessCrudView(CrudControllerAPI<E> controller, CrudState state) {
        this.controller = controller;
        this.state = state;
    }

    /** @return the title an action asked the view to show, or {@code null} */
    public String getTitle() {
        return title;
    }

    @Override
    public List<CrudAction> getActions() {
        return List.of();
    }

    @Override
    public void setState(CrudState crudState) {
        this.state = crudState;
    }

    @Override
    public CrudState getState() {
        return state;
    }

    @Override
    public CrudControllerAPI<E> getController() {
        return controller;
    }

    @Override
    public void setController(CrudControllerAPI<E> controller) {
        this.controller = controller;
    }

    @Override
    public void handleValidationError(ValidationError error) {
        throw error;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public DataSetView<E> getDataSetView() {
        return null;
    }

    @Override
    public View<E> getFormView() {
        return null;
    }

    @Override
    public View getParentView() {
        return null;
    }

    @Override
    public void setParentView(View view) {
        // no view tree when headless
    }

    @Override
    public Class getObjectClass() {
        return controller.getEntityClass();
    }

    @Override
    public Object getSource() {
        return null;
    }

    @Override
    public E getValue() {
        return value;
    }

    @Override
    public void setValue(E value) {
        this.value = value;
    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }
}
