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

import java.util.List;
import java.util.function.Function;

/**
 * What {@link UIChoices} asks the user to choose from.
 * <p>
 * Each option has a stable <b>key</b>: a headless client answers with keys, not positions, so if the list changes between
 * the question and the answer the answer is never applied to a different option.
 *
 * @param title    dialog title
 * @param options  the candidates, in the order they are shown
 * @param label    text shown for each option
 * @param key      stable identifier of each option; by default the {@code id} of an entity if it has one, else the label
 * @param multiple whether several options can be chosen
 * @param <T>      option type
 */
public record ChoiceOptions<T>(String title, List<T> options, Function<T, String> label, Function<T, String> key,
                               boolean multiple) {

    /** Options keyed by their entity id if they have one, else by their label. */
    public ChoiceOptions(String title, List<T> options, Function<T, String> label, boolean multiple) {
        this(title, options, label, defaultKey(label), multiple);
    }

    /** @return the label of each option, in order */
    public List<String> labels() {
        return options.stream().map(label).toList();
    }

    /** @return the key of each option, in order */
    public List<String> keys() {
        return options.stream().map(key).toList();
    }

    /**
     * @param label text shown for an option
     * @param <T>   option type
     * @return the default key: the {@code getId()} of the option when it has a non null one, else its label
     */
    public static <T> Function<T, String> defaultKey(Function<T, String> label) {
        return option -> {
            try {
                Object id = option.getClass().getMethod("getId").invoke(option);
                if (id != null) {
                    return String.valueOf(id);
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                // not an entity: the label identifies it
            }
            return label.apply(option);
        };
    }
}
