/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package tools.dynamia.modules.saas.migration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.dynamia.modules.saas.migration.api.CancellationToken;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CancellationTokenTest {

    @Test
    public void newTokenIsNotCancelled() {
        CancellationToken token = CancellationToken.active();
        Assertions.assertFalse(token.isCancelled());
        Assertions.assertNull(token.getReason());
    }

    @Test
    public void cancelWithoutReasonUsesDefault() {
        CancellationToken token = CancellationToken.active();
        token.cancel();

        Assertions.assertTrue(token.isCancelled());
        Assertions.assertNotNull(token.getReason());
    }

    @Test
    public void cancelWithReasonStoresReason() {
        CancellationToken token = CancellationToken.active();
        token.cancel("Timeout exceeded");

        Assertions.assertTrue(token.isCancelled());
        Assertions.assertEquals("Timeout exceeded", token.getReason());
    }

    @Test
    public void cancelIsIdempotent() {
        CancellationToken token = CancellationToken.active();
        token.cancel("first");
        token.cancel("second");

        Assertions.assertTrue(token.isCancelled());
        Assertions.assertEquals("second", token.getReason());
    }

    @Test
    public void cancelFromOtherThreadIsVisibleImmediately() throws InterruptedException {
        CancellationToken token = CancellationToken.active();
        AtomicBoolean seen = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        Thread t = Thread.ofVirtual().start(() -> {
            token.cancel("from-thread");
            latch.countDown();
        });

        latch.await(2, TimeUnit.SECONDS);
        seen.set(token.isCancelled());
        t.join(1000);

        Assertions.assertTrue(seen.get(), "Cancel from another thread must be visible");
    }
}
