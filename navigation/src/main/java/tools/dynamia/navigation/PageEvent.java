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
package tools.dynamia.navigation;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents an event related to a {@link Page} in the navigation system.
 * <p>
 * PageEvent is used to communicate page-related actions, data, and parameters between components.
 * </p>
 */
public class PageEvent implements Serializable {

    private final String name;
    private Page page;
    private Object data;
    private Map<String, Serializable> params;
    private Object source;

    /**
     * Constructs a PageEvent with a name only.
     *
     * @param name the event name
     */
    public PageEvent(String name) {
        this.name = name;
    }

    /**
     * Constructs a PageEvent with a name and page.
     *
     * @param name the event name
     * @param page the {@link Page} associated
     */
    public PageEvent(String name, Page page) {
        this.name = name;
        this.page = page;
    }

    /**
     * Constructs a PageEvent with a name, page, and data.
     *
     * @param name the event name
     * @param page the {@link Page} associated
     * @param data the event data
     */
    public PageEvent(String name, Page page, Object data) {
        this.name = name;
        this.page = page;
        this.data = data;
    }

    /**
     * Constructs a PageEvent with a name, page, data, and parameters.
     *
     * @param name the event name
     * @param page the {@link Page} associated
     * @param data the event data
     * @param params additional parameters
     */
    public PageEvent(String name, Page page, Object data, Map<String, Serializable> params) {
        this.name = name;
        this.page = page;
        this.data = data;
        this.params = params;
    }

    /**
     * Returns the event name.
     *
     * @return the event name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the event data.
     *
     * @return the event data
     */
    public Object getData() {
        return data;
    }

    /**
     * Returns the page associated with this event.
     *
     * @return the {@link Page}
     */
    public Page getPage() {
        return page;
    }

    /**
     * Returns a parameter value by name.
     *
     * @param name the parameter name
     * @return the parameter value or null if not found
     */
    public Object getParameter(String name) {
        if (params == null) {
            return null;
        } else {
            return params.get(name);
        }
    }

    /**
     * Returns the parameters map.
     *
     * @return the parameters map
     */
    public Map<String, Serializable> getParams() {
        return params;
    }

    /**
     * Checks if the event name matches the given name.
     *
     * @param name the name to compare
     * @return true if names match
     */
    public boolean is(String name) {
        return getName().equals(name);
    }

    /**
     * Returns the source object of this event.
     *
     * @return the source object
     */
    public Object getSource() {
        return source;
    }

    /**
     * Sets the source object for this event.
     *
     * @param source the source object
     */
    public void setSource(Object source) {
        this.source = source;
    }
}
