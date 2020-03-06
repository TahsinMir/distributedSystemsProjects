import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInput;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ServerConnection extends  Thread {

    private Socket client;
    private String clientName;
    private InputStream in;
	private OutputStream out;
	private ServerDatabase database;
	private boolean finished;
	private String group;

    ServerConnection(Socket client, ServerDatabase database) {
        this.client = client;
        setPriority(NORM_PRIORITY - 1);
        this.database = database;
        //By default client port number will be his nickname. Which will be updated once client update his role
        this.clientName = Integer.toString(client.getPort());
        System.out.println("setting port as nick initially: " + this.clientName);
        // Add user to the null channel by default
        // Hash table cannot contains null value so we are basically putting the null as a string
        database.AddUserToChannel(this.clientName, Constants.nullString);
        
        finished = false;
    }

    public void run(){
    	try{
    		while(true){
        		in = client.getInputStream();
    			out = client.getOutputStream();
    			
    			//receive command from client first in order to execute command
    			ObjectInputStream oin = new ObjectInputStream(in);
    			IRCMessage res = (IRCMessage) oin.readObject();
    			
    			System.out.println("received IRC message: ");
    			//PrintIRCCommand(res);
    			
    			IRCMessage response = PrepareResponse(res);
    			
    			ObjectOutputStream oout = new ObjectOutputStream(out);
    			oout.writeObject(response);
    			oout.flush();
        	}
    	}
    	catch(IOException e){
    		System.out.println("IOException occured");
    	}
    	catch (ClassNotFoundException e){
    		System.out.println("ClassNotFoundException occured");
		}
    }

    private IRCMessage PrepareResponse(IRCMessage ClientRequest)
    {
    	IRCMessage ServerResponse = new IRCMessage();
    	
    	ServerResponse.isServerResponse = true;
    	ServerResponse.isClientRequest = false;
    	ServerResponse.error = false;

		//If we received an actual valid command from the client, Execute the command
		if(ClientRequest.isCommand)
		{
			String commandType = ClientRequest.commandType;
			ServerResponse.commandType = commandType;
			if(commandType.contentEquals(Constants.nick))
			{
				// Change this name in the hashmap as well
                if(ChangeNickNameInHashMap(ClientRequest.nickName, this.clientName))
                {
                    this.clientName = ClientRequest.nickName;
                    ServerResponse.responseMessage = "Your nick name has been changed to: " + ClientRequest.nickName;
                    ServerResponse.commandStatus = Constants.success;
                }
                else
                {
                	ServerResponse.responseMessage = "Nickname: " + ClientRequest.nickName +" already taken";
                	ServerResponse.commandStatus = Constants.failure;
                }
			}
			else if(commandType.equals(Constants.list))
			{
				ServerResponse.channelList = database.getChannelWithUserNumber();
				ServerResponse.responseMessage = "ChannelList list has been populated";
			}
			else if(commandType.equals(Constants.join))
			{
				if(database.AddUserToChannel(this.clientName, ClientRequest.channelName))
				{
					ServerResponse.responseMessage = "You are added to the channel " + ClientRequest.channelName;
					ServerResponse.channelPort = database.getChannelPort(ClientRequest.channelName);
					
					this.group = "230.230.246.0";
					
					ServerResponse.group = this.group;
					ServerResponse.commandStatus = Constants.success;
                }
				else
				{
					ServerResponse.commandStatus = Constants.failure;
					ServerResponse.responseMessage = "Channel " + ClientRequest.channelName + "Doesn't exist";
                }
			}
			else if(commandType.equals(Constants.leave))
			{
			    String currChannel = database.getUsers().get(this.clientName);
			    if(currChannel.equals(Constants.nullString))	//user is not in any channel at all
			    {
			    	ServerResponse.responseMessage = "You are not connected to any channel.";
			    	ServerResponse.commandStatus = Constants.failure;
                }
			    else if(ClientRequest.leaveChannelType == Constants.currentChannel || ClientRequest.leaveChannelType.equals(Constants.currentChannel))	//user trying to leave the current channel, whatever that is
			    {
			    	if(database.AddUserToChannel(this.clientName, Constants.nullString))
			    	{
                    	ServerResponse.responseMessage = "You left the channel " + currChannel;
                    	ServerResponse.channelPort = 0;
                    	ServerResponse.commandStatus = Constants.success;
                    }
			    }
			    else if(ClientRequest.leaveChannelType == Constants.namedChannel|| ClientRequest.leaveChannelType.equals(Constants.namedChannel))	//user is trying to leave a channel, the name is also sent by the client
			    {
			    	if(!currChannel.equals(ClientRequest.channelName))
			    	{
			    		ServerResponse.responseMessage = "You are not in the channel " + ClientRequest.channelName;
			    		ServerResponse.commandStatus = Constants.failure;
			    	}
			    	else if(database.AddUserToChannel(this.clientName, Constants.nullString)){
                    	ServerResponse.responseMessage = "You left the channel " + currChannel;
                    	ServerResponse.channelPort = 0;
                    	ServerResponse.commandStatus = Constants.success;
                    }
                }
            }
		}
		else	//it's should be regular message to be broadcasted
		{
            
            if(ClientRequest.commandType.equals(Constants.textMessage))
            {
            	ServerResponse.commandType = Constants.textMessage;
                ServerResponse.responseMessage = "This is a group message to everyone:" + ClientRequest.message;
                ServerResponse.message = ClientRequest.message;
            }
		}
    	return ServerResponse;
    }

    private boolean ChangeNickNameInHashMap(String currentNickName, String oldNickName){
        if(!database.getUsers().containsKey(currentNickName) && database.getUsers().containsKey(oldNickName)){
            // this nick name is available
            String currChannel = database.getUsers().get(oldNickName);
            database.removeUser(oldNickName);
            database.AddUserToChannel(currentNickName, currChannel);
            return true;
        }
        return false;
    }

    private void PrintIRCCommand(IRCMessage message)
    {
        System.out.println("message.isCommand: " + message.isCommand);
        System.out.println("message.isClientRequest: " + message.isClientRequest);
        System.out.println("message.isServerResponse: " + message.isServerResponse);
        
        // all the IRC part
        System.out.println("message.commandType: " + message.commandType);
        System.out.println("message.serverName: " + message.serverName);
        System.out.println("message.nickName: " + message.nickName);
        System.out.println("message.channelList:" + message.channelList);
        System.out.println("message.channelName: " + message.channelName);
        System.out.println("message.leaveChannelType: " + message.leaveChannelType);
        
        //otherwise just a plain message
        System.out.println("message.message: " + message.message);
        
        //no message could be created
        System.out.println("message.error: " + message.error);
        System.out.println("message.errorMessage: " + message.errorMessage);
    }
    public boolean GetFinished()
    {
    	return finished;
    }
}
