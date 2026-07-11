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

import tools.dynamia.io.Resource;

import java.io.Reader;
import java.io.Serializable;
import java.util.List;


/**
 * Strategy for parsing a classpath (or filesystem) resource into a {@link ViewDescriptor}.
 *
 * <p>A {@code ViewDescriptorReader} is responsible for deserializing view metadata stored in
 * a particular file format (e.g., XML, YAML, JSON) and producing a populated
 * {@link ViewDescriptor}. Multiple readers can coexist in the same application — the
 * {@link ViewDescriptorFactory} selects the appropriate reader based on each resource's file
 * extension.</p>
 *
 * <p>After parsing, each registered {@link ViewDescriptorReaderCustomizer} whose
 * {@link ViewDescriptorReaderCustomizer#getTargetReader() target reader} matches this
 * implementation is applied to allow additional post-processing of the raw parsed content.</p>
 *
 * <p>Implementations must be serializable and stateless so that a single instance can be
 * safely shared across multiple descriptor loading calls.</p>
 *
 * @see ViewDescriptorFactory#loadViewDescriptors()
 * @see ViewDescriptorReaderCustomizer
 */
public interface ViewDescriptorReader extends Serializable {

    /**
     * Parses the content of {@code descriptorResource} using the provided {@code reader} and
     * returns a fully populated {@link ViewDescriptor}.
     *
     * <p>Each customizer in the {@code customizers} list whose
     * {@link ViewDescriptorReaderCustomizer#getTargetReader() target} matches this reader is
     * invoked with the raw parsed content before or after the descriptor is built (depending
     * on the implementation).</p>
     *
     * @param descriptorResource the resource that contains the descriptor source; used for
     *                           contextual information such as the resource path; must not be
     *                           {@code null}
     * @param reader             an open character-stream reader for the resource content;
     *                           the caller is responsible for closing it; must not be {@code null}
     * @param customizers        the list of reader customizers to apply during parsing;
     *                           must not be {@code null} (may be empty)
     * @return the parsed {@link ViewDescriptor}; never {@code null}
     * @throws ViewDescriptorReaderException if the resource cannot be parsed
     */
    ViewDescriptor read(Resource descriptorResource, Reader reader, List<ViewDescriptorReaderCustomizer> customizers);

    /**
     * Returns the file extensions this reader can handle (e.g., {@code "xml"}, {@code "yml"}).
     *
     * <p>Extensions are compared case-insensitively. The {@link ViewDescriptorFactory} uses
     * these extensions to route each discovered resource file to the correct reader.</p>
     *
     * @return a non-null, non-empty array of supported file extensions without leading dots
     */
    String[] getSupportedFileExtensions();
}
