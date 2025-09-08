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

import java.util.List;

/**
 * Interface for providing virtual files.
 * <p>
 * Implementations supply a list of VirtualFile objects representing files not present in the physical file system.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 *     VirtualFileProvider provider = ...;
 *     List<VirtualFile> files = provider.getVirtualFiles();
 *     for (VirtualFile vf : files) {
 *         System.out.println(vf.getName());
 *     }
 * </pre>
 *
 * <b>Important Methods:</b>
 * <ul>
 *   <li>{@link #getVirtualFiles()} - Returns a list of virtual files.</li>
 * </ul>
 *
 * @author Dynamia Soluciones IT S.A.S
 * @since 1.0
 */
public interface VirtualFileProvider {

    /**
     * Returns a list of virtual files.
     *
     * @return a list of {@link VirtualFile} objects
     */
    List<VirtualFile> getVirtualFiles();
}
