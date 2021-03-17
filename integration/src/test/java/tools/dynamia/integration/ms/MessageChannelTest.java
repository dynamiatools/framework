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
package tools.dynamia.integration.ms;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.integration.ms.listeners.AllMessageListener;
import tools.dynamia.integration.ms.listeners.DummyMessageListener;
import tools.dynamia.integration.ms.listeners.ThrowExceptionMessageListener;

public class MessageChannelTest {

    @Before
    public void init() {
        SimpleObjectContainer soc = new SimpleObjectContainer();
        soc.addObject("ml1", new DummyMessageListener());
        soc.addObject("ml2", new AllMessageListener());
        soc.addObject("ml3", new ThrowExceptionMessageListener());

        Containers.get().removeAllContainers();
        Containers.get().installObjectContainer(soc);
    }

    @Test
    public void sendSimpleMessage() {
        TextMessage msg = new TextMessage("HOLAAA MUNDOO!");
        SimpleMessageChannel channel = new SimpleMessageChannel("dummyChannel");
        channel.publish(msg);

        int listenerCount = (Integer) msg.getHeader(Message.HEADER_LISTENER_COUNT);
        Assert.assertEquals(2, listenerCount);
    }

    @Test
    public void sendSimpleMessageToOtherChannel() {
        TextMessage msg = new TextMessage("HOLAAA MUNDOO!");
        SimpleMessageChannel channel = new SimpleMessageChannel("otheRareChannel");
        channel.publish(msg);

        int listenerCount = (Integer) msg.getHeader(Message.HEADER_LISTENER_COUNT);
        Assert.assertEquals(1, listenerCount);

    }

    @Test
    public void shouldCaptureAnExceptionAndBeReceivedByOneListenerOnly() {
        TextMessage msg = new TextMessage("HOLAAA MUNDOO!");
        SimpleMessageChannel channel = new SimpleMessageChannel("someChannel");
        channel.publish(msg);

        int listenerCount = (Integer) msg.getHeader(Message.HEADER_LISTENER_COUNT);

        Assert.assertEquals(1, listenerCount);
    }
}
