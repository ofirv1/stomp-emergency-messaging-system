package bgu.spl.net.srv;

public class User
{
    private final String login;

    private final String password;

    private boolean isLogged = false;

    public User(String login, String password)
    {
        this.login = login;
        this.password = password;
    }

    public void loginUser()
    {
        isLogged = true;
    }

    public void logOut()
    {
        isLogged = false;
    }

    public boolean isLogged()
    {
        return isLogged;
    }

    public String getlogin()
    {
        return login;
    }

    public boolean validatePassword(String password)
    {
        if(this.password.equals(password))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
