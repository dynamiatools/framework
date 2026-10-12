/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.navigation;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DynamicPageCacheTest {

    /**
     * Group whose single page depends on the "account" in use, like the ERP's per-tenant dynamic groups.
     */
    private static class AccountGroup extends PageGroup {
        String account = "a";

        AccountGroup() {
            super("queries", "Queries");
            setDynamic(true);
        }

        @Override
        public List<Page> getPages() {
            Page page = new Page("sales" + account, "Sales", "zk/" + account);
            page.setPageGroup(this);
            return List.of(page);
        }
    }

    private static class MapCache implements DynamicPageCache {
        final Map<String, Page> data = new HashMap<>();

        public Page get(String key) {
            return data.get(key);
        }

        public void put(String key, Page page) {
            data.put(key, page);
        }
    }

    private ModuleContainer container(AccountGroup group, DynamicPageCache cache) {
        var container = new ModuleContainer();
        container.setDynamicPageCache(cache);
        container.installModule(new Module("mod", "Mod").addPageGroup(group));
        return container;
    }

    @Test
    public void shouldNotShareDynamicPagesBetweenSessions() {
        var group = new AccountGroup();
        var container = container(group, new MapCache());

        Page first = container.findPageByPrettyVirtualPath("mod/queries/sales");
        assertEquals("salesa", first.getId());

        // another account, another session (empty cache): must get its own page, not the first one
        group.account = "b";
        container.setDynamicPageCache(new MapCache());
        Page second = container.findPageByPrettyVirtualPath("mod/queries/sales");
        assertEquals("salesb", second.getId());
    }

    @Test
    public void shouldReuseDynamicPageWithinSession() {
        var group = new AccountGroup();
        var container = container(group, new MapCache());

        Page first = container.findPageByPrettyVirtualPath("mod/queries/sales");
        group.account = "b";
        assertSame(first, container.findPageByPrettyVirtualPath("mod/queries/sales"));
    }

    @Test
    public void shouldNotCacheDynamicPagesGloballyWithoutSessionCache() {
        var group = new AccountGroup();
        var container = container(group, null);

        assertEquals("salesa", container.findPageByPrettyVirtualPath("mod/queries/sales").getId());
        group.account = "b";
        assertEquals("salesb", container.findPageByPrettyVirtualPath("mod/queries/sales").getId());
        assertThrows(PageNotFoundException.class, () -> container.findPage("mod/nope"));
    }
}
