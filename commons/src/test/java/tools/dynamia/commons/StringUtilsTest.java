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
package tools.dynamia.commons;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class StringUtilsTest extends TestCase {

    public StringUtilsTest(String testName) {
        super(testName);
    }

    public void testGetLastCharacterMethod() {

        String string = "TheString";
        String expResult = "g";
        String result = StringUtils.getLastCharacter(string);

        assertEquals(expResult, result);
    }

    public void testGetFirstCharacterMethod() {

        String string = "TheString";
        String expResult = "T";
        String result = StringUtils.getFirstCharacter(string);

        assertEquals(expResult, result);
    }

    public void testSimpliedString() {
        String expected = "esta-prueba-servira-en-accion";
        String input = "está pruébá SERvirá en acción";
        String result = StringUtils.simplifiedString(input);

        assertEquals(expected, result);

    }

    public void testRandomString() {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            String result = StringUtils.randomString();
            set.add(result);
        }

        assertEquals(10, set.size());

    }

    public void testCapatilizeAllWords() {
        String expected = "This Is Nice";
        String text = "this is NICE";
        String result = StringUtils.capitalizeAllWords(text);

        assertEquals(expected, result);

    }

}
