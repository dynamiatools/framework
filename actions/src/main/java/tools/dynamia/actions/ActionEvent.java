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
package tools.dynamia.actions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent and action executed
 *
 * @author Mario A. Serrano Leones
 */
public class ActionEvent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6655504379665692294L;
    private Object data;
    private Object source;
    private Map<String, Object> params = new HashMap<>();

    private boolean propagatable = true;

    public ActionEvent(Object data, Object source) {
        super();
        this.data = data;
        this.source = source;
    }

    public ActionEvent(Object data, Object source, Map<String, Object> params) {
        super();
        this.data = data;
        this.source = source;
        if (params != null) {
            this.params = params;
        }
    }

    /**
     * Data associated to the action
     *
     * @return
     */
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * The action event source or origin.
     *
     * @return
     */
    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Object getParam(String name) {
        Object value = null;
        if (params != null) {
            value = params.get(name);
        }
        return value;
    }

    public void stopPropagation() {
        this.propagatable = false;
    }

    public boolean isPropagatable() {
        return propagatable;
    }
}
