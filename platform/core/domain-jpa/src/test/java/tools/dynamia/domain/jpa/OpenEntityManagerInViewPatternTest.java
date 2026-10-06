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

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenEntityManagerInViewPatternTest {

    private static boolean disabledWith(Map<String, Object> properties) {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
            ctx.register(OpenEntityManagerInViewPattern.class);
            ctx.refresh();
            return ctx.getBean(OpenEntityManagerInViewPattern.class).isDisabled();
        }
    }

    @Test
    public void enabledByDefault() {
        assertFalse(disabledWith(Map.of()));
    }

    @Test
    public void disabledByProperty() {
        assertTrue(disabledWith(Map.of("dynamia.app.open-persistence-in-view", "false")));
    }

    @Test
    public void enabledByProperty() {
        assertFalse(disabledWith(Map.of("dynamia.app.open-persistence-in-view", "true")));
    }

    @Test
    public void setterOverrides() {
        var pattern = new OpenEntityManagerInViewPattern();
        pattern.setEnabled(false);
        assertTrue(pattern.isDisabled());
    }
}
