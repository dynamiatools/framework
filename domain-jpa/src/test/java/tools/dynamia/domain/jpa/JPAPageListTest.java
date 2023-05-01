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
package tools.dynamia.domain.jpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class JPAPageListTest {

    @Autowired
    private CrudService crudService;

    @Test
    @Transactional
    public void testPagedListJPA() {
        for (int i = 0; i < 50; i++) {
            DummyEntity dummy = new DummyEntity("Dummy" + i);
            crudService.save(dummy);
        }

        DataPaginator paginator = new DataPaginator();
        paginator.setPageSize(10);

        QueryParameters params = new QueryParameters();
        params.paginate(paginator);

        List<DummyEntity> result = crudService.find(DummyEntity.class, params);

        assertTrue(result instanceof PagedList);

        PagedList<DummyEntity> pagedList = (PagedList<DummyEntity>) result;
        assertNotNull(pagedList.getDataSource().getPageData());
        assertFalse(pagedList.getDataSource().getPageData().isEmpty());

        showPage(paginator);

        int count = 0;
        for (DummyEntity parameter : result) {
            switch (count) {
                case 10:
                    showPage(paginator);
                    assertEquals(paginator.getPage(), 2);
                    break;
                case 20:
                    showPage(paginator);
                    assertEquals(paginator.getPage(), 3);
                    break;
                case 30:
                    showPage(paginator);
                    assertEquals(paginator.getPage(), 4);
                    break;
                case 40:
                    showPage(paginator);
                    assertEquals(paginator.getPage(), 5);
                    break;
            }
            count++;
        }
        assertTrue(true);
    }

    private void showPage(DataPaginator paginator) {
    }
}
