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
package tools.dynamia.ui;

import tools.dynamia.commons.Callback;
import tools.dynamia.integration.Containers;

import java.util.function.Consumer;

public class UIMessages {

    /**
     * Show a normal user message
     *
     * @param text
     */
    public static void showMessage(String text) {
        showMessage(text, MessageType.NORMAL);
    }

    /**
     * Show a user message with specific {@link MessageType}
     *
     * @param text
     * @param type
     */
    public static void showMessage(String text, MessageType type) {
        showMessage(text, null, type);
    }

    /**
     * Show a user message with custom title and {@link MessageType}
     *
     * @param text
     * @param title
     * @param type
     */
    public static void showMessage(String text, String title, MessageType type) {
        showMessage(text, title, type, null);
    }

    /**
     * Show Message with source object
     * @param text
     * @param title
     * @param type
     * @param source
     */
    public static void showMessage(String text, String title, MessageType type, Object source) {
        MessageDisplayer displayer = getDisplayer();
        displayer.showMessage(text, title, type, source);
    }

    private static MessageDisplayer getDisplayer() {
        return Containers.get().findObject(MessageDisplayer.class);
    }


    /**
     * Show question or confirmation dialog. When user click [Yes] the onYesResponse
     * callback is called
     *
     * @param text
     * @param onYesResponse
     */
    public static void showQuestion(String text, Callback onYesResponse) {
        showQuestion(text, (String) null, onYesResponse);
    }

    /**
     * Show question or confirmation dialog with custom title. When user click [Yes] the onYesResponse
     * callback is called.
     *
     * @param text
     * @param title
     * @param onYesResponse
     */
    public static void showQuestion(String text, String title, Callback onYesResponse) {
        getDisplayer().showQuestion(text, title, onYesResponse);
    }

    /**
     * Show question or confirmation dialog. When user click [Yes] the onYesResponse
     * callback is called and when [NO] onNoResponse callback is called.
     *
     * @param text
     * @param onYesResponse
     * @param onNoResponse
     */
    public static void showQuestion(String text, Callback onYesResponse, Callback onNoResponse) {
        showQuestion(text, null, onYesResponse, onNoResponse);
    }

    /**
     * Show question or confirmation dialog with custom title. When user click [Yes] the onYesResponse
     * callback is called and when [NO] onNoResponse callback is called.
     *
     * @param text
     * @param title
     * @param onYesResponse
     * @param onNoResponse
     */
    public static void showQuestion(String text, String title, Callback onYesResponse, Callback onNoResponse) {
        getDisplayer().showQuestion(text, title, onYesResponse, onNoResponse);
    }

    /**
     * Show basic input dialog with custom title. Value class is used to create a proper ui component. For example a {@link java.util.Date}
     * will show a Datebox o date picker component. Consumer is called when some value (null include) is selected.
     *
     * @param title
     * @param valueClass
     * @param onValue
     * @param <T>
     */
    public static <T> void showInput(String title, Class<T> valueClass, Consumer<T> onValue) {
        getDisplayer().showInput(title, valueClass, onValue);
    }

    /**
     * Show basic input dialog with custom title and default value. Value class is used to create a proper ui component. For example a {@link java.util.Date}
     * will show a Datebox o date picker component. Consumer is called when some value (null include) is selected.
     *
     * @param title
     * @param valueClass
     * @param defaultValue
     * @param onValue
     * @param <T>
     */
    public static <T> void showInput(String title, Class<T> valueClass, T defaultValue, Consumer<T> onValue) {
        getDisplayer().showInput(title, valueClass, defaultValue, onValue);
    }

    private UIMessages() {
    }

}
