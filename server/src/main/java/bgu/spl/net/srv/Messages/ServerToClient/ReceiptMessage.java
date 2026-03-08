package bgu.spl.net.srv.Messages.ServerToClient;

public class ReceiptMessage implements ServerToClientMessage
{
    private final Integer receiptId;

    public ReceiptMessage(int receiptId)
    {
        this.receiptId = receiptId;
    }

    public int getReceiptId()
    {
        return receiptId;
    }
}
