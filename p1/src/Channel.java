import java.util.ArrayList;
import java.util.List;

public class Channel
{
	private String channelName;
	private List<User> users;
	private List<Message> messages;
	
	public Channel(String channelName)
	{
		this.channelName = channelName;
		this.users = new ArrayList<User>();
		this.messages = new ArrayList<Message>();
	}

}

class User
{
    public String NickName; 
};
class Message
{
	public String message;
}