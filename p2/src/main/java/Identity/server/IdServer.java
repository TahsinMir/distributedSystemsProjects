package Identity.server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import java.util.*;

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
public class IdServer implements IdServerInterface {

    private Database db = null;
    private int ServerPort = 5005;
    private boolean isVerbose = false;
    private Logger log;
    private UUID serverUUID;
    /*private boolean isCoordinator;
    private boolean isCoordinatorElected;*/
    private CommunicationMode serverCommunicationMode;
    private int electionCounter;
    private syncObject sync;

    public IdServer(String[] args) throws RemoteException {
        super();
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%4$-7s] %5$s %n");
        log = Logger.getLogger(IdServer.class.getName());

        if (db == null) {
            db = new Database(log);
        }

        extractOption(makeOption(), args);

        this.serverUUID = UUID.randomUUID();

		/*this.isCoordinator = false;
		this.isCoordinatorElected = false;*/
        this.serverCommunicationMode = CommunicationMode.ELECTION_REQUIRED;
        this.electionCounter = 0;

        // Create new syncObject
        // My address will be set later once i connected with the RMI registry
        sync = new syncObject(getServerPort(), "localhost", this.serverUUID);
        sync.setLampTime(db.InitializeLamport());
    }

    public syncObject getSync() {
        return sync;
    }

    /**
     * creates a new user.
     *
     * @param LoginName - the login name of the user.
     * @param realName  - the real name of the user.
     * @param password  - the password hash of the user.
     * @param ipAddress - the creation ip address of the user.
     * @return A string explaining the result of the user creation operation.
     * @throws RemoteException
     * @see RemoteException
     */
    public String create(String LoginName, String realName, String password, String ipAddress) throws RemoteException {
        // Only coordinator will update
        // So other server will remotely invoke the coordinator method
        if(!sync.isCoordinator()){
            IdServerInterface coordinatorStub = getCoordinatorStub();
            return coordinatorStub.create(LoginName, realName, password, ipAddress);
        }
        if (db == null) {
            db = new Database(log);
        }

        String insertionResult;
        UUID randomUUID = UUID.randomUUID();
        String uuid = randomUUID.toString();
        LocalDate dateNow = LocalDate.now();
        String dateNowString = dateNow.toString();
        LocalTime timeNow = LocalTime.now();
        String timeNowString = timeNow.toString();
        sync.increaseLamportTime();
        //String loginName, String uuid, String password, String ipAddress, String date, String time, String realUserName, String lastChangeDate
        insertionResult = db.Insert(LoginName, uuid, password, ipAddress, dateNowString, timeNowString, realName, dateNowString, sync.getLampTime(), sync);

        return insertionResult;
    }

    /**
     * looks for a user using login name in the database.
     *
     * @param loginName - the login name of the user.
     * @return User data.
     * @throws RemoteException
     * @see RemoteException
     */
    public User lookup(String loginName) throws RemoteException {
        if(!sync.isCoordinator()){
            IdServerInterface coordinatorStub = getCoordinatorStub();
            return coordinatorStub.lookup(loginName);
        }
        if (db == null) {
            db = new Database(log);
        }
        User result = db.Search(Constants.loginName, loginName);
        return result;
    }

    /**
     * looks for a user using uuid in the database.
     *
     * @param UUID - the uuid assigned to the user.
     * @return User data.
     * @throws RemoteException
     * @see RemoteException
     */
    public User reverseLookUp(String UUID) throws RemoteException {
        if(!sync.isCoordinator()){
            IdServerInterface coordinatorStub = getCoordinatorStub();
            return coordinatorStub.reverseLookUp(UUID);
        }
        if (db == null) {
            db = new Database(log);
        }
        User result = db.Search(Constants.uuid, UUID);
        return result;
    }

    /**
     * modifies the login name of a user.
     *
     * @param oldLoginName - the old login name of the user.
     * @param newLoginName - the new login name of the user.
     * @param password     - the password hash of the user.
     * @return A string explaining the result of the user modification operation.
     * @throws RemoteException
     * @see RemoteException
     */
    public String modify(String oldLoginName, String newLoginName, String password) throws RemoteException {

        if(!sync.isCoordinator()){
            IdServerInterface coordinatorStub = getCoordinatorStub();
            return coordinatorStub.modify(oldLoginName,newLoginName,password);
        }
        if (db == null) {
            db = new Database(log);
        }

        if (!db.CheckPassword(Constants.loginName, oldLoginName, password)) {
            return Constants.failure + Constants.colon + Constants.wrongPassword;
        }
        sync.getLampTime();
        String resultUpdate = db.Update(Constants.loginName, oldLoginName, Constants.loginName, newLoginName, sync.getLampTime(), sync);

        if (resultUpdate.startsWith(Constants.failure)) {
            return resultUpdate;
        }

        LocalDate dateNow = LocalDate.now();
        String dateNowString = dateNow.toString();
        sync.increaseLamportTime();
        String resultUpdateLastChange = db.Update(Constants.loginName, newLoginName, Constants.lastChangeDate, dateNowString, sync.getLampTime(), sync);

        return resultUpdateLastChange;
    }

    /**
     * deletes the user information.
     *
     * @param loginName - the login name of the user.
     * @param password  - the password hash of the user.
     * @return A string explaining the result of the user deletion operation.
     * @throws RemoteException
     * @see RemoteException
     */
    public String delete(String loginName, String password) throws RemoteException {

        if(!sync.isCoordinator()){
            IdServerInterface coordinatorStub = getCoordinatorStub();
            return coordinatorStub.delete(loginName, password);
        }

        if (db == null) {
            db = new Database(log);
        }

        if (!db.CheckPassword(Constants.loginName, loginName, password)) {
            return Constants.failure + Constants.colon + Constants.wrongPassword;
        }

        String result = db.Delete(Constants.loginName, loginName);

        return result;
    }

    /**
     * retrieves user lists.
     *
     * @param option - one of the three options types for list retrieval.
     * @return user lists.
     * @throws RemoteException
     * @see RemoteException
     */
    public List<String> get(String option) throws RemoteException {

        if(!sync.isCoordinator()){
            IdServerInterface coordinatorStub = getCoordinatorStub();
            return coordinatorStub.get(option);
        }

        if (db == null) {
            db = new Database(log);
        }

        List<String> result = db.GetList(option);

        return result;
    }

    public static Options makeOption() {
        Options options = new Options();
        //Adding the command line options
        options.addOption("v", "verbose", false, "Print the details");
        options.addOption("n", "numport", true, "The port on which server will run");

        return options;
    }

    public void extractOption(Options options, String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("numport")) {
                ServerPort = Integer.parseInt(cmd.getOptionValue("numport"));
            }
            isVerbose = cmd.hasOption("verbose");
            log.setLevel(isVerbose ? Level.ALL : Level.OFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * binds the server.
     *
     * @return nothing.
     * @throws IOException On input error.
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

            sync.setMyAddress(InetAddress.getLocalHost().toString());

            registry.rebind("IdServer", server);

            log.info("Server binding successfull");
            log.info("server is up and running on port " + registry);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Exception occurred: " + e);
        }
    }

    /**
     * get the server port.
     *
     * @return server port.
     */
    public int getServerPort() {
        return ServerPort;
    }

    public void syncUsingLamportTime(syncObject referenceSync){
        for (int refLamportTime : referenceSync.getLampHistory().keySet()){
            if(!sync.getLampHistory().containsKey(refLamportTime)){
                //I don't have this update so I will update myself
                // Update my history table
                sync.getLampHistory().put(refLamportTime, referenceSync.getLampHistory().get(refLamportTime));
                sync.updateLamportTimeFromHistory(); // This will assign the higest value from the history to the lamport time.
                db.updateFromLamportTimeSync(refLamportTime, referenceSync.getLampHistory().get(refLamportTime));
            }
        }
    }

    public IdServerInterface getCoordinatorStub(){
        IdServerInterface stub;
        try{
            log.info("Coordinator address is: " + sync.getCoordinatorAddress() +" Port is : "+ sync.getCoordinatorPort());
            String[] coordinatoraddresswithHost = sync.getCoordinatorAddress().split("/");
            Registry registry = LocateRegistry.getRegistry(coordinatoraddresswithHost[coordinatoraddresswithHost.length - 1], sync.getCoordinatorPort());
            stub = (IdServerInterface) registry.lookup("IdServer");
        } catch (Exception e){
            e.printStackTrace();
            log.warning("Didn't find the coordinator");
            //So we will set the election is requied
            sync.unsetCoordinator(); // This will asign me as coordinator and automatically invoke a new election
            stub = this;
        }
        return stub;
    }

    public Logger getLog(){
        return log;
    }

    public static void main(String args[]) throws RemoteException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("javax.net.ssl.keyStore", "/p2/security/Server_Keystore");
        System.setProperty("java.security.policy", "/p2/security/mysecurity.policy");
        System.setProperty("javax.net.ssl.keyStorePassword", "test123");
        System.setProperty("javax.net.ssl.trustStore", "/p2/security/Client_Truststore"); // The server will also work as an client. During invoking the coordinator method
        System.setProperty("javax.net.ssl.trustStorePassword", "test123");

        IdServer server = new IdServer(args);
        server.bind();

        Thread t = new Thread(new CheckServersThread(5176, "224.0.0.1", server.GetServerUUID(), server));
        t.start();

        Thread t2 = new Thread(new SendStatusToOtherServersThread(5176, "224.0.0.1", server.GetServerUUID(), server));
        t2.start();




        //Adding shut down hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                server.HandleShutDown();
                server.log.info("Shutting down ...");
            }
        });
    }

    private void HandleShutDown() {
        if (db == null) {
            db = new Database(log);
        }

        this.log.info("Saving all to disk ...");
        db.SaveAllToDisk();
        this.log.info("Closing db connection ...");
        db.CloseDB();
    }

    public UUID GetServerUUID() {
        return this.serverUUID;
    }

    public CommunicationMode GetCommunicationMode() {
        return this.serverCommunicationMode;
    }

    public void SetCommunicationMode(CommunicationMode communicationMode) {
        this.serverCommunicationMode = communicationMode;
    }

    public int GetElectionCounter() {
        return this.electionCounter;
    }

    public void SetElectionCounter(int electionCounter) {
        this.electionCounter = electionCounter;
    }
}

