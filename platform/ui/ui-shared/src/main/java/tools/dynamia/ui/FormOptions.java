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
 * What {@link UIViews#showForm(FormOptions, java.util.function.BiConsumer)} shows.
 *
 * @param title       dialog title
 * @param beanClass   class of the bean the form edits; its view descriptor of type {@code viewName} describes the fields
 * @param value       the bean, already filled with what must not be edited (an owner, defaults); the form edits it
 * @param viewName    view descriptor type, usually {@code "form"}
 * @param submitLabel text of the button that submits, {@code null} for the default
 * @param width       dialog width ("60%", "500px"), {@code null} for the default
 * @param <T>         bean type
 */
public record FormOptions<T>(String title, Class<T> beanClass, T value, String viewName, String submitLabel, String width) {

    /** A {@code "form"} view of {@code beanClass} editing {@code value}, with default button and width. */
    public static <T> FormOptions<T> of(String title, Class<T> beanClass, T value) {
        return new FormOptions<>(title, beanClass, value, "form", null, null);
    }

    public FormOptions<T> submitLabel(String label) {
        return new FormOptions<>(title, beanClass, value, viewName, label, width);
    }

    public FormOptions<T> width(String width) {
        return new FormOptions<>(title, beanClass, value, viewName, submitLabel, width);
    }

    public FormOptions<T> viewName(String viewName) {
        return new FormOptions<>(title, beanClass, value, viewName, submitLabel, width);
    }
}
