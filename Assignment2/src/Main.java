// Defining the DataSource interface
interface DataSource {
    void execute();
}

// Implementing the DataSource interface in Update, View, and Delete classes
class Update implements DataSource {
    @Override
    public void execute() {
        System.out.println("Update operation executed.");
    }
}

class View implements DataSource {
    @Override
    public void execute() {

        System.out.println("View operation executed.");
    }
}

class Delete implements DataSource {
    @Override
    public void execute() {
        System.out.println("Delete operation executed.");
    }
}

// Abstract Account class
abstract class Account {
    protected int id;
    protected String name;
    private DataSource myData;

    public Account(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public void performOperation(DataSource myData) {
        this.myData = myData;
        System.out.println(name + " is performing an operation:");
        myData.execute();
    }
}

// Admin class extending Account
class Admin extends Account {
    private String authPassword;

    public Admin(int id, String name, String authPassword) {
        super(id, name);
        this.authPassword = authPassword;
    }

    public String getAuthPassword() {
        return authPassword;
    }
}

// User class extending Account
class User extends Account {
    public User(int id, String name) {
        super(id, name);
    }
}

// Main class to demonstrate functionality
public class Main {
    public static void main(String[] args) {
        // Creating Admin and User objects
        Admin admin = new Admin(1, "AdminUser", "admin123");
        User user = new User(2, "NormalUser");
        // Creating instances of Update, View, and Delete operations
        DataSource updateOperation = new Update();
        DataSource viewOperation = new View();
        DataSource deleteOperation = new Delete();
        // Performing operations
        admin.performOperation(updateOperation); // Admin performing update
        user.performOperation(viewOperation);   // User performing view
        admin.performOperation(deleteOperation); // Admin performing delete
    }
}