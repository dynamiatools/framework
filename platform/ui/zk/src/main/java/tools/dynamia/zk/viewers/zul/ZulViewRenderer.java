
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

package tools.dynamia.zk.viewers.zul;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewRenderer;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ZulViewRenderer implements ViewRenderer<String> {

    @Override
    public View<String> render(ViewDescriptor descriptor, String value) {
        ZulView zul = new ZulView(value);
        zul.setViewDescriptor(descriptor);

        BeanUtils.setupBean(zul, descriptor.getParams());

        return zul;
    }

}
