package tools.dynamia.integration.ms;

/**
 * An abstract implementation of the MessageChannelSubscription interface,
 * providing common functionality for message channel subscriptions.
 */
public class BaseMessageChannelSubscription<T extends Message> implements MessageChannelSubscription {

    private final String channelName;
    private final String topic;
    private final String subscriberId;
    private final MessageListener<T> listener;

    public BaseMessageChannelSubscription(String channelNamme, String topic, String subscriberId, MessageListener<T> listener) {
        this.channelName = channelNamme;
        this.topic = topic != null ? topic : MessageChannels.ALL_TOPICS;
        this.subscriberId = subscriberId;
        this.listener = listener;
    }

    @Override
    public String getChannelName() {
        return channelName;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getSubscriberId() {
        return subscriberId;
    }

    @Override
    public void unsubscribe() {
        // Default implementation does nothing. Subclasses should override this method to provide
        // specific unsubscription logic.
    }

    public MessageListener<T> getListener() {
        return listener;
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
