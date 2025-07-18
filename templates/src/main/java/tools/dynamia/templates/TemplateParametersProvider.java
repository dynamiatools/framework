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
package tools.dynamia.templates;

import java.util.Map;

/**
 * The Interface TemplateParametersProvider. Provides parameters for template processing.
 * This interface defines the contract for extracting template parameters from target objects,
 * enabling dynamic parameter injection into template evaluation processes. Parameter providers
 * are commonly used to transform domain objects into template-friendly data structures,
 * apply business logic during parameter extraction, and provide computed values or formatted
 * data for template rendering in reports, emails, and dynamic content generation.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class UserParametersProvider implements TemplateParametersProvider&lt;User&gt; {
 *     
 *     public Map&lt;String, Object&gt; getParameters(User user) {
 *         Map&lt;String, Object&gt; params = new HashMap&lt;&gt;();
 *         params.put("fullName", user.getFirstName() + " " + user.getLastName());
 *         params.put("displayAge", user.getAge() + " years old");
 *         params.put("isActive", user.getStatus() == UserStatus.ACTIVE);
 *         params.put("memberSince", formatDate(user.getCreatedDate()));
 *         return params;
 *     }
 * }
 * 
 * // Usage
 * TemplateParametersProvider&lt;User&gt; provider = new UserParametersProvider();
 * Map&lt;String, Object&gt; params = provider.getParameters(user);
 * String content = templateEngine.evaluate(template, params);
 * </code>
 *
 * @param <T> the type of target object
 * @author Mario A. Serrano Leones
 */
public interface TemplateParametersProvider<T> {

    /**
     * Gets the parameters for template processing from the target object.
     *
     * @param target the target object
     * @return the map of parameters
     */
    Map<String, Object> getParameters(T target);

}
