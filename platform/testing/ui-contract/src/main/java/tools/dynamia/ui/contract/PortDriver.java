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

import java.util.List;
import java.util.function.Consumer;

/**
 * Runs an action on one platform and plays the user. Each adapter provides one; the contract suites of every port run
 * unchanged against all of them, which is what keeps ZK, remote clients and the test platform behaving the same.
 */
public interface PortDriver {

    /**
     * Runs {@code action} as the platform does and answers what it asks with {@code replies}, in order. Notifications,
     * views, progress and redirects need no reply.
     *
     * @param action  the code under test; it receives a fresh {@link Effects} for each run
     * @param replies what the user does
     * @return what happened
     * @throws AssertionError when the user is asked something there is no reply for, or a reply does not fit the question
     */
    Outcome run(Consumer<Effects> action, List<Reply> replies);
}
