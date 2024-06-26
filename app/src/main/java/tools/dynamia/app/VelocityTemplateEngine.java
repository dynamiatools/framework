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
package tools.dynamia.app;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import tools.dynamia.templates.TemplateEngine;
import tools.dynamia.templates.TemplateEngines;
import tools.dynamia.templates.TemplateException;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * @author Mario Serrano Leones
 */
public class VelocityTemplateEngine implements TemplateEngine {

    private static final String LOG_TAG = "VelocityTemplateEngine";

    private final VelocityEngine velocityEngine = new VelocityEngine();

    @Override
    public String evaluate(String template, Map<String, Object> params) {
        StringWriter writer = new StringWriter();
        StringReader reader = new StringReader(template);
        evaluate(reader, writer, params);
        return writer.toString();
    }

    @Override
    public String evaluate(String content, Object target) {
        if (target instanceof VelocityContext context) {
            StringWriter writer = new StringWriter();
            StringReader reader = new StringReader(content);
            evaluate(reader, writer, context);
            return writer.toString();
        } else {
            return evaluate(content, TemplateEngines.getParameters(target));

        }
    }

    @Override
    public void evaluate(Reader reader, Writer writer, Object target) {
        evaluate(reader, writer, TemplateEngines.getParameters(target));

    }

    @Override
    public void evaluate(Reader reader, Writer writer, Map<String, Object> params) {
        velocityEval(reader, writer, new VelocityContext(params));
    }

    private void velocityEval(Reader reader, Writer writer, VelocityContext context) {
        try {
            if (!context.containsKey("numberTool")) {
                context.put("numberTool", new NumberTool());
            }
            if (!context.containsKey("dateTools")) {
                context.put("dateTools", new DateTool());
            }

            if (!context.containsKey("dateTool")) {
                context.put("dateTool", new DateTool());
            }

            velocityEngine.evaluate(context, writer, LOG_TAG, reader);
        } catch (Exception e) {
            throw new TemplateException("Error evaluationg template", e);
        }
    }

}