class CheckServersThread implements Runnable {
    private int port;
    private MulticastSocket socket;
    private InetAddress group;
    private int MAX_LEN = 10000000;
    private UUID serverUUID;
    private IdServer idServer;
    private Timer timer = null;
    private TimerTask timerTask = null;
    private LocalDateTime LastTimeCoordinatorResponded;

    CheckServersThread(int port, String group, UUID serverUUID, IdServer idServer) {
        this.port = port;
        try {
            this.group = InetAddress.getByName(group);
        } catch (UnknownHostException e) {

            idServer.getLog().warning("UnknownHostException during parsing group address");
            e.printStackTrace();
        }
        this.createMulticastConenction();
        this.serverUUID = serverUUID;
        this.idServer = idServer;
        this.LastTimeCoordinatorResponded = null;
    }

    private void createMulticastConenction() {
        try {
            socket = new MulticastSocket(port);
            socket.joinGroup(group);
        } catch (IOException e) {
            idServer.getLog().warning("IOException during socket initialization");
        }
    }

    public void run() {
        byte[] buffer = new byte[this.MAX_LEN];
        // This datagram will be syncObject
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, this.group, this.port);

        while (true) {
            try {
                socket.receive(datagram);
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                ObjectInput in = new ObjectInputStream(bis);
                syncObject receivedSyncObj = (syncObject) in.readObject();

                idServer.getLog().info(receivedSyncObj.getMessage());
                if (receivedSyncObj.getCommMode() == CommunicationMode.ELECTION_REQUIRED) {
                    this.LastTimeCoordinatorResponded = LocalDateTime.now();

                    if (timer != null) {
                        timer = null;
                        timerTask = null;
                    }

                    timer = new Timer();
                    timerTask = new ExecuteTimer(this);
                    timer.scheduleAtFixedRate(timerTask, 7000, 7000);
                    //
                    //
                    idServer.getLog().info("ping received to execute election");
                    //doElection uuid
                    if (this.idServer.getSync().getCoordinatorAddress() == null || idServer.getSync().getLampTime() > receivedSyncObj.getLampTime()) {
                        // If there is no coordinator then I will set myself as coordinator
                        idServer.getSync().setMyselfasCoordinator();
                    } else if(idServer.getSync().getLampTime() < receivedSyncObj.getLampTime()){
                        idServer.getSync().updateCoordinator(receivedSyncObj);
                    }else if (idServer.getSync().getCoordinatorUUID().toString().compareTo(receivedSyncObj.getCoordinatorUUID().toString()) <= 0) { // if lamport time is same  and if someone else has less UUID number then update myself and make him coordinator
                        idServer.getSync().updateCoordinator(receivedSyncObj);
                    }else if (idServer.getSync().getCoordinatorUUID().toString().compareTo(receivedSyncObj.getCoordinatorUUID().toString()) > 0) { // if lamport time is same  and if someone else has less UUID number then update myself and make him coordinator
                        idServer.getSync().setMyselfasCoordinator();
                    }
                    idServer.getSync().setCommMode(CommunicationMode.ELECTION_RUNNING);
                    this.idServer.SetElectionCounter(0);
                } else if (receivedSyncObj.getCommMode() == CommunicationMode.ELECTION_RUNNING) {
                    this.LastTimeCoordinatorResponded = LocalDateTime.now();
                    if (timer != null) {
                        timer = null;
                        timerTask = null;
                    }

                    timer = new Timer();
                    timerTask = new ExecuteTimer(this);
                    timer.scheduleAtFixedRate(timerTask, 7000, 7000);
                    if (idServer.getSync().getCoordinatorAddress() == null) { // IF my coordinator information is set to null then I will update myself
                        idServer.getLog().info("coord UUID being changed...1");
                        this.idServer.getSync().setCoordinatorUUID(receivedSyncObj.getCoordinatorUUID());
                    } else if(idServer.getSync().getLampTime() > receivedSyncObj.getLampTime()){ // I have the highest lamport time so I will be coordinator.
                        idServer.getSync().setMyselfasCoordinator();
                    } else if(idServer.getSync().getLampTime() < receivedSyncObj.getLampTime()){ // sender has the highest lamport time so he will be the co ordinator
                        idServer.getSync().updateCoordinator(receivedSyncObj);
                    }else if (idServer.getSync().getCoordinatorUUID().toString().compareTo(receivedSyncObj.getCoordinatorUUID().toString()) <= 0) { // if lamport time is same  and if someone else has less UUID number then update myself and make him coordinator
                        idServer.getLog().info("coord UUID being changed...2");
                        this.idServer.getSync().updateCoordinator(receivedSyncObj);
                    }else if (idServer.getSync().getCoordinatorUUID().toString().compareTo(receivedSyncObj.getCoordinatorUUID().toString()) > 0) { // if lamport time is same  and if someone else has less UUID number then update myself and make him coordinator
                        idServer.getLog().info("coord UUID being changed...2");
                        this.idServer.getSync().setMyselfasCoordinator();
                    }
                    this.idServer.SetElectionCounter(this.idServer.GetElectionCounter() + 1);

                    idServer.getLog().info("election running with counter: " + this.idServer.GetElectionCounter());

                    //check limit to stop the election
                    if (this.idServer.GetElectionCounter() >= Constants.limit) {
                        this.idServer.getSync().setCommMode(CommunicationMode.COORDINATOR_ELECTED);
                        this.idServer.SetElectionCounter(0);

                        idServer.getLog().info("coordinator elected: " + this.idServer.getSync().getCoordinatorUUID().toString());
                    }
                } else if (receivedSyncObj.isCoordinator()) {
                    this.LastTimeCoordinatorResponded = LocalDateTime.now();
                    if (timer != null) {
                        timer = null;
                        timerTask = null;
                    }

                    timer = new Timer();
                    timerTask = new ExecuteTimer(this);
                    timer.scheduleAtFixedRate(timerTask, 7000, 7000);
                    // Syncing the lamport time
                    if(receivedSyncObj.getLampTime() > idServer.getSync().getLampTime()){
                        // Lamport time doesn't match so we will update the server accordingly
                        idServer.syncUsingLamportTime(receivedSyncObj);
                    }
                }
            } catch (IOException e) {
                idServer.getLog().warning("IOException during receiving servers activity");
            } catch (ClassNotFoundException e){
                idServer.getLog().warning("Couldn't convert the byte array stream to sync object");
            }

        }
    }

    public LocalDateTime GetLastCoordinatorMessageTime() {
        return this.LastTimeCoordinatorResponded;
    }

    public void SetCoordinatorUUIDToNull() {
        idServer.getSync().unsetCoordinator();
    }
}

