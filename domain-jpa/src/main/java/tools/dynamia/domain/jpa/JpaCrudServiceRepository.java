/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

/**
 * @author Mario Serrano Leones
 */
public class JpaCrudServiceRepository<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> {

    private final CrudService crudService = Containers.get().findObject(JpaCrudService.class);

    public JpaCrudServiceRepository(JpaEntityInformation entityInformation,
                                    EntityManager entityManager) {
        super(entityInformation, entityManager);

    }

    @Override
    public List<T> findAll(Sort sort) {
        Sort.Order order = sort.iterator().next();
        if (order != null) {
            return crudService.find(getDomainClass(), new QueryParameters(new BeanSorter(order.getProperty(), order.isAscending()), null));
        } else {
            return crudService.findAll(getDomainClass());
        }
    }

    @Override
    public Page<T> findAll(Pageable pgbl) {
        DataPaginator paginator = new DataPaginator(0, pgbl.getPageSize(), pgbl.getPageNumber());
        QueryParameters qp = new QueryParameters(paginator);
        PagedList<T> page = ((PagedList<T>) crudService.find(getDomainClass(), qp));

        return new PageImpl(page.getDataSource().getPageData(), pgbl, paginator.getTotalSize());
    }

    @Override
    public <S extends T> S save(S s) {
        return crudService.save(s);
    }


    @Override
    public List<T> findAll() {
        return crudService.findAll(getDomainClass());
    }


    @Override
    public long count() {
        return crudService.count(getDomainClass());
    }


    @Override
    public void delete(T t) {
        crudService.delete(t);
    }


    @Override
    public void deleteAll() {
        crudService.deleteAll(getDomainClass());
    }

}
