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
package tools.dynamia.integration.scheduling;

public abstract class TaskWithResult<R> extends Task {

    private R result;

    public TaskWithResult() {
    }

    public TaskWithResult(String name) {
        super(name);
    }

    @Override
    public void doWork() {
        result = doWorkWithResult();

    }

    public abstract R doWorkWithResult();

    public R getResult() {
        return result;
    }

}
