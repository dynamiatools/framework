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

import java.util.function.BiConsumer;

/**
 * SPI behind {@link UIViews}. ZK opens a window with a {@code Viewer}; a headless run turns the form into a
 * {@code DIALOG} flow step the client renders from the view descriptor.
 */
public interface ViewsProvider {

    /**
     * Shows a form for {@code options.value()} and calls {@code onSubmit} with the edited bean when the user submits it.
     * It is never called if the user cancels.
     */
    <T> void showForm(FormOptions<T> options, BiConsumer<T, ViewDialog> onSubmit);

    /**
     * Shows {@code options.value()} read only. The user closes it; nothing is reported back. In a headless run the action
     * continues once the client acknowledged the view.
     */
    <T> void showView(ViewOptions<T> options);
}
