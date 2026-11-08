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
package tools.dynamia.web.query;

import org.junit.jupiter.api.Test;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Covers how a QuerySpec becomes a query: the whitelist, the conversions, the filter chain and the limits. */
class QuerySpecProcessorTest {

    public enum Status { OPEN, PAID }

    /** Entity-like bean with one field of each kind a filter can use. */
    public static class Invoice {
        private String number;
        private int total;
        private boolean paid;
        private Status status;
        private LocalDate issued;
        private LocalDateTime created;
        private Long accountId;

        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public boolean isPaid() { return paid; }
        public void setPaid(boolean paid) { this.paid = paid; }
        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
        public LocalDate getIssued() { return issued; }
        public void setIssued(LocalDate issued) { this.issued = issued; }
        public LocalDateTime getCreated() { return created; }
        public void setCreated(LocalDateTime created) { this.created = created; }
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }
    }

    private static final QueryContext CONTEXT = new QueryContext("invoices", Invoice.class,
            new DefaultViewDescriptor(Invoice.class, "json", true), QueryOperation.LIST);

    private static QuerySpec spec(QuerySpec.Filter... filters) {
        return new QuerySpec(1, List.of(filters), null, List.of(), null, null);
    }

    private static QuerySpec.Filter f(String field, String op, Object value) {
        return new QuerySpec.Filter(field, op, value);
    }

    private static String jpql(QuerySpec spec) {
        var query = QueryBuilder.select().from(Invoice.class, "e");
        QuerySpecProcessor.apply(QuerySpecProcessor.filter(CONTEXT, spec, List.of()), query, CONTEXT.descriptor(), "e");
        return query.toString();
    }

    @Test
    void eachOperatorBecomesACondition() {
        var jpql = jpql(spec(f("number", "like", "A-1"), f("total", "gte", 100), f("status", "in", List.of("OPEN", "paid")),
                f("paid", "eq", true), f("issued", "between", List.of("2026-01-01", "2026-01-31"))));

        assertTrue(jpql.contains("e.number"), jpql);
        assertTrue(jpql.contains("e.total >="), jpql);
        assertTrue(jpql.contains("e.status in"), jpql);
        assertTrue(jpql.contains("e.paid"), jpql);
        assertTrue(jpql.contains("e.issued between"), jpql);
    }

    @Test
    void aFieldOutsideTheDescriptorIsRefusedNamingIt() {
        var error = assertThrows(ValidationError.class, () -> jpql(spec(f("password", "eq", "x"))));

        assertTrue(error.getMessage().contains("password"));
    }

    @Test
    void anUnknownOperatorIsRefused() {
        assertThrows(ValidationError.class, () -> jpql(spec(f("number", "regex", ".*"))));
    }

    @Test
    void aBadValueIsRefusedNotIgnored() {
        assertThrows(ValidationError.class, () -> jpql(spec(f("total", "eq", "many"))));
        assertThrows(ValidationError.class, () -> jpql(spec(f("status", "eq", "LOST"))));
        assertThrows(ValidationError.class, () -> jpql(spec(f("issued", "eq", "yesterday"))));
    }

    @Test
    void aRangeMayBeOpenButNotBackwards() {
        assertTrue(jpql(spec(f("issued", "between", java.util.Arrays.asList("2026-01-01", null)))).contains("e.issued >="));
        assertTrue(jpql(spec(f("issued", "between", java.util.Arrays.asList(null, "2026-01-31")))).contains("e.issued <="));
        assertThrows(ValidationError.class, () -> jpql(spec(f("issued", "between", List.of("2026-02-01", "2026-01-01")))));
        assertThrows(ValidationError.class, () -> jpql(spec(f("issued", "between", java.util.Arrays.asList(null, null)))));
    }

    @Test
    void aDateWithoutTimeCoversTheWholeDayOfAFieldWithTime() {
        var from = FilterConditions.toCondition(CONTEXT.descriptor().getField("created"), f("created", "eq", "2026-03-05"));

        assertInstanceOf(tools.dynamia.domain.query.Between.class, from);
    }

    @Test
    void textAndOrderedOperatorsDoNotFitEveryType() {
        assertThrows(ValidationError.class, () -> jpql(spec(f("total", "like", "1"))));
        assertThrows(ValidationError.class, () -> jpql(spec(f("number", "gte", "A"))));
        assertThrows(ValidationError.class, () -> jpql(spec(f("status", "between", List.of("OPEN", "PAID")))));
    }

    @Test
    void onlyOneFilterPerFieldIsAccepted() {
        assertThrows(ValidationError.class, () -> jpql(spec(f("total", "gte", 1), f("total", "lte", 9))));
    }

    @Test
    void sortAndSearchAreCheckedAgainstTheDescriptorToo() {
        var ok = new QuerySpec(1, List.of(), "inv", List.of(new QuerySpec.Sort("total", "desc")), null, null);
        var query = QueryBuilder.select().from(Invoice.class, "e");
        QuerySpecProcessor.apply(ok, query, CONTEXT.descriptor(), "e");
        assertTrue(query.toString().toLowerCase().contains("order by e.total desc"), query.toString());

        var bad = new QuerySpec(1, List.of(), null, List.of(new QuerySpec.Sort("password", "asc")), null, null);
        assertThrows(ValidationError.class, () -> QuerySpecProcessor.apply(bad, QueryBuilder.select().from(Invoice.class, "e"), CONTEXT.descriptor(), "e"));
    }

    @Test
    void aFilterCanDropWhatTheClientMayNotChoose() {
        QuerySpecFilter noAccount = (context, spec) -> spec.without("accountId");

        var checked = QuerySpecProcessor.filter(CONTEXT, spec(f("accountId", "eq", 99), f("total", "gte", 1)), List.of(noAccount));

        assertFalse(checked.filters("accountId"));
        assertTrue(checked.filters("total"));
    }

    @Test
    void aFilterCanImposeAConditionTheClientCannotOverride() {
        QuerySpecFilter scope = (context, spec) -> spec.with(f("accountId", "eq", 7));

        var checked = QuerySpecProcessor.filter(CONTEXT, spec(f("accountId", "eq", 99)), List.of(scope));

        assertEquals(1, checked.filters().size());
        assertEquals(7, checked.filters().get(0).value());
    }

    @Test
    void aFilterCanRefuseAndTheChainRunsByOrder() {
        var calls = new java.util.ArrayList<String>();
        QuerySpecFilter second = new QuerySpecFilter() {
            public int order() { return 2; }
            public QuerySpec filter(QueryContext c, QuerySpec s) { calls.add("second"); return s; }
        };
        QuerySpecFilter first = new QuerySpecFilter() {
            public int order() { return 1; }
            public QuerySpec filter(QueryContext c, QuerySpec s) { calls.add("first"); return s; }
        };
        QuerySpecFilter refuse = (context, spec) -> {
            if (spec.filters("accountId")) {
                throw new ValidationError("accountId cannot be filtered");
            }
            return spec;
        };

        QuerySpecProcessor.filter(CONTEXT, QuerySpec.empty(), List.of(first, second));
        assertEquals(List.of("first", "second"), calls);
        assertThrows(ValidationError.class, () -> QuerySpecProcessor.filter(CONTEXT, spec(f("accountId", "eq", 1)), List.of(refuse)));
    }

    @Test
    void limitsStopAnUnboundedQuery() {
        var tooMany = new java.util.ArrayList<QuerySpec.Filter>();
        for (int i = 0; i <= QuerySpecLimits.MAX_FILTERS; i++) {
            tooMany.add(f("total", "eq", i));
        }
        assertThrows(ValidationError.class, () -> QuerySpecProcessor.filter(CONTEXT, new QuerySpec(1, tooMany, null, List.of(), null, null), List.of()));
        assertThrows(ValidationError.class, () -> QuerySpecProcessor.filter(CONTEXT, new QuerySpec(1, List.of(), null, List.of(), 1, QuerySpecLimits.MAX_SIZE + 1), List.of()));
        assertThrows(ValidationError.class, () -> QuerySpecProcessor.filter(CONTEXT, new QuerySpec(2, List.of(), null, List.of(), null, null), List.of()));
        var many = new java.util.ArrayList<Object>();
        for (int i = 0; i <= QuerySpecLimits.MAX_IN_VALUES; i++) {
            many.add(i);
        }
        assertThrows(ValidationError.class, () -> jpql(spec(f("total", "in", many))));
    }
}
