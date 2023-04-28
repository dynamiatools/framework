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
package tools.dynamia.io.converters;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import static org.junit.Assert.assertEquals;

/**
 * @author Mario A. Serrano Leones
 */
public class ConvertersTest {

    public ConvertersTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        var container = new SimpleObjectContainer();
        container.addObject("int", new IntegerConverter());
        container.addObject("class", new ClassConverter());
        container.addObject("long", new LongConverter());
        Containers.get().installObjectContainer(container);
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testGetConverter() {
        Class<Integer> clazz = Integer.class;
        Converter expResult = new IntegerConverter();
        Converter result = Converters.getConverter(clazz);
        assertEquals(expResult.getClass().getName(), result.getClass().getName());
    }

    @Test
    public void testGetClassConverter() {
        Converter expResult = new ClassConverter();
        Converter result = Converters.getConverter(Class.class);
        assertEquals(expResult.getClass().getName(), result.getClass().getName());
    }

    @Test
    public void testConvert_Object() {
        Long value = 123L;
        String expResult = "123";
        String result = Converters.convert(value);
        assertEquals(expResult, result);
    }

    @Test
    public void testConvert_Class_String() {
        String string = "java.lang.String";
        Class expResult = String.class;
        Object result = Converters.convert(Class.class, string);
        assertEquals(expResult, result);
    }
}
