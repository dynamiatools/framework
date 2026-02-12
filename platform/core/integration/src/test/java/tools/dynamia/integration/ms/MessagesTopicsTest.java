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
package tools.dynamia.integration.ms;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.integration.ms.listeners.AllErrorTopicMessageListener;
import tools.dynamia.integration.ms.listeners.AllLogMessageListener;
import tools.dynamia.integration.ms.listeners.ErrorWarnLogMessageListener;
import tools.dynamia.integration.ms.listeners.RegexInfoTopicMessageListener;

public class MessagesTopicsTest {

    @Before
    public void init() {
        SimpleObjectContainer soc = new SimpleObjectContainer();
        soc.addObject("ml1", new ErrorWarnLogMessageListener());
        soc.addObject("ml2", new AllLogMessageListener());
        soc.addObject("ml3", new AllErrorTopicMessageListener());
        soc.addObject("ml4", new RegexInfoTopicMessageListener());

        Containers.get().removeAllContainers();
        Containers.get().installObjectContainer(soc);
    }

    @Test
    public void shouldBeAllThreeListener() {

        SimpleMessageChannel channel = new SimpleMessageChannel("logs");

        TextMessage message = new TextMessage("Hello World!!");
        channel.publish(message, "error");

        Assert.assertEquals(3, message.getHeader(Message.HEADER_LISTENER_COUNT));
    }

    @Test
    public void shouldBeTwoListener() {

        SimpleMessageChannel channel = new SimpleMessageChannel("logs");

        TextMessage message = new TextMessage("Hello World!!");
        channel.publish(message, "warning");

        Assert.assertEquals(2, message.getHeader(Message.HEADER_LISTENER_COUNT));
    }

    @Test
    public void shouldBeOneListener() {
        SimpleMessageChannel channel = new SimpleMessageChannel("someChannel");

        TextMessage message = new TextMessage("Something fail");
        channel.publish(message, "error");

        Assert.assertEquals(1, message.getHeader(Message.HEADER_LISTENER_COUNT));
    }

    @Test
    public void shouldBeOneListenerButArraySizeTimes() {
        String[] topics = {"info", "info.system", "someinfo", "main.info.log"};
        SimpleMessageChannel channel = new SimpleMessageChannel("infoChannel");
        int times = 0;
        for (String topic : topics) {
            TextMessage message = new TextMessage("Some info tales");
            channel.publish(message, topic);
            times += (int) message.getHeader(Message.HEADER_LISTENER_COUNT);
        }

        Assert.assertEquals(topics.length, times);
    }
}
