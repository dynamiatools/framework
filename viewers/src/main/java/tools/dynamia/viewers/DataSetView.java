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
package tools.dynamia.viewers;

import tools.dynamia.domain.query.DataSet;

/**
 * The Interface DataSetView.
 *
 * @author Mario A. Serrano Leones
 * @param <V>
 *            the value type
 */
public interface DataSetView<V> extends View<V> {

	/**
	 * Sets the value.
	 *
	 * @param dataSet
	 *            the new value
	 */
    void setValue(DataSet<V> dataSet);

	/**
	 * Gets the selected.
	 *
	 * @return the selected
	 */
    Object getSelected();

	void setSelected(Object selected);

	boolean isEmpty();

}
