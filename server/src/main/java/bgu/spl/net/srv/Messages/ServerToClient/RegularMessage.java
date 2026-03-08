package bgu.spl.net.srv.Messages.ServerToClient;

public class RegularMessage implements ServerToClientMessage {
    private final String destination;
    private final int subscriptionId;
    private final int messageId;
    private final String body;

    public RegularMessage(String destination, int subscriptionId, int messageId, String body) 
    {
        this.destination = destination;
        this.subscriptionId = subscriptionId;
        this.messageId = messageId;
        this.body = body;
    }

    public String getDestination() 
    {
        return destination;
    }

    public int getSubscriptionId() 
    {
        return subscriptionId;
    }

    public int getMessageId() 
    {
        return messageId;
    }

    public String getBody() 
    {
        return body;
    }
}
