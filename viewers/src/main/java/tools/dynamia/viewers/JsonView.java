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

package tools.dynamia.viewers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

public class JsonView<T> implements View<T> {


    private T value;
    private View parentView;
    private ViewDescriptor viewDescriptor;
    private ObjectMapper mapper;

    public JsonView() {

    }


    public JsonView(T value, ViewDescriptor viewDescriptor) {
        this.value = value;
        this.viewDescriptor = viewDescriptor;
    }

    public JsonView(ViewDescriptor viewDescriptor) {
        this(null, viewDescriptor);
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public View getParentView() {
        return parentView;
    }

    @Override
    public void setParentView(View parentView) {
        this.parentView = parentView;
    }

    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    public String renderJson() {

        try {
            ObjectMapper mapper = getObjectMapper();
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ViewRendererException("Exception rendering json view of " + value + " with descriptor " + viewDescriptor, e);
        }
    }

    public void parse(String json) {
        if (viewDescriptor.getBeanClass() == null) {
            throw new ViewRendererException("Cannot parse json to object. Bean class is null in view descriptor");
        }
        try {
            value = (T) getObjectMapper().readValue(json, viewDescriptor.getBeanClass());
        } catch (IOException e) {
            throw new ViewRendererException("Error parsing json to object", e);
        }

    }

    private ObjectMapper getObjectMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            SimpleModule module = new SimpleModule();
            module.addSerializer(viewDescriptor.getBeanClass(), new JsonViewDescriptorSerializer(viewDescriptor));
            module.addDeserializer((Class) viewDescriptor.getBeanClass(), new JsonViewDescriptorDeserializer(viewDescriptor));
            mapper.registerModule(module);
        }
        return mapper;
    }
}
