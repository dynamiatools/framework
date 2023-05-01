package tools.dynamia.integration.ms;

import org.springframework.transaction.annotation.Transactional;

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
