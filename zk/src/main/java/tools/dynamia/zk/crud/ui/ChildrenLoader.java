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

/**
 * The Interface ChildrenLoader. Provides functionality to load children for lazy tree nodes.
 * This functional interface defines the contract for dynamically loading child nodes in tree
 * structures when they are needed (lazy loading). It's commonly used in hierarchical data
 * representations like file explorers, organizational charts, category trees, and nested
 * data structures where loading all nodes upfront would be inefficient or impractical.
 * The interface enables performance optimization by deferring child node loading until expansion.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * // Lambda expression for loading department employees
 * ChildrenLoader&lt;Department&gt; deptLoader = node -> {
 *     Department dept = node.getData();
 *     List&lt;Employee&gt; employees = employeeService.findByDepartment(dept);
 *     employees.forEach(emp -> node.add(new LazyEntityTreeNode&lt;&gt;(emp)));
 *     node.setChildrenLoaded(true);
 * };
 * 
 * // Method reference
 * ChildrenLoader&lt;Category&gt; categoryLoader = this::loadSubcategories;
 * 
 * // Usage in tree component
 * LazyEntityTreeNode&lt;Department&gt; rootNode = new LazyEntityTreeNode&lt;&gt;(rootDept);
 * rootNode.setChildrenLoader(deptLoader);
 * </code>
 *
 * @param <E> the type of entity
 * @author Mario A. Serrano Leones
 */
@FunctionalInterface
public interface ChildrenLoader<E> {

    /**
     * Loads children for the specified lazy tree node.
     *
     * @param node the lazy entity tree node
     */
    void loadChildren(LazyEntityTreeNode<E> node);
}
