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
 * What {@link UIViews#showView(ViewOptions)} shows: a view of a bean, read only.
 *
 * @param title     dialog title
 * @param beanClass class of the bean; its view descriptor of type {@code viewName} describes the fields
 * @param value     the bean to show
 * @param viewName  view descriptor type, usually {@code "form"}
 * @param width     dialog width ("60%", "500px"), {@code null} to let the front end decide from the view
 * @param height    dialog height, {@code null} to let the front end decide
 * @param <T>       bean type
 */
public record ViewOptions<T>(String title, Class<T> beanClass, T value, String viewName, String width, String height) {

    /** A {@code "form"} view of {@code value} with the default size. */
    public static <T> ViewOptions<T> of(String title, Class<T> beanClass, T value) {
        return new ViewOptions<>(title, beanClass, value, "form", null, null);
    }

    public ViewOptions<T> size(String width, String height) {
        return new ViewOptions<>(title, beanClass, value, viewName, width, height);
    }
}
