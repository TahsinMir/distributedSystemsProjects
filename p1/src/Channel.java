import java.util.ArrayList;
import java.util.List;

public class Channel
{
	public String channelName;
	public List<User> users;
	public List<Message> messages;
	
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
    public User(String NickName)
    {
    	this.NickName = NickName;
    }
};
class Message
{
	public String message;
	public Message(String message)
    {
    	this.message = message;
    }
}