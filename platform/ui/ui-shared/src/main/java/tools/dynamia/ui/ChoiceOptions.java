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
 *
 * @param title    dialog title
 * @param options  the candidates, in the order they are shown; a headless run identifies the answer by position, so the
 *                 list must be the same every time the action runs
 * @param label    text shown for each option
 * @param multiple whether several options can be chosen
 * @param <T>      option type
 */
public record ChoiceOptions<T>(String title, List<T> options, Function<T, String> label, boolean multiple) {

    public List<String> labels() {
        return options.stream().map(label).toList();
    }
}
