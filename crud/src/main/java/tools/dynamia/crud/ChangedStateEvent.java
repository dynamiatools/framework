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
package tools.dynamia.crud;

public class ChangedStateEvent {

    private final CrudState newState;
    private final CrudState oldState;
    private final GenericCrudView crudView;

    public ChangedStateEvent(CrudState newState, CrudState oldState, GenericCrudView crudView) {
        super();
        this.newState = newState;
        this.oldState = oldState;
        this.crudView = crudView;
    }

    public CrudState getNewState() {
        return newState;
    }

    public CrudState getOldState() {
        return oldState;
    }

    public GenericCrudView getCrudView() {
        return crudView;
    }

}
