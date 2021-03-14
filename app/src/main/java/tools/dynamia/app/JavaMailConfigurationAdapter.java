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
package tools.dynamia.app;

import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class JavaMailConfigurationAdapter {

    protected String host;
    protected int port;
    protected String protocol = "stmp";
    protected String username;
    protected String password;
    protected boolean tlsEnabled = false;
    protected boolean auth = true;
    protected String from;
    protected String personal;

    @Bean
    public MailSender mailSender() {
        JavaMailSenderImpl ms = new JavaMailSenderImpl();
        ms.setJavaMailProperties(javaMailProperties());
        ms.setPort(port);
        ms.setHost(host);
        ms.setPassword(password);
        ms.setUsername(username);
        ms.setProtocol(protocol);

        return ms;
    }

    protected Properties javaMailProperties() {
        Properties p = new Properties();
        p.setProperty("mail.smtp.auth", String.valueOf(auth));
        p.setProperty("mail.smtp.port", String.valueOf(port));
        p.setProperty("mail.smtp.starttls.enable", String.valueOf(tlsEnabled));
        if (from != null) {
            p.setProperty("mail.smtp.from", from);
            p.setProperty("mail.from", from);
        }

        p.setProperty("mail.smtp.host", host);
        if (personal != null) {
            p.setProperty("mail.personal", personal);
        }

        return p;
    }

}
