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


/**
 * The Interface LoggingService.
 *
 * @author Mario A. Serrano Leones
 */
public interface LoggingService {

    /**
     * Get a logging service for a class.
     *
     * @param clazz the clazz
     * @return the logging service
     */
    static LoggingService get(Class clazz) {
        return new SLF4JLoggingService(clazz);
    }

    /**
     * Get a logging service for a class.
     *
     * @param clazz  the clazz
     * @param prefix the prefix
     * @return the logging service
     */
    static LoggingService get(Class clazz, String prefix) {
        return new SLF4JLoggingService(clazz, prefix);
    }


    /**
     * Debug.
     *
     * @param message the message
     */
    void debug(String message);

    /**
     * Info.
     *
     * @param message the message
     */
    void info(String message);

    /**
     * Info with params.
     *
     * @param format the format
     * @param params the params
     */
    void info(String format, Object... params);


    /**
     * Warn.
     *
     * @param message the message
     */
    void warn(String message);

    /**
     * Warn with params.
     *
     * @param format the format
     * @param params the params
     */
    void warn(String format, Object... params);


    /**
     * Error.
     *
     * @param message the message
     */
    void error(String message);

    /**
     * Error with params.
     *
     * @param format the format
     * @param params the params
     */
    void error(String format, Object... params);

    /**
     * Error.
     *
     * @param message the message
     * @param t       the t
     */
    void error(String message, Throwable t);

    /**
     * Error with params.
     *
     * @param format the format
     * @param t      the t
     * @param params the params
     */
    void error(String format, Throwable t, Object... params);

    /**
     * Error.
     *
     * @param t the t
     */
    void error(Throwable t);

    /**
     * Checks if is debug enabled.
     *
     * @return true, if is debug enabled
     */
    boolean isDebugEnabled();
}
