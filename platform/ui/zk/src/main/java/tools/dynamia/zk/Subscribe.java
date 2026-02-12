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
package tools.dynamia.zk;

import org.zkoss.bind.sys.BinderCtrl;
import org.zkoss.zk.ui.event.EventQueues;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used in ZK ViewModels to automatically subscribe methods as listeners
 * to EventQueues. When an event is published to the specified EventQueue, the annotated
 * method will be invoked automatically.
 *
 * <p>This annotation enables event-driven communication between different ViewModels
 * or components in a ZK application without tight coupling.</p>
 *
 * <p>The EventQueue can be created automatically if it doesn't exist, and supports
 * different scopes (DESKTOP, SESSION, APPLICATION) to control the visibility and
 * lifecycle of events.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * public class MyViewModel {
 *
 *     //init
 *     @Init
 *     public void init() {
 *        ZKUtils.subscribeEventQueues(this);
 *     }
 *
 *     // Subscribe to default queue with specific event name
 *     @Subscribe(eventName = "onDataUpdate")
 *     @NotifyChange("items")
 *     public void handleDataUpdate(Event event) {
 *         // This method is called when "onDataUpdate" event is published
 *         System.out.println("Data updated: " + event.getData());
 *         // Refresh your data...
 *     }
 *
 *     // Subscribe to custom queue with desktop scope
 *     @Subscribe(value = "myQueue", eventName = "onRefresh", scope = EventQueues.DESKTOP)
 *     @NotifyChange("*")
 *     public void handleRefresh() {
 *         // Handle refresh event from custom queue
 *     }
 *
 *     // Subscribe with async processing
 *     @Subscribe(value = "asyncQueue", async = true)
 *     public void handleAsyncEvent(Event event) {
 *         // Process event asynchronously
 *     }
 *
 *     // Subscribe to session-scoped queue
 *     @Subscribe(value = "sessionQueue", scope = EventQueues.SESSION, eventName = "onUserAction")
 *     public void handleUserAction(Map<String, Object> data) {
 *         // Handle user action from session queue
 *     }
 *
 *     // Trigger command after event
 *     @Subscribe(eventName = "onEntitySaved", command = {"refresh", "showMessage"})
 *     public void handleEntitySaved() {
 *         // After processing, commands "refresh" and "showMessage" will be triggered
 *     }
 * }
 *
 * // Publishing events to the queue:
 * EventQueue queue = EventQueues.lookup("myQueue", EventQueues.DESKTOP, true);
 * queue.publish(new Event("onRefresh", null, data));
 * }</pre>
 *
 * @see org.zkoss.zk.ui.event.EventQueues
 * @see org.zkoss.zk.ui.event.EventQueue
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {

    /**
     * Specifies the name of the EventQueue to subscribe to.
     * If not specified, uses the default queue name from ZK Binder.
     *
     * @return the EventQueue name
     */
    String value() default BinderCtrl.DEFAULT_QUEUE_NAME;

    /**
     * Specifies the target event name to filter events.
     * When specified, only events with this name will trigger the annotated method.
     * If empty, all events from the queue will be processed.
     *
     * <p>Example: {@code @Subscribe(eventName = "onDataUpdate")}</p>
     *
     * @return the event name to filter, or empty string to accept all events
     */
    String eventName() default "";

    /**
     * Defines the scope of the EventQueue.
     * Controls the visibility and lifecycle of the event queue.
     *
     * <p>Available scopes:</p>
     * <ul>
     *   <li>{@link EventQueues#DESKTOP} - Events are visible only within the same desktop (default)</li>
     *   <li>{@link EventQueues#SESSION} - Events are visible across all desktops in the same session</li>
     *   <li>{@link EventQueues#APPLICATION} - Events are visible application-wide</li>
     * </ul>
     *
     * <p>Example: {@code @Subscribe(value = "myQueue", scope = EventQueues.SESSION)}</p>
     *
     * @return the EventQueue scope
     */
    String scope() default EventQueues.DESKTOP;

    /**
     * Determines whether the EventQueue should be created automatically if it doesn't exist.
     * When {@code true}, the queue will be created on first use.
     * When {@code false}, the queue must already exist or the subscription will fail.
     *
     * @return {@code true} to auto-create the queue, {@code false} otherwise
     */
    boolean autocreate() default true;

    /**
     * Specifies whether the event listener should be invoked asynchronously.
     * When {@code true}, the method will be executed in a separate thread,
     * allowing the event publisher to continue without waiting.
     *
     * <p><strong>Note:</strong> When using async mode, be careful with UI updates
     * as they may require proper synchronization with the ZK execution thread.</p>
     *
     * @return {@code true} for async invocation, {@code false} for synchronous (default)
     */
    boolean async() default false;

    /**
     * Specifies one or more ZK commands to execute after the event handler completes.
     * These commands will be triggered automatically in the ViewModel.
     *
     * <p>Example:</p>
     * <pre>{@code
     * @Subscribe(eventName = "onSave", command = {"refresh", "closeDialog"})
     * public void handleSave() {
     *     // After this method completes, "refresh" and "closeDialog" commands will execute
     * }
     * }</pre>
     *
     * @return array of command names to execute
     */
    String[] command() default {};

}
