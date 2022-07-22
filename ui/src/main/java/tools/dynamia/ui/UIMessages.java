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
import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.commons.Messages;
import tools.dynamia.integration.Containers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class UIMessages {

    private UIMessages() {
    }

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
     *
     * @param text
     * @param title
     * @param type
     * @param source
     */
    public static void showMessage(String text, String title, MessageType type, Object source) {
        MessageDisplayer displayer = getDisplayer();
        displayer.showMessage(text, title, type, source);
    }


    /**
     * Show a localized message using default {@link LocalizedMessagesProvider}
     *
     * @param template
     * @param messageType
     * @param vars
     */
    public static void showLocalizedMessage(String template, MessageType messageType, Object... vars) {
        String localizedMessage = getLocalizedMessage(template, "* UI Messages");
        String message = String.format(localizedMessage, vars);
        showMessage(message, messageType);
    }

    public static void showLocalizedMessage(String template, Object... vars) {
        showLocalizedMessage(template, MessageType.NORMAL, vars);
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
     * Show Questing with localized message
     *
     * @param template
     * @param vars
     * @param onYesResponse
     */
    public static void showLocalizedQuestion(String template, Object[] vars, Callback onYesResponse) {
        String localizedMessage = getLocalizedMessage(template, "* UI Messages");
        String message = String.format(localizedMessage, vars);
        showQuestion(message, onYesResponse);
    }

    /**
     * @param template
     * @param vars
     * @param onYesResponse
     */
    public static void showLocalizedQuestion(String template, List<Object> vars, Callback onYesResponse) {
        showLocalizedQuestion(template, vars.toArray(), onYesResponse);

    }

    /**
     * @param template
     * @param onYesResponse
     */
    public static void showLocalizedQuestion(String template, Callback onYesResponse) {
        showLocalizedQuestion(template, new Object[0], onYesResponse);

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

    /**
     * Return a localized message using the first non-null value from {@link LocalizedMessagesProvider} providers
     *
     * @param key
     * @param classfier
     * @param locale
     * @param defaultValue
     * @return
     */
    public static String getLocalizedMessage(String key, String classfier, Locale locale, String defaultValue) {
        var providers = new ArrayList<>(Containers.get().findObjects(LocalizedMessagesProvider.class));
        providers.sort(Comparator.comparingInt(LocalizedMessagesProvider::getPriority));

        if (!providers.isEmpty()) {
            String message = null;
            for (var provider : providers) {
                message = provider.getMessage(key, classfier, locale, defaultValue);
                if (message != null) {
                    return message;
                }
            }
        }
        return defaultValue;
    }

    /**
     * Localize message using key and default locale
     *
     * @param key
     * @return
     */
    public static String getLocalizedMessage(String key) {
        return getLocalizedMessage(key, null, Messages.getDefaultLocale(), key);
    }


    /**
     * Localize message using key, classifer and default locale
     *
     * @param key
     * @param classier
     * @return
     */
    public static String getLocalizedMessage(String key, String classier) {
        return getLocalizedMessage(key, classier, Messages.getDefaultLocale(), key);
    }

    /**
     * Localize message using a key with custom classifer or group and default locale
     *
     * @param key
     * @param classifier
     * @param defaultValue
     * @return
     */
    public static String getLocalizedMessage(String key, String classifier, String defaultValue) {
        return getLocalizedMessage(key, classifier, Messages.getDefaultLocale(), defaultValue);
    }

}
