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

import my.company.Dummy;
import org.junit.Test;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BigDecimalUtilsTest {

    @Test
    public void shouldSum() {
        List<Dummy> dummies = new ArrayList<>();
        dummies.add(new Dummy("Scott", 10));
        dummies.add(new Dummy("Charles", 30));
        dummies.add(new Dummy("Logan", 130));
        dummies.add(new Dummy("Jean", 5));

        int expectedTotal = 175;
        int result = BigDecimalUtils.sum("age", dummies).intValue();

        assertEquals(expectedTotal, result);
    }

    @Test
    public void shouldComputeScript() {
        MapBuilder<String, Number> map = new MapBuilder<>();

        int expected = 617;
        BigDecimal result = BigDecimalUtils.evaluate("(a + (b * c)) / 2", map
                .put("a", 34)
                .put("b", 12)
                .put("c", 100)
                .build());

        assertEquals(expected, result.intValue());
    }

    @Test
    public void showAddPercent() {
        long expected = 1160000;
        long result = BigDecimalUtils.addPercent(new BigDecimal("1000000"), 16).longValue();
    }

}
