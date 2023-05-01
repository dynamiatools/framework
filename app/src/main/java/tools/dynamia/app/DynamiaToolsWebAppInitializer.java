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
package tools.dynamia.app;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.io.IOException;

public class DynamiaToolsWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	private final LoggingService logger = new SLF4JLoggingService(DynamiaToolsWebAppInitializer.class);

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		super.onStartup(servletContext);
		registerRequestContextListener(servletContext);
	}

	protected void registerRequestContextListener(ServletContext servletContext) {
		servletContext.addListener(RequestContextListener.class);

	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		Class<?>[] configClasses = getRootConfigClasses();
		if (!ObjectUtils.isEmpty(configClasses)) {
			AnnotationConfigWebApplicationContext rootAppContext = new AnnotationConfigWebApplicationContext();
			scanPackages(rootAppContext);
			rootAppContext.register(configClasses);
			return rootAppContext;
		} else {
			return null;
		}
	}



	private void scanPackages(AnnotationConfigWebApplicationContext rootAppContext) {
		try {
			ApplicationInfo appInfo = RootAppConfiguration.loadApplicationInfo();
			if (appInfo.getBasePackage() != null && !appInfo.getBasePackage().isEmpty()) {
				logger.info("Scanning package: "+appInfo.getBasePackage());
				rootAppContext.scan(appInfo.getBasePackage());
			}
		} catch (IOException e) {
			logger.error("Error scanning packages from ApplicationInfo", e);
		}
	}

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] { RootAppConfiguration.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] {  };
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/*" };
	}

}
