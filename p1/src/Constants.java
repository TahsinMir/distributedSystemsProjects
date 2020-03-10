import java.util.Timer;
import java.util.TimerTask;

public class Constants
{
	public static String connect = "/connect";
	public static String nick = "/nick"; //Done
	public static String list = "/list"; //Done
	public static String join = "/join"; //Done
	public static String leave = "/leave"; // Done
	public static String quit = "/quit";
	public static String help = "/help"; // Will show all the available command
	public static String stats = "/stats"; // Will show all the channel and number of user connected to that channel.

	public static String currentChannel = "Current Channel";
	public static String namedChannel = "Named Channel";
	
	public static String textMessage = "Text Message";
	public static String nullString = "null";
	
	//command status
	public static String success = "Success";
	public static String failure = "Failure";
	
	//command line arguments
	public static String p = "-p";
	public static String d = "-d";
	
	//global channel address
	public static String GlobalChannelAddress = "230.230.246.0";
	
	//Timer time
	public static long timerTime = 3000;    //5 minutes
	
	
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
