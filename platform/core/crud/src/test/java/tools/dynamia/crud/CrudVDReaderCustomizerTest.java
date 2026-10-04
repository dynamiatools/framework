package tools.dynamia.crud;

import org.junit.jupiter.api.Test;
import tools.dynamia.viewers.impl.YamlViewDescriptorReader;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CrudVDReaderCustomizerTest {

    private static final tools.dynamia.io.Resource RESOURCE = (tools.dynamia.io.Resource) java.lang.reflect.Proxy.newProxyInstance(
            tools.dynamia.io.Resource.class.getClassLoader(), new Class[]{tools.dynamia.io.Resource.class},
            (proxy, method, args) -> method.getName().equals("getFilename") ? "test.yml" : null);

    private static final String CRUD_YAML = """
            view: crud
            controller: com.acme.BookController
            dataSetView: tree
            parentName: parent
            formView: bookEditorForm
            actions:
              FindAction:
                attributes:
                  searchFields: [name]
            """;

    @Test
    void doesNotNeedZk() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.zkoss.zk.ui.Component"));
    }

    @Test
    void appliesTheCrudSettingsOfAYamlDescriptor() {
        var descriptor = new YamlViewDescriptorReader().read(RESOURCE,
                new StringReader(CRUD_YAML), List.of(new CrudVDReaderCustomizer()));

        var params = descriptor.getParams();
        assertEquals("com.acme.BookController", params.get(CrudVDReaderCustomizer.CONTROLLER_CLASS));
        assertEquals("tree", params.get(CrudVDReaderCustomizer.DATA_SET_VIEW_TYPE));
        assertEquals("parent", params.get(CrudVDReaderCustomizer.PARENT_NAME));
        assertEquals("bookEditorForm", params.get(CrudVDReaderCustomizer.FORM_VIEW_DESCRIPTOR_ID));
        assertEquals(Map.of("FindAction", Map.of("attributes", Map.of("searchFields", List.of("name")))),
                params.get(CrudVDReaderCustomizer.ACTIONS));
    }

    @Test
    void ignoresDescriptorsThatAreNotCrud() {
        var yaml = CRUD_YAML.replace("view: crud", "view: form");

        var descriptor = new YamlViewDescriptorReader().read(RESOURCE,
                new StringReader(yaml), List.of(new CrudVDReaderCustomizer()));

        assertFalse(descriptor.getParams().containsKey(CrudVDReaderCustomizer.CONTROLLER_CLASS));
        assertFalse(descriptor.getParams().containsKey(CrudVDReaderCustomizer.ACTIONS));
    }
}
