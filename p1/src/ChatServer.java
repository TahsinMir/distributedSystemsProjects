
public class ChatServer {

    public static void main(String args[]){
        if(args.length != 2){
            System.out.println("Usage: java ChatServer -p #port -d #debug_level");
            System.exit(0);
        }
        int port = Integer.parseInt(args[1]);
        // Debug level 0 is normal only error are reported
        // Debug level 1 shows all the events
        int debug_level = Integer.parseInt(args[2]);
        //Create server socket

    }
}
