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
package tools.dynamia.zk;

import jakarta.servlet.http.HttpServletRequest;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.util.ExecutionCleanup;
import org.zkoss.zk.ui.util.ExecutionInit;
import tools.dynamia.domain.OpenPersistenceInViewProvider;
import tools.dynamia.integration.Containers;

import java.util.List;

/**
 * @author Mario A. Serrano Leones
 */
public class ZKOpenPersistenceInViewListener implements ExecutionInit, ExecutionCleanup {


    private final OpenPersistenceInViewProvider provider;


    public ZKOpenPersistenceInViewListener() {
        provider = Containers.get().findObject(OpenPersistenceInViewProvider.class);
    }


    public ZKOpenPersistenceInViewListener(OpenPersistenceInViewProvider provider) {
        this.provider = provider;
    }

    @Override
    public void init(Execution exec, Execution parent) {
        if (provider != null && !provider.isDisabled() && parent == null) {
            boolean participate = provider.beforeView();

            HttpServletRequest request = (HttpServletRequest) exec.getNativeRequest();
            request.setAttribute("EMF.PARTICIPATE", participate);
        }
    }

    @Override
    public void cleanup(Execution exec, Execution parent, List errs) {
        if (provider != null && !provider.isDisabled() && parent == null) {
            HttpServletRequest request = (HttpServletRequest) exec.getNativeRequest();
            Object attr = request.getAttribute("EMF.PARTICIPATE");
            boolean participate = attr == Boolean.TRUE;

            provider.afterView(participate);
        }
    }
}
