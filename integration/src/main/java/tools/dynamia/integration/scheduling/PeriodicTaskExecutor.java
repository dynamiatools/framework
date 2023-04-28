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

import org.springframework.scheduling.annotation.Scheduled;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Service;


/**
 * Execute {@link PeriodicTask}s using the next schedule everyday: <br/>
 * {@link MorningTask} at 6:00 AM <br/>
 * {@link MiddayTask} at 12:00 PM <br/>
 * {@link AfternoonTask} at 6:00 PM (18:00) <br/>
 * {@link MidnightTask} at 12:00 AM (23:59) <br/>
 * <p>
 * It use @{@link Scheduled} spring annotation with default server's local time zone.
 */
@Service
public class PeriodicTaskExecutor {

    private final LoggingService logger = new SLF4JLoggingService(PeriodicTaskExecutor.class);

    public PeriodicTaskExecutor() {
        logger.info("Starting " + getClass());
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void executeMorningTasks() {
        Containers.get().findObjects(MorningTask.class).forEach(task -> {
            try {
                logger.info("Executing morning task: " + task);
                task.execute();
            } catch (Exception e) {
                logger.error("Error executing morning task: " + task, e);
            }
        });
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void executeMiddayTasks() {
        Containers.get().findObjects(MiddayTask.class).forEach(task -> {
            try {
                logger.info("Executing midday task: " + task);
                task.execute();
            } catch (Exception e) {
                logger.error("Error executing midday task: " + task, e);
            }
        });
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void executeAfternoonTasks() {
        Containers.get().findObjects(AfternoonTask.class).forEach(task -> {
            try {
                logger.info("Executing afternoon task: " + task);
                task.execute();
            } catch (Exception e) {
                logger.error("Error executing afternoon task: " + task, e);
            }
        });
    }

    @Scheduled(cron = "0 59 23 * * *")
    public void executeMidnightTasks() {
        Containers.get().findObjects(MidnightTask.class).forEach(task -> {
            try {
                logger.info("Executing midnight task: " + task);
                task.execute();
            } catch (Exception e) {
                logger.error("Error executing midnight task: " + task, e);
            }
        });
    }
}
