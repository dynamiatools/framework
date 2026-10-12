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
package tools.dynamia.ui.testing;

import tools.dynamia.ui.MessageType;

import java.util.Map;

/**
 * One thing an action did towards the user, as the test platform saw it.
 *
 * @param type        what kind of interaction it was
 * @param title       title of the dialog, view or question, may be null
 * @param message     text of the question, input prompt or notification, or the error that made a form open again
 * @param messageType kind of message, for notifications and for forms that open again with an error
 * @param payload     the rest: form values, option labels, upload options, redirect target...
 */
public record UIInteraction(Type type, String title, String message, MessageType messageType, Map<String, Object> payload) {

    /** The kinds of interaction. */
    public enum Type {
        /** A yes/no question. */
        CONFIRM,
        /** A request for one value. */
        INPUT,
        /** A form the user fills in and submits. */
        DIALOG,
        /** A read only view. */
        VIEW,
        /** A selection among options. */
        CHOICE,
        /** A request for files. */
        UPLOAD,
        /** A message that needs no answer. */
        NOTIFY,
        /** A long task shown with progress. */
        PROGRESS,
        /** The user is sent somewhere else. */
        REDIRECT;

        /**
         * @return whether the action waits for the user to answer this kind of interaction
         */
        public boolean asksTheUser() {
            return this == CONFIRM || this == INPUT || this == DIALOG || this == CHOICE || this == UPLOAD;
        }
    }

    /**
     * @return what identifies the interaction for a human and for comparisons: the question text for confirmations and
     * inputs, the title for the rest
     */
    public String label() {
        return type == Type.CONFIRM || type == Type.INPUT || type == Type.NOTIFY ? message : title;
    }

    @Override
    public String toString() {
        return type + " '" + label() + "'";
    }
}
