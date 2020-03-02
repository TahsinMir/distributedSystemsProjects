import java.util.Hashtable;
import java.util.List;

public class ServerDatabase
{
	private	Hashtable<String, String> Users;
	
	private Hashtable<String, Integer> Channels;
	
	private Hashtable<Long, Boolean> threadIdList;
	
	public ServerDatabase()
	{
		Users = new Hashtable<String, String>();
		Channels = new Hashtable<String, Integer>();
		threadIdList = new Hashtable<Long, Boolean>();
	}
	
	public void AddUser(String userName, String channelName)
	{
		Users.put(userName, channelName);
	}
	public void AddChannel(String channelName)
	{
		Channels.put(channelName, 0);
	}
	public void AddThread(long threadId)
	{
		threadIdList.put(threadId, true);
	}
	
	public void AddUserToChannel(String channelName)
	{
		if(Channels.containsKey(channelName))
		{
			int val = Channels.get(channelName);
			
			Channels.replace(channelName, val + 1);
		}
	}
	
	public boolean DoesThreadIdExist(long id)
	{
		if(threadIdList.containsKey(id))
		{
			return true;
		}
		return false;
	}

}
