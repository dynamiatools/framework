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

import tools.dynamia.integration.Containers;
import tools.dynamia.ui.ChoicesProvider;
import tools.dynamia.ui.FileTransfer;
import tools.dynamia.ui.MessageDisplayer;
import tools.dynamia.ui.NavigationProvider;
import tools.dynamia.ui.ProgressRunner;
import tools.dynamia.ui.UIEnvironment;
import tools.dynamia.ui.ViewsProvider;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The {@link UIEnvironment} of a ZK execution: every UI port is served by the ZK widgets.
 * <p>
 * A port is first looked up in the container, so an application can replace one of them (the ERP and the themes register
 * their own {@link MessageDisplayer}) just by declaring a bean; otherwise the ZK implementation of this module is used.
 * It is created by {@link ZKUIEnvironmentProvider}, which activates it only while a ZK execution exists.
 */
public class ZKUIEnvironment implements UIEnvironment {

    private final Map<Class<?>, Object> defaults = Map.of(
            MessageDisplayer.class, new MessageNotification(),
            FileTransfer.class, new ZKFileTransfer(),
            ProgressRunner.class, new ZKProgressRunner(),
            ViewsProvider.class, new ZKViewsProvider(),
            ChoicesProvider.class, new ZKChoicesProvider(),
            NavigationProvider.class, new ZKNavigationProvider());

    @Override
    public String name() {
        return "zk";
    }

    @Override
    public <S> Optional<S> port(Class<S> spi) {
        Object fallback = defaults.get(spi);
        if (fallback == null) {
            return Optional.empty();
        }
        S override = Containers.get().findObject(spi);
        return Optional.of(spi.cast(override != null ? override : fallback));
    }
}
