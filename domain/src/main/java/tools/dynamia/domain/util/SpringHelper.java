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
package tools.dynamia.domain.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * The Class SpringHelper.
 *
 * @author Ing. Mario Serrano Leones
 */
public class SpringHelper {

    /**
     * The app context.
     */
    private final ApplicationContext appContext;

    /**
     * The instance.
     */
    private static SpringHelper instance;

    /**
     * Instantiates a new spring helper.
     */
    public SpringHelper() {
        /**
         * The contexts.
         */
        String[] contexts = {"applicationContext.xml", "dataAccessLayer.xml"};
        appContext = new ClassPathXmlApplicationContext(contexts);
    }

    /**
     * Gets the bean.
     *
     * @param name the name
     * @return the bean
     */
    public Object getBean(String name) {
        return appContext.getBean(name);
    }

    /**
     * Gets the.
     *
     * @return the spring helper
     */
    public static SpringHelper get() {
        if (instance == null) {
            instance = new SpringHelper();
        }
        return instance;

    }
}
