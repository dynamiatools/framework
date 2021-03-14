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

import junit.framework.TestCase;
import tools.dynamia.commons.collect.ArrayListMultiMap;
import tools.dynamia.commons.collect.MultiMap;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class MapBuilderTest extends TestCase {

    public MapBuilderTest(String testName) {
        super(testName);
    }

    /**
     * Test of put method, of class MapBuilder.
     */
    public void testPut_String_Object() {
        Map<String, Object> map = MapBuilder.put("value", 100);
        assertTrue(map.containsKey("value"));
        assertEquals(100, map.get("value"));
    }

    /**
     * Test of put method, of class MapBuilder.
     */
    public void testPut_ObjectArr() {
        Map<String, Object> map = MapBuilder.put("value", 100,
                "name", "mario",
                "age", 23);

        assertEquals(3, map.keySet().size());
        assertEquals("mario", map.get("name"));
    }

    public void testMultiMap() {
        MultiMap<String, String> mmap = new ArrayListMultiMap<>();
        mmap.put("names", "Mario");
        mmap.put("names", "Pepe");
        mmap.put("names", "Juan");
        mmap.put("names", "Sergio");
        mmap.put("names", "Lauor");
        mmap.put("names", "Esda");

        assertEquals(1, mmap.keySet().size());
        assertTrue(mmap.containsKey("names"));
        assertEquals(6, mmap.get("names").size());

    }

    @SuppressWarnings("rawtypes")
    public void testMultiMapGetKey() {
        MultiMap<String, Class> mm = new ArrayListMultiMap<>();

        mm.put("entero", int.class, Integer.class);
        mm.put("largo", long.class, Long.class);
        mm.put("doble", double.class, Double.class);
        mm.put("boleano", Boolean.class, boolean.class);
        mm.put("texto", String.class);
        mm.put("fecha", Date.class);
        mm.put("decimal", BigDecimal.class);
        mm.put("enum", Enum.class);

        assertEquals("boleano", mm.getKey(Boolean.class));
        assertEquals("enum", mm.getKey(Enum.class));
        assertEquals("entero", mm.getKey(Integer.class));
        assertEquals("entero", mm.getKey(int.class));
    }
}
