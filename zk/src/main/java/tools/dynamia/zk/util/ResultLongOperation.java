/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
package tools.dynamia.zk.util;

import org.springframework.core.task.TaskExecutor;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Let you perform LongOperations that returns something and is UI events aware.
 * Use executeWithResult() method instead execute()
 *
 * @author Mario
 *
 * @param <R>
 */
public class ResultLongOperation<R> extends LongOperation {

    private R result;
    private Supplier<R> executeSupplier;
    private Consumer<R> onResultConsumer;

    public ResultLongOperation() {
        super();
        
    }

    public ResultLongOperation(TaskExecutor taskExecutor) {
        super(taskExecutor);
        
    }

    public ResultLongOperation<R> executeWithResult(Supplier<R> supplier) {
        this.executeSupplier = supplier;
        return this;
    }

    public ResultLongOperation<R> onResult(Consumer<R> onResultConsumer) {
        this.onResultConsumer = onResultConsumer;
        return this;
    }

    @Override
    protected void execute() {
        result = executeSupplier.get();
    }

    @Override
    protected void finish() {
        if (onResultConsumer != null) {
            onResultConsumer.accept(result);
        }
        super.finish();
    }

    public static <R> ResultLongOperation<R> create(Class<R> expectedClass) {
        return new ResultLongOperation<>();
    }

    public static <R> ResultLongOperation<R> create(Class<R> expectedClass, TaskExecutor taskExecutor) {
        return new ResultLongOperation<>(taskExecutor);
    }

}
