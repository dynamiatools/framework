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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.integration.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Utility class for scheduling and executing tasks asynchronously using Virtual Threads. It also provides methods
 * for scheduling tasks using cron expressions and fixed delays with Spring's TaskScheduler.
 *
 * @author Mario A. Serrano Leones
 */
public class SchedulerUtil {

    /**
     *
     */
    public static String schedulingEnabledProperty = "schedulingEnabled";
    ;

    /**
     * Execute a task asynchronously using a Virtual Thread executor from {@link VT} helper class.
     *
     * @param task the runnable
     */
    public static void run(Runnable task) {
        Runnable runnableWithContext = getWithContext(task);
        VT.executor().execute(runnableWithContext);
    }

    /**
     * Execute a task using a Virtual Thread executor from {@link VT} helper class and wait for its completion
     * or timeout.
     *
     * @param task    the runnable
     * @param timeout the timeout
     */
    public static void runAndWait(Runnable task, Duration timeout) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        run(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        try {
            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new TaskException(e.getMessage(), e);
        }
    }

    /**
     * Run a list of task in sequence. Each task will be executed in order and
     * only when the previous task is completed.
     *
     * @param firstTask the first task
     * @param others    the tasks
     */
    public static CompletableFuture<Void> run(Runnable firstTask, Runnable... others) {

        List<Runnable> allTasks = new ArrayList<>();
        allTasks.add(firstTask);
        if (others != null) {
            allTasks.addAll(Arrays.asList(others));
        }
        return run(allTasks);
    }

    /**
     * Run a list of task in sequence. Each task will be executed in order and
     * only when the previous task is completed.
     *
     * @param allTasks the tasks
     */
    public static CompletableFuture<Void> run(List<Runnable> allTasks) {
        CompletableFuture<Void> sequence = CompletableFuture.completedFuture(null);
        for (var task : allTasks) {
            sequence = sequence.thenCompose(fn -> CompletableFuture.supplyAsync(() -> {
                task.run();
                return null;
            }, VT.executor()));
        }

        sequence.join();
        return sequence;
    }

    public static Runnable getWithContext(Runnable runnable) {


        SimpleObjectContainer context = buildAsyncContext();

        // return runnable that set the context before run and clear after run
        return () -> {
            ThreadLocalObjectContainer.set(context);
            try {
                runnable.run();
            } finally {
                ThreadLocalObjectContainer.clear();
            }
        };
    }

    /**
     * Build the async context from current thread local context and
     * ThreadLocalObjectAware beans in containers
     *
     * @return the simple object container
     */
    private static SimpleObjectContainer buildAsyncContext() {
        SimpleObjectContainer context = new SimpleObjectContainer();
        if (ThreadLocalObjectContainer.isInitialized()) {
            // copy current thread context
            ThreadLocalObjectContainer.copyTo(context);
        } else {
            Collection<ThreadLocalObjectAware> sessionBeans = Containers.get().findObjects(ThreadLocalObjectAware.class);
            if (sessionBeans != null) {
                sessionBeans.forEach(bean -> {
                    if (bean instanceof CloneableThreadLocalObject cloneable) {
                        try {
                            context.addObject(cloneable.clone());
                        } catch (Exception e) {
                            context.addObject(bean); // fallback a referencia
                        }
                    } else {
                        context.addObject(bean);
                    }
                });
            }

            // add context objects from providers
            Collection<ThreadLocalContextProvider> providers = Containers.get().findObjects(ThreadLocalContextProvider.class);
            if (providers != null) {
                providers.forEach(provider -> {
                    Map<String, Object> contextObjects = provider.getContextObjects();
                    if (contextObjects != null) {
                        contextObjects.forEach(context::addObject);
                    }
                });
            }
        }
        return context;
    }


    /**
     * Run the WorkerTask asynchronously using a Virtual Thread executor from {@link VT} helper class.
     * WorkerTask required override doWorkWithResult method.
     *
     * @param <T>  the generic type
     * @param task the task
     * @return the future
     */
    public static <T> CompletableFuture<T> runWithResult(final TaskWithResult<T> task) {
        return runWithResult(task::doWorkWithResult);
    }

