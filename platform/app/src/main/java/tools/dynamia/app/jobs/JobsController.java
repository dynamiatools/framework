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
package tools.dynamia.app.jobs;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.jobs.JobRegistry;
import tools.dynamia.ui.jobs.JobStatus;

/**
 * Lets a remote client follow the background task of a {@code PROGRESS} flow step:
 * {@code GET /api/app/jobs/{id}} answers {@code {id, title, state, current, max, message, error}}. A job of another user or
 * tenant, or one that expired, is {@code 404}.
 */
@RestController
@RequestMapping(JobsController.PATH)
public class JobsController {

    /** Base path of the endpoint. */
    public static final String PATH = "/api/app/jobs";

    private final JobRegistry jobs;

    /**
     * @param jobs where the jobs are
     */
    public JobsController(JobRegistry jobs) {
        this.jobs = jobs;
    }

    /**
     * @param id the job id
     * @return the status of the job, or 404
     */
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<JobStatus> status(@PathVariable("id") String id) {
        return jobs.status(id, FlowPrincipal.current()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
