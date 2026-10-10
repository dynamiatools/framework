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
package tools.dynamia.ui.testing;

import tools.dynamia.commons.Callback;
import tools.dynamia.crud.actions.remote.SaveSupport;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.ui.ChoiceOptions;
import tools.dynamia.ui.ChoicesProvider;
import tools.dynamia.ui.FileTransfer;
import tools.dynamia.ui.FormOptions;
import tools.dynamia.ui.MessageDisplayer;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.NavigationProvider;
import tools.dynamia.ui.NoUIEnvironment;
import tools.dynamia.ui.ProgressRunner;
import tools.dynamia.ui.ProgressTask;
import tools.dynamia.ui.UIEnvironment;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.UploadOptions;
import tools.dynamia.ui.UploadedFile;
import tools.dynamia.ui.ViewDialog;
import tools.dynamia.ui.ViewOptions;
import tools.dynamia.ui.ViewsProvider;
import tools.dynamia.ui.testing.UIInteraction.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The {@code test} platform: a {@link UIEnvironment} that implements every UI port by recording what the action does
 * and getting the user's answer from a {@link UIScript}. Callbacks run immediately, as in ZK.
 * <p>
 * It follows the same contract as the other adapters: cancelling never calls a success callback, a
 * {@link ValidationError} thrown by a form's {@code onSubmit} opens the form again with the error so the script can answer
 * once more, and a {@link tools.dynamia.ui.UIProgress} task runs with no UI available (see {@link NoUIEnvironment}).
 */
public final class TestUIEnvironment implements UIEnvironment {

    private final UIScript script;
    private final List<UIInteraction> interactions = new ArrayList<>();
    private final List<CapturedDownload> downloads = new ArrayList<>();
    private String redirectUrl;
    private final Map<Class<?>, Object> ports = new HashMap<>();

    /**
     * @param script the user
     */
    public TestUIEnvironment(UIScript script) {
        this.script = script;
        ports.put(MessageDisplayer.class, new Messages());
        ports.put(ViewsProvider.class, new Views());
        ports.put(ChoicesProvider.class, new Choices());
        ports.put(FileTransfer.class, new Files());
        ports.put(ProgressRunner.class, new Progress());
        ports.put(NavigationProvider.class, new Navigation());
    }

    @Override
    public String name() {
        return "test";
    }

    @Override
    public <S> Optional<S> port(Class<S> spi) {
        return Optional.ofNullable(ports.get(spi)).map(spi::cast);
    }

    /** @return everything the action did towards the user, in order */
    public List<UIInteraction> interactions() {
        return List.copyOf(interactions);
    }

    /** @return the files the action gave to the user */
    public List<CapturedDownload> downloads() {
        return List.copyOf(downloads);
    }

    /** @return where the action sent the user, or null */
    public String redirectUrl() {
        return redirectUrl;
    }

    private UIInteraction record(Type type, String title, String message, MessageType messageType, Map<String, Object> payload) {
        var interaction = new UIInteraction(type, title, message, messageType, payload);
        interactions.add(interaction);
        return interaction;
    }

    private UIScript.Answer ask(UIInteraction interaction) {
        return script.answer(interaction, interactions.size());
    }

    // -- messages ----------------------------------------------------------------------------------------

    private final class Messages implements MessageDisplayer {

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
            record(Type.NOTIFY, title, message, type, Map.of());
        }

        @Override
        public void showMessageDialog(String message, String title, MessageType messageType) {
            showMessage(message, title, messageType);
        }

        @Override
        public void showQuestion(String message, String title, Callback onYesResponse) {
            showQuestion(message, title, onYesResponse, null);
        }

        @Override
        public void showQuestion(String message, String title, Callback onYesResponse, Callback onNoResponse) {
            var interaction = record(Type.CONFIRM, title, message, null, Map.of());
            var answer = ask(interaction);
            boolean yes;
            if (answer == null || answer instanceof UIScript.Cancel) {
                yes = false;
            } else if (answer instanceof UIScript.Confirm confirm) {
                yes = confirm.yes();
            } else {
                throw UIScript.mismatch(interaction, interactions.size(), answer);
            }
            Callback chosen = yes ? onYesResponse : onNoResponse;
            if (chosen != null) {
                chosen.doSomething();
            }
        }

        @Override
        public <T> void showInput(String title, Class<T> valueClass, Consumer<T> onValue) {
            showInput(title, valueClass, null, onValue);
        }

