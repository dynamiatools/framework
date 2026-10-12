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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.domain.services.CrudService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the entity equality helpers of {@link JpaUtils} against real Hibernate proxies.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JpaTestConfig.class)
public class JpaUtilsProxyTest {

    @Autowired
    private CrudService crudService;

    @PersistenceContext
    private EntityManager em;

    @Test
    @Transactional
    public void proxyAndRealInstanceAreEqualWithoutInitializingTheProxy() {
        var real = new DummyEntity("real");
        crudService.save(real);
        em.flush();
        em.clear();

        DummyEntity proxy = em.getReference(DummyEntity.class, real.getId());
        assertFalse(Hibernate.isInitialized(proxy), "precondition: lazy proxy");

        assertEquals(real.getId(), JpaUtils.getJPAIdValue(proxy));
        assertEquals(JpaUtils.entityHashCode(real), JpaUtils.entityHashCode(proxy));
        assertFalse(Hibernate.isInitialized(proxy), "reading id and hash must not initialize the proxy");

        assertTrue(JpaUtils.entityEquals(proxy, real));
        assertTrue(JpaUtils.entityEquals(real, proxy));
    }

    @Test
    @Transactional
    public void proxiesOfDifferentRowsAreNotEqual() {
        var a = new DummyEntity("a");
        var b = new DummyEntity("b");
        crudService.save(a);
        crudService.save(b);
        em.flush();
        em.clear();

        DummyEntity proxyA = em.getReference(DummyEntity.class, a.getId());
        DummyEntity proxyB = em.getReference(DummyEntity.class, b.getId());

        assertFalse(JpaUtils.entityEquals(proxyA, proxyB));
        assertTrue(JpaUtils.entityEquals(proxyA, em.getReference(DummyEntity.class, a.getId())));
    }
}
