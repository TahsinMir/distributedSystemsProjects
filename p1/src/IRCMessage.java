import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

public class IRCMessage extends Object implements Serializable
{
	String serverName;
	String nickName;
	Hashtable<String, Integer> channelList;
	String joiningChannel;
	String leavingChannel;
	boolean quit;
	boolean help;
	boolean stats;

	//
	boolean isCommand = True;
	command = "quit";
	String message = ""
	
	public IRCMessage()
	{
		channelList = new Hashtable<String, Integer>();
	}
}

