package bgu.spl.net.srv.Messages.ClientToServer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Messages.ServerToClient.ErrorMessage;
import bgu.spl.net.srv.Messages.ServerToClient.RegularMessage;

public class SendMessage implements ClientToServerMessage {

    private String destination;
    private String body;
    private static AtomicInteger messageIdCounter = new AtomicInteger(0);
    
    public SendMessage(String destination, String body) 
    {
        this.destination = destination;
        this.body = body;
    }

    @Override
    public void handle(Boolean shouldTerminate, Integer connectionId, Connections connections) throws IOException
    {
        if (!connections.channelExists(destination)) 
        {
            try
            {
            connections.send(connectionId, new ErrorMessage("Channel does not exist."));
            }
            catch(Exception IGNORE){}
            connections.disconnect(connectionId);
            shouldTerminate = true;
        }
        else
        {
            if(connections.getChannelSubscribersCount(destination) > 0)
            {
                ConcurrentHashMap<Integer, Integer> subscribers = connections.getSubscribers(destination);
                for (Map.Entry<Integer, Integer> entry : subscribers.entrySet())
                {
                    int key = entry.getKey();
                    int value = entry.getValue();
                    int messageId = messageIdCounter.incrementAndGet();
                    RegularMessage message = new RegularMessage(destination, value, messageId, body);
                    connections.send(key, message);
                }

                
                
            }
            else
            {
                connections.send(connectionId, new ErrorMessage("Channel does not have subscribers."));
                connections.disconnect(connectionId);
                shouldTerminate = true;  
            }
        }
    }
}
