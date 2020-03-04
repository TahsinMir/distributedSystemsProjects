import java.net.Socket;
import java.net.SocketException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInput;
import java.net.InetAddress;

public class ServerConnection extends  Thread {

    private Socket client;
    private String clientName;
    private InputStream in;
	private OutputStream out;
	private ServerDatabase database;

    ServerConnection(Socket client, ServerDatabase database) {
        this.client = client;
        setPriority(NORM_PRIORITY - 1);
        this.database = database;
        //By default client port number will be his nickname. Which will be updated once client update his role
        this.clientName = Integer.toString(client.getPort());
        // Add user to the null channel by default
        // Hash table cannot contains null value so we are basically putting the null as a string
        database.AddUserToChannel(this.clientName, "null");
    }

    public void run(){
    	try{
    		while(true){
        		in = client.getInputStream();
    			out = client.getOutputStream();
    			
    			//client goes first, and then goes the server, the one with the lower value wins
    			ObjectInputStream oin = new ObjectInputStream(in);
    			IRCMessage res = (IRCMessage) oin.readObject();
    			
    			System.out.println("received IRC message: ");
    			PrintIRCCommand(res);
    			
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

    private IRCMessage PrepareResponse(IRCMessage ClientRequest){
    	//Just modify the client's sent request
		ClientRequest.isServerResponse = true;
		ClientRequest.isClientRequest = false;
		ClientRequest.error = false;

		//Execute the command
		if(ClientRequest.isCommand)
		{
			String commandType = ClientRequest.commandType;
			if(commandType.contentEquals(Constants.nick))
			{

				// Change this name in the hashmap as well
                if(ChangeNickNameInHashMap(ClientRequest.nickName, this.clientName)){
                    this.clientName = ClientRequest.nickName;
                    ClientRequest.responseMessage = "Your nick name has been changed to: " + ClientRequest.nickName;
                }else{
                    ClientRequest.responseMessage = "Nickname: " + ClientRequest.nickName +" already taken";
                }
			}else if(commandType.equals(Constants.join)){
				if(database.AddUserToChannel(this.clientName, ClientRequest.channelName)){
				    //
                    ClientRequest.responseMessage = "You are added to the channel " + ClientRequest.channelName;
                    ClientRequest.channelPort = database.getChannelPort(ClientRequest.channelName);
                } else{
                    ClientRequest.responseMessage = "Channel " + ClientRequest.channelName + "Doesn't exists";
                }
			}else if(commandType.equals(Constants.list)){
				ClientRequest.channelList = database.getChannelWithUserNumber();
                ClientRequest.responseMessage = "ChannelList list has been populated";
			}else if(commandType.equals(Constants.leave)){
			    String currChannel = database.getUsers().get(this.clientName);
			    if(currChannel.equals("null")){
                    ClientRequest.responseMessage = "You are not connected to any channel yet.";
                }else{
                    if(database.AddUserToChannel(this.clientName, "null")){
                        ClientRequest.responseMessage = "You left the channel " + currChannel;
                        ClientRequest.channelPort = 0;
                    }
                }
            }
		}
    	return ClientRequest;
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
}
