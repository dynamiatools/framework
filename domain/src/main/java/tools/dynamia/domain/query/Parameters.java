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
package tools.dynamia.domain.query;

import java.util.Collection;
import java.util.List;


/**
 * The Interface Parameters.
 *
 * @author Mario A. Serrano Leones
 */
public interface Parameters {

    /**
     * Gets the parameters.
     *
     * @param paramNames the param names
     * @return the parameters
     */
    List<Parameter> getParameters(List<String> paramNames);

    /**
     * Gets the parameters.
     *
     * @param parameterClass the parameter class
     * @param paramNames     the param names
     * @return the parameters
     */
    List<Parameter> getParameters(Class<? extends Parameter> parameterClass, List<String> paramNames);

    /**
     * All.
     *
     * @return the list
     */
    List<Parameter> all();

    /**
     * Gets the parameter.
     *
     * @param name the name
     * @return the parameter
     */
    Parameter getParameter(String name);

    /**
     * Gets the value.
     *
     * @param parameter the parameter
     * @return the value
     */
    String getValue(String parameter);

    /**
     * Gets the value.
     *
     * @param parameterClass the parameter class
     * @param parameter      the parameter
     * @return the value
     */
    String getValue(Class<? extends Parameter> parameterClass, String parameter);

    /**
     * Gets the value.
     *
     * @param parameter    the parameter
     * @param defaultValue the default value
     * @return the value
     */
    String getValue(String parameter, String defaultValue);

    /**
     * Gets the value.
     *
     * @param parameterClass the parameter class
     * @param parameter      the parameter
     * @param defaultValue   the default value
     * @return the value
     */
    String getValue(Class<? extends Parameter> parameterClass, String parameter, String defaultValue);

    /**
     * Save.
     *
     * @param p the p
     */
    void save(Parameter p);

    /**
     * Save.
     *
     * @param params the params
     */
    void save(Collection<Parameter> params);

    /**
     * Sets the parameter.
     *
     * @param parameterClass the parameter class
     * @param name           the name
     * @param value          the value
     */
    void setParameter(Class<? extends Parameter> parameterClass, String name, Object value);

    /**
     * Sets the parameter.
     *
     * @param name  the name
     * @param value the value
     */
    void setParameter(String name, Object value);

    /**
     * Gets the parameter.
     *
     * @param parameterClass the parameter class
     * @param name           the name
     * @return the parameter
     */
    Parameter getParameter(Class<? extends Parameter> parameterClass, String name);

    /**
     * Try to increase parameter counter
     *
     * @param param
     */
    void increaseCounter(Parameter param);

    /**
     * Increase and return next counter value
     *
     * @param counterParam
     * @return
     */
    long findNextCounterValue(Parameter counterParam);

    /**
     * @param parameterClass
     * @param name
     * @param filters
     */
    Parameter findParameter(Class<? extends Parameter> parameterClass, String name, QueryParameters filters);
}
