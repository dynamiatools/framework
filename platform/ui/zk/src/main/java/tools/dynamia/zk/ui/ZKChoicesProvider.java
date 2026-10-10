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
package tools.dynamia.zk.ui;

import tools.dynamia.ui.ChoiceOptions;
import tools.dynamia.ui.ChoicesProvider;
import tools.dynamia.zk.util.ZKUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ZK implementation of {@link ChoicesProvider}: a window with a list (single or multiple selection). The options are
 * wrapped so the list shows their label, and unwrapped before the callback.
 */
public class ZKChoicesProvider implements ChoicesProvider {

    /** An option with the text the list shows for it. */
    private record Labeled<T>(String label, T value) {
        @Override
        public String toString() {
            return label;
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void choose(ChoiceOptions<T> options, Consumer<List<T>> onChoice) {
        var labeled = new ArrayList<Labeled<T>>();
        options.options().forEach(option -> labeled.add(new Labeled<>(options.label().apply(option), option)));

        if (options.multiple()) {
            ZKUtil.showListboxMultiSelector(options.title(), "OK", labeled, event -> {
                var chosen = new ArrayList<T>();
                for (Object item : (List) event.getData()) {
                    chosen.add(((Labeled<T>) item).value());
                }
                onChoice.accept(chosen);
            });
        } else {
            ZKUtil.showListboxSelector(options.title(), labeled, event -> {
                var chosen = new ArrayList<T>();
                for (Object item : event.getSelectedObjects()) {
                    chosen.add(((Labeled<T>) item).value());
                }
                if (!chosen.isEmpty()) {
                    onChoice.accept(List.of(chosen.get(0)));
                }
            });
        }
    }
}
