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
 * Interface for views that display and manage a collection of data items using {@link DataSet}.
 * <p>
 * This interface extends {@link View} to provide specialized behavior for views that work with
 * datasets, such as tables, grids, or lists. It includes methods for managing the data source,
 * selection state, and checking if the dataset is empty.
 * </p>
 * <p>
 * Implementations are responsible for rendering the dataset items and handling user interactions
 * such as selection and navigation through the data.
 * </p>
 *
 * Example:
 * <pre>{@code
 * DataSetView<Customer> customerView = ...;
 * DataSet<Customer> dataSet = QueryBuilder.select(Customer.class).toDataSet();
 * customerView.setValue(dataSet);
 * Customer selected = (Customer) customerView.getSelected();
 * }</pre>
 *
 * @param <V> the type of items in the dataset
 * @author Mario A. Serrano Leones
 */
public interface DataSetView<V> extends View<V> {

	/**
	 * Sets the dataset to be displayed in the view.
	 * <p>
	 * This method updates the view to display the items contained in the provided
	 * {@link DataSet}. The view should refresh its UI to reflect the new data.
	 * </p>
	 *
	 * @param dataSet the dataset containing items to display
	 */
    void setValue(DataSet<V> dataSet);

	/**
	 * Gets the currently selected item in the view.
	 * <p>
	 * This method returns the item that is currently selected by the user.
	 * If no item is selected, it may return {@code null}.
	 * </p>
	 *
	 * @return the selected item, or {@code null} if no selection exists
	 */
    Object getSelected();

	/**
	 * Sets the selected item in the view.
	 * <p>
	 * This method programmatically selects an item in the dataset view.
	 * The view should update its UI to reflect the selection.
	 * </p>
	 *
	 * @param selected the item to select
	 */
	void setSelected(Object selected);

	/**
	 * Checks if the dataset is empty.
	 * <p>
	 * This method returns {@code true} if the dataset contains no items,
	 * {@code false} otherwise.
	 * </p>
	 *
	 * @return {@code true} if the dataset is empty, {@code false} otherwise
	 */
	boolean isEmpty();

}
