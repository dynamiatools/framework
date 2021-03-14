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
package tools.dynamia.domain.fx;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


/**
 * The Class CountFunction.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */
@Component
public class CountFunction<T> implements Function<List<T>, Long> {

    /* (non-Javadoc)
     * @see Function#getName()
     */
    @Override
    public String getName() {
        return "count";
    }

    /* (non-Javadoc)
     * @see Function#getArgumentsNames()
     */
    @Override
    public String[] getArgumentsNames() {
        return new String[]{"property"};
    }

    /* (non-Javadoc)
     * @see Function#compute(java.lang.Object, java.util.Map)
     */
    @Override
    public Long compute(List<T> value, Map<String, Object> args) {
        return (long) value.size();
    }

}
