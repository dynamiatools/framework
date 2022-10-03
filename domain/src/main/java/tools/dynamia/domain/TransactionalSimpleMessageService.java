package tools.dynamia.domain;

import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.integration.ms.Message;
import tools.dynamia.integration.ms.SimpleMessageService;

@Transactional
public class TransactionalSimpleMessageService extends SimpleMessageService {

    @Override
    public void publish(String channelName, Message message) {
        super.publish(channelName, message);
    }

    @Override
    public void publish(String channelName, Message message, String topic) {
        super.publish(channelName, message, topic);
    }

    @Override
    public void publish(String channelName, Message message, String topic, String callback) {
        super.publish(channelName, message, topic, callback);
    }

    @Override
    public void broadcast(Message message) {
        super.broadcast(message);
    }

    @Override
    public void broadcast(Message message, String topic) {
        super.broadcast(message, topic);
    }

    @Override
    public void broadcast(Message message, String topic, String callback) {
        super.broadcast(message, topic, callback);
    }
}
