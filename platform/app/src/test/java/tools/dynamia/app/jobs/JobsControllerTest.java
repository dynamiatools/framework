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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.jobs.InMemoryJobRegistry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The client follows a job with plain polling: state, progress and message, and only its owner sees it.
 */
class JobsControllerTest {

    private final InMemoryJobRegistry jobs = new InMemoryJobRegistry();
    private MockMvc mvc;
    private volatile String user = "ana";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new JobsController(jobs)).build();
        var beans = new SimpleObjectContainer("jobs-test");
        beans.addObject((FlowPrincipal) new FlowPrincipal() {
            @Override
            public String subject() {
                return user;
            }

            @Override
            public String tenant() {
                return null;
            }
        });
        Containers.get().installObjectContainer(beans);
    }

    @AfterEach
    void tearDown() {
        Containers.get().removeAllContainers();
    }

    @Test
    void aRunningJobReportsItsProgressAndAFinishedOneItsState() throws Exception {
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        String id = jobs.start("Moving files", monitor -> {
            monitor.setMax(10);
            monitor.setCurrent(4);
            monitor.setMessage("Moving a.txt");
            started.countDown();
            release.await(5, TimeUnit.SECONDS);
        }, FlowPrincipal.current());
        started.await(5, TimeUnit.SECONDS);

        mvc.perform(get("/api/app/jobs/" + id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state", is("RUNNING")))
                .andExpect(jsonPath("$.title", is("Moving files")))
                .andExpect(jsonPath("$.current", is(4)))
                .andExpect(jsonPath("$.max", is(10)))
                .andExpect(jsonPath("$.message", is("Moving a.txt")));

        release.countDown();
        for (int i = 0; i < 100 && !jobs.status(id, FlowPrincipal.current()).orElseThrow().state().isFinished(); i++) {
            Thread.sleep(20);
        }
        mvc.perform(get("/api/app/jobs/" + id)).andExpect(jsonPath("$.state", is("DONE")));
    }

    @Test
    void aFailedJobSaysWhy() throws Exception {
        String id = jobs.start("Failing", monitor -> {
            throw new IllegalStateException("disk full");
        }, FlowPrincipal.current());
        for (int i = 0; i < 100 && !jobs.status(id, FlowPrincipal.current()).orElseThrow().state().isFinished(); i++) {
            Thread.sleep(20);
        }

        mvc.perform(get("/api/app/jobs/" + id))
                .andExpect(jsonPath("$.state", is("FAILED")))
                .andExpect(jsonPath("$.error", is("disk full")));
    }

    @Test
    void anotherUserOrAnUnknownJobIsNotFound() throws Exception {
        String id = jobs.start("Mine", monitor -> {
        }, FlowPrincipal.current());

        user = "luis";
        mvc.perform(get("/api/app/jobs/" + id)).andExpect(status().isNotFound());
        user = "ana";
        mvc.perform(get("/api/app/jobs/nope")).andExpect(status().isNotFound());
    }
}
