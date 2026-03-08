package bgu.spl.net.srv;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Messages.*;
import bgu.spl.net.srv.Messages.ClientToServer.ClientToServerMessage;
import bgu.spl.net.srv.Messages.ServerToClient.ErrorMessage;

public class StompMessagingProtocolImplementation implements StompMessagingProtocol<Message>{

    private Boolean shouldTerminate = false;
    private Integer connectionId;
    private Connections connections;

    @Override
    public void start(int connectionId, Connections connections)
    {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public Message process(Message message) 
    {
        try
        {
            if(message != null)
            {
                if(message instanceof ClientToServerMessage)
                {
                    ((ClientToServerMessage) message).handle(shouldTerminate, connectionId, connections);
                }
                else
                {
                    try
                    {
                    connections.send(connectionId, new ErrorMessage("Oops, Houston we have a problem. Try reconnect later"));
                    }
                    catch(Exception IGNORE){}
                    connections.disconnect(connectionId);
                    shouldTerminate = true;
                }
            }
        }
        catch(Exception e)
        {
            try
            {
            connections.send(connectionId, new ErrorMessage("Oops, Houston we have a problem. Try reconnect later"));
            }
            catch(Exception IGNORE){}
            connections.disconnect(connectionId);
            shouldTerminate = true;
        }
        return null;
    }

    @Override
    public boolean shouldTerminate() 
    {
        return shouldTerminate;
    }
}
