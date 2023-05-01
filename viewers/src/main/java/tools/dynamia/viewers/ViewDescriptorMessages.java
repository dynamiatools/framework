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
package tools.dynamia.viewers;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * The Class ViewDescriptorMessages.
 *
 * @author Mario A. Serrano Leones
 */
public class ViewDescriptorMessages {

    /**
     * The bundle.
     */
    private ResourceBundle bundle;

    /**
     * The view descriptor.
     */
    private final ViewDescriptor viewDescriptor;

    /**
     * The locale.
     */
    private final Locale locale;

    /**
     * Instantiates a new view descriptor messages.
     *
     * @param viewDescriptor the view descriptor
     */
    public ViewDescriptorMessages(ViewDescriptor viewDescriptor) {
        this(viewDescriptor, Locale.getDefault());

    }

    /**
     * Instantiates a new view descriptor messages.
     *
     * @param viewDescriptor the view descriptor
     * @param locale         the locale
     */
    public ViewDescriptorMessages(ViewDescriptor viewDescriptor, Locale locale) {
        this.viewDescriptor = viewDescriptor;
        this.locale = locale;
        initBundle();

    }

    /**
     * Inits the bundle.
     */
    private void initBundle() {
        if (viewDescriptor != null && viewDescriptor.getMessages() != null) {
            String messages = viewDescriptor.getMessages() + ".Messages";
            bundle = ResourceBundle.getBundle(messages, locale);
        }
    }

    /**
     * Gets the message.
     *
     * @param key the key
     * @return the message
     */
    public final String getMessage(String key) {
        return getMessage(key, null);
    }

    /**
     * Gets the message.
     *
     * @param key    the key
     * @param params the params
     * @return the message
     */
    public final String getMessage(String key, Object... params) {
        String message = bundle.getString(key);
        try {
            if (params != null && params.length > 0) {
                message = MessageFormat.format(message, params, locale);
            }
        } catch (MissingResourceException e) {
            message = key;
        }

        return message;
    }

}
