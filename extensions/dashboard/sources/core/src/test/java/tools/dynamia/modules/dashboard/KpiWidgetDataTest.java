package tools.dynamia.modules.dashboard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KpiWidgetDataTest {

    @Test
    void theShortConstructorsLeaveThePresentationHintsEmpty() {
        var kpi = new KpiWidgetData(42, "Sales", "USD", 0.1);

        assertNull(kpi.description());
        assertNull(kpi.icon());
        assertNull(kpi.color());
        assertEquals(new KpiWidgetData(42, "Sales", null, null), new KpiWidgetData(42, "Sales"));
    }

    @Test
    void withHintsKeepsTheIndicatorAndAddsTheHints() {
        var kpi = new KpiWidgetData(42, "Sales", "USD", 0.1).withHints("12 sales made", "cart", "success");

        assertEquals(42, kpi.value());
        assertEquals("Sales", kpi.label());
        assertEquals("USD", kpi.unit());
        assertEquals(0.1, kpi.trend());
        assertEquals("12 sales made", kpi.description());
        assertEquals("cart", kpi.icon());
        assertEquals("success", kpi.color());
    }
}
