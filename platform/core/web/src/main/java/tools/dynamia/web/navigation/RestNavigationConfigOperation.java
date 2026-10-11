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
package tools.dynamia.web.navigation;

import org.springframework.http.ResponseEntity;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.crud.cfg.ConfigPage;
import tools.dynamia.crud.cfg.ConfigParameters;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.jackson.core.JacksonException;

import java.util.Map;

/**
 * Handles the REST API of a {@link ConfigPage}: reads and saves the values of its {@code view: config} descriptor as
 * application parameters through {@link ConfigParameters}.
 *
 * <p>The client sends field names only. The parameter that stores each field is resolved on the server from the
 * descriptor, and a name that is not a field of the descriptor is ignored, so the endpoint cannot be used to read or
 * write any other application parameter.</p>
 *
 * <pre>{@code
 * GET /api/system/config/entityFile       -> {"descriptor": "EntityFileCFG", "data": {"filesLocation": "/srv/files", "useHttps": true}, "response": "OK"}
 * PUT /api/system/config/entityFile {"filesLocation": "/data"}   -> the same body, with the saved values
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 * @see RestNavigationContext
 */
public class RestNavigationConfigOperation {

    private final RestNavigationContext ctx;

    /**
     * Constructs a new {@code RestNavigationConfigOperation}.
     *
     * @param ctx the shared navigation context
     */
    public RestNavigationConfigOperation(RestNavigationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Returns the current value of every field of the config page.
     *
     * @param path the navigation path resolving to a {@link ConfigPage}
     * @return a JSON response with the values by field name
     */
    public ResponseEntity<String> read(String path) {
        ViewDescriptor descriptor = descriptor(ctx.findConfigPage(path));
        return RestNavigationContext.buildJsonResponse(new ConfigResult(descriptor.getId(), ConfigParameters.read(descriptor), "OK"));
    }

    /**
     * Saves the values sent in the JSON body and returns the resulting values.
     *
     * @param path     the navigation path resolving to a {@link ConfigPage}
     * @param jsonData a JSON object with field name to value; fields that are not sent keep their value
     * @return a JSON response with the values by field name after saving
     * @throws ValidationError if the body is not a JSON object
     */
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> save(String path, String jsonData) {
        ViewDescriptor descriptor = descriptor(ctx.findConfigPage(path));
        Map<String, Object> values;
        try {
            values = StringPojoParser.createJsonMapper().readValue(jsonData, Map.class);
        } catch (JacksonException e) {
            throw new ValidationError("The body must be a JSON object with the values by field name");
        }
        ConfigParameters.save(descriptor, values);
        return read(path);
    }

    /**
     * Response of the config endpoints.
     *
     * @param descriptor id of the config view descriptor, to fetch it from the metadata API
     * @param data       the values by field name
     * @param response   a human-readable status message
     */
    public record ConfigResult(String descriptor, Map<String, Object> data, String response) {
    }

    private ViewDescriptor descriptor(ConfigPage page) {
        ViewDescriptor descriptor = Viewers.findViewDescriptor(page.getPath());
        if (!"config".equalsIgnoreCase(descriptor.getViewTypeName())) {
            throw new tools.dynamia.navigation.PageNotFoundException("Invalid Path " + page.getPath());
        }
        return descriptor;
    }
}
