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
package tools.dynamia.modules.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.integration.scheduling.SchedulerUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * An async task acts as the user that started it.
 */
class SecurityContextCapturerTest {

    @BeforeEach
    void install() {
        var beans = new SimpleObjectContainer("security-capturer-test");
        beans.addObject(new SecurityContextCapturer());
        Containers.get().installObjectContainer(beans);
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        Containers.get().removeAllContainers();
    }

    private static String userInTask() throws Exception {
        return SchedulerUtil.runWithResult(() -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return auth == null ? null : auth.getName();
        }).get(5, TimeUnit.SECONDS);
    }

    @Test
    void theTaskSeesTheAuthenticatedUserOfTheCaller() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("ana", "n/a", List.of()));

        assertEquals("ana", userInTask());
    }

    @Test
    void anAnonymousCallerStartsAnAnonymousTask() throws Exception {
        assertNull(userInTask());
    }

    @Test
    void theContextOfThePooledThreadIsRestoredWhenTheTaskEnds() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("ana", "n/a", List.of()));
        assertEquals("ana", userInTask());

        SecurityContextHolder.clearContext();

        assertNull(userInTask());
    }
}
