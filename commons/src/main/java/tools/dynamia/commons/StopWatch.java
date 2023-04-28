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

public class StopWatch {

    private final long rate;
    private final long startTime;
    private long currentTime;
    private long lastNow;

    public StopWatch() {
        this(0);
    }

    public StopWatch(long rate) {
        super();
        this.rate = rate;
        startTime = System.nanoTime();
        lastNow = startTime;
        currentTime = startTime;
    }

    public boolean now() {
        currentTime = System.nanoTime();

        if (rate > 0 && (TimeUnit.NANOSECONDS.toMillis(currentTime) - TimeUnit.NANOSECONDS.toMillis(lastNow)) >= rate) {
            lastNow = currentTime;
            return true;
        } else {
            return false;
        }
    }

    public long getDurantion() {
        return System.nanoTime() - startTime;
    }

}