        @Override
        public <T> void showInput(String title, Class<T> valueClass, T defaultValue, Consumer<T> onValue) {
            var payload = new LinkedHashMap<String, Object>();
            payload.put("valueClass", valueClass.getName());
            payload.put("defaultValue", defaultValue);
            var interaction = record(Type.INPUT, title, title, null, payload);
            var answer = ask(interaction);
            if (answer == null || answer instanceof UIScript.Cancel) {
                return;
            }
            if (answer instanceof UIScript.Input input) {
                if (input.value() != null && onValue != null) {
                    onValue.accept(convert(input.value(), valueClass));
                }
                return;
            }
            throw UIScript.mismatch(interaction, interactions.size(), answer);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T convert(Object answer, Class<T> type) {
        if (type.isInstance(answer)) {
            return (T) answer;
        }
        var text = String.valueOf(answer).trim();
        Object value;
        if (type == String.class) value = text;
        else if (type == Integer.class || type == int.class) value = Integer.valueOf(text);
        else if (type == Long.class || type == long.class) value = Long.valueOf(text);
        else if (type == Double.class || type == double.class) value = Double.valueOf(text);
        else if (type == BigDecimal.class) value = new BigDecimal(text);
        else if (type == Boolean.class || type == boolean.class) value = Boolean.valueOf(text);
        else if (type == LocalDate.class) value = LocalDate.parse(text);
        else throw new IllegalArgumentException("Cannot convert the answer to " + type.getName());
        return (T) value;
    }

    // -- views ---------------------------------------------------------------------------------------------

    private final class Views implements ViewsProvider {

        @Override
        public <T> void showForm(FormOptions<T> options, BiConsumer<T, ViewDialog> onSubmit) {
            String error = null;
            while (true) {
                var payload = new LinkedHashMap<String, Object>();
                payload.put("viewName", options.viewName());
                payload.put("beanClass", options.beanClass().getName());
                payload.put("value", options.value());
                var interaction = record(Type.DIALOG, options.title(), error, error == null ? null : MessageType.ERROR, payload);
                var answer = ask(interaction);
                if (answer == null || answer instanceof UIScript.Cancel) {
                    return;
                }
                if (!(answer instanceof UIScript.Form form)) {
                    throw UIScript.mismatch(interaction, interactions.size(), answer);
                }
                if (form.values() != null) {
                    SaveSupport.applyPatch(options.value(), SaveSupport.jsonFormDescriptor(options.beanClass()), form.values());
                }
                if (form.edit() != null) {
                    form.edit().accept(options.value());
                }
                try {
                    // Like the replay adapter today, returning from onSubmit means the user is done with the form.
                    onSubmit.accept(options.value(), () -> {
                    });
                    return;
                } catch (ValidationError e) {
                    error = e.getMessage();
                }
            }
        }

        @Override
        public <T> void showView(ViewOptions<T> options) {
            var payload = new LinkedHashMap<String, Object>();
            payload.put("viewName", options.viewName());
            payload.put("beanClass", options.beanClass().getName());
            payload.put("value", options.value());
            record(Type.VIEW, options.title(), null, null, payload);
        }
    }

    // -- choices -------------------------------------------------------------------------------------------

    private final class Choices implements ChoicesProvider {

        @Override
        public <T> void choose(ChoiceOptions<T> options, Consumer<List<T>> onChoice) {
            var payload = new LinkedHashMap<String, Object>();
            payload.put("options", options.labels());
            payload.put("multiple", options.multiple());
            var interaction = record(Type.CHOICE, options.title(), null, null, payload);
            var answer = ask(interaction);
            if (answer == null || answer instanceof UIScript.Cancel) {
                return;
            }
            if (!(answer instanceof UIScript.Choose choose)) {
                throw UIScript.mismatch(interaction, interactions.size(), answer);
            }
            var chosen = new ArrayList<T>();
            var labels = options.labels();
            for (Object selected : choose.selection()) {
                int position = selected instanceof Integer i ? i : labels.indexOf(String.valueOf(selected));
                if (position < 0 || position >= options.options().size()) {
                    throw new AssertionError("Choice " + selected + " is not one of the options " + labels);
                }
                chosen.add(options.options().get(position));
            }
            if (!chosen.isEmpty()) {
                onChoice.accept(options.multiple() ? chosen : List.of(chosen.get(0)));
            }
        }
    }

    // -- files ---------------------------------------------------------------------------------------------

    private final class Files implements FileTransfer {

        @Override
        public void download(String fileName, String contentType, byte[] content) {
            downloads.add(new CapturedDownload(fileName, contentType, content));
        }

        @Override
        public void upload(UploadOptions options, Consumer<List<UploadedFile>> onFiles) {
            var payload = new LinkedHashMap<String, Object>();
            payload.put("accept", options.accept());
            payload.put("multiple", options.multiple());
            var interaction = record(Type.UPLOAD, options.title(), null, null, payload);
            var answer = ask(interaction);
            if (answer == null || answer instanceof UIScript.Cancel) {
                return;
            }
            if (!(answer instanceof UIScript.Upload upload)) {
                throw UIScript.mismatch(interaction, interactions.size(), answer);
            }
            if (!upload.files().isEmpty()) {
                onFiles.accept(upload.files());
            }
        }
    }

    // -- progress ------------------------------------------------------------------------------------------

    private final class Progress implements ProgressRunner {

        @Override
        public void run(String title, String messageTemplate, ProgressTask task, Callback onFinish, Consumer<Throwable> onError) {
            record(Type.PROGRESS, title, messageTemplate, null, Map.of());
            Throwable[] failure = new Throwable[1];
            // The contract: no UI inside the task.
            UIFacades.with(NoUIEnvironment.INSTANCE, () -> {
                try {
                    task.run(new ProgressMonitor());
                } catch (Throwable e) {
                    failure[0] = e;
                }
                return null;
            });
            if (failure[0] != null) {
                onError.accept(failure[0]);
            } else if (onFinish != null) {
                onFinish.doSomething();
            }
        }
    }

    // -- navigation ----------------------------------------------------------------------------------------

    private final class Navigation implements NavigationProvider {

        @Override
        public void open(String url, boolean newWindow) {
            redirectUrl = url;
            record(Type.REDIRECT, null, url, null, Map.of("url", url, "newWindow", newWindow));
        }
    }
}
