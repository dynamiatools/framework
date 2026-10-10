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

import java.util.function.Supplier;

/**
 * Binds the headless implementation of a UI facade for the duration of a replay pass. Modules that own the knowledge a
 * facade needs (for example {@code crud}, which knows how to fill an entity from submitted values) register one as a
 * bean; {@link ReplayExecutor} applies all of them around the action.
 * <pre>{@code
 * public <T> T bind(ReplaySession session, Supplier<T> work) {
 *     return UIFacades.with(ViewsProvider.class, new HeadlessViews(session), work);
 * }
 * }</pre>
 */
public interface ReplayBinder {

    <T> T bind(ReplaySession session, Supplier<T> work);
}
