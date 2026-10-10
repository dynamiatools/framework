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

/**
 * Thrown when an action uses a UI facade where there is no UI able to serve it: a job, a background thread, the body of
 * a {@link ProgressTask}, or a deployment without the front end that implements that port. It replaces the
 * {@code NullPointerException} or "bean not found" an unavailable port used to end in.
 */
public class UIUnavailableException extends IllegalStateException {

    private final String port;
    private final String environment;

    /**
     * @param port        name of the missing port (see {@link UIPort#name()})
     * @param environment name of the environment that does not support it
     */
    public UIUnavailableException(String port, String environment) {
        super("UI port '" + port + "' is not available in environment '" + environment + "': run it from a ZK event or "
                + "from a replayed action, or move it out of background code");
        this.port = port;
        this.environment = environment;
    }

    /**
     * @return name of the missing port
     */
    public String getPort() {
        return port;
    }

    /**
     * @return name of the environment that does not support it
     */
    public String getEnvironment() {
        return environment;
    }
}
