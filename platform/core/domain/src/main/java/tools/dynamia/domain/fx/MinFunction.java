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
package tools.dynamia.domain.fx;

import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.domain.query.DataPaginatorPagedListDataSource;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * The Class MinFunction.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */
@Component
public class MinFunction<T> implements Function<List<T>, Object> {

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.fx.Function#getName()
     */
    @Override
    public String getName() {
        return "min";
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.fx.Function#getArgumentsNames()
     */
    @Override
    public String[] getArgumentsNames() {
        return new String[]{"property"};
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.fx.Function#compute(java.lang.Object, java.util.Map)
     */
    @Override
    public Object compute(List<T> data, Map<String, Object> args) {
        Object min = null;
        if (data instanceof PagedList<T> pagedList) {
            if (pagedList.getDataSource() instanceof DataPaginatorPagedListDataSource<T> datasource) {
                String jpqlProjection = datasource.getQueryMetadata().getQueryBuilder().createProjection("min", args.get("property").toString());
                CrudService crudService = Containers.get().findObject(CrudService.class);
                min = crudService.executeProjection(BigDecimal.class, jpqlProjection, datasource.getQueryMetadata().getParameters());
            }
        } else {
            try {
                for (T t : data) {
                    Object field = ObjectOperations.invokeGetMethod(t, args.get("property").toString());
                    if (min == null) {
                        min = field;
                    } else if (field instanceof Comparable comparable) {
                        //noinspection unchecked
                        if (comparable.compareTo(min) < 0) {
                            min = comparable;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return min;
    }

}
