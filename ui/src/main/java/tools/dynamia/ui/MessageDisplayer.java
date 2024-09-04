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
package tools.dynamia.ui;

import tools.dynamia.commons.Callback;

import java.util.function.Consumer;

/**
 * The MessageDisplayer interface defines a contract for displaying messages
 * and prompting questions in a user interface.
 *
 * @author Mario Serrano Leones
 */
public interface MessageDisplayer {

    /**
     * Show a message
     *
     * @param message the message
     */
    void showMessage(String message);

    /**
     * Show a message with a specific type
     *
     * @param message the message
     * @param type    the message type
     */
    void showMessage(String message, MessageType type);

    /**
     * Show a message with a specific type and title
     *
     * @param message the message
     * @param title   the message title
     * @param type    the message type
     */
    void showMessage(String message, String title, MessageType type);

    /**
     * Show a message with a specific type, title, and source object
     *
     * @param message the message
     * @param title   the message title
     * @param type    the message type
     * @param source  the source object
     */
    default void showMessage(String message, String title, MessageType type, Object source) {
        showMessage(message, title, type);
    }

    /**
     * Show a question with yes/no response
     *
     * @param message       the question message
     * @param title         the question title
     * @param onYesResponse the callback to execute if the user responds yes
     */
    void showQuestion(String message, String title, Callback onYesResponse);

    /**
     * Show a question with yes/no response
     *
     * @param message       the question message
     * @param title         the question title
     * @param onYesResponse the callback to execute if the user responds yes
     * @param onNoResponse  the callback to execute if the user responds no
     */
    void showQuestion(String message, String title, Callback onYesResponse, Callback onNoResponse);

    /**
     * Show an input dialog
     *
     * @param title      the input title
     * @param valueClass the input value class
     * @param onValue    the callback to execute with the input value
     * @param <T>        the input value type
     */
    <T> void showInput(String title, Class<T> valueClass, Consumer<T> onValue);

    /**
     * Show an input dialog with a default value
     *
     * @param title        the input title
     * @param valueClass   the input value class
     * @param defaultValue the default value
     * @param onValue      the callback to execute with the input value
     * @param <T>          the input value type
     */
    <T> void showInput(String title, Class<T> valueClass, T defaultValue, Consumer<T> onValue);

    /**
     * Show an exception message
     *
     * @param message   the exception message
     * @param title     the exception title
     * @param type      the message type
     * @param exception the exception
     */
    default void showException(String message, String title, MessageType type, Exception exception) {
        showMessage(message, title, type);
    }

    /**
     * Show an exception message
     *
     * @param message   the exception message
     * @param title     the exception title
     * @param exception the exception
     */
    default void showException(String message, String title, Exception exception) {
        showMessage(message, title, MessageType.ERROR, exception);
    }

    /**
     * Show an exception message
     *
     * @param message   the exception message
     * @param exception the exception
     */
    default void showException(String message, Exception exception) {
        showMessage(message, null, MessageType.ERROR, exception);
    }

    /**
     * Show a message dialog with a specific type, title, and message type
     *
     * @param message     the message
     * @param title       the title
     * @param messageType the message type
     */
    void showMessageDialog(String message, String title, MessageType messageType);
}
