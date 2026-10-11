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
package tools.dynamia.web.navigation;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the free text search of the {@code _q} request parameter of the REST CRUD read.
 */
class RestNavigationSearchTest {

    /** Entity-like bean: two text fields, a number and a flag. */
    public static class Book {
        private String title;
        private String author;
        private int year;
        private boolean available;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }

    private static QueryBuilder search(String text) {
        var request = new MockHttpServletRequest("GET", "/api/store/books");
        if (text != null) {
            request.setParameter("_q", text);
        }
        var query = QueryBuilder.select().from(Book.class, "e");
        RestNavigationQuerySupport.applyRequestSearch(request, query, new DefaultViewDescriptor(Book.class, "json", true), "e");
        return query;
    }

    @Test
    void searchesEveryTextFieldInOneOrGroup() {
        var query = search("  Dune ");

        String jpql = query.toString();
        assertTrue(jpql.contains("lower(e.title) like :searchText"), jpql);
        assertTrue(jpql.contains("lower(e.author) like :searchText"), jpql);
        assertTrue(jpql.contains(" or "), jpql);
        assertEquals("%dune%", query.getQueryParameters().get("searchText"));
    }

    @Test
    void numbersAndFlagsAreNotSearched() {
        String jpql = search("1965").toString();

        assertFalse(jpql.contains("e.year"), jpql);
        assertFalse(jpql.contains("e.available"), jpql);
    }

    @Test
    void missingOrBlankTextAddsNothing() {
        assertFalse(search(null).toString().contains("where"));
        assertFalse(search("   ").toString().contains("where"));
    }
}
