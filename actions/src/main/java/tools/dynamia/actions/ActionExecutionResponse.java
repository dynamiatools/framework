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
import java.util.Map;

/**
 * Represents the response of an {@link Action} execution, including result data, status, and metadata.
 * <p>
 * This class encapsulates the outcome of an action execution, providing the result data, status information,
 * additional parameters, and metadata such as source, data type, ID, and name. It supports flexible JSON
 * serialization/deserialization using Jackson annotations for ignoring unknown properties and including only non-null fields.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     ActionExecutionResponse response = new ActionExecutionResponse(resultData);
 *     response.setStatus("SUCCESS");
 *     response.setStatusCode(200);
 *     response.setParams(Map.of("key", "value"));
 *     response.setSource("API");
 *     response.setDataType("Book");
 *     response.setDataId("123");
 *     response.setDataName("My Book");
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionExecutionResponse {

    /**
     * The result data produced by the action execution.
     */
    private Object data;

    /**
     * Additional parameters related to the response.
     */
    private Map<String, Object> params;

    /**
     * The source identifier for the response (e.g., UI, API, etc.).
     */
    private String source;

    /**
     * The status of the action execution (e.g., SUCCESS, ERROR).
     */
    private String status;

    /**
     * The status code representing the result (e.g., HTTP status code).
     */
    private int statusCode;

    /**
     * The type of the result data.
     */
    private String dataType;

    /**
     * The unique identifier for the result data.
     */
    private String dataId;

    /**
     * The name of the result data.
     */
    private String dataName;

    /**
     * Default constructor.
     */
    public ActionExecutionResponse() {
    }

    /**
     * Constructs a response with the specified result data.
     *
     * @param data the result data produced by the action
     */
    public ActionExecutionResponse(Object data) {
        this.data = data;
    }

    /**
     * Constructs a response with the specified result data, status, and status code.
     *
     * @param data the result data produced by the action
     * @param status the status of the action execution
     * @param statusCode the status code representing the result
     */
    public ActionExecutionResponse(Object data, String status, int statusCode) {
        this.data = data;
        this.status = status;
        this.statusCode = statusCode;
    }

    /**
     * Returns the result data produced by the action execution.
     *
     * @return the result data object
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the result data produced by the action execution.
     *
     * @param data the result data object to set
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Returns the additional parameters related to the response.
     *
     * @return a map of parameters
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Sets the additional parameters related to the response.
     *
     * @param params a map of parameters to set
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /**
     * Returns the source identifier for the response.
     *
     * @return the source string
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source identifier for the response.
     *
     * @param source the source string to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Returns the type of the result data.
     *
     * @return the data type string
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the type of the result data.
     *
     * @param dataType the data type string to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Returns the unique identifier for the result data.
     *
     * @return the data ID string
     */
    public String getDataId() {
        return dataId;
    }

    /**
     * Sets the unique identifier for the result data.
     *
     * @param dataId the data ID string to set
     */
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    /**
     * Returns the name of the result data.
     *
     * @return the data name string
     */
    public String getDataName() {
        return dataName;
    }

    /**
     * Sets the name of the result data.
     *
     * @param dataName the data name string to set
     */
    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    /**
     * Returns the status of the action execution.
     *
     * @return the status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the action execution.
     *
     * @param status the status string to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the status code representing the result.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the status code representing the result.
     *
     * @param statusCode the status code to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
