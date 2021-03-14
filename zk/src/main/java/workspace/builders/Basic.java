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
package workspace.builders;

import org.zkoss.zk.ui.Component;
import tools.dynamia.navigation.Page;
import tools.dynamia.zk.AbstractZKWorkspaceBuilder;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class Basic extends AbstractZKWorkspaceBuilder {

    @Override
    public void build(Page page) {
        update(page, null);
    }

    @Override
    public Component getPageContainer(Page page) {
        return container;
    }

    @Override
    public void clearPageContainer(Component pageContainer) {
        pageContainer.getChildren().clear();
    }

}
