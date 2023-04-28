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
 * @author Mario Serrano Leones
 */
public interface MessageDisplayer {

    void showMessage(String message);

    void showMessage(String message, MessageType type);

    void showMessage(String message, String title, MessageType type);

    default void showMessage(String message, String title, MessageType type, Object source) {
        showMessage(message, title, type);
    }

    void showQuestion(String message, String title, Callback onYesResponse);

    void showQuestion(String message, String title, Callback onYesResponse, Callback onNoResponse);

    <T> void showInput(String title, Class<T> valueClass, Consumer<T> onValue);

    <T> void showInput(String title, Class<T> valueClass, T defaultValue, Consumer<T> onValue);

    default void showException(String message, String title, MessageType type, Exception exception) {
        showMessage(message, title, type);
    }

    default void showException(String message, String title, Exception exception) {
        showMessage(message, title, MessageType.ERROR, exception);
    }

    default void showException(String message, Exception exception) {
        showMessage(message, null, MessageType.ERROR, exception);
    }
}