    /**
     * Run the Supplier task asynchronously using a Virtual Thread executor from {@link VT} helper class.
     *
     * @param task supplier task
     * @param <T>  the generic type
     * @return the future
     */
    public static <T> CompletableFuture<T> runWithResult(Supplier<T> task) {
        SimpleObjectContainer context = buildAsyncContext();
        Supplier<T> supplierWithContext = () -> {
            ThreadLocalObjectContainer.set(context);
            try {
                return task.get();
            } finally {
                ThreadLocalObjectContainer.clear();
            }
        };

        return CompletableFuture.supplyAsync(supplierWithContext, VT.executor());
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
    public static ScheduledFuture<?> schedule(String cron, TimeZone timeZone, Runnable task) {
        TaskScheduler scheduler = getTaskScheduler();

        Trigger trigger = new CronTrigger(cron, timeZone);
        return scheduler.schedule(task, trigger);

    }

    /**
     * Gets the task scheduler from spring containers or create a default one.
     *
     * @return the task scheduler
     */
    public static TaskScheduler getTaskScheduler() {
        TaskScheduler scheduler = Containers.get().findObject(TaskScheduler.class);
        if (scheduler == null) {
            // create a default one
            scheduler = new ThreadPoolTaskScheduler();
        }
        return scheduler;
    }

    /**
     * Schedule a WorkerTask using the cron expression passed. This method use
     * Spring TaskScheduler to perform scheduling
     *
     * @param cron the cron
     * @param task the task
     * @return the scheduled future
     */
    public static ScheduledFuture<?> schedule(String cron, Runnable task) {
        return schedule(cron, TimeZone.getDefault(), task);
    }


    /**
     * Schedule a Task at specified date.
     *
     * @param startDate the start date
     * @param task      the task
     * @return the scheduled future
     */
    public static ScheduledFuture<?> schedule(Date startDate, Runnable task) {
        return getTaskScheduler().schedule(task, DateTimeUtils.toInstant(startDate));
    }

    /**
     * Schedule a Task at specified date.
     *
     * @param startDate the start date
     * @param zoneId    the zone id
     * @param task      the task
     * @return the scheduled future
     */
    public static ScheduledFuture<?> schedule(LocalDateTime startDate, ZoneId zoneId, Runnable task) {
        Instant instant = startDate.atZone(zoneId).toInstant();
        return getTaskScheduler().schedule(task, instant);
    }

    /**
     * Schedule a Task at specified date using system default time zone.
     *
     * @param startDate the start date
     * @param task      the task
     * @return the scheduled future
     */
    public static ScheduledFuture<?> schedule(LocalDateTime startDate, Runnable task) {
        return schedule(startDate, ZoneId.systemDefault(), task);
    }

    private SchedulerUtil() {
    }

    /**
     * Find in spring {@link Environment} if schedulingEnabledProperty is true
     *
     * @return true, if is scheduling enabled
     */
    public static boolean isSchedulingEnabled() {
        Environment env = Containers.get().findObject(Environment.class);
        if (env != null && schedulingEnabledProperty != null) {
            return "true".equalsIgnoreCase(env.getProperty(schedulingEnabledProperty, "true"));
        }
        return Containers.get().findObject(ScheduledAnnotationBeanPostProcessor.class) != null;
    }

    /**
     * Schedule a task at fixed rate using Spring TaskScheduler for repeated execution.
     *
     * @param rate the rate
     * @param task the task to repeat each period
     * @return the scheduled future
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Duration rate, Runnable task) {
        return getTaskScheduler().scheduleAtFixedRate(task, rate);
    }

    /**
     * Sleep current thread for the given duration. Handles InterruptedException by re-interrupting the thread.
     *
     * @param duration the duration
     */
    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sleep current thread for the given duration. Handles InterruptedException by re-interrupting the thread
     * and passing the exception to the provided consumer.
     *
     * @param duration          the duration
     * @param exceptionConsumer the exception consumer
     */
    public static void sleep(Duration duration, Consumer<Exception> exceptionConsumer) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (exceptionConsumer != null) {
                exceptionConsumer.accept(e);
            }
        }
    }


}
