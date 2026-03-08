package bgu.spl.net.srv;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    private final ConcurrentHashMap<Integer, ConnectionHandler<T>> activeConnections;
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> channelSubscriptions;

    private ConnectionsImpl() {
        this.activeConnections = new ConcurrentHashMap<>();
        this.channelSubscriptions = new ConcurrentHashMap<>();
    }

    private static class SingletonHolder {
        private static final ConnectionsImpl<?> INSTANCE = new ConnectionsImpl<>();
    }

    @SuppressWarnings("unchecked")
    public static <T> ConnectionsImpl<T> getInstance() {
        return (ConnectionsImpl<T>) SingletonHolder.INSTANCE;
    }

    @Override
    public boolean send(int connectionId, T msg) throws IOException 
    {
        ConnectionHandler<T> handler = activeConnections.get(connectionId);
        if (handler != null) 
        {
            handler.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void send(String channel, T msg) throws IOException 
    {
        if (channelSubscriptions.containsKey(channel)) 
        {
            for (Map.Entry<Integer, Integer> entry : channelSubscriptions.get(channel).entrySet()) 
            {
                send(entry.getValue(), msg);
            }
        }
    }

    @Override
    public void disconnect(int connectionId) {
        activeConnections.remove(connectionId);
        channelSubscriptions.values()
                .forEach(subscribers -> subscribers.values().removeIf(id -> id.equals(connectionId)));
    }

    @Override
    public void subscribeToChannel(String channel, int subscriptionId, int connectionId) {
        channelSubscriptions.computeIfAbsent(channel, k -> new ConcurrentHashMap<>()).put(subscriptionId, connectionId);
    }

    @Override
    public void unsubscribeFromChannel(int subscriptionId, int connectionId) {
        channelSubscriptions.forEach((channel, subscribers) -> {
            Integer subscribedConnectionId = subscribers.get(subscriptionId);
            if (subscribedConnectionId != null && subscribedConnectionId.equals(connectionId)) {
                subscribers.remove(subscriptionId);
            }
        });
    }

    @Override
    public boolean isSubscribed(String channel, int subscriptionId, int connectionId) {
        return channelSubscriptions.containsKey(channel)
                && channelSubscriptions.get(channel).getOrDefault(subscriptionId, -1).equals(connectionId);
    }

    @Override
    public boolean channelExists(String channel) {
        return channelSubscriptions.containsKey(channel);
    }

    @Override
    public void removeChannel(String channel) {
        channelSubscriptions.remove(channel);
    }

    @Override
    public boolean isConnected(int connectionId) {
        return activeConnections.containsKey(connectionId);
    }

    @Override
    public int getActiveConnectionsCount() {
        return activeConnections.size();
    }

    @Override
    public int getChannelSubscribersCount(String channel) {
        return channelSubscriptions.getOrDefault(channel, new ConcurrentHashMap<>()).size();
    }

    @Override
    public ConcurrentHashMap<Integer, Integer> getSubscribers(String channel) {
        ConcurrentHashMap<Integer, Integer> subscribers;
        subscribers = channelSubscriptions.get(channel);
        return subscribers;
    }

    public void addConnection(int connectionId, ConnectionHandler<T> handler) {
        activeConnections.put(connectionId, handler);
    }
}
