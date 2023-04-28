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
package tools.dynamia.viewers.util;

import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorInterceptor;
import tools.dynamia.viewers.ViewDescriptorTarget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * The Class ViewDescriptorInterceptorUtils.
 *
 * @author Mario A. Serrano Leones
 */
public class ViewDescriptorInterceptorUtils {

    /**
     * Fire interceptors for.
     *
     * @param viewDescriptor the view descriptor
     */
    public static void fireInterceptorsFor(ViewDescriptor viewDescriptor) {
        for (ViewDescriptorInterceptor vdi : filter(viewDescriptor)) {
            vdi.intercepted(viewDescriptor);
        }
    }

    /**
     * Filter.
     *
     * @param vd the vd
     * @return the list
     */
    private static List<ViewDescriptorInterceptor> filter(ViewDescriptor vd) {
        List<ViewDescriptorInterceptor> f = new ArrayList<>();
        Collection<ViewDescriptorInterceptor> interceptors = Containers.get().findObjects(ViewDescriptorInterceptor.class);
        for (ViewDescriptorInterceptor vdi : interceptors) {
            if (vdi.getTarget() == ViewDescriptorTarget.ALL) {
                f.add(vdi);
            } else {
                ViewDescriptorTarget target = vdi.getTarget();
                if (vd.getViewTypeName().equals(target.getType())
                        || vd.getId().equals(target.getId())
                        || vd.getBeanClass().equals(target.getBeanClass())) {
                    f.add(vdi);
                }
            }
        }

        return f;
    }

    private ViewDescriptorInterceptorUtils() {
    }

}
