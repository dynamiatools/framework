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
package tools.dynamia.zk.crud.ui;

import java.util.List;
import java.util.function.Supplier;

public class LazyEntityTreeNode<E> extends EntityTreeNode<E> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private ChildrenLoader<E> loader;

    public LazyEntityTreeNode(String label, String icon) {
        super(null, label, icon);
    }

    public LazyEntityTreeNode(String label, String icon, ChildrenLoader<E> loader) {
        super(null, label, icon);
        setLoader(loader);
    }

    public LazyEntityTreeNode(E entity) {
        super(entity);
    }

    public LazyEntityTreeNode(E entity, ChildrenLoader<E> loader) {
        super(entity);
        setLoader(loader);
    }

    public LazyEntityTreeNode(E entity, String label, ChildrenLoader<E> loader) {
        super(entity, label);
        setLoader(loader);
    }

    public LazyEntityTreeNode(E entity, String label, String icon, ChildrenLoader<E> loader) {
        super(entity, label, icon);
        setLoader(loader);
    }

    public void load() {
        if (loader != null && getChildren().isEmpty()) {
            loader.loadChildren(this);
        }
    }

    public LazyEntityTreeNode(E entity, Supplier<List<E>> supplier) {
        super(entity);
        setLoader((node) -> supplier.get().forEach(node::addChild));
    }

    public ChildrenLoader<E> getLoader() {
        return loader;
    }

    public void setLoader(ChildrenLoader<E> loader) {
        this.loader = loader;
        setOnOpenListener(event -> load());
    }

    @Override
    public boolean isLeaf() {
        return loader == null;
    }

}
