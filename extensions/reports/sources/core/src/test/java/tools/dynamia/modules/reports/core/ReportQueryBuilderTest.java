package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.Test;
import tools.dynamia.modules.reports.core.domain.ReportFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ReportQueryBuilderTest {

    private static ReportFilters filters(String... nameAndCondition) {
        var filters = new ReportFilters();
        for (int i = 0; i < nameAndCondition.length; i += 2) {
            var filter = new ReportFilter(nameAndCondition[i]);
            filter.setCondition(nameAndCondition[i + 1]);
            filters.add(filter, "x");
        }
        return filters;
    }

    private static String normalize(String sql) {
        return sql.trim().replaceAll("\\s+", " ");
    }

    @Test
    void withoutFiltersTheQueryIsUntouched() {
        assertEquals("select * from sales", normalize(ReportQueryBuilder.build("select * from sales", new ReportFilters())));
    }

    @Test
    void addsWhereWhenQueryHasNone() {
        var sql = ReportQueryBuilder.build("select * from sales", filters("min", "total >= :min"));
        assertEquals("select * from sales where 1=1 and total >= :min", normalize(sql));
    }

    @Test
    void detectsUppercaseWhere() {
        var sql = ReportQueryBuilder.build("SELECT * FROM sales WHERE active = true", filters("min", "total >= :min"));
        assertEquals("SELECT * FROM sales WHERE active = true and total >= :min", normalize(sql));
    }

    @Test
    void whereInsideSubqueryOrIdentifierDoesNotCount() {
        var sql = ReportQueryBuilder.build("select s.somewhere, (select max(x) from t where t.id = 1) m from sales s", filters("min", "total >= :min"));
        assertEquals("select s.somewhere, (select max(x) from t where t.id = 1) m from sales s where 1=1 and total >= :min", normalize(sql));
    }

    @Test
    void whereInsideStringOrCommentDoesNotCount() {
        var sql = ReportQueryBuilder.build("select 'where' as w -- where\n from sales", filters("min", "total >= :min"));
        assertEquals("select 'where' as w -- where from sales where 1=1 and total >= :min", normalize(sql).replace("-- where from", "-- where from"));
    }

    @Test
    void filtersGoBeforeGroupByAndOrderBy() {
        var sql = ReportQueryBuilder.build("select region, sum(total) from sales group by region order by region", filters("min", "total >= :min"));
        assertEquals("select region, sum(total) from sales where 1=1 and total >= :min group by region order by region", normalize(sql));

        sql = ReportQueryBuilder.build("select * from sales where active = true order by id limit 10", filters("min", "total >= :min"));
        assertEquals("select * from sales where active = true and total >= :min order by id limit 10", normalize(sql));
    }

    @Test
    void jpqlEntityCalledOrderIsNotAnOrderByClause() {
        var sql = ReportQueryBuilder.build("select o from Order o order by o.id", filters("min", "o.total >= :min"));
        assertEquals("select o from Order o where 1=1 and o.total >= :min order by o.id", normalize(sql));
    }

    @Test
    void explicitMarkerIsReplaced() {
        var sql = ReportQueryBuilder.build("select * from sales where active = true <FILTERS> group by region", filters("min", "total >= :min", "max", "total <= :max"));
        assertEquals("select * from sales where active = true and total >= :min and total <= :max group by region", normalize(sql));
    }

    @Test
    void markerWithoutWhereAddsOne() {
        var sql = ReportQueryBuilder.build("select * from sales <FILTERS> order by id", filters("min", "total >= :min"));
        assertEquals("select * from sales where 1=1 and total >= :min order by id", normalize(sql));
    }

    @Test
    void markerIsRemovedWhenThereAreNoConditions() {
        var sql = ReportQueryBuilder.build("select * from sales where active = true <FILTERS>", new ReportFilters());
        assertEquals("select * from sales where active = true", normalize(sql));
    }

    @Test
    void markerInsideSubqueryUsesTheWhereOfThatSubquery() {
        var sql = ReportQueryBuilder.build("select * from (select * from sales <FILTERS>) s where s.id > 0", filters("min", "total >= :min"));
        assertEquals("select * from (select * from sales where 1=1 and total >= :min) s where s.id > 0", normalize(sql));
    }

    @Test
    void filtersWithoutConditionAreIgnoredAndTrailingSemicolonRemoved() {
        var filters = filters("min", "total >= :min");
        filters.add(new ReportFilter("accountId"), 1L);
        var sql = ReportQueryBuilder.build("select * from sales;", filters);
        assertEquals("select * from sales where 1=1 and total >= :min", normalize(sql));
        assertFalse(sql.contains(";"));
    }

    @Test
    void newLinesAndTabsAreNormalized() {
        var sql = ReportQueryBuilder.build("select *\n\tfrom sales\nwhere a = 1", new ReportFilters());
        assertEquals("select * from sales where a = 1", normalize(sql));
    }
}
