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

import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the pure (no database) helpers of {@link JpaUtils}.
 */
class JpaUtilsTest {

    static class NotEntity {
    }

    @Test
    void isJPAEntityDetectsAnnotatedClasses() {
        assertTrue(JpaUtils.isJPAEntity(new DummyEntity()));
        assertTrue(JpaUtils.isJPAEntity(DummyEntity.class));
        assertFalse(JpaUtils.isJPAEntity(new NotEntity()));
        assertFalse(JpaUtils.isJPAEntity((Object) null));
        assertFalse(JpaUtils.isJPAEntity((Class<?>) null));
    }

    @Test
    void getJPAIdValueReadsIdentifiableAndAnnotatedField() {
        var dummy = new DummyEntity("a");
        dummy.setId(5L);
        assertEquals(5L, JpaUtils.getJPAIdValue(dummy));
    }

    @Test
    void getJPAIdValueFailsForInvalidObjects() {
        assertThrows(PersistenceException.class, () -> JpaUtils.getJPAIdValue(new NotEntity()));
            }

    @Test
    void isNewIsTrueOnlyWithoutId() {
        var dummy = new DummyEntity("a");
        assertTrue(JpaUtils.isNew(dummy));
        dummy.setId(1L);
        assertFalse(JpaUtils.isNew(dummy));
    }

    @Test
    void isSameEntityComparesClassAndId() {
        var a = new DummyEntity("a");
        var b = new DummyEntity("b");
        a.setId(1L);
        b.setId(1L);
        assertTrue(JpaUtils.isSameEntity(a, b));
        assertTrue(JpaUtils.isSameEntity(a, a));

        b.setId(2L);
        assertFalse(JpaUtils.isSameEntity(a, b));

        assertFalse(JpaUtils.isSameEntity(a, new MapperCategory()));
    }

    @Test
    void isSameEntityIsFalseForNullNonEntitiesAndTransientEntities() {
        var a = new DummyEntity("a");
        var b = new DummyEntity("b");
        assertFalse(JpaUtils.isSameEntity(a, b), "both transient");
        assertFalse(JpaUtils.isSameEntity(a, null));
        assertFalse(JpaUtils.isSameEntity(null, null));
        assertFalse(JpaUtils.isSameEntity(new NotEntity(), new NotEntity()));
    }

    @Test
    void getEntityClassHandlesNullAndPlainObjects() {
        assertNull(JpaUtils.getEntityClass(null));
        assertEquals(DummyEntity.class, JpaUtils.getEntityClass(new DummyEntity()));
    }

    @Test
    void getEntityNameUsesAnnotationNameOrSimpleName() {
        assertEquals("DummyEntity", JpaUtils.getEntityName(DummyEntity.class));
        assertThrows(PersistenceException.class, () -> JpaUtils.getEntityName(NotEntity.class));
    }

    @Test
    void unproxyReturnsSameInstanceForNonProxies() {
        var dummy = new DummyEntity("a");
        assertSame(dummy, JpaUtils.unproxy(dummy));
        assertNull(JpaUtils.unproxy(null));
    }

    @Test
    void checkIdTypeConvertsStringsAndKeepsOtherValues() {
        assertNull(JpaUtils.checkIdType(DummyEntity.class, "  "));
        assertEquals(10L, JpaUtils.checkIdType(DummyEntity.class, 10L));
        assertEquals("abc", JpaUtils.checkIdType(NotEntity.class, "abc"));
    }

    @Test
    void initializeEntityAcceptsNull() {
        assertDoesNotThrow(() -> JpaUtils.initializeEntity(null));
        assertDoesNotThrow(() -> JpaUtils.initializeEntity(null, true));
    }
}
