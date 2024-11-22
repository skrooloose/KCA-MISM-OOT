package common;

public abstract class User {
    String userName;
    String password;

    abstract void login();
    abstract void logout();
    abstract void SignUp();
}


