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

import tools.dynamia.ui.files.UploadedFile;

import java.util.List;
import java.util.Map;

/**
 * What the user does when an action asks something, in a contract test. An adapter driver turns each reply into what its
 * platform understands: a click in ZK, an answer to a step in a remote client, a line of a script in the test platform.
 */
public sealed interface Reply {

    /** Answers a question yes. */
    record Yes() implements Reply {
    }

    /** Answers a question no. */
    record No() implements Reply {
    }

    /** Closes whatever was asked without answering. */
    record Cancel() implements Reply {
    }

    /** Answers an input with a value, as the user typed it. */
    record Input(Object value) implements Reply {
    }

    /** Fills a form with values by field name and submits it. */
    record Form(Map<String, Object> values) implements Reply {
    }

    /** Chooses options by key. */
    record Choose(List<String> keys) implements Reply {
    }

    /** Gives files to an upload. */
    record Upload(List<UploadedFile> files) implements Reply {
    }

    static Reply yes() {
        return new Yes();
    }

    static Reply no() {
        return new No();
    }

    static Reply cancel() {
        return new Cancel();
    }

    static Reply input(Object value) {
        return new Input(value);
    }

    static Reply form(Map<String, Object> values) {
        return new Form(values);
    }

    static Reply choose(String... keys) {
        return new Choose(List.of(keys));
    }

    static Reply upload(UploadedFile... files) {
        return new Upload(List.of(files));
    }
}
