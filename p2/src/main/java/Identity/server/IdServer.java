package Identity.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class IdServer extends UnicastRemoteObject implements IdServerInterface{
	
	private Database db = null;

    public String create(String LoginName, String realName, String password, String ipAddress) throws RemoteException{
    	if(db == null)
    	{
    		db = new Database();
    	}
    	
    	boolean insertionResult = false;
   		UUID randomUUID = UUID.randomUUID();
   		String uuid = randomUUID.toString();
   		LocalDate dateNow = LocalDate.now();
   		String dateNowString = dateNow.toString();
   		LocalTime timeNow = LocalTime.now();
   		String timeNowString = timeNow.toString();
   		
   		//String loginName, String uuid, String password, String ipAddress, String date, String time, String realUserName, String lastChangeDate
    	insertionResult = db.Insert(LoginName, uuid, password, ipAddress, dateNowString, timeNowString, realName, dateNowString);
    	
    	if(insertionResult)
    	{
    		return Constants.success;
    	}
    	return Constants.failure;
    }

    public User lookup(String loginName) throws RemoteException
    {    	
        User result = db.Search(Constants.loginName, loginName);
        return result;
    }

    public User reverseLookUp(String UUID) throws RemoteException
    {
        User result = db.Search(Constants.uuid, UUID);
        return result;
    }

    public String modify(String oldLoginName, String newLoginName, String password) throws RemoteException{
        
    	boolean result = db.Update(Constants.loginName, oldLoginName, Constants.loginName, newLoginName);
    	
    	if(!result)
    	{
    		return Constants.failure;
    	}
    	
    	result = db.Update(Constants.loginName, newLoginName, Constants.password, password);
    	
    	if(!result)
    	{
    		return Constants.failure;
    	}
    	
    	LocalDate dateNow = LocalDate.now();
   		String dateNowString = dateNow.toString();
    	result = db.Update(Constants.loginName, newLoginName, Constants.lastChangeDate, dateNowString);
    	
    	if(!result)
    	{
    		return Constants.failure;
    	}
    	
    	return Constants.success;
    }

    public String delete(String loginName, String password) throws RemoteException
    {
    	boolean result = db.Delete(Constants.loginName, loginName);
    	
    	if(!result)
    	{
    		return Constants.failure;
    	}
    	
    	return Constants.success;
    }

    public List<String> get(String option) throws RemoteException
    {
        List<String> result = db.GetList(option);

        return result;
    }

    public String sayHello(String input){
        return "Say Hello from server: " + input;
    }

    public IdServer() throws RemoteException{
        super();
    }

    public void bind(){

    }

    public static void main(String args[]) throws RemoteException{
        Registry registry = LocateRegistry.createRegistry(5099);
        registry.rebind("idServer", new IdServer());
    }

}