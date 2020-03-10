import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/***
 * Represents a chat server
 */
public class ChatServer {
	private final int NUMBER_OF_THREAD_FOR_THREADPOOL = 4;	//limiting the number of threads using threadpool
	// These channel will be created during the server creation process
	String[] defaultChannel = {"Python", "Java", "C/C++", "PHP", "JavaScript"};		//available sample channels in the server
	ServerSocket serverSocket;
	int debugLevel;	//holds the debug level
	ServerDatabase database;
	Timer timer;	//used to check if there is any activity with the clients, otherwise the server will shut down in 5 minutes

	ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREAD_FOR_THREADPOOL);
	private Logger log = Logger.getLogger(ChatServer.class.getName());

    /***
     * Create the server socket
     * Start the timer for shutting down the server after 5 minutes.
     * @param port
     * @param debugLevel
     */
	public ChatServer(int port, int debugLevel) {
		log.setLevel(debugLevel == 1 ? Level.ALL : Level.OFF);
		this.debugLevel = debugLevel;
		try {
			//Initial channel created by the server;
			database = new ServerDatabase(debugLevel);
			// some example channel created for the upcoming users
			for (int i = 0; i < defaultChannel.length; i++) {
				database.CreateChannel(defaultChannel[i]);
			}
			serverSocket = new ServerSocket(port);
			this.timer = Constants.SetTimer(this.timer, ProcessHandle.current().pid());
			System.out.println("ChatServer is up and running on port " + port + " " + InetAddress.getLocalHost());
		} catch (IOException e) {
			log.warning("Server creation problem");
		}
	}

    /***
     * Entry point of chatServer program. It will extract the port and debug level and start using those.
     * @param args
     */
	public static void main(String args[]) {
		if (args.length != 4) {
			System.out.println("Usage: java ChatServer -p #port -d #debug_level");
			System.exit(0);
		} else if (!((args[0].equals(Constants.p) || args[0].equals(Constants.d)) && (args[2].equals(Constants.p) || args[2].equals(Constants.d)) && !args[0].equals(args[2]))) {
			System.out.println("Usage: java ChatServer -p #port -d #debug_level");
			System.exit(0);
		}
		int port = 0;
		int debug_level = 0;
		try {
			if (args[0].equals(Constants.p)) {
				port = Integer.parseInt(args[1]);
				debug_level = Integer.parseInt(args[3]);
			} else {
				port = Integer.parseInt(args[3]);
				debug_level = Integer.parseInt(args[1]);
			}
		} catch (NumberFormatException e) {
			System.out.println("NumberFormatException occured: " + e.getStackTrace());
			System.out.println("Usage: java ChatServer -p #port -d #debug_level");
			System.exit(0);
		} catch (NullPointerException e) {
			System.out.println("NullPointerException occured: " + e.getStackTrace());
			System.out.println("Usage: java ChatServer -p #port -d #debug_level");
			System.exit(0);
		}

		//Create chat server socket
		ChatServer cs = new ChatServer(port, debug_level);


		//Adding shut down hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				cs.log.info("Shutting down ...");
					
					cs.SendShutDownMessage();
					// If shutdown we will close all the thread
					cs.threadPool.shutdownNow();
					// Wait until all threads shut down
					while (!cs.threadPool.isShutdown()) {
					}
			}
		});
		cs.runServer();
	}

    /***
     * Take each individual client request and put that on the threadpool queue
     */
	public void runServer() {
		Socket client;
		while (true) {
			try {
				client = serverSocket.accept();
				log.info("Received connect from " + client.getInetAddress().getHostName() + " [ " + client.getInetAddress().getHostAddress() + " ] ");
				threadPool.execute(new ServerConnection(client, database, timer, debugLevel));
			} catch (IOException e) {
				log.warning("IOException occur during accepting client request");
			}
		}
	}

    /***
     * Send message to all the available channel that the server is shutting down.
     */
	private void SendShutDownMessage() {
		String[] channelList = database.GetChannelNames();
		String GlobalChannelAddress = Constants.GlobalChannelAddress;
		for (int i = 0; i < channelList.length; i++) {
			int portNo = database.getChannelPort(channelList[i]);

			try {
				InetAddress inetAddress = InetAddress.getByName(GlobalChannelAddress);

				MulticastSocket newMultiCast = new MulticastSocket(portNo);
				newMultiCast.joinGroup(inetAddress);


				String message = "Server is shutting down";
				byte[] buffer = message.getBytes();
				DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, inetAddress, portNo);

				newMultiCast.send(datagram);


				newMultiCast.leaveGroup(inetAddress);
				newMultiCast.close();

			} catch (UnknownHostException e) {
				log.warning(e.toString());
			} catch (IOException e) {
				log.warning(e.toString());
			}
		}
	}
}