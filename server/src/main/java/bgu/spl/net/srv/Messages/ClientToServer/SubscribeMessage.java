package bgu.spl.net.srv.Messages.ClientToServer;

import java.io.IOException;

import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Messages.ServerToClient.ErrorMessage;

public class SubscribeMessage implements ClientToServerMessage
{
    private int id;
    private String destination;

    public SubscribeMessage(int id, String destination)
    {
        this.id = id;
        this.destination = destination;
    }


    @Override
    public void handle(Boolean shouldTerminate, Integer connectionId, Connections connections) throws IOException
    {
        if(connections.isConnected(connectionId))
        {
            connections.subscribeToChannel(destination, id, connectionId);
        }
        else
        {
            connections.send(connectionId, new ErrorMessage("User is not connected."));
            connections.disconnect(connectionId);
            shouldTerminate = true;
        }
    }
}
