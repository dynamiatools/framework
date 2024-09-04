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

import tools.dynamia.domain.ValidationError;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.viewers.View;

import java.util.List;

/**
 * CrudView API
 *
 * @param <T>
 */
public interface CrudViewComponent<T> extends View<T> {

    /**
     * Retunr current {@link CrudAction}s
     *
     */
    List<CrudAction> getActions();

    /**
     * Update crud state
     *
     */
    void setState(CrudState crudState);

    /**
     * Return current state
     *
     */
    CrudState getState();

    /**
     * Get controller
     *
     */
    CrudControllerAPI<T> getController();

    /**
     * Update crud controller
     *
     */

    void setController(CrudControllerAPI<T> controller);

    void handleValidationError(ValidationError error);

    void setTitle(String title);

    DataSetView<T> getDataSetView();

    View<T> getFormView();

    View getParentView();

    Class getObjectClass();

    Object getSource();


}
