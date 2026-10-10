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

import tools.dynamia.commons.Callback;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * The environment of code that runs with no user in front: jobs, schedulers, the task of a {@link UIProgress}. Messages
 * are written to the log; anything that needs an answer or a widget fails with {@link UIUnavailableException}.
 */
public final class NoUIEnvironment implements UIEnvironment {

    /** The only instance. */
    public static final NoUIEnvironment INSTANCE = new NoUIEnvironment();

    private final MessageDisplayer logger = new LoggingDisplayer();

    private NoUIEnvironment() {
    }

    @Override
    public String name() {
        return "none";
    }

    @Override
    public <S> Optional<S> port(Class<S> spi) {
        if (spi == MessageDisplayer.class) {
            return Optional.of(spi.cast(logger));
        }
        return Optional.empty();
    }

    /** Messages go to the log; questions and inputs cannot be answered without a user. */
    private static final class LoggingDisplayer implements MessageDisplayer {

        private final LoggingService log = new SLF4JLoggingService(NoUIEnvironment.class);

        private static UIUnavailableException unavailable() {
            return new UIUnavailableException("messages", INSTANCE.name());
        }

        @Override
        public void showMessage(String message) {
            showMessage(message, null, MessageType.NORMAL);
        }

        @Override
        public void showMessage(String message, MessageType type) {
            showMessage(message, null, type);
        }

        @Override
        public void showMessage(String message, String title, MessageType type) {
            String text = title == null ? message : title + ": " + message;
            if (type == MessageType.ERROR || type == MessageType.CRITICAL) {
                log.error(text);
            } else if (type == MessageType.WARNING) {
                log.warn(text);
            } else {
                log.info(text);
            }
        }

        @Override
        public void showMessageDialog(String message, String title, MessageType messageType) {
            showMessage(message, title, messageType);
        }

        @Override
        public void showQuestion(String message, String title, Callback onYesResponse) {
            throw unavailable();
        }

        @Override
        public void showQuestion(String message, String title, Callback onYesResponse, Callback onNoResponse) {
            throw unavailable();
        }

        @Override
        public <T> void showInput(String title, Class<T> valueClass, Consumer<T> onValue) {
            throw unavailable();
        }

        @Override
        public <T> void showInput(String title, Class<T> valueClass, T defaultValue, Consumer<T> onValue) {
            throw unavailable();
        }
    }
}
