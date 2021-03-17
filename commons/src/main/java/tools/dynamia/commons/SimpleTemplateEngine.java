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
package tools.dynamia.commons;

import java.util.Map;


/**
 * The Class SimpleTemplateEngine.
 */
public class SimpleTemplateEngine {

    /**
     * The left limiter.
     */
    private final String leftLimiter;

    /**
     * The right limiter.
     */
    private final String rightLimiter;

    /**
     * Instantiates a new simple template engine.
     */
    public SimpleTemplateEngine() {
        this("\\$\\{", "\\}");
    }

    /**
     * Instantiates a new simple template engine.
     *
     * @param leftLimiter the left limiter
     * @param rightLimiter the right limiter
     */
    public SimpleTemplateEngine(String leftLimiter, String rightLimiter) {
        this.leftLimiter = leftLimiter;
        this.rightLimiter = rightLimiter;
    }

    /**
     * Parses the.
     *
     * @param text the text
     * @param vars the vars
     * @return the string
     */
    public static String parse(String text, Map<String, Object> vars) {

        SimpleTemplateEngine e = new SimpleTemplateEngine();
        return e.parseText(text, vars);
    }

    /**
     * Parses the text.
     *
     * @param text the text
     * @param vars the vars
     * @return the string
     */
    public String parseText(String text, Map<String, Object> vars) {

        try {
            if (text != null && vars != null && vars.size() > 0) {
                for (String key : vars.keySet()) {
                    Object value = vars.get(key);
                    if (value == null) {
                        value = "";
                    }

                    text = text.replaceAll(leftLimiter + key + rightLimiter, value.toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return text;
    }
}
