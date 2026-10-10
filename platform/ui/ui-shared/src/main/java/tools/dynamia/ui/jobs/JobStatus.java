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
package tools.dynamia.ui.jobs;

/**
 * Where a background job is, as a remote client sees it.
 *
 * @param id      identifier of the job
 * @param title   what the user is told the job does
 * @param state   whether it runs, finished, failed
 * @param current how much work is done, in the unit of {@code max}
 * @param max     how much work there is, or 0 when unknown
 * @param message what the job is doing now, may be null
 * @param error   why it failed, when {@code state} is {@link State#FAILED}
 */
public record JobStatus(String id, String title, State state, long current, long max, String message, String error) {

    /** Life cycle of a job. */
    public enum State {
        /** The task is still running. */
        RUNNING,
        /** The task ended without error. */
        DONE,
        /** The task ended with an error. */
        FAILED;

        /** @return whether the job will not change any more */
        public boolean isFinished() {
            return this != RUNNING;
        }
    }
}
