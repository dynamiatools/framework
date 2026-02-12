package tools.dynamia.commons;

import my.company.Dummy;
import my.company.PlainDummy;
import org.junit.Assert;
import org.junit.Test;

public class AliasResolverTest {

    @Test
    public void shouldResolveFieldAlias() {
        Dummy dummy = new Dummy();

        var nameAlias = AliasResolver.getFieldAlias(dummy.getClass(), "name");
        Assert.assertNotNull(nameAlias);

        Assert.assertEquals("nombre", nameAlias.value()[0]);
    }

    @Test
    public void shouldResolveClassAlias() {
        String alias = AliasResolver.resolve(Dummy.class, "test");
        Assert.assertEquals("dummy_entity", alias);
    }

    @Test
    public void shouldResolveClassAliasDefaultScope() {
        // Even if scope is "test", it falls back to it if no other match
        String alias = AliasResolver.resolve(Dummy.class);
        Assert.assertEquals("dummy_entity", alias);
    }

    @Test
    public void shouldResolveFieldAliasWithLocale() throws NoSuchFieldException {
        var field = Dummy.class.getDeclaredField("age");
        String alias = AliasResolver.resolve(field, "default", "es");
        Assert.assertEquals("edad", alias);
    }

    @Test
    public void shouldResolveFieldAliasWithScope() throws NoSuchFieldException {
        var field = Dummy.class.getDeclaredField("age");
        String alias = AliasResolver.resolve(field, "dto", "");
        Assert.assertEquals("dummy_age", alias);
    }

    @Test
    public void shouldResolveFieldAliasFallback() throws NoSuchFieldException {
        var field = Dummy.class.getDeclaredField("age");
        // No match for locale "en" and scope "default", so it falls back to all aliases
        // "edad" is first in the list
        String alias = AliasResolver.resolve(field, "default", "en");
        Assert.assertEquals("edad", alias);
    }

    @Test
    public void shouldResolveClassAliasForUnannotatedClass() {
        String alias = AliasResolver.resolve(PlainDummy.class);
        Assert.assertEquals("PlainDummy", alias);
    }

    @Test
    public void shouldResolveFieldAliasForUnannotatedField() throws NoSuchFieldException {
        var field = PlainDummy.class.getDeclaredField("description");
        String alias = AliasResolver.resolve(field, "default", "");
        Assert.assertEquals("description", alias);
    }

    @Test
    public void shouldResolveClassAliasForUnannotatedClassWithScope() {
        String alias = AliasResolver.resolve(PlainDummy.class, "someScope");
        Assert.assertEquals("PlainDummy", alias);
    }
}
