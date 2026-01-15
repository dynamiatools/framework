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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.dynamia.domain.DefaultEntityReferenceRepository;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.integration.ms.MessageChannel;
import tools.dynamia.integration.ms.MessageService;
import tools.dynamia.modules.email.domain.EmailTemplate;

/**
 * Configuration class for email-related beans.
 */
@Configuration
public class EmailConfig {

    public static final String EMAIL_CHANNEL = "emailChannel";

    @Autowired
    private MessageService messageService;

    @Bean
    public MessageChannel emailChannel() {
        return messageService.createChannel(EMAIL_CHANNEL, null);
    }

    @Bean
    public EntityReferenceRepository<Long> emailTemplateRefRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(EmailTemplate.class, "name");
        repo.setCacheable(true);
        return repo;
    }

}
