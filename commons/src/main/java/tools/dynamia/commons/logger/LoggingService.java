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
package tools.dynamia.commons.logger;


/**
 * The Interface LoggingService.
 *
 * @author Mario A. Serrano Leones
 */
public interface LoggingService {

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
     * Warn.
     *
     * @param message the message
     */
    void warn(String message);

    /**
     * Error.
     *
     * @param message the message
     */
    void error(String message);

    /**
     * Error.
     *
     * @param message the message
     * @param t the t
     */
    void error(String message, Throwable t);

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
