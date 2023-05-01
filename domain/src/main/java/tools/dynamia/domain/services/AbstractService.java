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

package tools.dynamia.domain.services;

import jakarta.validation.ConstraintViolation;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.query.Parameters;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;

import java.util.Collections;
import java.util.Set;

public abstract class AbstractService {


    private final LoggingService logger = new SLF4JLoggingService(getClass());
    private CrudService crudService;
    private GraphCrudService graphCrudService;
    private Parameters appParams;

    protected CrudService crudService() {
        if (crudService == null) {
            crudService = DomainUtils.lookupCrudService();
        }

        return crudService;
    }

    protected GraphCrudService graphCrudService() {
        if (graphCrudService == null) {
            graphCrudService = DomainUtils.lookupGraphCrudService();
        }
        return graphCrudService;
    }

    protected Parameters appParams() {
        if (appParams == null) {
            appParams = Containers.get().findObject(Parameters.class);
        }
        if (appParams == null) {
            throw new NullPointerException("Cannot lookup an instance of " + Parameters.class);
        }
        return appParams;
    }

    protected void log(String message) {
        logger.info(message);
    }

    protected void logWarn(String message) {
        logger.warn(message);
    }

    protected void log(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * Throw a {@link tools.dynamia.domain.ValidationError} if some invalid property is found
     */
    protected void validate(Object object) {
        ValidatorService service = Containers.get().findObject(ValidatorService.class);
        if (service != null) {
            service.validate(object);
        }
    }

    /**
     * Validate all properties from object
     */
    protected <T> Set<ConstraintViolation<T>> validateAll(T object) {
        ValidatorService service = Containers.get().findObject(ValidatorService.class);
        if (service != null) {
            return service.validateAll(object);
        }
        return Collections.emptySet();
    }

}
