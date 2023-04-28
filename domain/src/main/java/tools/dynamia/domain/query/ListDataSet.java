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
package tools.dynamia.domain.query;

import java.util.List;


/**
 * The Class ListDataSet.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */
public class ListDataSet<T> extends DataSet<List<T>> {

    /**
     * Instantiates a new list data set.
     *
     * @param data the data
     */
    public ListDataSet(List<T> data) {
        super(data);
    }

    /* (non-Javadoc)
     * @see DataSet#getData()
     */
    @Override
    public List<T> getData() {
        return super.getData(); //To change body of generated methods, choose Tools | Templates.
    }

    /* (non-Javadoc)
     * @see DataSet#getSize()
     */
    @Override
    public long getSize() {
        return getData().size();
    }

}
