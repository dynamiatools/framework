package tools.dynamia.commons;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test class for {@link Scopes} utility functionality.
 * Demonstrates the usage of scope annotations and filtering.
 */
public class ScopesTest {

    /**
     * Test entity with exposed fields in different scopes
     */
    static class TestEntity {
        @Exposure(scope = {Scopes.UI})
        private String displayName;

        @Exposure(scope = {Scopes.API, Scopes.EXTERNAL})
        private String publicId;

        @Exposure(scope = {Scopes.INTERNAL})
        private String internalCode;

        @Exposure(scope = {Scopes.UI, Scopes.API})
        private String email;

        private String noExposure;

        @Exposure(scope = {Scopes.UI})
        public String getDisplayName() {
            return displayName;
        }

        @Exposure(scope = {Scopes.API, Scopes.EXTERNAL})
        public String getPublicData() {
            return "public";
        }

        @Exposure(scope = {Scopes.INTERNAL})
        public void internalMethod() {
            // internal only
        }

        public void publicMethod() {
            // no exposure annotation
        }
    }

    @Test
    public void testScopeConstantsAreLowercase() {
        // Verify all scope constants are lowercase
        assertEquals("module", Scopes.MODULE);
        assertEquals("application", Scopes.APPLICATION);
        assertEquals("system", Scopes.SYSTEM);
        assertEquals("external", Scopes.EXTERNAL);
        assertEquals("internal", Scopes.INTERNAL);
        assertEquals("ui", Scopes.UI);
        assertEquals("api", Scopes.API);
        assertEquals("service", Scopes.PUBLIC);
        assertEquals("data-api", Scopes.DATA_API);
        assertEquals("admin-api", Scopes.ADMIN_API);
        assertEquals("auth-api", Scopes.AUTH_API);
        assertEquals("reporting-api", Scopes.REPORTING_API);
        assertEquals("meta-api", Scopes.METADATA_API);
    }

    @Test
    public void testRegisterScope() {
        String customScope = "custom-scope";
        Scopes.registerScope(customScope);
        assertTrue(Scopes.isRegistered(customScope));

        // Test case insensitivity
        assertTrue(Scopes.isRegistered("CUSTOM-SCOPE"));
        assertTrue(Scopes.isRegistered("Custom-Scope"));
    }

