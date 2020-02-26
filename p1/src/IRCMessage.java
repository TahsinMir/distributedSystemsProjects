import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

public class IRCMessage extends Object implements Serializable
{
	boolean isCommand;	//if it is not a command, it is a string message
	boolean isServerResponse;	//if it is a server response, will be handled differently
	
	// all the IRC part
	String serverName;
	String nickName;
	Hashtable<String, Integer> channelList;
	String joiningChannel;
	String leavingChannel;
	boolean quit;
	boolean help;
	boolean stats;
	
	//otherwise just a plain message
	String message;
	
	public IRCMessage()
	{
		channelList = new Hashtable<String, Integer>();
	}
}

