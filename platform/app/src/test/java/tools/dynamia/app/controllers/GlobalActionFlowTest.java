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
package tools.dynamia.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.dynamia.actions.ActionFlowContext;
import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.ApplicationGlobalRemoteAction;
import tools.dynamia.actions.FlowRemoteAction;
import tools.dynamia.app.metadata.ApplicationMetadataLoader;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A global action that asks something must be resumable: the resume token travels in the JSON body.
 */
class GlobalActionFlowTest {

    public static class AskGlobalAction extends ApplicationGlobalRemoteAction implements FlowRemoteAction {

        public AskGlobalAction() {
            setId("askGlobal");
            setName("Ask");
        }

        @Override
        public ActionFlowStep start(ActionFlowContext ctx) {
            return ActionFlowStep.confirm("Sure?");
        }

        @Override
        public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
            return ActionFlowStep.done(Boolean.TRUE.equals(answer) ? "confirmed" : "declined");
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        Containers.get().removeAllContainers();
        var container = new SimpleObjectContainer();
        container.addObject(new AskGlobalAction());
        Containers.get().installObjectContainer(container);
        mvc = MockMvcBuilders.standaloneSetup(new ApplicationMetadataController(new ApplicationMetadataLoader(null, null), null)).build();
    }

    @AfterEach
    void tearDown() {
        Containers.get().removeAllContainers();
    }

    private String send(String body) throws Exception {
        return mvc.perform(post(ApplicationMetadataController.PATH + "/actions/askGlobal")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    @Test
    void theResumeTokenInTheJsonBodyContinuesTheFlow() throws Exception {
        var first = mapper.readTree(send("{}"));
        assertEquals("PENDING", first.get("status").asText());
        var token = first.get("flow").get("resumeToken").asText();
        assertNotNull(token);

        var second = mapper.readTree(send("{\"resumeToken\":\"" + token + "\",\"data\":true}"));

        assertEquals("SUCCESS", second.get("status").asText());
        assertEquals("confirmed", second.get("data").asText());
    }
}
