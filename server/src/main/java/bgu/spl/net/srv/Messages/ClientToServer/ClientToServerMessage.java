package bgu.spl.net.srv.Messages.ClientToServer;
import bgu.spl.net.srv.Messages.*;

import java.io.IOException;

import bgu.spl.net.srv.Connections;

public interface ClientToServerMessage extends Message
{
    public void handle(Boolean shouldTerminate, Integer connectionId, Connections connections) throws IOException;
}