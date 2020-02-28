import java.util.Hashtable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClient
{
	private final static Logger LOGGER =  Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public static void main(String args[])
	{
        if(args.length != 0){
            System.out.println("ChatClient takes zero argument");
            System.exit(0);
        }
        
        
        ChatClient client = new ChatClient();
        client.ExecuteClient();

    }
	public void ExecuteClient()
	{
		Scanner scan = new Scanner(System.in);
        
        while(true)
        {
        	String line = scan.nextLine();
        	IRCMessage command = PrepareRequest(line);
        	
        	PrintIRCCommand(command);
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
