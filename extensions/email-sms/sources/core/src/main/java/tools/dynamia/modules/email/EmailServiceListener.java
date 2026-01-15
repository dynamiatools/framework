
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

/**
 * Implement this class if you need listener email message sending process. You also need annotated
 * with @{@link tools.dynamia.integration.sterotypes.Listener}
 *
 * @author Mario Serrano Leones
 */
public interface EmailServiceListener {

    /**
     * Executed after email templates and data is process
     *
     * @param message
     */
    void onMailProcessing(EmailMessage message);

    /**
     * Executed just before email is send
     *
     * @param message
     */
    void onMailSending(EmailMessage message);

    /**
     * Executed after email is sended succefull
     *
     * @param message
     */
    void onMailSended(EmailMessage message);

    /**
     * Executed if something explode sending the email message
     *
     * @param message
     * @param cause
     */
    void onMailSendFail(EmailMessage message, Throwable cause);

}
