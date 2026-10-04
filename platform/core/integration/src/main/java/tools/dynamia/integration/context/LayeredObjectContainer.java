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
package tools.dynamia.integration.context;

import tools.dynamia.integration.ObjectContainer;
import tools.dynamia.integration.SimpleObjectContainer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * An object container made of its own objects on top of a parent: a lookup asks its own objects first and then the
 * parent. It is what makes nested {@link ObjectsContext#with(Object...)} scopes add up. Its own objects are never
 * exposed, so a layer is immutable once built and can be shared across threads.
 */
final class LayeredObjectContainer implements ObjectContainer {

    private final ObjectContainer parent;
    private final SimpleObjectContainer own;

    LayeredObjectContainer(ObjectContainer parent, SimpleObjectContainer own) {
        this.parent = parent;
        this.own = own;
    }

    @Override
    public String getName() {
        return "ObjectsContext";
    }

    @Override
    public <T> T getObject(String name, Class<T> type) {
        T found = own.getObject(name, type);
        return found != null || parent == null ? found : parent.getObject(name, type);
    }

    @Override
    public <T> T getObject(Class<T> type) {
        T found = own.getObject(type);
        return found != null || parent == null ? found : parent.getObject(type);
    }

    @Override
    public <T> List<T> getObjects(Class<T> type) {
        var all = new LinkedHashSet<T>(own.getObjects(type));
        if (parent != null) {
            all.addAll(parent.getObjects(type));
        }
        return new ArrayList<>(all);
    }

    @Override
    public Object getObject(String name) {
        Object found = own.getObject(name);
        return found != null || parent == null ? found : parent.getObject(name);
    }
}
