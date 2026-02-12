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
package tools.dynamia.integration.ms;

/**
 * Interface for handling exceptions that occur during message processing in the messaging system.
 * <p>
 * Implementations of this interface are responsible for managing error scenarios when a {@link MessageListener}
 * fails to process a message. This allows for centralized exception handling, logging, retry logic, or
 * dead-letter queue management.
 * </p>
 *
 * <p>
 * <b>Key responsibilities:</b>
 * <ul>
 *   <li>Handle and log message processing exceptions</li>
 *   <li>Implement retry strategies for failed messages</li>
 *   <li>Route failed messages to error queues or dead-letter channels</li>
 *   <li>Send notifications or alerts on critical failures</li>
 *   <li>Perform cleanup or rollback operations</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class CustomMessageExceptionHandler implements MessageExceptionHandler<OrderMessage> {
 *
 *     @Override
 *     public void onMessageException(MessageEvent<OrderMessage> evt, MessageException exception) {
 *         logger.error("Failed to process order message: {}", evt.getData(), exception);
 *
 *         // Implement retry logic
 *         if (evt.getRetryCount() < 3) {
 *             messageService.retry(evt.getData());
 *         } else {
 *             // Move to dead-letter queue
 *             deadLetterService.send(evt.getData());
 *         }
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @param <T> the type of message being handled, must extend {@link Message}
 * @author Mario A. Serrano Leones
 * @see MessageListener
 * @see MessageEvent
 * @see MessageException
 */
public interface MessageExceptionHandler<T extends Message> {

    /**
     * Invoked when an exception occurs during message processing.
     * <p>
     * This method is called by the messaging system when a {@link MessageListener} throws an exception
     * while processing a message. Implementations should handle the error appropriately, such as logging,
     * retrying, or routing to a dead-letter queue.
     * </p>
     *
     * @param evt the message event that was being processed when the exception occurred
     * @param exception the exception that was thrown during message processing
     */
    void onMessageException(MessageEvent<T> evt, MessageException exception);

}
