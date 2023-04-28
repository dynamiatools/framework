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
package tools.dynamia.zk.ui;

import org.junit.Assert;
import org.zkoss.zul.ListModelList;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

public class ProviderPickerBoxTest {

    public static void init() {
        SimpleObjectContainer container = new SimpleObjectContainer();
        container.addObject(DefaultProvider.ID, new DefaultProvider());
        container.addObject(SpecialProvider.ID, new SpecialProvider());

        Containers.get().installObjectContainer(container);

    }

    public void shouldHas2Providers() {
        ProviderPickerBox box = new ProviderPickerBox();
        box.setClassName(MyProvider.class.getName());

        Assert.assertEquals(2, box.getModel().getSize());
    }

    public void shouldSelectProvider() {

        ProviderPickerBox box = new ProviderPickerBox();
        box.setClassName(MyProvider.class.getName());

        box.setSelected(DefaultProvider.ID);

        ListModelList model = (ListModelList) box.getModel();
        Assert.assertFalse(model.getSelection().isEmpty());
        model.getSelection().forEach(p -> Assert.assertEquals(DefaultProvider.class, p.getClass()));
    }

    static class DefaultProvider implements MyProvider {

        static final String ID = "DFMP";

        @Override
        public String getId() {
            return ID;
        }

        @Override
        public String getName() {
            return "Default";
        }
    }

    static class SpecialProvider implements MyProvider {

        static final String ID = "SPMP";

        @Override
        public String getId() {
            return ID;
        }

        @Override
        public String getName() {
            return "Special";
        }

    }

}