    @Test
    public void testGetRegisteredScopes() {
        Set<String> scopes = Scopes.getRegisteredScopes();
        assertNotNull(scopes);
        assertTrue(scopes.contains(Scopes.UI));
        assertTrue(scopes.contains(Scopes.API));
        assertTrue(scopes.contains(Scopes.MODULE));

        // Verify immutability
        try {
            scopes.add("test");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testGetFieldsByScope() {
        List<Field> uiFields = Scopes.getFieldsByScope(TestEntity.class, Scopes.UI);
        assertEquals(2, uiFields.size());
        assertTrue(uiFields.stream().anyMatch(f -> f.getName().equals("displayName")));
        assertTrue(uiFields.stream().anyMatch(f -> f.getName().equals("email")));

        List<Field> apiFields = Scopes.getFieldsByScope(TestEntity.class, Scopes.API);
        assertEquals(2, apiFields.size());
        assertTrue(apiFields.stream().anyMatch(f -> f.getName().equals("publicId")));
        assertTrue(apiFields.stream().anyMatch(f -> f.getName().equals("email")));

        List<Field> internalFields = Scopes.getFieldsByScope(TestEntity.class, Scopes.INTERNAL);
        assertEquals(1, internalFields.size());
        assertEquals("internalCode", internalFields.get(0).getName());
    }

    @Test
    public void testGetFieldsByScopesMultiple() {
        List<Field> fields = Scopes.getFieldsByScopes(TestEntity.class, Scopes.UI, Scopes.API);
        assertEquals(3, fields.size()); // displayName, publicId, email
    }

    @Test
    public void testGetMethodsByScope() {
        List<Method> uiMethods = Scopes.getMethodsByScope(TestEntity.class, Scopes.UI);
        assertEquals(1, uiMethods.size());
        assertEquals("getDisplayName", uiMethods.get(0).getName());

        List<Method> apiMethods = Scopes.getMethodsByScope(TestEntity.class, Scopes.API);
        assertEquals(1, apiMethods.size());
        assertEquals("getPublicData", apiMethods.get(0).getName());

        List<Method> internalMethods = Scopes.getMethodsByScope(TestEntity.class, Scopes.INTERNAL);
        assertEquals(1, internalMethods.size());
        assertEquals("internalMethod", internalMethods.get(0).getName());
    }

    @Test
    public void testGetMethodsByScopesMultiple() {
        List<Method> methods = Scopes.getMethodsByScopes(TestEntity.class, Scopes.API, Scopes.EXTERNAL);
        assertEquals(1, methods.size());
        assertEquals("getPublicData", methods.get(0).getName());
    }

    @Test
    public void testIsExposedInScopeForField() throws NoSuchFieldException {
        Field displayName = TestEntity.class.getDeclaredField("displayName");
        assertTrue(Scopes.isExposedInScope(displayName, Scopes.UI));
        assertFalse(Scopes.isExposedInScope(displayName, Scopes.API));

        Field email = TestEntity.class.getDeclaredField("email");
        assertTrue(Scopes.isExposedInScope(email, Scopes.UI));
        assertTrue(Scopes.isExposedInScope(email, Scopes.API));
        assertFalse(Scopes.isExposedInScope(email, Scopes.INTERNAL));

        Field noExposure = TestEntity.class.getDeclaredField("noExposure");
        assertFalse(Scopes.isExposedInScope(noExposure, Scopes.UI));
    }

    @Test
    public void testIsExposedInScopeForMethod() throws NoSuchMethodException {
        Method getDisplayName = TestEntity.class.getDeclaredMethod("getDisplayName");
        assertTrue(Scopes.isExposedInScope(getDisplayName, Scopes.UI));
        assertFalse(Scopes.isExposedInScope(getDisplayName, Scopes.API));

        Method getPublicData = TestEntity.class.getDeclaredMethod("getPublicData");
        assertTrue(Scopes.isExposedInScope(getPublicData, Scopes.API));
        assertTrue(Scopes.isExposedInScope(getPublicData, Scopes.EXTERNAL));

        Method publicMethod = TestEntity.class.getDeclaredMethod("publicMethod");
        assertFalse(Scopes.isExposedInScope(publicMethod, Scopes.UI));
    }

    @Test
    public void testIsExposedInAnyScopeForField() throws NoSuchFieldException {
        Field email = TestEntity.class.getDeclaredField("email");
        assertTrue(Scopes.isExposedInAnyScope(email, Scopes.UI, Scopes.API));
        assertTrue(Scopes.isExposedInAnyScope(email, Scopes.UI));
        assertTrue(Scopes.isExposedInAnyScope(email, Scopes.API, Scopes.INTERNAL));
        assertFalse(Scopes.isExposedInAnyScope(email, Scopes.INTERNAL, Scopes.EXTERNAL));

        Field noExposure = TestEntity.class.getDeclaredField("noExposure");
        assertFalse(Scopes.isExposedInAnyScope(noExposure, Scopes.UI, Scopes.API));
    }

    @Test
    public void testIsExposedInAnyScopeForMethod() throws NoSuchMethodException {
        Method getPublicData = TestEntity.class.getDeclaredMethod("getPublicData");
        assertTrue(Scopes.isExposedInAnyScope(getPublicData, Scopes.API, Scopes.EXTERNAL));
        assertTrue(Scopes.isExposedInAnyScope(getPublicData, Scopes.API));
        assertFalse(Scopes.isExposedInAnyScope(getPublicData, Scopes.UI, Scopes.INTERNAL));
    }

    @Test
    public void testGetExposedScopesForField() throws NoSuchFieldException {
        Field email = TestEntity.class.getDeclaredField("email");
        String[] scopes = Scopes.getExposedScopes(email);
        assertEquals(2, scopes.length);
        assertTrue(containsScope(scopes, Scopes.UI));
        assertTrue(containsScope(scopes, Scopes.API));

        Field noExposure = TestEntity.class.getDeclaredField("noExposure");
        String[] emptyScopes = Scopes.getExposedScopes(noExposure);
        assertEquals(0, emptyScopes.length);
    }

    @Test
    public void testGetExposedScopesForMethod() throws NoSuchMethodException {
        Method getPublicData = TestEntity.class.getDeclaredMethod("getPublicData");
        String[] scopes = Scopes.getExposedScopes(getPublicData);
        assertEquals(2, scopes.length);
        assertTrue(containsScope(scopes, Scopes.API));
        assertTrue(containsScope(scopes, Scopes.EXTERNAL));
    }

    @Test
    public void testCaseInsensitiveScopeMatching() throws NoSuchFieldException {
        Field displayName = TestEntity.class.getDeclaredField("displayName");
        // Scope constants are lowercase, but matching should be case-insensitive
        assertTrue(Scopes.isExposedInScope(displayName, "UI"));
        assertTrue(Scopes.isExposedInScope(displayName, "ui"));
        assertTrue(Scopes.isExposedInScope(displayName, "Ui"));
    }

    @Test
    public void testNullSafetyForFields() {
        List<Field> fields = Scopes.getFieldsByScope(null, Scopes.UI);
        assertTrue(fields.isEmpty());

        fields = Scopes.getFieldsByScope(TestEntity.class, null);
        assertTrue(fields.isEmpty());

        assertFalse(Scopes.isExposedInScope((Field) null, Scopes.UI));
    }

    @Test
    public void testNullSafetyForMethods() {
        List<Method> methods = Scopes.getMethodsByScope(null, Scopes.API);
        assertTrue(methods.isEmpty());

        methods = Scopes.getMethodsByScope(TestEntity.class, null);
        assertTrue(methods.isEmpty());

        assertFalse(Scopes.isExposedInScope((Method) null, Scopes.API));
    }

    @Test
    public void testInheritedFieldsAndMethods() {
        class Parent {
            @Exposure(scope = {Scopes.UI})
            private String parentField;

            @Exposure(scope = {Scopes.API})
            public void parentMethod() {
            }
        }

        class Child extends Parent {
            @Exposure(scope = {Scopes.UI})
            private String childField;

            @Exposure(scope = {Scopes.API})
            public void childMethod() {
            }
        }

        // Should include both parent and child fields/methods
        List<Field> uiFields = Scopes.getFieldsByScope(Child.class, Scopes.UI);
        assertEquals(2, uiFields.size());

        List<Method> apiMethods = Scopes.getMethodsByScope(Child.class, Scopes.API);
        assertEquals(2, apiMethods.size());
    }

    private boolean containsScope(String[] scopes, String target) {
        for (String scope : scopes) {
            if (scope.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }
}
