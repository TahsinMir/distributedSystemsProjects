import java.net.Socket;
import java.net.SocketException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectInput;

public class ServerConnection extends  Thread {

    private Socket client;

    ServerConnection(Socket client) throws SocketException {
        this.client = client;
        setPriority(NORM_PRIORITY - 1);
        System.out.println("Created thread " + this.getName());
    }

    public void run(){
        try {
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            ObjectInput oin = new ObjectInputStream(in);

            Date date = (Date) oin.readObject();

            oout.writeObject(new java.util.Date());
            oout.flush();

            client.close();
        } catch (IOException e) {
            System.out.println("I/O error " + e);
        }
    }
}
