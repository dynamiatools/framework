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
package tools.dynamia.zk.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.view.InternalResourceView;
import org.zkoss.zk.ui.Executions;

import java.util.Map;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ZKView extends InternalResourceView {

    private final String url;

    public ZKView(String url) {
        this.url = url;
    }

    @Override
    public String getContentType() {
        return "text/html;charset=UTF-8";
    }

    @Override
    public void render(java.util.Map<String, ?> model,
                       HttpServletRequest request,
                       HttpServletResponse response) throws Exception {
        // Pasar atributos del modelo al request
        if (model != null) {
            model.forEach(request::setAttribute);
        }
        // Renderizar el .zul usando ZK
        Executions.createComponents(url, null, model);
    }

}
