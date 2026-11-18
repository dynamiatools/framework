package tools.dynamia.commons.collect;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PagedListPageDataTest {

    private static PagedList<String> paged(List<String> page, int total) {
        return new PagedList<>(new PagedListDataSource<>() {
            @Override
            public String getData(int index) {
                throw new AssertionError("pageData must not load other pages");
            }

            @Override
            public int getTotalSize() {
                return total;
            }

            @Override
            public void clear() {
            }

            @Override
            public List<String> getPageData() {
                return page;
            }

            @Override
            public int getPageSize() {
                return page.size();
            }

            @Override
            public int getPageCount() {
                return 1;
            }

            @Override
            public int getActivePage() {
                return 0;
            }

            @Override
            public void setActivePage(int activePage) {
            }
        });
    }

    @Test
    void pagedListReturnsOnlyTheCurrentPage() {
        List<String> page = List.of("a", "b");
        assertSame(page, PagedList.pageData(paged(page, 100)));
    }

    @Test
    void plainListIsReturnedAsIs() {
        List<String> list = new ArrayList<>(List.of("a"));
        assertSame(list, PagedList.pageData(list));
    }

    @Test
    void otherCollectionsAreCopied() {
        assertEquals(List.of("a"), PagedList.pageData(Set.of("a")));
    }

    @Test
    void nullAndOtherObjectsGiveAnEmptyList() {
        assertTrue(PagedList.pageData(null).isEmpty());
        assertTrue(PagedList.pageData("text").isEmpty());
    }
}
