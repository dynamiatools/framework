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

import java.io.Serializable;


/**
 * The Class DataSet.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */
public class DataSet<T> implements Serializable {

    /**
     * The data.
     */
    private final T data;

    /**
     * Instantiates a new data set.
     *
     * @param data the data
     */
    public DataSet(T data) {
        this.data = data;
    }

    /**
     * Return the query result data.
     *
     * @return the data
     */
    public T getData() {
        return data;
    }

    /**
     * Return the size of query result data. Should -1 if size is unknow
     *
     * @return the size
     */
    public long getSize() {
        return -1;
    }

}
