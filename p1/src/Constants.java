import java.util.Timer;
import java.util.TimerTask;


/***
 * Has all the possible constant fields for the server/client to use
 */
public class Constants
{
	public static String connect = "/connect";	//the connect command
	public static String nick = "/nick";	//the nick command
	public static String list = "/list";	//the list command
	public static String join = "/join";	//the join command
	public static String leave = "/leave";	//the leave command
	public static String quit = "/quit";	//the quit command
	public static String help = "/help"; // Will show all the available command
	public static String stats = "/stats"; // Will show all the channel and number of user connected to that channel.

	public static String currentChannel = "Current Channel";	//if client wants to leave the current channel
	public static String namedChannel = "Named Channel";	//if client wants to leave the named channel
	
	public static String textMessage = "Text Message";	//if it is not a command, just a regular text message
	public static String nullString = "null";	//initially hashmaps will contain null channel for clients
	
	//command status
	public static String success = "Success";	//if the IRC message execution is a success
	public static String failure = "Failure";	//if the IRC message execution is a failure
	
	//command line arguments
	public static String p = "-p";
	public static String d = "-d";
	
	//global channel address
	public static String GlobalChannelAddress = "230.230.246.0";
	
	//Timer time
	public static long timerTime = 300000;    //5 minutes
	
	
	public static Timer SetTimer(Timer timer)
    {
    	if(timer != null)
    	{
    		timer.cancel();
    	}
    	
    	timer = new Timer();
        timer.schedule(new TimerTask() {
        	  @Override
        	  public void run() {
        		  System.out.println("No activity from users. Sever is shutting down");
        		  System.exit(0);
        	  }
        	}, timerTime);
        
        return timer;
    }
}
