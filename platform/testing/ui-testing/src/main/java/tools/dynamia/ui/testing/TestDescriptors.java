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
package tools.dynamia.ui.testing;

import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorBuilder;
import tools.dynamia.viewers.ViewDescriptorFactory;

import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * View descriptors for tests that do not have the application's.
 */
public final class TestDescriptors {

    private TestDescriptors() {
    }

    /**
     * Stands for the application's descriptor factory (forms filled with values need one): every class gets a form with one
     * field per property, which is what forms of entities without a descriptor file look like.
     *
     * @return the factory
     */
    public static ViewDescriptorFactory autoFields() {
        return (ViewDescriptorFactory) Proxy.newProxyInstance(ViewDescriptorFactory.class.getClassLoader(),
                new Class<?>[]{ViewDescriptorFactory.class}, (proxy, method, args) -> {
                    if (ViewDescriptor.class.isAssignableFrom(method.getReturnType())) {
                        for (Object arg : args) {
                            if (arg instanceof Class<?> type) {
                                return ViewDescriptorBuilder.viewDescriptor("form", type).autofields(true).build();
                            }
                        }
                        return null;
                    }
                    if (Set.class.isAssignableFrom(method.getReturnType())) {
                        return Set.of();
                    }
                    return null;
                });
    }
}
