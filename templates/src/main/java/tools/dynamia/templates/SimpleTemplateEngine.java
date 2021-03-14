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
package tools.dynamia.templates;

import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Mario
 */
public class SimpleTemplateEngine implements TemplateEngine {

    @Override
    public String evaluate(String template, Map<String, Object> params) {
        if (template == null) {
            throw new TemplateException("Contenido del template nulo!!!");
        }
        Iterator iter = params.keySet().iterator();
        String salida = template;
        while (iter.hasNext()) {
            String key = (String) iter.next();
            Object obj = params.get(key);
            salida = salida.replace("${" + key + "}", (String) obj);
        }
        return salida;
    }

    @Override
    public String evaluate(String content, Object target) {
        return evaluate(content, TemplateEngines.getParameters(target));
    }

    @Override
    public void evaluate(Reader reader, Writer writer, Map<String, Object> params) {
        throw new UnsupportedOperationException("This is a simple template engine, use VelocityTemplateEngine instead");

    }

    @Override
    public void evaluate(Reader reader, Writer writer, Object target) {
        throw new UnsupportedOperationException("This is a simple template engine, use VelocityTemplateEngine instead");

    }
}
