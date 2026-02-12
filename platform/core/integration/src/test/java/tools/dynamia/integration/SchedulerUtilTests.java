package tools.dynamia.integration;

import org.junit.Assert;
import org.junit.Test;
import tools.dynamia.integration.scheduling.SchedulerUtil;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static tools.dynamia.integration.scheduling.SchedulerUtil.runAndWait;
import static tools.dynamia.integration.scheduling.SchedulerUtil.runWithResult;

public class SchedulerUtilTests {

    @Test
    public void testSimpleAsyncTask() throws Exception {

        runAndWait(() -> {
            System.out.println(">Processing async task...");
            SchedulerUtil.sleep(Duration.ofSeconds(1));
            System.out.println(">>Hello from async task!");
        }, Duration.ofSeconds(5));
    }

    @Test
    public void testAsyncTaskWithResult() throws Exception {

        var result = runWithResult(() -> {
            System.out.println("> Processing async task with result...");
            SchedulerUtil.sleep(Duration.ofSeconds(1));
            System.out.println(">> Returning result from async task!");
            return "Task Result";
        }).get(); // Wait for the result

        Assert.assertEquals("Task Result", result);
    }

    @Test
    public void shouldSubmitMultipleTasksInOrder() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);

        var sequence = SchedulerUtil.run(
                () -> {
                    int sleepTime = new Random().nextInt(500);
                    SchedulerUtil.sleep(Duration.ofMillis(sleepTime));
                    int value = counter.incrementAndGet();
                    System.out.println("Task One: " + value + " completed after " + sleepTime + " ms");
                },
                () -> {
                    int sleepTime = 10; //fix to ensure order
                    SchedulerUtil.sleep(Duration.ofMillis(sleepTime));
                    int value = counter.incrementAndGet();
                    System.out.println("Task Two: " + value + " completed after " + sleepTime + " ms");
                },
                () -> {
                    int sleepTime = new Random().nextInt(500);
                    SchedulerUtil.sleep(Duration.ofMillis(sleepTime));
                    int value = counter.incrementAndGet();
                    System.out.println("Task Three: " + value + " completed after " + sleepTime + " ms");
                }
        );

        sequence.get(); // Wait for all tasks to complete
        Assert.assertEquals(3, counter.get());
    }
}
