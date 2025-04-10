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

import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.domain.query.DataPaginatorPagedListDataSource;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MultiFunctionProcessor that use {@link CrudService} to create a SQL like projections for each function. It make only
 * one datasource call
 *
 * @param <T>
 */

public class CrudServiceMultiFunctionProcessor<T> implements MultiFunctionProcessor<List<T>, Number> {


    @Override
    public Map<FunctionProvider, Number> compute(List<T> data, Map<String, Object> args, List<FunctionProvider> functions) {

        Map<FunctionProvider, Number> result = new HashMap<>();
        functions.forEach(fx -> result.put(fx, BigDecimal.ZERO));

        if (data instanceof PagedList<T> pagedList) {
            if (pagedList.getDataSource() instanceof DataPaginatorPagedListDataSource<T> datasource) {

                var query = datasource.getQueryMetadata().getQueryBuilder().clone();
                query.resultType(null);


                var projections = new ArrayList<String>();
                functions.forEach(fx -> {
                    var proj = fx.getFunction() + "(" + query.getVarName() + fx.getName() + ")";
                    projections.add(proj);
                });

                query.customSelect("select " + String.join(",", projections));
                CrudService crudService = DomainUtils.lookupCrudService();
                var queryResult = crudService.executeQuery(query);
                if (!queryResult.isEmpty()) {
                    for (int i = 0; i < functions.size(); i++) {
                        var function = functions.get(i);
                        Object[] functionResult = (Object[]) queryResult.get(0);
                        result.put(function, (Number) functionResult[i]);
                    }
                }
            }
        } else {
            functions.forEach(fx -> {
                var value = Functions.compute(fx.getFunction(), data, Map.of("property", fx.getName()));
                if (value instanceof Number) {
                    result.put(fx, (Number) value);
                }
            });
        }
        return result;
    }
}
