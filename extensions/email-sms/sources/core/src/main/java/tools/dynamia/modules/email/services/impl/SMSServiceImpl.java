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

package tools.dynamia.modules.email.services.impl;


import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import tools.dynamia.domain.services.AbstractService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.email.SMSMessage;
import tools.dynamia.modules.email.SMSServiceListener;
import tools.dynamia.modules.email.domain.SMSMessageLog;
import tools.dynamia.modules.email.services.SMSService;

import java.util.HashMap;
import java.util.Map;

@Service
public class SMSServiceImpl extends AbstractService implements SMSService {

    @Override
    public String send(SMSMessage message) {
        validate(message);

        try (var snsClient = buildSNSClient(message)) {


            Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
            if (message.isTransactional()) {
                smsAttributes.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                        .stringValue("Transactional")
                        .dataType("String").build());
            }

            if (message.getSenderID() != null && !message.getSenderID().isEmpty()) {
                smsAttributes.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
                        .stringValue(message.getSenderID())
                        .dataType("String").build());
            }


            log("Sending SMS to " + message.getPhoneNumber());
            fireSendingListener(message);
            try {
                var request = PublishRequest.builder()
                        .message(message.getText())
                        .phoneNumber(message.getPhoneNumber())
                        .messageAttributes(smsAttributes)
                        .build();

                var result = snsClient.publish(request);

                log("SMS Result: " + result);
                log("Creating log for sms message " + message.getPhoneNumber());
                new SMSMessageLog(message.getPhoneNumber(),
                        message.getText(), result.messageId(),
                        message.getAccountId())
                        .save();


                message.setResult(result.messageId());
                message.setMessageId(result.messageId());
                message.setSended(true);
                log("SMS Sended - " + message.getPhoneNumber() + "  message id: " + message.getResult());
                fireSendedListener(message);
                return message.getResult();
            } catch (Exception ex) {
                message.setResult(ex.getMessage());
                log("Error sending sms to " + message.getPhoneNumber() + ": " + ex.getMessage(), ex);
                return null;
            }
        }
    }

    private SnsClient buildSNSClient(SMSMessage message) {
        var credentials = AwsBasicCredentials.create(message.getUsername(), message.getPassword());
        return SnsClient.builder()
                .credentialsProvider(() -> credentials)
                .region(Region.of(message.getRegion()))
                .build();
    }

    private void fireSendingListener(SMSMessage smsMessage) {
        Containers.get().findObjects(SMSServiceListener.class).forEach(l -> l.onMessageSending(smsMessage));
    }


    private void fireSendedListener(SMSMessage smsMessage) {
        Containers.get().findObjects(SMSServiceListener.class).forEach(l -> l.onMessageSended(smsMessage));
    }
}
