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
package tools.dynamia.zk.crud.ui;

/**
 *
 * @author Ing. Mario Serrano Leones
 */
public class RootTreeNode<E> extends EntityTreeNode<E> {

	/**
	 *
	 */
	private static final long serialVersionUID = -1401576873934370681L;
	private boolean visible;

	public RootTreeNode(Object entity) {
		this(entity, null);
	}

	public RootTreeNode(Object entity, String label) {
		this(entity, null, label);
	}

	public RootTreeNode(Object entity, String icon, String label) {
		this(icon, label, false);
	}

	public RootTreeNode(String icon, String label, boolean visible) {
		super(null);
		setLabel(label);
		setIcon(icon);
		this.visible = visible;
		super.root = true;
	}

	public RootTreeNode(String label, String icon) {
		this(label, icon, false);
	}

	public RootTreeNode(String label) {
		this(label, null);
	}

	public boolean isVisible() {
		return visible;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	public void addChild(EntityTreeNode<E> child) {
		super.addChild(child);
	}

}
