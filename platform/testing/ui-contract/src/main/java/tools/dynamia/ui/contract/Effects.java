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
package tools.dynamia.ui.contract;

import java.util.ArrayList;
import java.util.List;

/**
 * What the code of a contract case did, recorded by the case itself. A driver gives the case a fresh {@code Effects} for
 * every run of the action and reports those of the last run, because a remote adapter runs the action again for every
 * answer and only the last run counts.
 */
public final class Effects {

    private final List<String> entries = new ArrayList<>();

    /**
     * @param entry what happened
     */
    public void add(String entry) {
        entries.add(entry);
    }

    /** @return what happened, in order */
    public List<String> entries() {
        return entries;
    }
}
