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

import org.junit.Assert;
import org.junit.Test;
import tools.dynamia.templates.TemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author programador
 */
public class VelocityTemplateEngineTest {

    @Test
    public void initVelocityTemplate() {
        System.out.println("Testing Velocity Template Engine ");
        TemplateEngine templateEngine = new VelocityTemplateEngine();
        Map<String, Object> params = new HashMap<>();
        params.put("nombre", "Juan");
        Object obj = templateEngine.evaluate("Hola ${nombre}", params);
        Assert.assertEquals("Hola Juan", obj);
    }

}
