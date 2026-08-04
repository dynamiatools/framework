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
package tools.dynamia.actions.flow;

import org.junit.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class FlowTokenSignerTest {

    private final FlowTokenSigner signer = new FlowTokenSigner(null);

    private FlowTokenPayload freshPayload(String actionId, long expiresAt) {
        return new FlowTokenPayload("flow-1", actionId, "CONFIRM", Map.of("bookId", "42"), expiresAt);
    }

    @Test
    public void signAndVerifyRoundTripsThePayload() {
        FlowTokenPayload payload = freshPayload("archiveBook", futureExpiry());
        String token = signer.sign(payload);

        FlowTokenPayload verified = signer.verify(token, "archiveBook");

        assertEquals("flow-1", verified.flowId());
        assertEquals("archiveBook", verified.actionId());
        assertEquals("CONFIRM", verified.step());
        assertEquals("42", verified.data().get("bookId"));
    }

    @Test
    public void rejectsATokenSignedWithADifferentKey() {
        FlowTokenSigner otherSigner = new FlowTokenSigner(null);
        String token = otherSigner.sign(freshPayload("archiveBook", futureExpiry()));

        assertThrows(FlowTokenException.class, () -> signer.verify(token, "archiveBook"));
    }

    @Test
    public void rejectsATokenWithATamperedPayload() {
        String token = signer.sign(freshPayload("archiveBook", futureExpiry()));
        String[] parts = token.split("\\.", 2);

        // Flip the payload segment (e.g. as if a client tried to smuggle a different bookId/actionId)
        // while keeping the original signature — must not verify.
        String tamperedPayload = parts[0] + "AA";
        String tamperedToken = tamperedPayload + "." + parts[1];

        assertThrows(FlowTokenException.class, () -> signer.verify(tamperedToken, "archiveBook"));
    }

    @Test
    public void rejectsATamperedSignature() {
        String token = signer.sign(freshPayload("archiveBook", futureExpiry()));
        String[] parts = token.split("\\.", 2);
        String tamperedToken = parts[0] + "." + parts[1] + "AA";

        assertThrows(FlowTokenException.class, () -> signer.verify(tamperedToken, "archiveBook"));
    }

    @Test
    public void rejectsAnExpiredToken() {
        String token = signer.sign(freshPayload("archiveBook", Instant.now().minusSeconds(1).toEpochMilli()));

        assertThrows(FlowTokenException.class, () -> signer.verify(token, "archiveBook"));
    }

    @Test
    public void rejectsATokenIssuedForADifferentAction() {
        String token = signer.sign(freshPayload("archiveBook", futureExpiry()));

        assertThrows(FlowTokenException.class, () -> signer.verify(token, "deleteBook"));
    }

    @Test
    public void rejectsAMalformedToken() {
        assertThrows(FlowTokenException.class, () -> signer.verify("not-a-real-token", "archiveBook"));
        assertThrows(FlowTokenException.class, () -> signer.verify(null, "archiveBook"));
    }

    @Test
    public void defaultTtlIsTenMinutes() {
        assertTrue(new FlowTokenSigner(null).ttl().toMinutes() == 10);
    }

    private long futureExpiry() {
        return Instant.now().plusSeconds(60).toEpochMilli();
    }
}
