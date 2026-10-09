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
import java.util.function.Consumer;

/**
 * Facade for showing views to the user, whatever front end it is on. The action describes <em>what</em> to show (a view
 * descriptor of a bean class and the bean), never a window or a component; the {@link ViewsProvider} of the
 * environment decides how (see {@link UIFacades}).
 *
 * <pre>{@code
 * var payment = new AccountPayment();
 * payment.setAccount(account);
 * UIViews.showForm(FormOptions.of(account.toString(), AccountPayment.class, payment).submitLabel("Create"),
 *         (p, dialog) -> UIMessages.showQuestion("Create payment?", () -> {
 *             crudService().save(p);
 *             dialog.close();
 *         }));
 * }</pre>
 * <p>
 * Headless limits: the bean class must be known to the client (an entity exposed by the REST metadata), because the
 * client builds the form from its view descriptor; and the submit handler runs when the action is executed again with
 * the submitted values, so what comes before {@code showForm} must be repeatable.
 */
public final class UIViews {

    private UIViews() {
    }

    /**
     * Shows a form and calls {@code onSubmit} with the edited bean and the dialog.
     */
    public static <T> void showForm(FormOptions<T> options, BiConsumer<T, ViewDialog> onSubmit) {
        UIFacades.resolve(ViewsProvider.class).showForm(options, onSubmit);
    }

    /**
     * Shows a form that closes by itself when the user submits it.
     */
    public static <T> void showForm(FormOptions<T> options, Consumer<T> onSubmit) {
        showForm(options, (bean, dialog) -> {
            onSubmit.accept(bean);
            dialog.close();
        });
    }
}
