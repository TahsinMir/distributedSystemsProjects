import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.HashSet;


public class ChatServer
{
    ServerSocket serverSocket;
    int debug_level;
    ServerDatabase database;


    public ChatServer(int port, int debug_level)
    {
        try
        {
            serverSocket = new ServerSocket(port);
            System.out.println("ChatServer is up and running on port " + port + " " + InetAddress.getLocalHost());
            this.debug_level = debug_level;
            
            database = new ServerDatabase();
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }

    public void runServer()
    {
        Socket client;
        try
        {
        	client = serverSocket.accept();
            System.out.println("Received connect from " + client.getInetAddress().getHostName() + " [ " + client.getInetAddress().getHostAddress() + " ] ");
            new ServerConnection(client).start();
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }

    public static void main(String args[])
    {
        if(args.length != 2)
        {
            System.out.println("Usage: java ChatServer #port #debug_level");
            System.exit(0);
        }
        int port = Integer.parseInt(args[0]);
        // Debug level 0 is normal only error are reported
        // Debug level 1 shows all the events
        int debug_level = Integer.parseInt(args[1]);
        //Create chat server socket
        ChatServer cs = new ChatServer(port, debug_level);
        cs.runServer();
    }
}
