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
package tools.dynamia.templates;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * The Interface TemplateEngine. Provides template evaluation capabilities.
 * This interface defines the contract for template processing engines that can evaluate
 * template content with dynamic data substitution. Template engines are commonly used for
 * generating dynamic content such as HTML pages, email templates, reports, configuration
 * files, and code generation. The interface supports both parameter-based and object-based
 * template evaluation with flexible input/output mechanisms.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * TemplateEngine engine = templateEngineProvider.getEngine();
 * 
 * // Using parameters map
 * Map&lt;String, Object&gt; params = new HashMap&lt;&gt;();
 * params.put("name", "John");
 * params.put("age", 30);
 * String result = engine.evaluate("Hello ${name}, you are ${age} years old", params);
 * 
 * // Using object context
 * User user = new User("Alice", 25);
 * String result = engine.evaluate("Welcome ${name}!", user);
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface TemplateEngine {

    /**
     * Evaluates a template content with the given parameters.
     *
     * @param content the template content
     * @param params the parameters map
     * @return the evaluated template as string
     */
    String evaluate(String content, Map<String, Object> params);

    /**
     * Evaluates a template content using a target object as context.
     *
     * @param content the template content
     * @param target the target object for context
     * @return the evaluated template as string
     */
    String evaluate(String content, Object target);

    /**
     * Evaluates a template from reader and writes the result to writer using parameters.
     *
     * @param reader the input reader for template content
     * @param writer the output writer for evaluated content
     * @param params the parameters map
     */
    void evaluate(Reader reader, Writer writer, Map<String, Object> params);

    /**
     * Evaluates a template from reader and writes the result to writer using target object.
     *
     * @param reader the input reader for template content
     * @param writer the output writer for evaluated content
     * @param target the target object for context
     */
    void evaluate(Reader reader, Writer writer, Object target);
}
