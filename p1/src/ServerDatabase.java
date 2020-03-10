//TODO: add appropriate debug message to the right place
import java.util.ArrayList;
import java.util.Hashtable;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;

public class ServerDatabase {
	private	Hashtable<String, String> Users;  // username, channel name
	private Hashtable<String, Integer> Channels; // string -> channel name, Integer->port

	private final static Logger log =  Logger.getLogger("Server database logger");
	
	public ServerDatabase(int debugLevel){
		Users = new Hashtable<>();
		Channels = new Hashtable<>();
		log.setLevel(debugLevel == 1 ? Level.ALL : Level.OFF);
	}

	public boolean AddUserToChannel(String userName, String channelName){
		//Hash table cannot contains null value so we are basically putting the null as a string
		if(channelName == Constants.nullString || Channels.containsKey(channelName)) {
			Users.put(userName, channelName);
			return true;
		}
		return false; //Channel wasn't found so return True
	}
	public boolean CreateChannel(String channelName)
	{
		if(!Channels.containsKey(channelName)){
			int FreePort = GetFreePort();
			if(FreePort > 0){ // If we successfully get a free port.
				Channels.put(channelName, FreePort);
				return true; // new channel created
			}
		}
		log.log(Level.SEVERE, "Error in channel creation.");
		return false; // Channel already exists
	}

	private int GetFreePort(){
		int port;
		ServerSocket serverSocket;
		try{
			serverSocket = new ServerSocket(0);
			port =  serverSocket.getLocalPort();
		} catch (Exception IOException){
			//Show that as debug message
			log.log(Level.SEVERE, "Unable to find any free port");
			return 0;
		}
		try{
			serverSocket.close();
		} catch (Exception IOException){
			//Show that as debug message
			log.log(Level.SEVERE, "Unable to close the open port");
			return 0;
		}
		return port;
	}

	public Hashtable<String, Integer> getChannelWithUserNumber(){
		//Channel list with number of user
		Hashtable<String, Integer> channel = new Hashtable<>();
		for(String key: Channels.keySet()){
			channel.put(key, 0); // Initialize the channel with no user.
		}
		// Now we will iterate over the user and update the channel
		for (String user : Users.keySet()){
			String userChannel = Users.get(user);
			if(userChannel.equals("null")){
				continue;
			}
			int currentUser = channel.get(userChannel);
			currentUser += 1;
			channel.put(userChannel, currentUser);
		}
		return channel;
	}

	public Hashtable<String, String> getUsers(){
		return Users;
	}
	public void removeUser(String userName){
		Users.remove(userName);
	}
	public int getChannelPort(String channelName){
		return Channels.get(channelName);
	}
	public String GetUserChannelName(String userName)
	{
		return Users.get(userName);
	}
	public String[] GetChannelNames()
	{
		String[] result = new String[Channels.size()];
		
		int counter = 0;
		for(String key: Channels.keySet())
		{
			result[counter] = key;
			counter = counter + 1;
		}
		
		return result;
	}

}
