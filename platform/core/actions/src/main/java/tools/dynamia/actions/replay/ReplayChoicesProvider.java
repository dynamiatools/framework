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
package tools.dynamia.actions.replay;

import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.ui.ChoiceOptions;
import tools.dynamia.ui.ChoicesProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link ChoicesProvider} of a headless run: a {@code CHOICE} step with the labels; the client answers with the positions
 * chosen, which are mapped back to the options (the list is the same on every pass of a deterministic action).
 */
public final class ReplayChoicesProvider implements ChoicesProvider {

    private final ReplaySession session;

    public ReplayChoicesProvider(ReplaySession session) {
        this.session = session;
    }

    @Override
    public <T> void choose(ChoiceOptions<T> options, Consumer<List<T>> onChoice) {
        session.interact(ActionFlowStep.choice(options.title(), options.labels(), options.multiple()), answer -> {
            var chosen = new ArrayList<T>();
            for (int position : positions(answer)) {
                if (position < 0 || position >= options.options().size()) {
                    throw new IllegalArgumentException("Choice " + position + " is not one of the " + options.options().size() + " options");
                }
                chosen.add(options.options().get(position));
            }
            if (!chosen.isEmpty()) {
                onChoice.accept(options.multiple() ? chosen : List.of(chosen.get(0)));
            }
        });
    }

    private static List<Integer> positions(Object answer) {
        var positions = new ArrayList<Integer>();
        if (answer instanceof Number number) {
            positions.add(number.intValue());
        } else if (answer instanceof List<?> list) {
            for (Object item : list) {
                positions.add(Integer.parseInt(String.valueOf(item)));
            }
        }
        return positions;
    }
}
