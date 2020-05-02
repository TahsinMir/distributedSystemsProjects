package Identity.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
	private UUID serverUUID;
	private UUID coordinatorUUID;
	/*private boolean isCoordinator;
	private boolean isCoordinatorElected;*/
	private CommunicationMode serverCommunicationMode;
	private int lamportTime = 0;	//TODO: set to 0 initially for testing
	private int electionCounter;

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
    	insertionResult = db.Insert(LoginName, uuid, password, ipAddress, dateNowString, timeNowString, realName, dateNowString, lamportTime++);
    	
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
    	
    	String resultUpdate = db.Update(Constants.loginName, oldLoginName, Constants.loginName, newLoginName, lamportTime++);
    	
    	if(resultUpdate.startsWith(Constants.failure))
    	{
    		return resultUpdate;
    	}
    	
    	LocalDate dateNow = LocalDate.now();
   		String dateNowString = dateNow.toString();
    	String resultUpdateLastChange = db.Update(Constants.loginName, newLoginName, Constants.lastChangeDate, dateNowString, lamportTime++);
    	
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
		
		this.serverUUID = UUID.randomUUID();
		/*this.isCoordinator = false;
		this.isCoordinatorElected = false;*/
		this.coordinatorUUID = null;
		this.serverCommunicationMode = CommunicationMode.ELECTION_REQUIRED;
		this.electionCounter = 0;
		
		System.out.println("my id: " + this.serverUUID.toString());
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
		
		Thread t = new Thread(new CheckServersThread(5176, "230.230.246.1", server.GetServerUUID(), server));
		t.start();
		
		Thread t2 = new Thread(new SendStatusToOtherServersThread(5176, "230.230.246.1", server.GetServerUUID(), server));
		t2.start();
		
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
    
    public UUID GetServerUUID()
    {
    	return this.serverUUID;
    }
    
    public UUID GetCoordinatorUUID()
    {
    	return this.coordinatorUUID;
    }
    public void SetCoordinatorUUID(UUID coordinatorUUID)
    {
    	this.coordinatorUUID = coordinatorUUID;
    }
    
    /*public boolean GetIsCoordinator()
    {
    	return this.isCoordinator;
    }
    public void SetIsCoordinator(boolean isCoordinator)
    {
    	this.isCoordinator = isCoordinator;
    }
    
    public boolean GetIsCoordinatorElected()
    {
    	return this.isCoordinatorElected;
    }
    public void SetIsCoordinatorElected(boolean isCoordinatorElected)
    {
    	this.isCoordinatorElected = isCoordinatorElected;
    }*/
    
    public CommunicationMode GetCommunicationMode()
    {
    	return this.serverCommunicationMode;
    }
    public void SetCommunicationMode(CommunicationMode communicationMode)
    {
    	this.serverCommunicationMode = communicationMode;
    }
    
    public int GetElectionCounter()
    {
    	return this.electionCounter;
    }
    public void SetElectionCounter(int electionCounter)
    {
    	this.electionCounter = electionCounter;
    }
}

class CheckServersThread implements Runnable
{
	private int port;
	private MulticastSocket socket;
	private InetAddress group;
	private int MAX_LEN = 1000;
	private UUID serverUUID;
	private IdServer idServer;
	private Timer timer = null;
	private TimerTask timerTask = null;
	private LocalDateTime LastTimeCoordinatorResponded;
	
	CheckServersThread(int port, String group, UUID serverUUID, IdServer idServer)
	{
		this.port = port;
		try
		{
			this.group = InetAddress.getByName(group);
		}
		catch (UnknownHostException e)
		{
			System.out.println("UnknownHostException during parsing group address");
			e.printStackTrace();
		}
		
		this.createMulticastConenction();
		
		this.serverUUID = serverUUID;
		this.idServer = idServer;
		this.LastTimeCoordinatorResponded = null;
	}
	
