import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;

public class ChatServer {
    ServerSocket ss;
    int debug_level;

    public ChatServer(int port, int debug_level) {
        try {
            ss = new ServerSocket(port);
            System.out.println("ChatServer is up and running on port " + port + " " + InetAddress.getLocalHost());
            this.debug_level = debug_level;
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void main(String args[]){
        if(args.length != 2){
            System.out.println("Usage: java ChatServer -p #port -d #debug_level");
            System.exit(0);
        }
        int port = Integer.parseInt(args[1]);
        // Debug level 0 is normal only error are reported
        // Debug level 1 shows all the events
        int debug_level = Integer.parseInt(args[2]);
        //Create chat server socket
        ChatServer cs = new ChatServer(port, debug_level);


    }
}
