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
package tools.dynamia.app.template;

import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public class ChainableUrlBasedViewResolver extends UrlBasedViewResolver {

    public ChainableUrlBasedViewResolver() {

        setViewClass(InternalResourceView.class);
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {

        if (isStatic(viewName)) {
            return new InternalResourceView(viewName);
        }



        String url = getPrefix() + viewName + getSuffix();
        InputStream stream = getServletContext().getResourceAsStream(url);

        if (stream == null) {
            if (url.startsWith("/zkau/")) {
                url = url.substring("/zkau".length());
            }
            stream = getClass().getResourceAsStream(url);
        }

        if (stream == null) {

            return new NonExistentView();
        }

        return super.buildView(viewName);
    }

    private boolean isStatic(String viewName) {
        return viewName != null && viewName.startsWith("static/");
    }

    private static class NonExistentView extends AbstractUrlBasedView {

        @Override
        protected boolean isUrlRequired() {

            return false;
        }

        @Override
        public boolean checkResource(Locale locale) {

            return false;
        }

        @Override
        protected void renderMergedOutputModel(Map<String, Object> model,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {

            // Purposely empty, it should never get called
        }
    }
}
