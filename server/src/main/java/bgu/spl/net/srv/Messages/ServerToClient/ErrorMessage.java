package bgu.spl.net.srv.Messages.ServerToClient;

public class ErrorMessage implements ServerToClientMessage
{

    private String ErrorDescription;

    public ErrorMessage(String ErrorDescription)
    {
        this.ErrorDescription = ErrorDescription;
    }

    public String getErrorDescription()
    {
        return ErrorDescription;
    }
}
