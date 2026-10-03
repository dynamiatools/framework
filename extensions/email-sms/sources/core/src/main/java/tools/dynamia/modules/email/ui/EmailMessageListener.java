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

package tools.dynamia.modules.email.ui;

import org.springframework.beans.factory.annotation.Autowired;
import tools.dynamia.integration.ms.*;
import tools.dynamia.integration.sterotypes.Listener;
import tools.dynamia.modules.email.EmailConfig;
import tools.dynamia.modules.email.EmailMessage;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.services.EmailService;

import java.util.Map;

/**
 * The EmailMessageListener class is a message listener that listens to messages
 * from the email channel and sends email messages accordingly.
 */
@Listener
@MessageChannelExchange(channel = EmailConfig.EMAIL_CHANNEL, broadcastReceive = false)
public class EmailMessageListener implements MessageListener<Message> {

	@Autowired
	private EmailService service;

	@Override
	public void onMessage(MessageEvent<Message> evt) {
		Message message = evt.getMessage();

		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setTo((String) message.getHeader("to"));

		if (emailMessage.getTo() != null && !emailMessage.getTo().isEmpty()) {
			emailMessage.setSubject((String) message.getHeader("subject"));
			emailMessage.setContent((String) message.getHeader("content"));
			emailMessage.setTemplate(loadTemplate(message));

			if (message instanceof MapMessage) {
				emailMessage.setTemplateModel((Map<String, Object>) message.getContent());
			}
			service.send(emailMessage);
		}
	}

	private EmailTemplate loadTemplate(Message message) {
		String name = (String) message.getHeader("template");
		if (name != null && !name.isEmpty()) {
			EmailTemplate template = service.getTemplateByName(name);
			return template;
		}
		return null;
	}

}
