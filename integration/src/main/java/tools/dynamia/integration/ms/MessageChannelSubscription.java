package tools.dynamia.integration.ms;

/**
 * Represents a subscription to a message channel, allowing for unsubscription from the channel.
 */
public interface MessageChannelSubscription {


    /**
     * Gets the topic associated with this subscription.
     *
     * @return the topic name
     */
    String getTopic();

    /**
     * Gets the name of the message channel.
     *
     * @return the channel name
     */
    String getChannelName();

    /**
     * Gets the unique identifier of the subscriber.
     *
     * @return the subscriber ID
     */
    String getSubscriberId();

    /**
     * Unsubscribes from the message channel.
     */
    void unsubscribe();
}
