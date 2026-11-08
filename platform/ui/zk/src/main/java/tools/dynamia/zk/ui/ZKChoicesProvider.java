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

    /**
     * Shows a dialog with a search box and a list; typing searches the lookup (at most 20 results) and selecting a row
     * picks it. The lookup applies its own filters, as it does for remote clients.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> void pickEntity(String title, tools.dynamia.ui.EntityLookup<T> lookup, Consumer<T> onPick) {
        var search = new org.zkoss.zul.Textbox();
        search.setHflex("1");
        search.setInstant(true);
        var list = new org.zkoss.zul.Listbox();
        list.setHflex("1");
        list.setHeight("300px");
        var layout = new org.zkoss.zul.Vlayout();
        layout.setHflex("1");
        layout.appendChild(search);
        layout.appendChild(list);

        java.util.function.Consumer<String> load = text -> {
            list.getItems().clear();
            for (T value : lookup.search(text == null ? "" : text, 20)) {
                var item = new org.zkoss.zul.Listitem(lookup.label(value));
                item.setValue(value);
                item.setParent(list);
            }
        };
        var dialog = ZKUtil.showDialog(title != null ? title : lookup.name(), layout, "480px", "440px");
        search.addEventListener(org.zkoss.zk.ui.event.Events.ON_CHANGING,
                event -> load.accept(((org.zkoss.zk.ui.event.InputEvent) event).getValue()));
        list.addEventListener(org.zkoss.zk.ui.event.Events.ON_SELECT, event -> {
            var selected = list.getSelectedItem();
            if (selected != null) {
                T value = selected.getValue();
                dialog.detach();
                onPick.accept(value);
            }
        });
        load.accept("");
    }

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