class SendStatusToOtherServersThread implements Runnable {
    private int port;
    private MulticastSocket socket;
    private InetAddress group;
    private UUID serverUUID;
    private IdServer idServer;

    SendStatusToOtherServersThread(int port, String group, UUID serverUUID, IdServer idServer) {
        this.port = port;
        try {
            this.group = InetAddress.getByName(group);
        } catch (UnknownHostException e) {
            idServer.getLog().warning("UnkownHostException during parsing group address");
        }

        this.createMulticastConenction();

        this.serverUUID = serverUUID;
        this.idServer = idServer;
    }

    private void createMulticastConenction() {
        try {
            socket = new MulticastSocket(port);
            socket.setTimeToLive(5);
        } catch (IOException e) {
            idServer.getLog().warning("IOException during socket initialization");
        }
    }

    public void run() {
        while (true) {
            //Don't send the raw message instead send the syncronization message
            if(idServer.getSync().getCommMode() == CommunicationMode.COORDINATOR_ELECTED){
                if (idServer.getSync().getMyUUID().toString().compareTo(idServer.getSync().getCoordinatorUUID().toString()) == 0) {
//                    I am the coordinator
                    idServer.getSync().setMyselfasCoordinator();
                } else {
                    idServer.getSync().setMyselfasNotCoordinator();
                }
            }
            try {
                // convert the object to bytes
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(idServer.getSync());
                oos.flush();

                byte[] buffer = bos.toByteArray();
                bos.close();
                DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                socket.send(datagram);
                Thread.sleep(3000);
            } catch (IOException e) {
                idServer.getLog().warning("IOException during receiving server activity");
            } catch (InterruptedException e) {
                idServer.getLog().warning("InterruptedException during receiving servers activity");
            }
        }
    }
}

class ExecuteTimer extends TimerTask {
    CheckServersThread checkServersThread;

    public ExecuteTimer(CheckServersThread checkServersThread) {
        this.checkServersThread = checkServersThread;
    }

    public void run() {
        LocalDateTime timeNow = LocalDateTime.now();
        LocalDateTime lastContactTimeWithCoordinator = this.checkServersThread.GetLastCoordinatorMessageTime();
        long difference = 0;
        if (timeNow.compareTo(lastContactTimeWithCoordinator) > 0) {
            difference = Math.abs(timeNow.until(lastContactTimeWithCoordinator, ChronoUnit.SECONDS));
        }
        //System.out.println("No response from coordinator for: " + difference);
        if (difference > 5)    //seconds
        {
            System.out.println("No response from coordinator for: " + difference);
            this.checkServersThread.SetCoordinatorUUIDToNull();
        }
    }
}