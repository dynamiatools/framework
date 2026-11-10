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
package tools.dynamia.crud;

import tools.dynamia.actions.ActionEvent;
import tools.dynamia.commons.BeanMap;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;

import java.io.Serializable;

import java.util.Map;

@SuppressWarnings("rawtypes")
public class CrudActionEvent extends ActionEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final CrudViewComponent crudView;
    private final CrudControllerAPI controller;

    public CrudActionEvent(Object data, Object source, CrudViewComponent view, CrudControllerAPI controller) {
        this(data, source, null, view, controller);
    }

    public CrudActionEvent(Object data, Object source, Map<String, Object> params, CrudViewComponent view, CrudControllerAPI controller) {
        super(data, source, params);
        this.crudView = view;
        this.controller = controller;
    }

    public CrudViewComponent getCrudView() {
        return crudView;
    }


    public CrudControllerAPI getController() {
        return controller;
    }


    /**
     * Same as {@link ActionEvent#getData(Class)}, and if the data is still a read-only {@link BeanMap} of that type
     * (Open Persistence In View disabled) the entity is loaded by id with the application {@link CrudService}.
     *
     * @param type the expected type
     * @param <T>  the expected type
     * @return the data, or {@code null} if there is none or it is not a {@code type}
     */
    @Override
    public <T> T getData(Class<T> type) {
        if (super.getData() instanceof BeanMap row && row.isBeanOf(type) && row.isLoadable()) {
            CrudService crudService = Containers.get().findObject(CrudService.class);
            if (crudService != null) {
                setData(crudService.load(row.getBeanClass(), (Serializable) row.getId()));
            }
        }
        return super.getData(type);
    }

    /**
     * Returns the data typed like {@link #getData(Class)}, or fails when there is none.
     *
     * <pre>{@code
     * Venta venta = evt.requireData(Venta.class, "Seleccione venta");
     * }</pre>
     *
     * @param type    the expected type
     * @param message the message of the {@link ValidationError} to throw, shown to the user as is
     * @param <T>     the expected type
     * @return the data, never {@code null}
     * @throws ValidationError if there is no data of that type
     */
    public <T> T requireData(Class<T> type, String message) {
        T data = getData(type);
        if (data == null) {
            throw new ValidationError(message);
        }
        return data;
    }
}
