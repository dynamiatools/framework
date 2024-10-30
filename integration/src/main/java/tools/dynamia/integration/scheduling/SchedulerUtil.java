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

import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.support.CronTrigger;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.ObjectContainerContextHolder;
import tools.dynamia.integration.SimpleObjectContainer;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Supplier;


/**
 * The Class SchedulerUtil.
 */
public class SchedulerUtil {

    /**
     *
     */
    public static String schedulingEnabledProperty = "schedulingEnabled";
    ;

    /**
     * Run the WorkerTask asynchronously using any Spring AsyncTaskExecutor
     * found in the Spring Containers or standard java Thread(runnable) if no
     * TaskExecutor is found.
     *
     * @param task the task
     */
    public static void run(Task task) {
        run((Runnable) task);
    }

    /**
     * Run the WorkerTask asynchronously using any Spring AsyncTaskExecutor
     * found in the Spring Containers or standard java Thread(runnable) if no
     * TaskExecutor is found.
     *
     * @param runnable the task to run
     */
    public static void run(Runnable runnable) {


        Runnable runnableWithContext = getWithContext(runnable);

        AsyncTaskExecutor executor = Containers.get().findObject(AsyncTaskExecutor.class);
        if (executor != null) {
            // use spring executor
            executor.execute(runnableWithContext);
        } else {
            CompletableFuture.runAsync(runnableWithContext);
        }
    }

    private static Runnable getWithContext(Runnable runnable) {
        var asyncContextAwares = Containers.get().findObjects(AsyncContextAware.class);
        var context = new SimpleObjectContainer();
        if (asyncContextAwares != null && !asyncContextAwares.isEmpty()) {
            asyncContextAwares.forEach(context::addObject);
        }

        ObjectContainerContextHolder.set(context);
        Containers.get().installObjectContainer(context);
        return () -> {
            try {
                runnable.run();
            } finally {
                ObjectContainerContextHolder.clear();
                Containers.get().removeContainer(context.getName());
            }
        };
    }


    /**
     * Run the WorkerTask asynchronously using Spring AsyncTaskExecutor
     * WorkerTask required override doWorkWithResult method.
     *
     * @param <T>  the generic type
     * @param task the task
     * @return the future
     */
    public static <T> Future<T> runWithResult(final TaskWithResult<T> task) {
        AsyncTaskExecutor executor = Containers.get().findObject(AsyncTaskExecutor.class);
        if (executor != null) {
            // use spring executor
            return executor.submit(task::doWorkWithResult);
        } else {
            return CompletableFuture.supplyAsync(task::doWorkWithResult);
        }
    }

    /**
     * Functional override of runWithResult({@link TaskWithResult }
     */
    public static <T> Future<T> runWithResult(Supplier<T> task) {
        return runWithResult(new TaskWithResult<>() {
            @Override
            public T doWorkWithResult() {
                return task.get();
            }
        });
    }

    /**
     * Schedule a WorkerTask using the cron expression passed. This method use
     * Spring TaskScheduler to perform scheduling. Check {@link org.springframework.scheduling.support.CronExpression}
     *
     * @param cron     the cron
     * @param timeZone the time zone
     * @param task     the task
     * @return the scheduled future
     */
    public static ScheduledFuture schedule(String cron, TimeZone timeZone, Task task) {
        TaskScheduler scheduler = Containers.get().findObject(TaskScheduler.class);
        if (scheduler != null) {
            Trigger trigger = new CronTrigger(cron, timeZone);
            return scheduler.schedule(task, trigger);
        } else {
            throw new TaskException("No TaskScheduler found to run task " + task + " with cron " + cron);
        }
    }

    /**
     * Schedule a WorkerTask using the cron expression passed. This method use
     * Spring TaskScheduler to perform scheduling
     *
     * @param cron the cron
     * @param task the task
     * @return the scheduled future
     */
    public static ScheduledFuture schedule(String cron, Task task) {
        return schedule(cron, TimeZone.getDefault(), task);
    }

    /**
     * Schedule a Task at specified date.
     *
     * @param startDate the start date
     * @param task      the task
     * @return the scheduled future
     */
    public static ScheduledFuture schedule(Date startDate, Task task) {
        TaskScheduler scheduler = Containers.get().findObject(TaskScheduler.class);
        if (scheduler != null) {
            return scheduler.schedule(task, startDate);
        } else {
            throw new TaskException("No TaskScheduler found to run task " + task + " at start date " + startDate);
        }
    }

    private SchedulerUtil() {
    }

    /**
     * Find in spring {@link Environment} if schedulingEnabledProperty is true
     *
     * @return
     */
    public static boolean isSchedulingEnabled() {
        Environment env = Containers.get().findObject(Environment.class);
        if (env != null && schedulingEnabledProperty != null) {
            return "true".equalsIgnoreCase(env.getProperty(schedulingEnabledProperty, "true"));
        }
        return Containers.get().findObject(ScheduledAnnotationBeanPostProcessor.class) != null;
    }

}
