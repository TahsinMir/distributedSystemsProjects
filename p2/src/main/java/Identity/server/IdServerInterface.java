package Identity.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IdServerInterface extends Remote {
    String create(String LoginName, String realName, String password, String ipAddress) throws RemoteException;

    User lookup(String loginName) throws RemoteException;

    User reverseLookUp(String UUID) throws RemoteException;

    String modify(String oldLoginName, String newLoginName, String password) throws RemoteException;

    String delete(String loginName, String password) throws RemoteException;

    List<String> get(String option) throws RemoteException;

    //This was done for test
    String sayHello(String input) throws RemoteException;
}
