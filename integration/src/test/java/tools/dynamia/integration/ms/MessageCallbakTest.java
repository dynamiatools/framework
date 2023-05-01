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
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import java.util.HashMap;
import java.util.Map;

public class MessageCallbakTest {

    private static final String CALLBACK_CHANNEL = "calcClient";
    private static final String HEADER_EXPECTED_RESULT = "expectedResult";

    @Before
    public void init() {
        SimpleObjectContainer soc = new SimpleObjectContainer();
        ResultMessageListener resultListener = new ResultMessageListener();
        MessageService service = new SimpleMessageService();

        soc.addObject("ml1", resultListener);
        soc.addObject("ml2", new CalculatorServiceListener(service));
        soc.addObject("service", service);

        Containers.get().removeAllContainers();
        Containers.get().installObjectContainer(soc);
    }

    @Test
    public void shouldSum() {
        MessageService service = Containers.get().findObject(MessageService.class);
        ResultMessageListener resultML = Containers.get().findObject(ResultMessageListener.class);

        Message request = new MapMessage(MapBuilder.put("A", 2, "B", 5, "OP", "SUM"));
        request.addHeader(HEADER_EXPECTED_RESULT, 7);

        String correlationId = StringUtils.randomString();
        request.addHeader(Message.HEADER_CORRELATION_ID, correlationId);

        resultML.messagesWithoutResult.put(correlationId, request);
        service.publish("calculator", request, "", CALLBACK_CHANNEL);
    }

    @Test
    public void shouldSubstract() {
        MessageService service = Containers.get().findObject(MessageService.class);
        ResultMessageListener resultML = Containers.get().findObject(ResultMessageListener.class);

        Message request = new MapMessage(MapBuilder.put("A", 2, "B", 5, "OP", "SUB"));
        request.addHeader(HEADER_EXPECTED_RESULT, 2 - 5);

        String correlationId = StringUtils.randomString();
        request.addHeader(Message.HEADER_CORRELATION_ID, correlationId);

        resultML.messagesWithoutResult.put(correlationId, request);
        service.publish("calculator", request, "", CALLBACK_CHANNEL);
    }

    @Test
    public void shouldMultiply() {
        MessageService service = Containers.get().findObject(MessageService.class);
        ResultMessageListener resultML = Containers.get().findObject(ResultMessageListener.class);

        Message request = new MapMessage(MapBuilder.put("A", 2, "B", 5, "OP", "MUL"));
        request.addHeader(HEADER_EXPECTED_RESULT, 2 * 5);

        String correlationId = StringUtils.randomString();
        request.addHeader(Message.HEADER_CORRELATION_ID, correlationId);

        resultML.messagesWithoutResult.put(correlationId, request);
        service.publish("calculator", request, "", CALLBACK_CHANNEL);
    }

    @Test
    public void shouldDivide() {
        MessageService service = Containers.get().findObject(MessageService.class);
        ResultMessageListener resultML = Containers.get().findObject(ResultMessageListener.class);

        Message request = new MapMessage(MapBuilder.put("A", 10, "B", 2, "OP", "DIV"));
        request.addHeader(HEADER_EXPECTED_RESULT, 10 / 2);

        String correlationId = StringUtils.randomString();
        request.addHeader(Message.HEADER_CORRELATION_ID, correlationId);

        resultML.messagesWithoutResult.put(correlationId, request);
        service.publish("calculator", request, "", CALLBACK_CHANNEL);
    }

    @MessageChannelExchange(channel = CALLBACK_CHANNEL)
    static
    class ResultMessageListener implements MessageListener<NumberMessage> {

        final Map<String, Message> messagesWithoutResult = new HashMap<>();

        @Override
        public void onMessage(MessageEvent<NumberMessage> evt) {
            String correlationId = (String) evt.message().getHeader(Message.HEADER_CORRELATION_ID);
            Message message = messagesWithoutResult.get(correlationId);
            if (message != null) {
                int expectedResult = (int) message.getHeader(HEADER_EXPECTED_RESULT);
                int result = (int) evt.message().getContent();

                String description = (String) evt.message().getHeader(Message.HEADER_DESCRIPTION);
                Assert.assertEquals(expectedResult, result);
                messagesWithoutResult.remove(correlationId);
            }
        }
    }

    @MessageChannelExchange(channel = "calculator")
    static
    class CalculatorServiceListener implements MessageListener<MapMessage> {

        private final MessageService service;

        public CalculatorServiceListener(MessageService service) {
            super();
            this.service = service;
        }

        @Override
        public void onMessage(MessageEvent<MapMessage> evt) {
            Map<String, Object> content = evt.message().getContent();

            try {
                int numberA = (int) content.get("A");
                int numberB = (int) content.get("B");
                int result = 0;
                String operation = (String) content.get("OP");

                switch (operation) {
                    case "SUM":
                        result = numberA + numberB;
                        break;
                    case "SUB":
                        result = numberA - numberB;
                        break;
                    case "MUL":
                        result = numberA * numberB;
                        break;
                    case "DIV":
                        result = numberA / numberB;
                    default:
                        break;
                }

                String description = String.format("Calculator Operation:  %s %s %s = %s", numberA, operation, numberB, result);

                NumberMessage resultMessage = new NumberMessage(result);
                resultMessage.addHeader(Message.HEADER_DESCRIPTION, description);
                resultMessage.addHeader(Message.HEADER_CORRELATION_ID, (String) evt.message().getHeader(Message.HEADER_CORRELATION_ID));

                service.publish(evt.callback(), resultMessage);

            } catch (Exception e) {
                // ignore
            }

        }
    }
}
