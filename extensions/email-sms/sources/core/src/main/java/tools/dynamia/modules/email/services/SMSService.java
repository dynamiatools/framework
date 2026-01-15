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

package tools.dynamia.modules.email.services;

import tools.dynamia.modules.email.SMSMessage;

/**
 * Simple SMS sender service.
 * <p>
 * Defines the contract to dispatch SMS messages using the configured provider. Implementations
 * are responsible for formatting, transport selection, and returning a provider-specific response
 * string that may include tracking identifiers or delivery status notes.
 * </p>
 */
public interface SMSService {

    /**
     * Sends an SMS message.
     * <p>
     * Implementations should validate destination numbers and message length according to provider limits
     * (e.g., GSM-7 vs. Unicode, segmentation). The returned value is the raw response from the underlying
     * SMS provider and can be used for logging or troubleshooting.
     * </p>
     *
     * @param message The {@link SMSMessage} to be sent, containing destination number, text content, and optional metadata.
     *                Must not be {@code null}.
     * @return The response string from the SMS service/provider. The format depends on the implementation and provider;
     * may include message ID or delivery status information.
     */
    String send(SMSMessage message);
}
