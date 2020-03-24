package Identity.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IdServerInterface extends Remote {
    User create(String LoginName, String realName, String password) throws RemoteException;

    User lookup(String loginName) throws RemoteException;

    User reverseLookUp(String UUID) throws RemoteException;

    boolean modify(String oldLoginName, String newLoginName, String password) throws RemoteException;

    List<User> get(String option) throws RemoteException;
}
