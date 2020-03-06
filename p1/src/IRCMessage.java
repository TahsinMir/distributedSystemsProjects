import java.io.Serializable;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.List;

public class IRCMessage extends Object implements Serializable
{
	boolean isCommand;	//if it is not a command, it is a string message
	boolean isClientRequest;	//it is a client request
	boolean isServerResponse;	//it is a server response
	
	// all the IRC part
	String commandType;
	String serverName;
	String nickName;
	Hashtable<String, Integer> channelList;
	String channelName;
	String leaveChannelType;

	int channelPort;
	String group;
	
	//otherwise just a plain message
	String message;
	
	//no message could be created
	boolean error;
	String errorMessage;
	
	String responseMessage;
	
	public IRCMessage()
	{
		error = false;
		isCommand = true;
		channelList = new Hashtable<String, Integer>();
	}
}

