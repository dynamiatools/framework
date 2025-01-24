package tools.dynamia.web.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.integration.scheduling.PeriodicTaskExecutor;

@RestController
@RequestMapping("/schedule/execute-tasks")
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
