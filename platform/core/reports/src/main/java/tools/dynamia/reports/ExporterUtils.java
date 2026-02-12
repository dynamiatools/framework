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

package tools.dynamia.reports;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.EntityReference;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.domain.util.DomainUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ExporterUtils {

    private static final Map<String, Object> cache = new HashMap<>();

    public static void clearCache() {
        cache.clear();
    }

    public static Object checkAndLoadEntityReferenceValue(ExporterColumn col, Object value) {
        try {
            if (col.isEntityAlias() && value instanceof Serializable) {
                String key = col.getEntityAlias() + ":" + value;
                Object cacheValue = cache.get(key);
                if (cacheValue == null) {
                    EntityReferenceRepository repo = DomainUtils
                            .getEntityReferenceRepositoryByAlias(col.getEntityAlias());
                    if (repo != null) {
                        @SuppressWarnings("unchecked") EntityReference ref = repo.load((Serializable) value);
                        if (ref != null) {
                            value = ref.toString();
                            cache.put(key, value);
                        }
                    }
                } else {
                    value = cacheValue;
                }
            }
        } catch (Exception e) {
            LoggingService.get(ExporterUtils.class).error("Error checking entity reference value", e);
        }
        return value;
    }

}
