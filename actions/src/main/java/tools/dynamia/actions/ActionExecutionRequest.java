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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Represents a request to execute an {@link Action} with optional data, parameters, and metadata.
 * <p>
 * This class is used to encapsulate all the information required to perform an action, including the main data payload,
 * additional parameters, source identifier, and metadata such as data type, ID, and name. It supports flexible JSON
 * serialization/deserialization using Jackson annotations for aliasing and ignoring unknown properties.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     ActionExecutionRequest request = new ActionExecutionRequest(myData);
 *     request.setParams(Map.of("key", "value"));
 *     request.setSource("UI");
 *     request.setDataType("Book");
 *     request.setDataId("123");
 *     request.setDataName("My Book");
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionExecutionRequest {

    /**
     * The main data payload for the action. Can be aliased as 'data', 'value', 'payload', or 'content' in JSON.
     */
    @JsonAlias({"data", "value", "payload", "content"})
    private Object data;

    /**
     * Additional parameters for the action. Can be aliased as 'params' or 'parameters' in JSON.
     */
    @JsonAlias({"params", "parameters"})
    private Map<String, Object> params;

    /**
     * The source identifier for the request (e.g., UI, API, etc.).
     */
    private String source;

    /**
     * The type of the data payload. Can be aliased as 'dataType', 'type', 'data-type', 'className', or 'class' in JSON.
     */
    @JsonAlias({"dataType", "type", "data-type", "className", "class"})
    private String dataType;

    /**
     * The unique identifier for the data payload. Can be aliased as 'dataId', 'id', or 'data-id' in JSON.
     */
    @JsonAlias({"dataId", "id", "data-id"})
    private String dataId;

    /**
     * The name of the data payload. Can be aliased as 'dataName', 'name', or 'data-name' in JSON.
     */
    @JsonAlias({"dataName", "name", "data-name"})
    private String dataName;

    /**
     * Default constructor.
     */
    public ActionExecutionRequest() {
    }

    /**
     * Constructs a request with the specified data payload.
     *
     * @param data the main data payload for the action
     */
    public ActionExecutionRequest(Object data) {
        this.data = data;
    }

    /**
     * Returns the main data payload for the action.
     *
     * @return the data object
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the main data payload for the action.
     *
     * @param data the data object to set
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Returns the additional parameters for the action.
     *
     * @return a map of parameters
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Sets the additional parameters for the action.
     *
     * @param params a map of parameters to set
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /**
     * Returns the source identifier for the request.
     *
     * @return the source string
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source identifier for the request.
     *
     * @param source the source string to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Returns the type of the data payload.
     *
     * @return the data type string
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the type of the data payload.
     *
     * @param dataType the data type string to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Returns the unique identifier for the data payload.
     *
     * @return the data ID string
     */
    public String getDataId() {
        return dataId;
    }

    /**
     * Sets the unique identifier for the data payload.
     *
     * @param dataId the data ID string to set
     */
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    /**
     * Returns the name of the data payload.
     *
     * @return the data name string
     */
    public String getDataName() {
        return dataName;
    }

    /**
     * Sets the name of the data payload.
     *
     * @param dataName the data name string to set
     */
    public void setDataName(String dataName) {
        this.dataName = dataName;
    }
}
