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
package tools.dynamia.domain;

import tools.dynamia.integration.Containers;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

public class IdGenerators {

    public static <T extends Serializable> T createId(Class<T> type) {
        IdGenerator<T> idGenerator = getIdGeneratorFor(type);
        if (idGenerator != null) {
            return idGenerator.createId();
        } else {
            throw new IdGeneratorNotFoundException("Target type " + type);
        }
    }

    @SuppressWarnings("rawtypes")
    public static Collection<IdGenerator> getAvailableGenerators() {
        return Containers.get().findObjects(IdGenerator.class);
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Serializable> IdGenerator<T> getIdGeneratorFor(Class<T> type) {

        Optional<IdGenerator> idGenerator = getAvailableGenerators()
                .stream().filter(idg -> idg.getTargetType().equals(type))
                .findFirst();
        return idGenerator.orElse(null);
    }

    private IdGenerators() {
    }
}
