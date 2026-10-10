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
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.dynamia.integration.Containers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SecurityFlowPrincipalTest {

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        Containers.get().removeAllContainers();
    }

    @Test
    void theSubjectIsTheAuthenticatedUser() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("ana", "n/a", List.of()));

        assertEquals("ana", new SecurityFlowPrincipal().subject());
    }

    @Test
    void noOneOrAnAnonymousTokenIsAnonymous() {
        assertEquals("anonymous", new SecurityFlowPrincipal().subject());

        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "guest", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        assertEquals("anonymous", new SecurityFlowPrincipal().subject());
    }

    @Test
    void withoutTheSaasModuleThereIsNoTenant() {
        assertNull(new SecurityFlowPrincipal().tenant());
    }
}
