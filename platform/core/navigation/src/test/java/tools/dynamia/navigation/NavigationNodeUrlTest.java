package tools.dynamia.navigation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The navigation JSON may carry the address of a page only when a browser can load it; internal paths (classpath
 * resources, bean names, class names) must never be sent to clients.
 */
class NavigationNodeUrlTest {

    @Test
    void browsableAddressesAreExposed() {
        assertEquals("/insights/insights.js?page=dashboard", NavigationNode.browsableUrl("/insights/insights.js?page=dashboard"));
        assertEquals("https://dynamia.tools/docs", NavigationNode.browsableUrl("https://dynamia.tools/docs"));
        assertEquals("HTTP://example.com", NavigationNode.browsableUrl("HTTP://example.com"));
    }

    @Test
    void internalPathsAreNeverExposed() {
        assertNull(NavigationNode.browsableUrl("classpath:/zk/pages/books.zul"));
        assertNull(NavigationNode.browsableUrl("tools.dynamia.demo.domain.Book"));
        assertNull(NavigationNode.browsableUrl("discountsCfg"));
        assertNull(NavigationNode.browsableUrl("//evil.example.com/x.js"), "protocol relative URLs are not local paths");
        assertNull(NavigationNode.browsableUrl(null));
    }

    @Test
    void externalPageNodeCarriesItsUrlAndZkPageDoesNot() {
        var external = new NavigationNode(new ExternalPage("dash", "Dashboard", "/insights/insights.js?page=dashboard"));
        assertEquals("/insights/insights.js?page=dashboard", external.getUrl());
        assertEquals("ExternalPage", external.getType());

        var zk = new NavigationNode(new Page("books", "Books", "classpath:/pages/books.zul"));
        assertNull(zk.getUrl());
    }

    @Test
    void invisibleModulesAreLeftOutOfTheTree() {
        var visible = new Module("visible", "Visible").addPage(new Page("p", "P", "/p.html"));
        var hidden = new Module("hidden", "Hidden").visible(false).addPage(new Page("p", "P", "/p.html"));
        assertEquals(true, visible.isVisible());
        assertEquals(false, hidden.isVisible());
    }
}
