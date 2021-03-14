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

package tools.dynamia.commons;

import java.util.HashMap;
import java.util.Locale;


/**
 * Helper to use class messages like a map
 */
public class BeanMessagesMap extends HashMap<String, String> {

    private Class messageClass;
    private Locale locale;
    private BeanMessages messages;


    public BeanMessagesMap(Class messageClass, Locale locale) {
        this.messageClass = messageClass;
        this.locale = locale;
        this.messages = BeanMessages.get(messageClass, locale);

    }

    public BeanMessagesMap(Object target, Locale locale) {
        this(target.getClass(), locale);

    }

    public BeanMessagesMap(Class messageClass) {
        this(messageClass, Messages.getDefaultLocale());
    }

    public BeanMessagesMap(Object target) {
        this(target, Messages.getDefaultLocale());
    }

    @Override
    public String put(String key, String value) {
        throw new IllegalStateException("Cannot use this method.");
    }

    @Override
    public String get(Object key) {
        return messages.getMessage(key.toString());
    }
}
