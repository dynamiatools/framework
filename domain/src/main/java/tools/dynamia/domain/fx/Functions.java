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

import tools.dynamia.integration.Containers;

import java.util.Map;


/**
 * The Class Functions.
 *
 * @author Mario A. Serrano Leones
 */
public class Functions {

    /**
     * Gets the.
     *
     * @param functionName the function name
     * @return the function
     */
    public static Function get(String functionName) {
        for (Function fx : Containers.get().findObjects(Function.class)) {
            if (fx.getName().equalsIgnoreCase(functionName)) {
                return fx;
            }
        }
        return null;
    }

    /**
     * Compute.
     *
     * @param functionName the function name
     * @param value the value
     * @param args the args
     * @return the object
     */
    public static Object compute(String functionName, Object value, Map<String, Object> args) {
        Function fx = get(functionName);
        if (fx == null) {
            throw new FunctionNotFoundException("Function with name '" + functionName + "' not found");
        }

        //noinspection unchecked
        return fx.compute(value, args);
    }

    private Functions() {
    }

}
