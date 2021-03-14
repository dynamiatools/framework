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
package tools.dynamia.domain;

import org.junit.Test;
import tools.dynamia.domain.query.DataPaginator;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class DataPaginatorTest {

    @Test
    public void testDataPaginator() {
        DataPaginator dp = new DataPaginator(100, 5, 1);
        assertEquals(20, dp.getPagesNumber());
        assertEquals(1, dp.getPage());
        assertEquals(0, dp.getFirstResult());

        dp.nextPage();
        assertEquals(2, dp.getPage());
        assertEquals(5, dp.getFirstResult());

        dp.nextPage();
        assertEquals(3, dp.getPage());

        dp.nextPage();
        assertEquals(4, dp.getPage());

        dp.nextPage();
        assertEquals(5, dp.getPage());
        assertEquals(20, dp.getFirstResult());

    }

    @Test
    public void testPageNumbers() {
        DataPaginator dp = new DataPaginator(20, 10, 1);
        assertEquals(2, dp.getPagesNumber());
    }

    @Test
    public void testDataPaginatorSetPage() {
        DataPaginator dp = new DataPaginator(100, 5, 1);
        assertEquals(20, dp.getPagesNumber());
        assertEquals(1, dp.getPage());
        assertEquals(0, dp.getFirstResult());

        dp.setPage(5);

        assertEquals(5, dp.getPage());
        assertEquals(20, dp.getFirstResult());

    }

    @Test
    public void testScrollToIndex() {
        DataPaginator dp = new DataPaginator(18, 4, 1);
        int relativeIndex = 0;

        relativeIndex = dp.scrollToIndex(7);
        assertEquals(3, relativeIndex);
        assertEquals(2, dp.getPage());

        relativeIndex = dp.scrollToIndex(5);
        assertEquals(1, relativeIndex);
        assertEquals(2, dp.getPage());

        relativeIndex = dp.scrollToIndex(11);
        assertEquals(3, relativeIndex);
        assertEquals(3, dp.getPage());

        relativeIndex = dp.scrollToIndex(17);
        assertEquals(1, relativeIndex);
        assertEquals(5, dp.getPage());

        relativeIndex = dp.scrollToIndex(2);
        assertEquals(2, relativeIndex);
        assertEquals(1, dp.getPage());

    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testScrollToIndexSecurity() {
        DataPaginator dp = new DataPaginator(18, 4, 1);
        dp.scrollToIndex(600);

    }
}
