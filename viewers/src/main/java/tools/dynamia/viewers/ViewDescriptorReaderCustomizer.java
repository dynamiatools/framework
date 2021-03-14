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
package tools.dynamia.viewers;


/**
 * The Interface ViewDescriptorReaderCustomizer.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */
public interface ViewDescriptorReaderCustomizer<T> {

    /**
     * Gets the target reader.
     *
     * @return the target reader
     */
    Class<? extends ViewDescriptorReader> getTargetReader();

    /**
     * Customize.
     *
     * @param content the content
     * @param viewDescriptor the view descriptor
     */
    void customize(T content, ViewDescriptor viewDescriptor);

}
