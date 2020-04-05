package Identity.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Option;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * Represents the IdServer
 */
public class IdServer implements IdServerInterface{
	
	private Database db = null;
	private int ServerPort = 5099;
	private boolean isVerbose = false;
	private Logger log;

	/**
	   * creates a new user.
	   * @param LoginName - the login name of the user.
	   * @param realName - the real name of the user.
	   * @param password - the password hash of the user.
	   * @param ipAddress - the creation ip address of the user.
	   * @return A string explaining the result of the user creation operation.
	   * @exception RemoteException
	   * @see RemoteException
	   */
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

    /**
	   * looks for a user using login name in the database.
	   * @param LoginName - the login name of the user.
	   * @return User data.
	   * @exception RemoteException
	   * @see RemoteException
	   */
    public User lookup(String loginName) throws RemoteException
    {
    	if(db == null)
    	{
    		db = new Database(log);
    	}
        User result = db.Search(Constants.loginName, loginName);
        return result;
    }

    /**
	   * looks for a user using uuid in the database.
	   * @param UUID - the uuid assigned to the user.
	   * @return User data.
	   * @exception RemoteException
	   * @see RemoteException
	   */
    public User reverseLookUp(String UUID) throws RemoteException
    {
    	if(db == null)
    	{
    		db = new Database(log);
    	}
        User result = db.Search(Constants.uuid, UUID);
        return result;
    }

    /**
	   * modifies the login name of a user.
	   * @param oldLoginName - the old login name of the user.
	   * @param newLoginName - the new login name of the user.
	   * @param password - the password hash of the user.
	   * @return A string explaining the result of the user modification operation.
	   * @exception RemoteException
	   * @see RemoteException
	   */
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

    /**
	   * deletes the user information.
	   * @param loginName - the login name of the user.
	   * @param password - the password hash of the user.
	   * @return A string explaining the result of the user deletion operation.
	   * @exception RemoteException
	   * @see RemoteException
	   */
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

    /**
	   * retrieves user lists.
	   * @param option - one of the three options types for list retrieval.
	   * @return user lists.
	   * @exception RemoteException
	   * @see RemoteException
	   */
    public List<String> get(String option) throws RemoteException
    {
    	if(db == null)
    	{
    		db = new Database(log);
    	}
    	
        List<String> result = db.GetList(option);

        return result;
    }

    public IdServer(String[] args) throws RemoteException{
        super();
		log = Logger.getLogger(IdServer.class.getName());

		if(db == null)
    	{
    		db = new Database(log);
    	}
		
		extractOption(makeOption(), args);
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
			if(cmd.hasOption("numport")){
				ServerPort = Integer.parseInt(cmd.getOptionValue("numport"));
			}
			isVerbose = cmd.hasOption("verbose");
			log.setLevel(isVerbose? Level.ALL : Level.OFF);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	   * binds the server.
	   * @return nothing.
	   * @exception IOException On input error.
	   * @see IOException
	   */
	public void bind() {
		try {
			log.info("Binding server on port " + getServerPort());
			RMIClientSocketFactory csf = new SslRMIClientSocketFactory();
			RMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
			IdServerInterface server = (IdServerInterface) UnicastRemoteObject.exportObject(this, 0, csf,
					ssf);

			Registry registry = LocateRegistry.createRegistry(getServerPort());
			registry.rebind("IdServer", server);
			log.info("Server binding successfull");
			log.info("server is up and running on port " + getServerPort());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception occurred: " + e);
		}
	}

	/**
	   * get the server port.
	   * @return server port.
	   */
	public int getServerPort(){
    	return ServerPort;
	}

    public static void main(String args[]) throws RemoteException{
		System.setProperty("javax.net.ssl.keyStore", "security/Server_Keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "test123");
		System.setProperty("java.security.policy", "security/mysecurity.policy");


		IdServer server = new IdServer(args);
		server.bind();
		
		//Adding shut down hook
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				server.HandleShutDown();
				server.log.info("Shutting down ...");
			}
		});
    }
    
    private void HandleShutDown()
    {
    	if(db == null)
    	{
    		db = new Database(log);
    	}
    	
    	this.log.info("Saving all to disk ...");
    	db.SaveAllToDisk();
    	this.log.info("Closing db connection ...");
    	db.CloseDB();
    }
}