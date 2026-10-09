package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.dynamia.domain.ValidationError;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportQueryValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "select * from sales",
            "SELECT id, name FROM customers WHERE active = true ORDER BY name",
            "  select 1;",
            "with t as (select id from sales) select * from t",
            "(select 1) union (select 2)",
            "select * from sales where status = 'deleted'",
            "select * from sales where note = 'it''s an update; really'",
            "select \"update\" from sales",
            "select update_date, created_by, delete_flag from sales",
            "select * from sales -- remove this later: delete\n where id > 1",
            "/* update the totals */ select sum(total) from sales",
            "select /*+ INDEX(s idx) */ * from sales s",
            "select * from sales where total > :min"
    })
    void acceptsReadOnlySelects(String query) {
        assertDoesNotThrow(() -> ReportQueryValidator.validateQuery(query, "sql"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "delete from sales",
            "update sales set total = 0",
            "insert into sales values (1)",
            "drop table sales",
            "truncate table sales",
            "alter table sales add x int",
            "create table x (id int)",
            "select * from sales; drop table sales",
            "select 1; select 2",
            "with d as (delete from sales returning *) select * from d",
            "select * from sales for update",
            "select * into backup from sales",
            "call do_something()",
            "grant all on sales to bob",
            "select /*! 1; */ 1",
            "show tables",
            "  "
    })
    void rejectsEverythingThatIsNotASingleSelect(String query) {
        assertThrows(ValidationError.class, () -> ReportQueryValidator.validateQuery(query, "sql"));
    }

    @Test
    void rejectsNullQuery() {
        assertThrows(ValidationError.class, () -> ReportQueryValidator.validateQuery(null, "sql"));
    }

    @Test
    void jpqlAcceptsFromAndSelectButNotModifications() {
        assertDoesNotThrow(() -> ReportQueryValidator.validateQuery("from Customer c where c.active = true", "jpql"));
        assertDoesNotThrow(() -> ReportQueryValidator.validateQuery("select c.name, count(o) from Customer c join c.orders o group by c.name", "jpql"));
        assertThrows(ValidationError.class, () -> ReportQueryValidator.validateQuery("update Customer c set c.active = false", "jpql"));
        assertThrows(ValidationError.class, () -> ReportQueryValidator.validateQuery("delete from Customer", "jpql"));
        assertThrows(ValidationError.class, () -> ReportQueryValidator.validateQuery("with x as (select 1) select * from x", "jpql"));
    }

    @Test
    void errorMessageDoesNotEchoTheQuery() {
        var error = assertThrows(ValidationError.class, () -> ReportQueryValidator.validateQuery("select 1; drop table secret_table", "sql"));
        assert !error.getMessage().contains("secret_table");
    }

    @Test
    void fragmentsAreCheckedToo() {
        assertDoesNotThrow(() -> ReportQueryValidator.validateFragment("s.date >= :from and s.status = 'update'"));
        assertDoesNotThrow(() -> ReportQueryValidator.validateFragment(null));
        assertThrows(ValidationError.class, () -> ReportQueryValidator.validateFragment("1=1; drop table sales"));
        assertThrows(ValidationError.class, () -> ReportQueryValidator.validateFragment("id in (delete from sales returning id)"));
    }
}
