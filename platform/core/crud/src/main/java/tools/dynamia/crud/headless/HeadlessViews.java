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
package tools.dynamia.crud.headless;

import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.replay.ReplayRetry;
import tools.dynamia.actions.replay.ReplaySession;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.ui.FormOptions;
import tools.dynamia.ui.ViewDialog;
import tools.dynamia.ui.ViewOptions;
import tools.dynamia.ui.ViewsProvider;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * {@link ViewsProvider} of a headless run. A form becomes a {@code DIALOG} step: the client builds it from the view
 * descriptor of the bean class and answers with the submitted values (or {@code null} if the user cancels); the values
 * are applied to the bean and the submit handler runs with it. The form stays open (the same step is asked again, with the
 * values the user sent) when the handler throws {@link ValidationError}, which also carries the message and the field, or
 * returns without calling {@link ViewDialog#close()}; only {@code close()} lets the action go on.
 * <p>
 * How a bean is turned into values and values are applied to it is the same as the headless save of a CRUD (see
 * {@link tools.dynamia.crud.actions.remote.SaveSupport}), so the form shows and accepts the same fields.
 */
public final class HeadlessViews implements ViewsProvider {

    private final ReplaySession session;
    private final BiFunction<Object, Class<?>, Object> toValues;
    private final ValuesApplier applier;

    /** How submitted values reach the bean. */
    @FunctionalInterface
    public interface ValuesApplier {
        void apply(Object bean, Class<?> beanClass, Map<String, Object> values);
    }

    public HeadlessViews(ReplaySession session) {
        this(session, HeadlessViews::entityToValues, HeadlessViews::applyToEntity);
    }

    HeadlessViews(ReplaySession session, BiFunction<Object, Class<?>, Object> toValues, ValuesApplier applier) {
        this.session = session;
        this.toValues = toValues;
        this.applier = applier;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void showForm(FormOptions<T> options, BiConsumer<T, ViewDialog> onSubmit) {
        var step = ActionFlowStep.dialog(options.viewName(), options.beanClass().getName(),
                toValues.apply(options.value(), options.beanClass()), options.title());
        session.interact(step, answer -> {
            if (answer instanceof Map<?, ?> values) {
                applier.apply(options.value(), options.beanClass(), (Map<String, Object>) values);
                var closed = new boolean[1];
                try {
                    onSubmit.accept(options.value(), () -> closed[0] = true);
                } catch (ValidationError e) {
                    // the form stays open with what was sent, the message and the field that failed
                    throw new ReplayRetry(e.getMessage(), e.getInvalidProperty());
                }
                if (!closed[0] && !session.isPending()) {
                    throw new ReplayRetry(null); // the action did not close it: it is still open, as in ZK
                }
            }
        });
    }

    @Override
    public <T> void showView(ViewOptions<T> options) {
        var step = ActionFlowStep.view(options.viewName(), options.beanClass().getName(),
                toValues.apply(options.value(), options.beanClass()), options.title());
        session.interact(step, answer -> {
            // the user only had to see it
        });
    }

    private static Object entityToValues(Object bean, Class<?> beanClass) {
        return HeadlessCrudRemoteAction.toJson(bean, beanClass);
    }

    private static void applyToEntity(Object bean, Class<?> beanClass, Map<String, Object> values) {
        tools.dynamia.crud.actions.remote.SaveSupport.applyPatch(bean,
                tools.dynamia.crud.actions.remote.SaveSupport.jsonFormDescriptor(beanClass), values);
    }
}
