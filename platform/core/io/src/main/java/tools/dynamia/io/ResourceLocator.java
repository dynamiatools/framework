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
package tools.dynamia.io;

import java.io.IOException;


/**
 * The Interface ResourceLocator.
 *
 * @author Mario A. Serrano Leones
 */
public interface ResourceLocator {

    /**
     * Gets the resource.
     *
     * @param location the location
     * @return the resource
     */
    Resource getResource(String location);

    /**
     * Gets the resources.
     *
     * @param location the location
     * @return the resources
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Resource[] getResources(String location) throws IOException;
}
