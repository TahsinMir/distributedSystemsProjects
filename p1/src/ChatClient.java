import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClient
{
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private Socket server;
	private int port;
	private String host;
	private InputStream in;
	private OutputStream out;
	private int channelPort = 0;
	private MulticastSocket newMultiCast;
	private String name;
	private String tempName;
	private InetAddress group;
	
	public static void main(String args[])
	{
        if(args.length != 0){
            System.out.println("ChatClient takes zero argument");
            System.exit(0);
        }
        
        
        ChatClient client = new ChatClient();
        client.ExecuteClient();

    }
	public void ExecuteClient(){
		//TODO: This connection will be moved in the while loop
		//User will connect to the server using /connect [servername] [portnumber]
		//Before connecting to the server user cannot perform any other operation without the /help command
		//TODO: quit will disconnect user from the server. And exit the chat.
		port = 5005;
		host = "localhost";
		try{
			server = new Socket(this.host, this.port);
			System.out.println("Connected to server!");
		}
		catch (UnknownHostException e){
			System.out.println("Unknown Host Exception: " + e);
		}
		catch (IOException e){
			System.out.println("IO Exception: " + e);
		}
		System.out.println("Client connected to server!");
		Scanner scan = new Scanner(System.in);
		
		System.out.println("starting reading commands");
        
        while(true)
        {
        	String line = scan.nextLine();
        	IRCMessage command = PrepareRequest(line);
        	
        	if(command.error == true)
        	{
        		System.out.println("Invalid command");
        		continue;
        	}
        	
        	try
        	{
        		in = server.getInputStream();
    			out = server.getOutputStream();
    			
    			ObjectOutputStream oout = new ObjectOutputStream(out);
    			oout.writeObject(command);
    			oout.flush();
    			
    			//sleep(1000);
    			
    			ObjectInputStream oin = new ObjectInputStream(in);
    			IRCMessage res = (IRCMessage) oin.readObject();

				if(res.isServerResponse){
					//need to provide client feedback about server response
					ShowServerResponse(res);
				}
//            	System.out.println("server responded with:");
//            	//PrintIRCCommand(res);
        	}
        	catch (IOException e)
    		{
    			System.out.println("IO Exception occured: " + e);
    			System.out.println("Game Over!");
    		}
    		catch (ClassNotFoundException e)
    		{
    			System.out.println("Class Not Found Exception occured: " + e);
    		}
        }
	}

	private void ShowServerResponse(IRCMessage ServerResponse)
	{
		if(ServerResponse.commandType.equals(Constants.nick))
		{
			if(ServerResponse.commandStatus.equals(Constants.success))
			{
				System.out.println(ServerResponse.responseMessage);
				this.name = this.tempName;
			}
			else if(ServerResponse.commandStatus.equals(Constants.failure))
			{
				System.out.println(ServerResponse.responseMessage);
			}
		}
		else if(ServerResponse.commandType.equals(Constants.list)){
			//ServerResponse.channelList show this
			System.out.println("Available channels are: ");
			for(String key: ServerResponse.channelList.keySet()){
				System.out.println(key);
			}
		}
		else if(ServerResponse.commandType.equals(Constants.join))
		{
			if(ServerResponse.commandStatus.equals(Constants.success))
			{
				System.out.println(ServerResponse.responseMessage);
				
				try
				{
					this.group = InetAddress.getByName(ServerResponse.group);
					this.channelPort = ServerResponse.channelPort;
				
					System.out.println("Received group: " + this.group + ", " + ServerResponse.group);
					System.out.println("Received port: " + this.channelPort);
					newMultiCast = new MulticastSocket(this.channelPort);
					newMultiCast.joinGroup(this.group);
				}
				catch (IOException e)
				{
					System.out.println("UnknownHostException occured");
					ServerResponse.responseMessage = "Error occured: " + e.getStackTrace();
				}
				
				Thread t = new Thread(new MultiCastThread(newMultiCast, this.group, this.channelPort, this.name));
				t.start();
			}
			else if(ServerResponse.commandStatus.equals(Constants.failure))
			{
				System.out.println(ServerResponse.responseMessage);
			}
		}
		else if(ServerResponse.commandType.equals(Constants.leave))
		{
			if(ServerResponse.commandStatus.equals(Constants.success))
			{
				try {
					newMultiCast.leaveGroup(this.group);
					newMultiCast.close();
					this.channelPort = ServerResponse.channelPort;
					System.out.println(ServerResponse.responseMessage);
				}
				catch (IOException e)
				{
					System.out.println("IOException occured while closing connection with channel");
				}
			}
			else if(ServerResponse.commandStatus.equals(Constants.failure))
			{
				System.out.println(ServerResponse.responseMessage);
			}
		}
		else if(ServerResponse.commandType.equals(Constants.stats))
		{
			
		}
		else if(ServerResponse.commandType.equals(Constants.help))
		{
			if(ServerResponse.commandStatus.equals(Constants.success))
			{
				System.out.println(ServerResponse.responseMessage);
			}
			else if(ServerResponse.commandStatus.equals(Constants.failure))
			{
				System.out.println("Error Occured..");
			}
		}
		else if(ServerResponse.commandType.equals(Constants.textMessage))
		{
			System.out.println(ServerResponse.responseMessage);
			
			
			String message = this.name + ": " + ServerResponse.message;
			byte[] buffer = message.getBytes();
			DatagramPacket datagram = new
            DatagramPacket(buffer,buffer.length,this.group, this.channelPort); 
            try {
            	newMultiCast.send(datagram);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{ // We can just show server response here. // More if will be added later if necessary
			System.out.println(ServerResponse.responseMessage);
		}
	}


	private IRCMessage PrepareRequest(String command)
	{
		IRCMessage message = new IRCMessage();
		
		String[] splitted = command.split("\\s+");
    	
    	if(splitted.length == 0)
    	{
    		LOGGER.log(Level.SEVERE, "Invalid Command");
    		message.error = true;
    		message.errorMessage = "Invalid Command";
    	}
    	else if(splitted[0] == Constants.connect || splitted[0].equals(Constants.connect))
    	{
    		if(splitted.length != 2)
    		{
    			LOGGER.log(Level.SEVERE, "Invalid Command");
    			message.error = true;
        		message.errorMessage = "Invalid Command";
    		}
    		else
    		{
    			LOGGER.log(Level.FINEST, "Client requested connection to the server");
    			message.isCommand = true;
    			message.commandType = Constants.connect;
    			message.isClientRequest = true;
    			message.serverName = splitted[1];
    		}
    	}
    	else if(splitted[0] == Constants.nick || splitted[0].equals(Constants.nick))
    	{
    		if(splitted.length != 2)
    		{
    			LOGGER.log(Level.SEVERE, "Invalid Command");
    			message.error = true;
        		message.errorMessage = "Invalid Command";
    		}
    		else
    		{
    			LOGGER.log(Level.FINEST, "Client requested to set a nick name");
    			message.isCommand = true;
    			message.commandType = Constants.nick;
    			message.isClientRequest = true;
    			message.nickName = splitted[1];
    			
    			tempName = splitted[1];
    		}
    		
    	}
    	else if(splitted[0] == Constants.list || splitted[0].equals(Constants.list))
    	{
    		if(splitted.length != 1)
    		{
    			LOGGER.log(Level.SEVERE, "Invalid Command");
    			message.error = true;
        		message.errorMessage = "Invalid Command";
    		}
    		else
    		{
    			LOGGER.log(Level.FINEST, "Client requested List channels and number of users");
    			message.isCommand = true;
    			message.commandType = Constants.list;
    			message.isClientRequest = true;
    		}
    	}
    	else if(splitted[0] == Constants.join || splitted[0].equals(Constants.join))
    	{
    		if(splitted.length != 2)
    		{
    			LOGGER.log(Level.SEVERE, "Invalid Command");
    			message.error = true;
        		message.errorMessage = "Invalid Command";
    		}
    		else
    		{
    			LOGGER.log(Level.FINEST, "Client requested to set a nick name");
    			message.isCommand = true;
    			message.commandType = Constants.join;
    			message.isClientRequest = true;
    			message.channelName = splitted[1];
    		}
    		
    	}
    	else if(splitted[0] == Constants.leave || splitted[0].equals(Constants.leave))
    	{
    		if(splitted.length > 2)
    		{
    			LOGGER.log(Level.SEVERE, "Invalid Command");
    			message.error = true;
        		message.errorMessage = "Invalid Command";
    		}
    		else
    		{
    			LOGGER.log(Level.FINEST, "Client requested to leave channel");
    			message.isCommand = true;
    			message.commandType = Constants.leave;
    			message.isClientRequest = true;
    			
    			if(splitted.length == 2)
    			{
    				message.channelName = splitted[1];
    				message.leaveChannelType = Constants.namedChannel;
    			}
    			else
    			{
    				message.leaveChannelType = Constants.currentChannel;
    			}
    			
    		}
    	}
    	else if(splitted[0] == Constants.quit || splitted[0].equals(Constants.quit))
    	{
    		if(splitted.length != 1)
    		{
    			LOGGER.log(Level.SEVERE, "Invalid Command");
    			message.error = true;
        		message.errorMessage = "Invalid Command";
    		}
    		else
    		{
    			LOGGER.log(Level.FINEST, "Client requested to quit");
    			message.isCommand = true;
    			message.commandType = Constants.quit;
    			message.isClientRequest = true;
    		}
    	}
    	else if(splitted[0] == Constants.help || splitted[0].equals(Constants.help))
    	{
    		if(splitted.length != 1)
    		{
    			LOGGER.log(Level.SEVERE, "Invalid Command");
    			message.error = true;
        		message.errorMessage = "Invalid Command";
    		}
    		else
    		{
    			LOGGER.log(Level.FINEST, "Client requested help");
    			message.isCommand = true;
    			message.commandType = Constants.help;
    			message.isClientRequest = true;
    		}
    	}
    	else if(splitted[0] == Constants.stats || splitted[0].equals(Constants.stats))
    	{
    		if(splitted.length != 1)
    		{
    			LOGGER.log(Level.SEVERE, "Invalid Command");
    			message.error = true;
        		message.errorMessage = "Invalid Command";
    		}
    		else
    		{
    			LOGGER.log(Level.FINEST, "Client requested stats");
    			message.isCommand = true;
    			message.commandType = Constants.stats;
    			message.isClientRequest = true;
    		}
    	}
    	else	//it is a plain message from the client
    	{
    		LOGGER.log(Level.FINEST, "Client sending a message");
    		message.isCommand = false;
    		message.commandType = Constants.textMessage;
    		message.isClientRequest = true;
    		message.message = command;
    	}
    	
    	return message;
	}
	//just for testing
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

class MultiCastThread implements Runnable 
{ 
    private MulticastSocket socket; 
    private InetAddress group; 
    private int port; 
    private static final int MAX_LEN = 1000;
    private String clientName;
    
    MultiCastThread(MulticastSocket socket,InetAddress group,int port, String clientName) 
    { 
        this.socket = socket; 
        this.group = group; 
        this.port = port; 
        this.clientName = clientName;
    } 
      
    @Override
    public void run() 
    { 
        while(true) 
        { 
                byte[] buffer = new byte[MultiCastThread.MAX_LEN]; 
                DatagramPacket datagram = new
                DatagramPacket(buffer,buffer.length,group,port); 
                String message; 
            try
            { 
                socket.receive(datagram); 
                message = new
                String(buffer,0,datagram.getLength(),"UTF-8");
                //System.out.println("datagram message being printed");
                if(!message.startsWith(this.clientName)) 
                    System.out.println(message); 
            } 
            catch(IOException e) 
            { 
                System.out.println("Socket closed!");
                return;
            } 
        } 
    } 
}
