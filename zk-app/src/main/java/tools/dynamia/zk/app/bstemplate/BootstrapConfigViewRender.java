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
package tools.dynamia.zk.app.bstemplate;

import tools.dynamia.domain.query.Parameter;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.zk.crud.cfg.ConfigView;
import tools.dynamia.zk.crud.cfg.ConfigViewRender;

import java.util.List;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class BootstrapConfigViewRender extends ConfigViewRender {

    public BootstrapConfigViewRender() {
    }

    @Override
    protected ConfigView newConfigView() {
        ConfigView formView = new ConfigView();
        formView.setZclass("content");
        return formView;
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void delegateRender(ConfigView configView, ViewDescriptor descriptor, List<Parameter> value) {
        BootstrapFormViewRenderer delegate = new BootstrapFormViewRenderer<>();
        delegate.render(configView, descriptor, value);
    }

}
