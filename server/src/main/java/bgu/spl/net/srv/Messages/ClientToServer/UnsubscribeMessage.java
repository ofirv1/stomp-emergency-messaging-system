package bgu.spl.net.srv.Messages.ClientToServer;

import java.io.IOException;

import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Messages.ServerToClient.ErrorMessage;

public class UnsubscribeMessage implements ClientToServerMessage{

    private int id;

    public UnsubscribeMessage(int id)
    {
        this.id = id;
    }

    @Override
    public void handle(Boolean shouldTerminate, Integer connectionId, Connections connections) throws IOException 
    {
        if(connections.isConnected(connectionId))
        {
            connections.unsubscribeFromChannel(id, connectionId);
        }
        else
        {
            connections.send(connectionId, new ErrorMessage("User is not connected."));
            connections.disconnect(connectionId);
            shouldTerminate = true;
        }
    }

}
