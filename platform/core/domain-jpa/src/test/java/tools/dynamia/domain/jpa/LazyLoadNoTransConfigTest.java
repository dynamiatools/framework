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

import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code dynamia.app.lazy-load-no-trans} switches Hibernate's {@code enable_lazy_load_no_trans} and is off by default.
 */
public class LazyLoadNoTransConfigTest {

    private static Object lazyLoadNoTransFor(Map<String, Object> properties) {
        var context = new StaticApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
        var adapter = new JpaConfigurationAdapter(null);
        adapter.setApplicationContext(context);

        var factory = new LocalContainerEntityManagerFactoryBean();
        adapter.registerLazyLoadNoTrans(factory);
        return factory.getJpaPropertyMap().get(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS);
    }

    @Test
    public void offByDefault() {
        assertNull(lazyLoadNoTransFor(Map.of()));
    }

    @Test
    public void enabledByProperty() {
        assertEquals(true, lazyLoadNoTransFor(Map.of(JpaConfigurationAdapter.LAZY_LOAD_NO_TRANS_PROPERTY, "true")));
    }

    @Test
    public void explicitFalseKeepsItOff() {
        assertNull(lazyLoadNoTransFor(Map.of(JpaConfigurationAdapter.LAZY_LOAD_NO_TRANS_PROPERTY, "false")));
    }

    @Test
    public void withoutApplicationContextItStaysOff() {
        var adapter = new JpaConfigurationAdapter(null);
        var factory = new LocalContainerEntityManagerFactoryBean();

        adapter.registerLazyLoadNoTrans(factory);

        assertFalse(factory.getJpaPropertyMap().containsKey(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS));
    }
}
