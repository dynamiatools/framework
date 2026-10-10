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
package tools.dynamia.ui.contract;

import java.util.List;

/**
 * What a contract case observed.
 *
 * @param asked     what the user was asked, in order
 * @param effects   what the case recorded in its callbacks during the last run
 * @param notices   the messages shown without a question, as {@code "TYPE text"}
 * @param downloads the files given to the user
 * @param redirect  where the user was sent, or null
 * @param failure   the exception the action ended with, or null
 */
public record Outcome(List<Asked> asked, List<String> effects, List<String> notices, List<Download> downloads,
                      Redirect redirect, Throwable failure) {

    /**
     * One thing the user was asked.
     *
     * @param type    {@code CONFIRM}, {@code INPUT}, {@code DIALOG}, {@code VIEW}, {@code CHOICE} or {@code UPLOAD}
     * @param label   the question, or the title of the dialog
     * @param message why it was asked again (a validation error), or null
     */
    public record Asked(String type, String label, String message) {
        public static Asked of(String type, String label) {
            return new Asked(type, label, null);
        }
    }

    /**
     * A file given to the user.
     *
     * @param name        file name
     * @param contentType MIME type, may be null
     * @param content     the bytes
     */
    public record Download(String name, String contentType, byte[] content) {
        public String text() {
            return new String(content, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /**
     * @param url       where
     * @param newWindow whether in a new window
     */
    public record Redirect(String url, boolean newWindow) {
    }
}
