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

/**
 * What {@link UIFiles#upload(UploadOptions, java.util.function.Consumer)} asks the user.
 *
 * @param title    dialog title, may be {@code null}
 * @param accept   accepted types as in an HTML {@code accept} attribute (".json", "image/*"), may be {@code null}
 * @param multiple whether several files can be chosen
 */
public record UploadOptions(String title, String accept, boolean multiple) {

    /** One file of any type. */
    public static UploadOptions single() {
        return new UploadOptions(null, null, false);
    }

    /** One file of the given type(s). */
    public static UploadOptions single(String accept) {
        return new UploadOptions(null, accept, false);
    }

    /** Several files of the given type(s). */
    public static UploadOptions multiple(String accept) {
        return new UploadOptions(null, accept, true);
    }
}
