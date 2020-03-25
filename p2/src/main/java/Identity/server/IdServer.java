package Identity.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class IdServer extends UnicastRemoteObject implements IdServerInterface{

    public String create(String LoginName, String realName, String password) throws RemoteException{
        //TODO: This is dummy. Remove this query from database using the loginName
        // Create UUID
        return "username created successfully";
    }

    public User lookup(String loginName) throws RemoteException{
        //TODO: This is dummy. Remove this query from database using the loginName
        User returnUserFromDatabase = new User();
        return returnUserFromDatabase;
    }

    public User reverseLookUp(String UUID) throws RemoteException{
        //TODO: This is dummy. Remove this query from database using the loginName
        User returnUserFromDatabase = new User();
        return returnUserFromDatabase;
    }

    public String modify(String oldLoginName, String newLoginName, String password) throws RemoteException{
        //TODO: This is dummy. Remove this query from database using the loginName
        return "User modified successfully/no userfound/password didn't match";
    }

    public String delete(String loginName, String password) throws RemoteException{

        //TODO: This is dummy. Remove this query from database using the loginName
        return "User modified successfully/no userfound/password didn't match";
    }

    public List<String> get(String option) throws RemoteException{
        // TODO: This is dummy. Remove this query from database using the loginName
        // Possible options are : UUID | userName | all
        //if options is UUID list will only contain UUID
        //if all list will contain a mapping of loginName and UUID as a string ex: alice -> asdf40230923

        List<String> userFromDatabase = new ArrayList<>();

        return userFromDatabase;
    }


    public String sayHello(String input){
        return "Say Hello from server: " + input;
    }

    public IdServer() throws RemoteException{
        super();
    }

    public static void main(String args[]) throws RemoteException{
        Registry registry = LocateRegistry.createRegistry(5099);
        registry.rebind("idServer", new IdServer());
    }

}