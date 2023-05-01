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

/**
 * The Class Task.
 *
 */
public abstract class Task implements Runnable {

    /**
     * The name.
     */
    private String name;

    /**
     * Instantiates a new task.
     */
    public Task() {
        name = "WorkerTask_" + System.currentTimeMillis();
    }

    public Task(String name) {
        super();
        this.name = name;
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        doWork();
    }

    /**
     * Do work.
     */
    public abstract void doWork();

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getName();
    }

}
