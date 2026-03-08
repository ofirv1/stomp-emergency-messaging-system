package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class Users 
{
    private ConcurrentHashMap<String,User> usersDataBase;

    private static Users instance;

    private Users() 
    {
        usersDataBase = new ConcurrentHashMap<>();
    }

    public static Users getInstance() 
    {
        if (instance == null) 
        {
            synchronized (Users.class) 
            {
                if (instance == null) 
                {
                    instance = new Users();
                }
            }
        }
        return instance;
    }

    public boolean isUserLogged(String login)
    {
        return usersDataBase.get(login).isLogged();
    }

    public boolean isUserExist(String login)
    {
        return usersDataBase.containsKey(login);
    }

    public void createNewUser(String login, String password)
    {
        usersDataBase.put(login, new User(login, password));
    }

    public boolean loginUser(String login, String password)
    {
        if(usersDataBase.get(login).validatePassword(password))
        {
            usersDataBase.get(login).loginUser();
            return true; 
        }
        return false;
    }
}
