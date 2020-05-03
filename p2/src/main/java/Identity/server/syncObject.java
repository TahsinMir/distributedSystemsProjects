package Identity.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import Identity.server.CommunicationMode;

// This object will be passed in multicast
// This will contain coordinator information serverId, port, RMI registry
// Server information -> serverID, port
// Server lamport time
// Action history of the server

public class syncObject implements Serializable {
    private int lampTime;
    private HashMap<Integer, String> lampHistory;
    private String myAddress;
    private int myPort;
    private UUID myUUID;
    private String coordinatorAddress;
    private int coordinatorPort;
    private UUID coordinatorUUID;
    private String message; // This is a message for reference
    private CommunicationMode commMode;
    private boolean amICoordinator = false;

    public syncObject(int myPort, String myAddress, UUID myUUID){
        lampHistory = new HashMap<>();
        this.myPort = myPort;
        this.myAddress = myAddress;
        this.myUUID = myUUID;
        this.commMode = CommunicationMode.ELECTION_REQUIRED; // Whenever a new server join a new eleciton will happen
        unsetCoordinator();
    }

    public void setMyselfasCoordinator(){
        this.coordinatorAddress = this.myAddress;
        this.coordinatorUUID = this.myUUID;
        this.coordinatorPort = this.myPort;
        this.message = "I am the coordinator. I am running on: "+this.myAddress+":"+this.myPort+ " My latest lamport time is " + lampTime;
        this.amICoordinator = true;
    }

    public void setMyselfasNotCoordinator(){
        this.message = "I am not the coordinator. I am running on: "+ this.myAddress + " : " + this.myPort + " My latest lamport time is " + lampTime;
        this.amICoordinator = false;
    }

    public void updateCoordinator(syncObject receivedObj){
        setMyselfasCoordinator();
        this.coordinatorAddress = receivedObj.getCoordinatorAddress();
        this.coordinatorUUID = receivedObj.getCoordinatorUUID();
        this.coordinatorPort = receivedObj.getCoordinatorPort();
        setMyselfasNotCoordinator();
    }

    public void unsetCoordinator(){
        setMyselfasCoordinator(); // Temporarily I am setting myself as coordinator
        this.message = "No coordinaotr is selected yet. Election required";
        this.commMode = CommunicationMode.ELECTION_REQUIRED;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void insertHistory(int lampTime, String databasecommand){
        lampHistory.put(lampTime, databasecommand);
    }

    public int getLampTime() {
        return lampTime;
    }

    public void setLampTime(int lampTime) {
        this.lampTime = lampTime;
    }

    public HashMap<Integer, String> getLampHistory() {
        return lampHistory;
    }

    public void setLampHistory(HashMap<Integer, String> lampHistory) {
        this.lampHistory = lampHistory;
    }

    public String getMyAddress() {
        return myAddress;
    }

    public void setMyAddress(String myAddress) {
        this.myAddress = myAddress;
    }

    public int getMyPort() {
        return myPort;
    }

    public void setMyPort(int myPort) {
        this.myPort = myPort;
    }

    public UUID getMyUUID() {
        return myUUID;
    }

    public void setMyUUID(UUID myUUID) {
        this.myUUID = myUUID;
    }

    public String getCoordinatorAddress() {
        return coordinatorAddress;
    }

    public void setCoordinatorAddress(String coordinatorAddress) {
        this.coordinatorAddress = coordinatorAddress;
    }

    public int getCoordinatorPort() {
        return coordinatorPort;
    }

    public void setCoordinatorPort(int coordinatorPort) {
        this.coordinatorPort = coordinatorPort;
    }

    public UUID getCoordinatorUUID() {
        return coordinatorUUID;
    }

    public void setCoordinatorUUID(UUID coordinatorUUID) {
        this.coordinatorUUID = coordinatorUUID;
    }

    public CommunicationMode getCommMode() {
        return commMode;
    }

    public void setCommMode(CommunicationMode commMode) {
        this.commMode = commMode;
    }

    public boolean isCoordinator(){
        return amICoordinator;
    }
}
