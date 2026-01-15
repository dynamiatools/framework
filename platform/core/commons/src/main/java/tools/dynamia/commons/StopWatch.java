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
package tools.dynamia.commons;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for measuring elapsed time and controlling execution rate.
 * <p>
 * The {@code StopWatch} class provides a simple way to measure the time elapsed since its creation and to control the frequency of operations based on a configurable rate (in milliseconds).
 * <p>
 * Typical use cases include profiling code, throttling operations, or implementing periodic tasks.
 * <p>
 * Example usage:
 * <pre>
 *     StopWatch sw = new StopWatch(1000); // 1 second rate
 *     while (true) {
 *         if (sw.now()) {
 *             // Perform periodic action every second
 *         }
 *     }
 * </pre>
 * <p>
 * Thread safety: This class is not thread-safe.
 */
public class StopWatch {

    /**
     * The minimum interval (in milliseconds) between allowed executions.
     * If zero, {@link #now()} always returns false.
     */
    private final long rate;
    /**
     * The timestamp (in nanoseconds) when the stopwatch was started.
     */
    private final long startTime;
    /**
     * The current timestamp (in nanoseconds) when {@link #now()} is called.
     */
    private long currentTime;
    /**
     * The last timestamp (in nanoseconds) when {@link #now()} returned true.
     */
    private long lastNow;

    /**
     * Creates a new {@code StopWatch} with no rate limit.
     * <p>
     * Equivalent to {@code new StopWatch(0)}.
     */
    public StopWatch() {
        this(0);
    }

    /**
     * Creates a new {@code StopWatch} with the specified rate limit.
     *
     * @param rate the minimum interval in milliseconds between allowed executions
     */
    public StopWatch(long rate) {
        super();
        this.rate = rate;
        startTime = System.nanoTime();
        lastNow = startTime;
        currentTime = startTime;
    }

    /**
     * Checks if the specified rate interval has elapsed since the last successful call.
     * <p>
     * If the rate is greater than zero and the interval has passed, returns {@code true} and resets the timer.
     * Otherwise, returns {@code false}.
     *
     * @return {@code true} if the rate interval has elapsed, {@code false} otherwise
     */
    public boolean now() {
        currentTime = System.nanoTime();

        if (rate > 0 && (TimeUnit.NANOSECONDS.toMillis(currentTime) - TimeUnit.NANOSECONDS.toMillis(lastNow)) >= rate) {
            lastNow = currentTime;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the elapsed time in nanoseconds since the stopwatch was started.
     *
     * @return the duration in nanoseconds
     */
    public long getDurantion() {
        return System.nanoTime() - startTime;
    }

}
