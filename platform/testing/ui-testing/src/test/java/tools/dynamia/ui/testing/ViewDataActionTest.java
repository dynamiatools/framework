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
package tools.dynamia.ui.testing;

import org.junit.jupiter.api.Test;
import tools.dynamia.commons.Callback;
import tools.dynamia.crud.actions.ViewDataAction;
import tools.dynamia.domain.services.CrudService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static tools.dynamia.ui.testing.UIInteraction.Type.VIEW;

/**
 * {@link ViewDataAction} shows the selected entity read only, or tells the user to select one, the same way everywhere.
 */
class ViewDataActionTest {

    public static class Product {
        public Long id;
        public String name;
    }

    private static CrudService service() {
        var service = mock(CrudService.class);
        doAnswer(invocation -> {
            ((Callback) invocation.getArgument(0)).doSomething();
            return null;
        }).when(service).executeWithinTransaction(any(Callback.class));
        return service;
    }

    @Test
    void showsTheSelectedEntityReadOnly() {
        var product = new Product();
        product.name = "Coffee";

        var result = ActionTester.of(new ViewDataAction())
                .crud(Product.class, service())
                .on(product)
                .runEverywhere();

        assertEquals(List.of(VIEW), result.types());
        assertEquals(Product.class.getName(), result.interactions().get(0).payload().get("beanClass"));
    }

    @Test
    void doesNothingWhenThereIsNoSelection() {
        var result = ActionTester.of(new ViewDataAction())
                .crud(Product.class, service())
                .runEverywhere();

        assertEquals(List.of(), result.types());
    }
}
