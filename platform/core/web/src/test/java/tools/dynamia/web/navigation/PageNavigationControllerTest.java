package tools.dynamia.web.navigation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.navigation.ModuleContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers the hardening of {@link PageNavigationController#navigate}: the {@code zoom} parameter only accepts numbers,
 * and static resources are recognized by their file extension without touching the file system.
 */
class PageNavigationControllerTest {

    @BeforeEach
    void setUp() {
        Containers.get().removeAllContainers();
        var container = new SimpleObjectContainer();
        container.addObject(new ModuleContainer());
        Containers.get().installObjectContainer(container);
    }

    @AfterEach
    void tearDown() {
        Containers.get().removeAllContainers();
    }

    private static MockHttpServletRequest request(String uri, String zoom) {
        var request = new MockHttpServletRequest("GET", uri);
        if (zoom != null) {
            request.setParameter("zoom", zoom);
        }
        return request;
    }

    private static Object zoomOf(String zoom) {
        var mv = PageNavigationController.navigate("missing/page", request("/page/missing/page", zoom), new MockHttpServletResponse());
        assertNotNull(mv);
        return mv.getModel().get("zoom");
    }

    @Test
    void numericZoomIsPassedToTheStyle() {
        assertEquals("zoom: 0.8;", zoomOf("0.8"));
        assertEquals("zoom: 90%;", zoomOf("90%"));
        assertEquals("zoom: 1;", zoomOf("1"));
    }

    @Test
    void nonNumericZoomIsIgnored() {
        assertNull(zoomOf("1;background:url(//evil)"));
        assertNull(zoomOf("\"><script>alert(1)</script>"));
        assertNull(zoomOf("abc"));
        assertNull(zoomOf(""));
    }

    @Test
    void uriWithAFileExtensionIsAStaticResource() {
        assertNull(PageNavigationController.navigate("logo.png", request("/page/img/logo.png", null), new MockHttpServletResponse()));
        assertNull(PageNavigationController.navigate("app.min.js", request("/page/app.min.js", null), new MockHttpServletResponse()));
    }

    @Test
    void uriWithoutAFileExtensionIsAPage() {
        assertNotNull(PageNavigationController.navigate("store/sales", request("/page/store/sales", null), new MockHttpServletResponse()));
    }
}
