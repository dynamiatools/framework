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
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.ui.ChoiceOptions;
import tools.dynamia.ui.ChoicesProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link ChoicesProvider} of a headless run: a {@code CHOICE} step whose options are identified by key. The client answers
 * with the keys it chose; a key that no longer exists means the options changed since the question was asked, so the
 * question is asked again instead of applying the answer to another option. Choosing nothing is cancelling.
 */
public final class ReplayChoicesProvider implements ChoicesProvider {

    private final ReplaySession session;

    public ReplayChoicesProvider(ReplaySession session) {
        this.session = session;
    }

    @Override
    public <T> void choose(ChoiceOptions<T> options, Consumer<List<T>> onChoice) {
        var keys = options.keys();
        session.interact(ActionFlowStep.choice(options.title(), keys, options.labels(), options.multiple()), answer -> {
            var chosen = new ArrayList<T>();
            for (String key : keysOf(answer)) {
                int position = keys.indexOf(key);
                if (position < 0) {
                    throw new ReplayRetry(ClassMessages.get(ReplayChoicesProvider.class).get("flow.invalidChoice"));
                }
                chosen.add(options.options().get(position));
            }
            if (!chosen.isEmpty()) {
                onChoice.accept(options.multiple() ? chosen : List.of(chosen.get(0)));
            }
        });
    }

    private static List<String> keysOf(Object answer) {
        var keys = new ArrayList<String>();
        if (answer instanceof List<?> list) {
            for (Object item : list) {
                keys.add(String.valueOf(item));
            }
        } else if (answer != null) {
            keys.add(String.valueOf(answer));
        }
        return keys;
    }
}
