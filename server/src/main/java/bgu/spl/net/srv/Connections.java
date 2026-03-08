package bgu.spl.net.srv;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public interface Connections<T> {
    boolean send(int connectionId, T msg) throws IOException;

    void send(String channel, T msg) throws IOException;

    void disconnect(int connectionId);

    void subscribeToChannel(String channel, int subscriptionId, int connectionId);

    void unsubscribeFromChannel(int subscriptionId, int connectionId);

    boolean isSubscribed(String channel, int subscriptionId, int connectionId);

    boolean channelExists(String channel);

    void removeChannel(String channel);

    boolean isConnected(int connectionId);

    int getActiveConnectionsCount();

    int getChannelSubscribersCount(String channel);

    ConcurrentHashMap<Integer, Integer> getSubscribers(String channel);
}
