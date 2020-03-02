import java.net.Socket;
import java.net.SocketException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInput;

public class ServerConnection extends  Thread {

    private Socket client;
    private Channel facebook;
    private InputStream in;
	private OutputStream out;

    ServerConnection(Socket client) throws SocketException
    {
        this.client = client;
        setPriority(NORM_PRIORITY - 1);
        System.out.println("Created thread " + this.getName());
        
        facebook = new Channel("facebook");
        
        System.out.println("created channel");
    }
    public void run()
    {
    	try
    	{
    		while(true)
        	{
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
    	catch(IOException e)
    	{
    		System.out.println("IOException occured");
    	}
    	catch (ClassNotFoundException e)
    	{
    		System.out.println("ClassNotFoundException occured");
		}
    }
    private IRCMessage PrepareResponse(IRCMessage command)
    {
    	IRCMessage response = new IRCMessage();
    	
    	response.isServerResponse = true;
    	response.isClientRequest = false;
    	response.isCommand = false;
    	response.error = false;
    	
    	if(command.commandType == Constants.join || command.commandType.equals(Constants.join))
    	{
    		facebook.users.add(new User(command.nickName));
    		System.out.println("sample on storing a user into a new channel");
    	}
    	
    	response.responseMessage = "order executed";
    	
    	return response;
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
