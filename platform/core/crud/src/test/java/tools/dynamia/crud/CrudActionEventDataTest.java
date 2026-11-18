package tools.dynamia.crud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.commons.BeanMap;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrudActionEventDataTest {

    static class Sale {
    }

    private final Sale loaded = new Sale();

    @AfterEach
    void tearDown() {
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
        SimpleObjectContainer container = new SimpleObjectContainer("crud-action-event-test");
        container.addObject(service);
        Containers.get().installObjectContainer(container);
        return loads;
    }

    private static BeanMap row(Class<?> type, Object id) {
        BeanMap row = new BeanMap();
        row.setBeanClass(type);
        row.setId(id);
        return row;
    }

    private static CrudActionEvent event(Object data) {
        return new CrudActionEvent(data, "source", null, null);
    }

    @Test
    void entityIsReturnedWithoutTouchingTheCrudService() {
        List<Object[]> loads = installCrudService();
        Sale sale = new Sale();
        assertSame(sale, event(sale).getData(Sale.class));
        assertTrue(loads.isEmpty());
    }

    @Test
    void beanMapOfTheTypeIsLoadedByIdOnlyOnce() {
        List<Object[]> loads = installCrudService();
        CrudActionEvent evt = event(row(Sale.class, 7L));

        assertSame(loaded, evt.getData(Sale.class));
        assertSame(loaded, evt.getData(Sale.class));
        assertEquals(1, loads.size());
        assertEquals(Sale.class, loads.getFirst()[0]);
        assertEquals(7L, loads.getFirst()[1]);
    }

    @Test
    void beanMapOfAnotherTypeIsNotLoaded() {
        List<Object[]> loads = installCrudService();
        assertNull(event(row(String.class, 7L)).getData(Sale.class));
        assertTrue(loads.isEmpty());
    }

    @Test
    void beanMapWithoutIdIsNotLoaded() {
        List<Object[]> loads = installCrudService();
        assertNull(event(row(Sale.class, null)).getData(Sale.class));
        assertTrue(loads.isEmpty());
    }

    @Test
    void beanMapStaysWhenThereIsNoCrudService() {
        BeanMap row = row(Sale.class, 7L);
        assertNull(event(row).getData(Sale.class));
        assertSame(row, event(row).getData());
    }

    @Test
    void requireDataReturnsTheDataOrFailsWithTheMessage() {
        installCrudService();
        Sale sale = new Sale();
        assertSame(sale, event(sale).requireData(Sale.class, "Seleccione venta"));

        ValidationError error = assertThrows(ValidationError.class, () -> event(null).requireData(Sale.class, "Seleccione venta 100%"));
        assertEquals("Seleccione venta 100%", error.getMessage());
    }
}
