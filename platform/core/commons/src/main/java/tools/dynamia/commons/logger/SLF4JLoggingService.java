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
package tools.dynamia.commons.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;


/**
 * The Class SLF4JLoggingService.
 *
 * @author Mario A. Serrano Leones
 */
public class SLF4JLoggingService implements LoggingService, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4196725472501595832L;
    /**
     * The logger.
     */
    private Logger logger = null;
    private String prefix = "";

    /**
     * Instantiates a new SL f4 j logging service.
     */
    public SLF4JLoggingService() {
        logger = LoggerFactory.getLogger(LoggingService.class);
    }

    /**
     * Instantiates a new SL f4 j logging service.
     *
     * @param clazz the clazz
     */
    public SLF4JLoggingService(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    public SLF4JLoggingService(Class<?> clazz, String prefix) {
        this(clazz);
        if (prefix == null) {
            prefix = "";
        }
        this.prefix = prefix;

    }

    /*
     * (non-Javadoc)
     *
     * @see tools.dynamia.commons.logger.LoggingService#debug(java.lang.String)
     */
    @Override
    public void debug(String message) {
        if (isDebugEnabled()) {
            logger.debug(prefix + message);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see tools.dynamia.commons.logger.LoggingService#info(java.lang.String)
     */
    @Override
    public void info(String message) {
        logger.info("{}{}", prefix, message);
    }

    @Override
    public void info(String format, Object... params) {
        logger.info(prefix + format, params);
    }

    /*
     * (non-Javadoc)
     *
     * @see tools.dynamia.commons.logger.LoggingService#warn(java.lang.String)
     */
    @Override
    public void warn(String message) {
        logger.warn("{}{}", prefix, message);
    }


    @Override
    public void warn(String format, Object... params) {
        logger.warn(prefix + format, params);
    }

    /*
     * (non-Javadoc)
     *
     * @see tools.dynamia.commons.logger.LoggingService#error(java.lang.String)
     */
    @Override
    public void error(String message) {
        logger.error("{}{}", prefix, message);
    }

    @Override
    public void error(String format, Object... params) {
        logger.error(prefix + format, params);
    }

    @Override
    public void error(String format, Throwable t, Object... params) {
        logger.atError().setCause(t).log(prefix + format, params);
    }

    /*
     * (non-Javadoc)
     *
     * @see tools.dynamia.commons.logger.LoggingService#error(java.lang.String,
     * java.lang.Throwable)
     */
    @Override
    public void error(String message, Throwable t) {
        logger.error(prefix + message, t);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * tools.dynamia.commons.logger.LoggingService#error(java.lang.Throwable)
     */
    @Override
    public void error(Throwable t) {
        logger.error(prefix + t.getMessage(), t);
    }

    /*
     * (non-Javadoc)
     *
     * @see tools.dynamia.commons.logger.LoggingService#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
}
