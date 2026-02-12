package tools.dynamia.crud;

import org.junit.Assert;
import org.junit.Test;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.viewers.ViewDescriptor;

import static tools.dynamia.viewers.ViewDescriptorBuilder.field;
import static tools.dynamia.viewers.ViewDescriptorBuilder.viewDescriptor;

public class QueryProjectionBuilderTest {


    @Test
    public void shouldBuildQuery() {
        var descriptor = buildDescriptor();

        QueryBuilder builder = QueryProjectionBuilder.buildFromViewDescriptor(TestEntity.class, descriptor, new QueryParameters());
        String jpql = builder.toString();
        String expected = "select e.id, e.name, e.date, e.description, e.notes, e.subentity, (sub.name) as subentity_name from tools.dynamia.crud.TestEntity as e";
        Assert.assertEquals(expected, jpql);
    }

    private ViewDescriptor buildDescriptor() {
        return viewDescriptor("table", TestEntity.class, false)
                .fields(
                        field("name"),
                        field("date"),
                        field("description"),
                        field("notes"),
                        field("subentity"),
                        field("subentity.name")
                                .path("sub.name")
                )
                .build();
    }

}
