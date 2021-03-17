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
package tools.dynamia.web.util;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Map;

/**
 * Basic JS utils to evaluate javascript code using JVM default js engine
 *
 * @author Mario Serrano Leones
 */
public class JavaScriptUtils {

    private static ScriptEngine scriptEngine;
    private static final LoggingService LOGGER = new SLF4JLoggingService(JavaScriptUtils.class);

    private JavaScriptUtils() {
        throw new IllegalStateException("this is a private constructor");
    }

    public static Object eval(String jsCode) {
        return eval(jsCode, null);
    }


    public static Object eval(String jsCode, Map<String, Object> bindings) {
        try {
            initScriptEngine();
            if (scriptEngine != null) {
                if (bindings != null) {
                    return scriptEngine.eval(jsCode, new SimpleBindings(bindings));
                } else {
                    return scriptEngine.eval(jsCode);
                }
            } else {
                LOGGER.warn("No script engine found - Return null");
                return null;
            }
        } catch (ScriptException e) {
            LOGGER.error("Error evaluation JS code: " + jsCode, e);
            return null;
        }
    }

    private static void initScriptEngine() {
        if (scriptEngine == null) {
            var sm = new ScriptEngineManager();
            scriptEngine = sm.getEngineByExtension("js");

            if (scriptEngine == null) {
                scriptEngine = sm.getEngineByName("JavaScript");
            }

            if (scriptEngine == null) {
                scriptEngine = sm.getEngineByExtension("groovy");
            }

            if (scriptEngine == null) {
                scriptEngine = sm.getEngineByExtension("python");
            }
        }
    }


}
