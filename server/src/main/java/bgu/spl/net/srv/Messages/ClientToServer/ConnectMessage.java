package bgu.spl.net.srv.Messages.ClientToServer;

import java.io.IOException;

import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Users;
import bgu.spl.net.srv.Messages.ServerToClient.ConnectedMessage;
import bgu.spl.net.srv.Messages.ServerToClient.ErrorMessage;

public class ConnectMessage implements ClientToServerMessage
{

    private String login;
    private String password;

    public ConnectMessage(String login, String password)
    {
        this.login = login;
        this.password = password;
    }

    @Override
    public void handle(Boolean shouldTerminate, Integer connectionId, Connections connections) throws IOException
    {
        if(!Users.getInstance().isUserExist(login))
        {
            Users.getInstance().createNewUser(login, password);
            connections.send(connectionId, new ConnectedMessage());
        }
        else if(Users.getInstance().isUserLogged(login))
        {
            connections.send(connectionId, new ErrorMessage("User already logged in."));
            connections.disconnect(connectionId);
            shouldTerminate = true;
        }
        else if(Users.getInstance().loginUser(login, password))
        {
            connections.send(connectionId, new ConnectedMessage());
        }
        else
        {
            connections.send(connectionId, new ErrorMessage("Password is incorrect."));
            connections.disconnect(connectionId);
            shouldTerminate = true;
        }
    }
}