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
 * Listener interface for the SMS message sending lifecycle.
 * <p>
 * Implement this interface to receive callbacks during the SMS dispatch process. To enable discovery
 * and registration by the integration framework, annotate your implementation class with
 * {@link tools.dynamia.integration.sterotypes.Listener}.
 * </p>
 * <p>
 * Typical implementations may log audit trails, collect metrics, or alter message metadata before
 * sending. Note: method names reflect existing API and should not be changed for compatibility.
 * </p>
 *
 * @author Mario Serrano Leones
 */
public interface SMSServiceListener {

    /**
     * Callback invoked right before an {@link SMSMessage} is sent.
     * <p>
     * Use this hook to validate, enrich, or log the message prior to delivery. Avoid long-running
     * operations to prevent delaying the sending process.
     * </p>
     *
     * @param message The SMS message about to be sent. Never null.
     */
    void onMessageSending(SMSMessage message);

    /**
     * Callback invoked immediately after an {@link SMSMessage} has been sent.
     * <p>
     * Despite the name "Sended" kept for backward compatibility, this method indicates that the
     * sending operation was executed. Implementations may record provider responses or update
     * delivery tracking. This does not necessarily guarantee final delivery to the recipient.
     * </p>
     *
     * @param message The SMS message that was processed by the sender. Never null.
     */
    void onMessageSended(SMSMessage message);


}
