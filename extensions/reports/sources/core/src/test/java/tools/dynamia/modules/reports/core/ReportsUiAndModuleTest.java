package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.modules.reports.core.navigation.ReportViewerPage;
import tools.dynamia.modules.reports.ui.DynamiaReportsModule;

import static org.junit.jupiter.api.Assertions.*;

class ReportsUiAndModuleTest {

    @AfterEach
    void clear() {
        System.clearProperty(ReportsUi.PROPERTY);
    }

    @Test
    void coreDoesNotDependOnZk() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.zkoss.zk.ui.Component"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.zkoss.zul.Div"));
    }

    @Test
    void vueIsTheDefaultFront() {
        assertEquals(ReportsUi.VUE, ReportsUi.current());
        assertEquals(ReportsUi.VUE, ReportsUi.parse(null));
        assertEquals(ReportsUi.VUE, ReportsUi.parse(" "));
    }

    @Test
    void zkCanBeSelected() {
        assertEquals(ReportsUi.ZK, ReportsUi.parse("zk"));
        assertEquals(ReportsUi.ZK, ReportsUi.parse(" ZK "));
        System.setProperty(ReportsUi.PROPERTY, "zk");
        assertEquals(ReportsUi.ZK, ReportsUi.current());
    }

    @Test
    void invalidValuesAreRejectedWithTheValidOptions() {
        var error = assertThrows(ReportsException.class, () -> ReportsUi.parse("angular"));
        assertTrue(error.getMessage().contains("vue or zk"));
    }

    @Test
    void settingsExposeTheFront() {
        var settings = new ReportsSettings();
        assertEquals("vue", settings.getUi());
        settings.setUi("zk");
        assertEquals(ReportsUi.ZK, ReportsUi.parse(settings.getUi()));
    }

    @Test
    void moduleUsesTheVueViewerPageByDefaultKeepingTheLegacyPath() {
        var module = new DynamiaReportsModule("reports", "Reports", "desc");

        var page = module.getReportViewerPage();
        assertInstanceOf(ReportViewerPage.class, page);
        assertEquals("ReportViewerPage", page.getClass().getSimpleName());
        assertEquals("classpath:/zk/dynamia/reports/pages/viewer.zul", page.getPath());
    }

    @Test
    void moduleUsesAPlainPageInZkMode() {
        System.setProperty(ReportsUi.PROPERTY, "zk");
        var page = new DynamiaReportsModule("reports", "Reports", "desc").getReportViewerPage();

        assertEquals(tools.dynamia.navigation.Page.class, page.getClass());
        assertEquals("classpath:/zk/dynamia/reports/pages/viewer.zul", page.getPath());
    }

    @Test
    void designPagesAreTheSameInBothModes() {
        var vue = new DynamiaReportsModule("reports", "Reports", "desc");
        System.setProperty(ReportsUi.PROPERTY, "zk");
        var zk = new DynamiaReportsModule("reports", "Reports", "desc");

        assertEquals(vue.getReportDesignPage().getClass(), zk.getReportDesignPage().getClass());
        assertEquals(4, vue.getDefaultPageGroup().getPages().size());
        assertEquals(4, zk.getDefaultPageGroup().getPages().size());
    }
}
