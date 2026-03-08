package bgu.spl.net.srv.Messages.ClientToServer;

import java.io.IOException;

import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Messages.ServerToClient.ReceiptMessage;

public class DisconnectMessage implements ClientToServerMessage
{
    private final Integer receiptId;

    public DisconnectMessage(int receiptId)
    {
        this.receiptId = receiptId;
    }

    @Override
    public void handle(Boolean shouldTerminate, Integer connectionId, Connections connections) throws IOException
    {
        shouldTerminate = true;
        connections.send(connectionId, new ReceiptMessage(receiptId));
        connections.disconnect(connectionId);
    }

}
