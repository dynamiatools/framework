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
package tools.dynamia.app;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class ApplicationInfoTest {

    @Test
    public void testAppInfoProperties() {
        Properties properties = buildProperties();

        ApplicationInfo applicationInfo = ApplicationInfo.load(properties);

        assertEquals("DynamiaTools", applicationInfo.getName());
        assertEquals("1.1.1", applicationInfo.getVersion());
        assertEquals("the//url", applicationInfo.getProperty("jdbcUrl"));
        assertEquals("MyRDS", applicationInfo.getProperty("awsDatabasename"));

    }

    private Properties buildProperties() {
        Properties p = new Properties();

        p.setProperty("name", "DynamiaTools");
        p.setProperty("version", "1.1.1");
        p.setProperty("prop.jdbcUrl", "the//url");
        p.setProperty("prop.awsDatabasename", "MyRDS");

        return p;
    }

}
