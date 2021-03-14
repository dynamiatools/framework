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
package tools.dynamia.integration;

import java.io.Serializable;

public class ProgressEvent implements Serializable {

    private long current, max;
    private int percent;
    private String message;

    public ProgressEvent(long current, long max, int percent, String message) {
        super();
        this.current = current;
        this.max = max;
        this.percent = percent;
        this.message = message;
    }

    public long getCurrent() {
        return current;
    }

    public long getMax() {
        return max;
    }

    public int getPercent() {
        if (percent > 100) {
            percent = 100;
        } else if (percent < 0) {
            percent = 0;
        }
        return percent;
    }

    public String getMessage() {
        return message;
    }
}