	private void createMulticastConenction()
	{
		try
		{
			socket = new MulticastSocket(port);
			socket.setTimeToLive(5);
			socket.joinGroup(group);
		}
		catch (IOException e)
		{
			System.out.println("IOException during socket intialization");
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		byte[] buffer = new byte[this.MAX_LEN];
		DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, this.group, this.port);
		String message;
		
		while(true)
		{
			try
			{
				socket.receive(datagram);
				
				message = new String(buffer, 0, datagram.getLength(), "UTF-8");
				System.out.println("receiving message: " + message);
				
				String[] splitted = message.split("\\s+");
				
				String command = splitted[0];
				String tempCoordUUID = splitted[1];
				
				if(command.equals(Constants.doElection))
				{
					//
					//
					this.LastTimeCoordinatorResponded = LocalDateTime.now();
					if(timer != null)
					{
						timer = null;
						timerTask = null;
					}
					
					timer = new Timer();
					timerTask = new ExecuteTimer(this);
					timer.scheduleAtFixedRate(timerTask, 7000, 7000);
					//
					//
					System.out.println("ping received to execute election");
					//doElection uuid
					if(this.idServer.GetCoordinatorUUID() == null)
					{
						this.idServer.SetCoordinatorUUID(UUID.fromString(tempCoordUUID));
					}
					else if(this.idServer.GetCoordinatorUUID().toString().compareTo(tempCoordUUID) <= 0)
					{
						System.out.println("value: " + this.idServer.GetCoordinatorUUID().toString() + " being replaced with: " +tempCoordUUID);
						this.idServer.SetCoordinatorUUID(UUID.fromString(tempCoordUUID));
					}
					this.idServer.SetCommunicationMode(CommunicationMode.ELECTION_RUNNING);
					this.idServer.SetElectionCounter(0);
				}
				else if(command.equals(Constants.keepRunningElection))
				{
					//
					//
					this.LastTimeCoordinatorResponded = LocalDateTime.now();
					if(timer != null)
					{
						timer = null;
						timerTask = null;
					}
					
					timer = new Timer();
					timerTask = new ExecuteTimer(this);
					timer.scheduleAtFixedRate(timerTask, 7000, 7000);
					//
					//
					if(this.idServer.GetCoordinatorUUID() == null)
					{
						System.out.println("coord UUID being changed...1");
						this.idServer.SetCoordinatorUUID(UUID.fromString(tempCoordUUID));
					}
					else if(this.idServer.GetCoordinatorUUID().toString().compareTo(tempCoordUUID) <= 0)
					{
						System.out.println("coord UUID being changed...2");
						System.out.println("value: " + this.idServer.GetCoordinatorUUID().toString() + " being replaced with: " +tempCoordUUID);
						this.idServer.SetCoordinatorUUID(UUID.fromString(tempCoordUUID));
					}
					this.idServer.SetElectionCounter(this.idServer.GetElectionCounter() + 1);
					
					System.out.println("election running with counter: " + this.idServer.GetElectionCounter());
					
					//check limit to stop the election
					if(this.idServer.GetElectionCounter() >= Constants.limit)
					{
						this.idServer.SetCommunicationMode(CommunicationMode.COORDINATOR_ELECTED);
						this.idServer.SetElectionCounter(0);
						
						System.out.println("coordinator elected: " + this.idServer.GetCoordinatorUUID());
					}
				}
				else if(command.equals(Constants.iAmHere))
				{
					//do nothing, just another server pinging
				}
				else if(command.equals(Constants.iAmCoordinator))
				{
					this.LastTimeCoordinatorResponded = LocalDateTime.now();
					if(timer != null)
					{
						timer = null;
						timerTask = null;
					}
					
					timer = new Timer();
					timerTask = new ExecuteTimer(this);
					timer.scheduleAtFixedRate(timerTask, 7000, 7000);
				}				
			}
			catch(IOException e)
			{
				System.out.println("IOException during receiving servers activity");
				e.printStackTrace();
			}
			
		}
	}
	public LocalDateTime GetLastCoordinatorMessageTime()
	{
		return this.LastTimeCoordinatorResponded;
	}
	public void SetCoordinatorUUIDToNull()
	{
		this.idServer.SetCoordinatorUUID(null);
		this.idServer.SetCommunicationMode(CommunicationMode.ELECTION_REQUIRED);
	}
}

class SendStatusToOtherServersThread implements Runnable
{
	private int port;
	private MulticastSocket socket;
	private InetAddress group;
	private UUID serverUUID;
	private IdServer idServer;
	
	SendStatusToOtherServersThread(int port, String group, UUID serverUUID, IdServer idServer)
	{
		this.port = port;
		try
		{
			this.group = InetAddress.getByName(group);
		}
		catch (UnknownHostException e)
		{
			System.out.println("UnknownHostException during parsing group address");
			e.printStackTrace();
		}
		
		this.createMulticastConenction();
		
		this.serverUUID = serverUUID;
		this.idServer = idServer;
	}
	private void createMulticastConenction()
	{
		try
		{
			socket = new MulticastSocket(port);
			socket.setTimeToLive(5);
			socket.joinGroup(group);
		}
		catch (IOException e)
		{
			System.out.println("IOException during socket intialization");
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		//String message = "I am alive server_no: " + this.serverUUID.toString();
		
		while(true)
		{
			String message = null;
			UUID myCoordUUID = this.idServer.GetCoordinatorUUID();
			if(myCoordUUID == null)
			{
				message = Constants.doElection + Constants.space + this.serverUUID.toString();
			}
			else if(this.idServer.GetCommunicationMode() == CommunicationMode.ELECTION_REQUIRED)
			{
				message = Constants.doElection + Constants.space + this.serverUUID.toString();
			}
			else if(this.idServer.GetCommunicationMode() == CommunicationMode.ELECTION_RUNNING)
			{
				message = Constants.keepRunningElection + Constants.space + this.serverUUID.toString();
			}
			else if(this.idServer.GetCommunicationMode() == CommunicationMode.COORDINATOR_ELECTED)
			{
				if(this.idServer.GetServerUUID().toString().compareTo(this.idServer.GetCoordinatorUUID().toString()) == 0)
				{
					message = Constants.iAmCoordinator + Constants.space + this.serverUUID.toString();
				}
				else
				{
					message = Constants.iAmHere + Constants.space + this.serverUUID.toString();
				}
			}
			try
			{
				byte[] buffer = message.getBytes();
				DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
				socket.send(datagram);
				Thread.sleep(3000);
			}
			catch(IOException e)
			{
				System.out.println("IOException during receiving servers activity");
				e.printStackTrace();
			}
			catch(InterruptedException e)
			{
				System.out.println("InterruptedException during receiving servers activity");
				e.printStackTrace();
			}
		}
	}
}

class ExecuteTimer extends TimerTask
{
	CheckServersThread checkServersThread;
	public ExecuteTimer(CheckServersThread checkServersThread)
	{
		this.checkServersThread = checkServersThread;
	}
	public void run()
	{
		LocalDateTime timeNow = LocalDateTime.now();
		LocalDateTime lastContactTimeWithCoordinator = this.checkServersThread.GetLastCoordinatorMessageTime();
		long difference = 0;
		if(timeNow.compareTo(lastContactTimeWithCoordinator) > 0)
		{
			difference = Math.abs(timeNow.until(lastContactTimeWithCoordinator, ChronoUnit.SECONDS));
		}
		//System.out.println("No response from coordinator for: " + difference);
		if(difference > 5)	//seconds
		{
			System.out.println("No response from coordinator for: " + difference);
			this.checkServersThread.SetCoordinatorUUIDToNull();
		}
	}
}