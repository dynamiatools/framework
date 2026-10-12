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
package tools.dynamia.web.navigation;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.test.util.ReflectionTestUtils;
import tools.dynamia.navigation.DynamicPageCache;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.navigation.ModuleProvider;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Verifies that component scanning registers {@link HttpSessionDynamicPageCache} and that the Spring container
 * injects it into {@link ModuleContainer}.
 */
class HttpSessionDynamicPageCacheWiringTest {

    @Test
    void isRegisteredAndInjectedIntoModuleContainer() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            var scanner = new ClassPathBeanDefinitionScanner(ctx);
            scanner.addExcludeFilter((reader, factory) ->
                    !reader.getClassMetadata().getClassName().equals(HttpSessionDynamicPageCache.class.getName()));
            scanner.scan("tools.dynamia.web.navigation");
            ctx.registerBean(ModuleContainer.class);
            java.util.function.Supplier<ModuleProvider> noModules = () -> () -> null;
            ctx.registerBean(ModuleProvider.class, noModules);
            ctx.refresh();

            var cache = ctx.getBean(DynamicPageCache.class);
            assertInstanceOf(HttpSessionDynamicPageCache.class, cache);
            assertSame(cache, ReflectionTestUtils.getField(ctx.getBean(ModuleContainer.class), "dynamicPageCache"));
        }
    }
}
