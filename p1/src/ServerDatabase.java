import java.util.Hashtable;
import java.util.List;

public class ServerDatabase
{
	private	Hashtable<String, String> Users;
	
	private Hashtable<String, Integer> Channels;
	
	private Hashtable<Long, Boolean> threadIdList;
	
	public ServerDatabase()
	{
		Users = new Hashtable<String, String>(); // username, channel name
		Channels = new Hashtable<String, Integer>(); // Channel name, number of user in that channel
		threadIdList = new Hashtable<Long, Boolean>(); // No work so far
	}

	public boolean AddUserToChannel(String userName, String channelName){
		if(Channels.containsKey(channelName)) {
			int val = Channels.get(channelName);
			Channels.replace(channelName, val + 1);
			Users.put(userName, channelName);
			return true;
		}
		return false; //Channel wasn't found so return True
	}
	public boolean CreateChannel(String channelName)
	{
		if(!Channels.containsKey(channelName)){
			Channels.put(channelName, 0);
			return true; // new channel created
		}
		return false; // Channel already exists
	}
	public void AddThread(long threadId)
	{
		threadIdList.put(threadId, true);
	}

	//Removed everything into the addUser
//	public void AddUserToChannel(String channelName)
//	{
//		if(Channels.containsKey(channelName))
//		{
//			int val = Channels.get(channelName);
//
//			Channels.replace(channelName, val + 1);
//		}
//	}
	//No work so far
	public boolean DoesThreadIdExist(long id)
	{
		if(threadIdList.containsKey(id))
		{
			return true;
		}
		return false;
	}

}
