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
package tools.dynamia.ui.testing;

import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.crud.headless.HeadlessCrudController;
import tools.dynamia.crud.headless.HeadlessCrudView;
import tools.dynamia.domain.services.CrudService;

/**
 * The CRUD side of an action under test: the headless controller and view the REST layer uses, over a {@link CrudService}
 * you provide (real or mock). It exposes what happened to the CRUD while the action ran.
 *
 * @param <E> entity type
 */
public final class TestCrud<E> {

    private final HeadlessCrudController<E> controller;
    private final HeadlessCrudView<E> view;

    private TestCrud(Class<E> entityClass, CrudService crudService, CrudState state) {
        this.controller = new HeadlessCrudController<>(entityClass, crudService);
        this.view = new HeadlessCrudView<>(controller, state);
    }

    /**
     * @param entityClass the entity the CRUD handles
     * @param crudService the service behind it
     * @param <E>         entity type
     * @return a CRUD in {@link CrudState#READ}
     */
    public static <E> TestCrud<E> of(Class<E> entityClass, CrudService crudService) {
        return new TestCrud<>(entityClass, crudService, CrudState.READ);
    }

    /**
     * @param state the state the CRUD is in when the action starts
     * @return this CRUD
     */
    public TestCrud<E> inState(CrudState state) {
        view.setState(state);
        return this;
    }

    /** @return the controller the action receives */
    public HeadlessCrudController<E> controller() {
        return controller;
    }

    /** @return the CRUD view the action receives */
    public HeadlessCrudView<E> view() {
        return view;
    }

    /**
     * @param data   the data of the event: usually the selected entity
     * @param source the action
     * @return the event to hand to the action
     */
    CrudActionEvent event(Object data, Object source, java.util.Map<String, Object> params) {
        return new CrudActionEvent(data, source, params, view, controller);
    }

    /** @return whether the action asked the CRUD to query again */
    public boolean queried() {
        return controller.isQueryRequested();
    }

    /** @return whether the last save went through */
    public boolean saved() {
        return controller.isSaved();
    }

    /** @return whether the last delete went through */
    public boolean deleted() {
        return controller.isDeleted();
    }

    /** @return the entity returned by the last save */
    public E savedEntity() {
        return controller.getSavedEntity();
    }

    /** @return the state the CRUD ended in */
    public CrudState state() {
        return view.getState();
    }
}
