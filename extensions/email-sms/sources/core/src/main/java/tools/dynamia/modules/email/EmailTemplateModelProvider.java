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

package tools.dynamia.modules.email;

import java.util.Map;

/**
 * Represents a provider for email template models.
 */
public interface EmailTemplateModelProvider {

	/**
	 *
	 * Retrieves the source of the email template model provider.
	 *
	 * @return the source of the email template model provider
	 */
	String getSource();

	/**
	 * Retrieves the model for an email message.
	 *
	 * @param message the email message for which to retrieve the model
	 * @return the model for the email message
	 */
	Map<String, Object> getModel(EmailMessage message);

}
