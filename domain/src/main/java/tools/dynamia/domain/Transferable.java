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

package tools.dynamia.domain;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.util.DomainUtils;

/**
 * The Interface Transferable. Represents entities that can be converted to Data Transfer Objects (DTOs).
 * This interface provides automatic conversion capabilities from domain entities to DTOs, which is
 * essential for API responses, service layer communication, and decoupling presentation logic from
 * domain logic. The default implementation uses the framework's automatic mapping utilities to
 * create DTOs based on generic type information, reducing boilerplate code while maintaining flexibility.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class User implements Transferable&lt;UserDTO&gt; {
 *     private String name;
 *     private String email;
 *     
 *     // The toDTO() method is inherited and works automatically
 *     // Custom implementation example:
 *     public UserDTO toDTO() {
 *         UserDTO dto = new UserDTO();
 *         dto.setDisplayName(this.name);
 *         dto.setContactEmail(this.email);
 *         return dto;
 *     }
 * }
 * 
 * // Usage
 * User user = userService.findById(1L);
 * UserDTO dto = user.toDTO();
 * </code>
 *
 * @param <DTO> the type of the data transfer object
 * @author Mario A. Serrano Leones
 */
public interface Transferable<DTO> {

    /**
     * Converts this entity to a Data Transfer Object (DTO).
     *
     * @return the DTO representation of this entity
     */
    default DTO toDTO() {
        DTO dto = null;
        try {
            @SuppressWarnings("unchecked") Class<DTO> dtoClass = BeanUtils.getGenericTypeClass(this);
            if (dtoClass == null) {
                //noinspection unchecked
                dtoClass = BeanUtils.getGenericTypeInterface(this, Transferable.class);
            }
            dto = DomainUtils.autoDataTransferObject(this, dtoClass);
        } catch (Exception e) {
            System.err.println("WARN: Cannot auto create DTO for class " + getClass() + ". Implement it yourself. Exception: " + e.getMessage());
        }
        return dto;
    }
}
