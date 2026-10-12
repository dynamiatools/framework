package tools.dynamia.commons;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BeanMapTest {

    static class WithField {
        @InstanceName
        private String label = "From field";

        @Override
        public String toString() {
            return "toString";
        }
    }

    static class WithMethod {
        @InstanceName
        public String display() {
            return "From method";
        }

        @Override
        public String toString() {
            return "toString";
        }
    }

    static class WithNullName {
        @InstanceName
        private String label;

        @Override
        public String toString() {
            return "toString";
        }
    }

    static class Plain {
        @Override
        public String toString() {
            return "toString";
        }
    }

    private static String loaded(Object bean) {
        var map = new BeanMap();
        map.load(bean);
        return map.toString();
    }

    @Test
    void loadUsesInstanceNameField() {
        assertEquals("From field", loaded(new WithField()));
    }

    @Test
    void loadUsesInstanceNameMethod() {
        assertEquals("From method", loaded(new WithMethod()));
    }

    @Test
    void loadFallsBackToToString() {
        assertEquals("toString", loaded(new Plain()));
        assertEquals("toString", loaded(new WithNullName()));
    }

    @Test
    void getInstanceNameFallsBackWhenFieldIsBlank() {
        assertEquals("toString", ObjectOperations.getInstanceName(new WithNullName()));
    }

    @Test
    void toJsonWritesOnlyEntries() {
        var map = new BeanMap();
        map.setName("Person");
        map.setId(1L);
        map.set("name", "Mario");
        map.set("age", 30);

        var json = map.toJson();
        assertEquals(Map.of("name", "Mario", "age", 30), StringPojoParser.parseJsonToMap(json));
    }

    @Test
    void toXmlUsesNameAsRoot() {
        var map = new BeanMap();
        map.setName("Person");
        map.set("name", "Mario");

        var xml = map.toXml();
        assertTrue(xml.contains("<Person>"), xml);
        assertTrue(xml.contains("<name>Mario</name>"), xml);
        assertFalse(xml.contains("beanClass"), xml);
    }

    @Test
    void toXmlFallsBackToBeanMapRoot() {
        var map = new BeanMap();
        map.set("a", 1);
        assertTrue(map.toXml().contains("<BeanMap>"));
    }
}
