package tools.dynamia.zk.crud;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.commons.BeanMap;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.commons.collect.PagedListDataSource;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checked-rows helpers of {@link CrudTables} with entities, read-only BeanMap rows and paged lists.
 */
public class CrudTablesTest {

    public static class Sale {
        private final long id;
        private boolean selected;

        Sale(long id) {
            this.id = id;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    public static class Plain {
    }

    private final Map<Long, Sale> database = Map.of(2L, new Sale(2), 3L, new Sale(3));

    @AfterEach
    public void tearDown() {
        Containers.get().removeAllContainers();
    }

    private void installCrudService() {
        CrudService service = (CrudService) Proxy.newProxyInstance(CrudService.class.getClassLoader(), new Class[]{CrudService.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("load") && args.length == 2) {
                        return database.get((Long) args[1]);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        SimpleObjectContainer container = new SimpleObjectContainer("crud-tables-test");
        container.addObject(service);
        Containers.get().installObjectContainer(container);
    }

    private static BeanMap row(long id, boolean selected) {
        BeanMap row = new BeanMap();
        row.setBeanClass(Sale.class);
        row.setId(id);
        row.setSelected(selected);
        return row;
    }

    @Test
    public void checkedRowsKeepsOnlyMarkedEntities() {
        Sale marked = new Sale(1);
        marked.setSelected(true);

        List<Sale> result = CrudTables.checkedRows(List.of(marked, new Sale(9)), Sale.class);

        assertEquals(List.of(marked), result);
    }

    @Test
    public void checkedRowsLoadsTheEntityOfMarkedBeanMaps() {
        installCrudService();

        List<Sale> result = CrudTables.checkedRows(List.of(row(2, true), row(3, false)), Sale.class);

        assertEquals(1, result.size());
        assertSame(database.get(2L), result.getFirst());
    }

    @Test
    public void checkedRowsIgnoresRowsOfOtherTypesAndMissingProperty() {
        installCrudService();
        BeanMap other = row(2, true);
        other.setBeanClass(Plain.class);

        assertTrue(CrudTables.checkedRows(List.of(new Plain(), other), Sale.class).isEmpty());
        assertTrue(CrudTables.checkedRows(List.of(new Plain()), Plain.class).isEmpty(), "no selected property means not marked");
    }

    @Test
    public void checkedRowsAcceptsNullAndEmpty() {
        assertTrue(CrudTables.checkedRows(null, Sale.class).isEmpty());
        assertTrue(CrudTables.checkedRows(List.of(), Sale.class).isEmpty());
    }

    @Test
    public void checkedRowsReadsOnlyTheCurrentPage() {
        Sale marked = new Sale(1);
        marked.setSelected(true);
        PagedList<Sale> paged = new PagedList<>(new PagedListDataSource<>() {
            public Sale getData(int index) {
                throw new AssertionError("other pages must not be loaded");
            }

            public int getTotalSize() {
                return 500;
            }

            public void clear() {
            }

            public List<Sale> getPageData() {
                return List.of(marked);
            }

            public int getPageSize() {
                return 1;
            }

            public int getPageCount() {
                return 500;
            }

            public int getActivePage() {
                return 0;
            }

            public void setActivePage(int activePage) {
            }
        });

        assertEquals(List.of(marked), CrudTables.checkedRows(paged, Sale.class));
    }

    @Test
    public void setCheckedMarksEntitiesAndBeanMaps() {
        Sale sale = new Sale(1);
        BeanMap row = row(2, false);
        Plain plain = new Plain();

        int changed = CrudTables.setChecked(List.of(sale, row, plain), true);

        assertEquals(2, changed);
        assertTrue(sale.isSelected());
        assertTrue(row.isSelected());

        CrudTables.setChecked(List.of(sale, row), false);
        assertFalse(sale.isSelected());
        assertFalse(row.isSelected());
    }

    @Test
    public void tableOfIsEmptyWithoutCrudView() {
        assertTrue(CrudTables.tableOf(null).isEmpty());
        assertTrue(CrudTables.tableOf(new tools.dynamia.crud.CrudActionEvent(null, "x", null, null)).isEmpty());
        assertEquals(0, CrudTables.setAllChecked(null, true));
        assertTrue(CrudTables.checkedRows((tools.dynamia.crud.CrudActionEvent) null, Sale.class).isEmpty());
    }
}
