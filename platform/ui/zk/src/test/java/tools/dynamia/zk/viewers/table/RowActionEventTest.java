package tools.dynamia.zk.viewers.table;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.commons.BeanMap;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The event of row actions and of field actions (a column with {@code action:}) hands the real entity to the action
 * when the row is a read-only BeanMap, loading it only when the action asks for it.
 */
public class RowActionEventTest {

    public static class Sale {
    }

    private final Sale loaded = new Sale();

    @AfterEach
    public void tearDown() {
        Containers.get().removeAllContainers();
    }

    private List<Object[]> installCrudService() {
        List<Object[]> loads = new ArrayList<>();
        CrudService service = (CrudService) Proxy.newProxyInstance(CrudService.class.getClassLoader(), new Class[]{CrudService.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("load") && args.length == 2) {
                        loads.add(args);
                        return loaded;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        SimpleObjectContainer container = new SimpleObjectContainer("row-action-event-test");
        container.addObject(service);
        Containers.get().installObjectContainer(container);
        return loads;
    }

    private static BeanMap row() {
        BeanMap row = new BeanMap();
        row.setBeanClass(Sale.class);
        row.setId(5L);
        return row;
    }

    @Test
    public void nothingIsLoadedUntilTheActionAsksForTheData() {
        List<Object[]> loads = installCrudService();

        new TableViewRowRenderer.RowActionEvent(row(), "item");

        assertTrue(loads.isEmpty());
    }

    @Test
    public void beanMapRowIsLoadedOnceWhenTheActionReadsTheData() {
        List<Object[]> loads = installCrudService();
        TableViewRowRenderer.RowActionEvent evt = new TableViewRowRenderer.RowActionEvent(row(), "item");

        assertSame(loaded, evt.getData());
        assertSame(loaded, evt.getData());
        assertSame(loaded, evt.getData(Sale.class));
        assertEquals(1, loads.size());
        assertEquals(5L, loads.getFirst()[1]);
    }

    @Test
    public void fieldActionEventKeepsItsParamsAndResolvesTheRow() {
        installCrudService();
        TableViewRowRenderer.RowActionEvent evt = new TableViewRowRenderer.RowActionEvent(row(), "link", Map.of("field", "numero"));

        assertEquals("numero", evt.getParam("field"));
        assertSame(loaded, evt.getData());
    }

    @Test
    public void entityRowsAndMissingCrudServiceAreLeftAlone() {
        Sale sale = new Sale();
        assertSame(sale, new TableViewRowRenderer.RowActionEvent(sale, "item").getData());

        BeanMap row = row();
        assertSame(row, new TableViewRowRenderer.RowActionEvent(row, "item").getData());
    }
}
