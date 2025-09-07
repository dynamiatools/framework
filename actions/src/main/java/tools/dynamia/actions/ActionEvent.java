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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an event triggered by the execution of an {@link Action}.
 * <p>
 * This class encapsulates the context of an action execution, including the associated data, the source of the event,
 * custom parameters, and propagation control. It is used to pass information between action handlers and listeners.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     ActionEvent event = new ActionEvent(data, source);
 *     event.getParams().put("key", "value");
 *     if (!event.isPropagatable()) {
 *         // handle stopped propagation
 *     }
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionEvent implements Serializable {

    private static final long serialVersionUID = 6655504379665692294L;

    /**
     * The data associated with the action event (e.g., domain object, payload).
     */
    private Object data;

    /**
     * The source or origin of the action event (e.g., UI component, service).
     */
    private Object source;

    /**
     * Custom parameters for the event, allowing additional context to be passed.
     */
    private Map<String, Object> params = new HashMap<>();

    /**
     * Indicates whether the event is propagatable to other listeners or handlers.
     */
    private boolean propagatable = true;

    /**
     * Constructs an ActionEvent with the specified data and source.
     *
     * @param data the data associated with the event
     * @param source the source or origin of the event
     */
    public ActionEvent(Object data, Object source) {
        super();
        this.data = data;
        this.source = source;
    }

    /**
     * Constructs an ActionEvent with the specified data, source, and custom parameters.
     *
     * @param data the data associated with the event
     * @param source the source or origin of the event
     * @param params a map of custom parameters for the event
     */
    public ActionEvent(Object data, Object source, Map<String, Object> params) {
        super();
        this.data = data;
        this.source = source;
        if (params != null) {
            this.params = params;
        }
    }

    /**
     * Returns the data associated with the action event.
     *
     * @return the event data
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the data associated with the action event.
     *
     * @param data the event data to set
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Returns the source or origin of the action event.
     *
     * @return the event source
     */
    public Object getSource() {
        return source;
    }

    /**
     * Sets the source or origin of the action event.
     *
     * @param source the event source to set
     */
    public void setSource(Object source) {
        this.source = source;
    }

    /**
     * Returns the custom parameters for the event.
     *
     * @return a map of custom parameters
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Returns the value of a specific parameter by name.
     *
     * @param name the name of the parameter
     * @return the value of the parameter, or {@code null} if not present
     */
    public Object getParam(String name) {
        Object value = null;
        if (params != null) {
            value = params.get(name);
        }
        return value;
    }

    /**
     * Stops the propagation of this event to other listeners or handlers.
     */
    public void stopPropagation() {
        this.propagatable = false;
    }

    /**
     * Returns whether the event is propagatable to other listeners or handlers.
     *
     * @return {@code true} if the event is propagatable; {@code false} otherwise
     */
    public boolean isPropagatable() {
        return propagatable;
    }
}
