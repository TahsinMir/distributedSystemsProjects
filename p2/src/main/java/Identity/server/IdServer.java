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

import Identity.client.IdClient;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Option;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdServer extends UnicastRemoteObject implements IdServerInterface{
	
	private Database db = null;
	private int ServerPort = 5099;
	private boolean isVerbose = false;
	private Logger log;

    public String create(String LoginName, String realName, String password, String ipAddress) throws RemoteException
    {
    	if(db == null)
    	{
    		db = new Database(log);
    	}
    	
    	String insertionResult;
   		UUID randomUUID = UUID.randomUUID();
   		String uuid = randomUUID.toString();
   		LocalDate dateNow = LocalDate.now();
   		String dateNowString = dateNow.toString();
   		LocalTime timeNow = LocalTime.now();
   		String timeNowString = timeNow.toString();
   		
   		//String loginName, String uuid, String password, String ipAddress, String date, String time, String realUserName, String lastChangeDate
    	insertionResult = db.Insert(LoginName, uuid, password, ipAddress, dateNowString, timeNowString, realName, dateNowString);
    	
    	return insertionResult;
    }

    public User lookup(String loginName) throws RemoteException
    {
    	if(db == null)
    	{
    		db = new Database(log);
    	}
        User result = db.Search(Constants.loginName, loginName);
        return result;
    }

    public User reverseLookUp(String UUID) throws RemoteException
    {
    	if(db == null)
    	{
    		db = new Database(log);
    	}
        User result = db.Search(Constants.uuid, UUID);
        return result;
    }

    public String modify(String oldLoginName, String newLoginName, String password) throws RemoteException
    {
    	if(db == null)
    	{
    		db = new Database(log);
    	}
        
    	if(!db.CheckPassword(Constants.loginName, oldLoginName, password))
    	{
    		return Constants.failure + Constants.colon + Constants.wrongPassword;
    	}
    	
    	String resultUpdate = db.Update(Constants.loginName, oldLoginName, Constants.loginName, newLoginName);
    	
    	if(resultUpdate.startsWith(Constants.failure))
    	{
    		return resultUpdate;
    	}
    	
    	LocalDate dateNow = LocalDate.now();
   		String dateNowString = dateNow.toString();
    	String resultUpdateLastChange = db.Update(Constants.loginName, newLoginName, Constants.lastChangeDate, dateNowString);
    	
    	return resultUpdateLastChange;
    }

    public String delete(String loginName, String password) throws RemoteException
    {
		if(db == null)
    	{
    		db = new Database(log);
    	}
    	
    	if(!db.CheckPassword(Constants.loginName, loginName, password))
    	{
    		return Constants.failure + Constants.colon + Constants.wrongPassword;
    	}
    	
    	String result = db.Delete(Constants.loginName, loginName);
    	
    	return result;
    }

    public List<String> get(String option) throws RemoteException
    {
    	if(db == null)
    	{
    		db = new Database(log);
    	}
    	
        List<String> result = db.GetList(option);

        return result;
    }

    public String sayHello(String input){
        return "Say Hello from server: " + input;
    }

    public IdServer() throws RemoteException{
        super();
		log = Logger.getLogger(IdClient.class.getName());
    }

	public static Options makeOption(){
		Options options = new Options();
		//Adding the command line options
		options.addOption("v", "verbose", false, "Print the details");
		options.addOption("n", "numport", true, "The port on which server will run");

		return options;
	}
	public void extractOption(Options options, String[] args){
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			ServerPort = Integer.parseInt(cmd.getOptionValue("numport"));
			isVerbose = cmd.hasOption("verbose");
			log.setLevel(isVerbose? Level.ALL : Level.OFF);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public int getServerPort(){
    	return ServerPort;
	}

    public static void main(String args[]) throws RemoteException{
		Options options = makeOption();
		IdServer server = new IdServer();
		server.extractOption(options, args);

        Registry registry = LocateRegistry.createRegistry(server.getServerPort());
        registry.rebind("idServer", server);
    }
}