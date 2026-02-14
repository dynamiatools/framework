package tools.dynamia.web.controllers;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.integration.scheduling.PeriodicTaskExecutor;

/**
 * REST controller that exposes endpoints for executing periodic tasks defined in the {@link PeriodicTaskExecutor} class. This controller allows you to trigger the execution of morning, midday, afternoon, and evening tasks via HTTP GET requests. Each endpoint corresponds to a specific time of day and will execute the associated tasks when accessed. This can be useful for testing or manually triggering scheduled tasks without waiting for their scheduled execution time.
 *
 * @author Mario A. Serrano Leones
 */
@RestController
@RequestMapping("/schedule/execute-tasks")
@Tag(name = "DynamiaPeriodicTasks")
public class PeriodicTaskExecutorRestController extends PeriodicTaskExecutor {

    @Override
    @GetMapping("/morning")
    public void executeMorningTasks() {
        super.executeMorningTasks();
    }

    @Override
    @GetMapping("/midday")
    public void executeMiddayTasks() {
        super.executeMiddayTasks();
    }

    @Override
    @GetMapping("/afternoon")
    public void executeAfternoonTasks() {
        super.executeAfternoonTasks();
    }

    @Override
    @GetMapping("/evening")
    public void executeMidnightTasks() {
        super.executeMidnightTasks();
    }
}
