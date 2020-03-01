import java.net.Socket;
import java.net.SocketException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInput;

public class ServerConnection extends  Thread {

    private Socket client;

    ServerConnection(Socket client) throws SocketException
    {
        this.client = client;
        setPriority(NORM_PRIORITY - 1);
        System.out.println("Created thread " + this.getName());
        
        facebook = new Channel("facebook");
        
        System.out.println("created channel");
    }

    public void run(){
        try {
    public void run()
    {
        try 
        {
        	while(true)
        	{
        		in = client.getInputStream();
				out = client.getOutputStream();
				
				//client goes first, and then goes the server, the one with the lower value wins
				ObjectInputStream oin = new ObjectInputStream(in);
				IRCMessage res = (IRCMessage) oin.readObject();
				
				System.out.println("received IRC message: ");
				PrintIRCCommand(res);
				
				IRCMessage response = PrepareResponse(res);
				
				ObjectOutputStream oout = new ObjectOutputStream(out);
				oout.writeObject(response);
				oout.flush();
        	}
    public void run(){
        try {
            // Variables to read and write object.
            // These variables will be used to communicate with server.

        }
        catch (IOException e)
        {
            System.out.println("I/O error " + e);
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private IRCMessage PrepareResponse(IRCMessage command)
    {
    	IRCMessage response = new IRCMessage();
    	
    	response.isServerResponse = true;
    	response.isClientRequest = false;
    	response.isCommand = false;
    	response.error = false;
    	
    	if(command.commandType == Constants.join || command.commandType.equals(Constants.join))
    	{
    		facebook.users.add(new User(command.nickName));
    		System.out.println("sample on storing a user into a new channel");
    	}
    	
    	response.responseMessage = "order executed";
    	
    	return response;
    }
}
