package Identity.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/***
 * Represents the IdServer interface
 */
public interface IdServerInterface extends Remote
{
	// The function to create a new user
    String create(String LoginName, String realName, String password, String ipAddress) throws RemoteException;

    // The function to look for a user using login name
    User lookup(String loginName) throws RemoteException;

    // The function to look for a user using uuid
    User reverseLookUp(String UUID) throws RemoteException;

    // The function to use to modify user info
    String modify(String oldLoginName, String newLoginName, String password) throws RemoteException;

    // The function to use to delete user info
    String delete(String loginName, String password) throws RemoteException;

    // The function to get user list based to the three possible options
    List<String> get(String option) throws RemoteException;
}
