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
 * @author Ing. Mario Serrano Leones
 */
public class PageEvent implements Serializable {

    private final String name;
    private Page page;
    private Object data;
    private Map<String, Serializable> params;
    private Object source;

    public PageEvent(String name) {
        this.name = name;
    }

    public PageEvent(String name, Page page) {
        this.name = name;
        this.page = page;
    }

    public PageEvent(String name, Page page, Object data) {
        this.name = name;
        this.page = page;
        this.data = data;
    }

    public PageEvent(String name, Page page, Object data, Map<String, Serializable> params) {
        this.name = name;
        this.page = page;
        this.data = data;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public Object getData() {
        return data;
    }

    public Page getPage() {
        return page;
    }

    public Object getParameter(String name) {
        if (params == null) {
            return null;
        } else {
            return params.get(name);
        }
    }

    public Map<String, Serializable> getParams() {
        return params;
    }

    /**
     * Check if the page event name IS the same as parameter
     *
     */
    public boolean is(String name) {
        return getName().equals(name);
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }
}
