//TODO: add appropriate debug message to the right place
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.logging.Level;


public class ChatServer
{
    ServerSocket serverSocket;
    int debugLevel;
    ServerDatabase database;
    String[] defaultChannel = {"Python", "Java", "C/C++", "PHP", "JavaScript"};
    Timer timer;
    long interval = 5000;
    boolean isTimerRunning;
    // Maximum number of thread is 4
    ExecutorService threadPool = Executors.newFixedThreadPool(4);
    private Logger log = Logger.getLogger(ChatServer.class.getName());


    public ChatServer(int port, int debugLevel) {
        if(debugLevel == 1){
            log.setLevel(Level.ALL);
        }else{
            log.setLevel(Level.OFF);
        }
        try {
        	this.isTimerRunning  = false;
            //Initial channel created by the server;
            database = new ServerDatabase(debugLevel);
            // some example channel created for the upcoming users
            for(int i = 0; i < defaultChannel.length; i++){
                database.CreateChannel(defaultChannel[i]);
            }
            serverSocket = new ServerSocket(port);
            
            this.timer = Constants.SetTimer(this.timer, isTimerRunning);
            this.isTimerRunning = true;
            log.info("ChatServer is up and running on port " + port + " " + InetAddress.getLocalHost());

            this.debugLevel = debugLevel;
        } catch (IOException e) {
            log.warning("Server creation problem");
        }
    }

    public void runServer()
    {
        Socket client;
        
        try
        {
            while(true){
                client = serverSocket.accept();
                log.info("Received connect from " + client.getInetAddress().getHostName() + " [ " + client.getInetAddress().getHostAddress() + " ] ");
                threadPool.execute(new ServerConnection(client, database, this.timer, isTimerRunning, debugLevel));
            }
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }

    public static void main(String args[])
    {
        if(args.length != 4)
        {
            System.out.println("Usage: java ChatServer -p #port -d #debug_level");
            System.exit(0);
        }
        else if(!((args[0].equals(Constants.p) || args[0].equals(Constants.d)) && (args[2].equals(Constants.p) || args[2].equals(Constants.d)) && !args[0].equals(args[2])))
        {
        	System.out.println("Usage: java ChatServer -p #port -d #debug_level");
            System.exit(0);
        }
        int port = 0;
    	int debug_level = 0;
        try
        {
        	if(args[0].equals(Constants.p))
        	{
        		port = Integer.parseInt(args[1]);
        		// Debug level 0 is normal only error are reported
                // Debug level 1 shows all the events
                debug_level = Integer.parseInt(args[3]);
        	}
        	else
        	{
        		port = Integer.parseInt(args[3]);
        		// Debug level 0 is normal only error are reported
                // Debug level 1 shows all the events
                debug_level = Integer.parseInt(args[1]);
        	}
        }
        catch(NumberFormatException e)
        { 
        	System.out.println("NumberFormatException occured: " + e.getStackTrace());
        	System.out.println("Usage: java ChatServer -p #port -d #debug_level");
            System.exit(0); 
        }
        catch(NullPointerException e)
        {
        	System.out.println("NullPointerException occured: " + e.getStackTrace());
        	System.out.println("Usage: java ChatServer -p #port -d #debug_level");
            System.exit(0); 
        }
        
        //Create chat server socket
        ChatServer cs = new ChatServer(port, debug_level);
        
        
        //Adding shut down hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    cs.log.info("Shutting down ...");
                    cs.SendShutDownMessage();
                    // If shutdown we will close all the thread
                    cs.threadPool.shutdown();
                    // Wait until all threads shut down
                    while (!cs.threadPool.isTerminated()) {   }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });
        
        
        cs.runServer();
    }
    
    private void SendShutDownMessage()
    {
    	String [] channelList = database.GetChannelNames();
    	
    	String GlobalChannelAddress = Constants.GlobalChannelAddress;
    	for(int i=0;i<channelList.length;i++)
    	{
    		int portNo = database.getChannelPort(channelList[i]);
    		
    		try {
				InetAddress inetAddress = InetAddress.getByName(GlobalChannelAddress);
				
				MulticastSocket newMultiCast = new MulticastSocket(portNo);
				newMultiCast.joinGroup(inetAddress);
				
				
				String message = "Server is shutting down";
				byte[] buffer = message.getBytes();
				DatagramPacket datagram = new DatagramPacket(buffer,buffer.length, inetAddress, portNo); 
	            
				newMultiCast.send(datagram);
				
				
				newMultiCast.leaveGroup(inetAddress);
				newMultiCast.close();
				
			}
    		catch (UnknownHostException e)
    		{
                log.warning(e.toString());
			}
    		catch (IOException e)
    		{
                log.warning(e.toString());
			}
			
			
    	}
    }


}