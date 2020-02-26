import java.util.Scanner;

public class ChatClient
{
	public static void main(String args[])
	{
        if(args.length != 02){
            System.out.println("ChatClient takes zero argument");
            System.exit(0);
        }
        
        Scanner scan = new Scanner(System.in);
        
        while(true)
        {
        	String line = scan.nextLine();
        	
        	String[] splitted = line.split("\\s+");
        	
        	if(splitted.length == 0)
        	{
        		System.out.println("Invalid Command");
        	}
        	else if(splitted[0] == Constants.connect)
        	{
        		if(splitted.length != 2)
        		{
        			System.out.println("Invalid Request");
        		}
        	}
        	else if(splitted[0] == Constants.nick)
        	{
        		if(splitted.length != 2)
        		{
        			System.out.println("Invalid Request");
        		}
        	}
        	else if(splitted[0] == Constants.list)
        	{
        		if(splitted.length != 1)
        		{
        			System.out.println("Invalid Request");
        		}
        	}
        	else if(splitted[0] == Constants.join)
        	{
        		if(splitted.length != 2)
        		{
        			System.out.println("Invalid Request");
        		}
        	}
        	else if(splitted[0] == Constants.leave)
        	{
        		
        	}
        	else if(splitted[0] == Constants.quit)
        	{
        		if(splitted.length != 1)
        		{
        			System.out.println("Invalid Request");
        		}
        	}
        	else if(splitted[0] == Constants.help)
        	{
        		if(splitted.length != 1)
        		{
        			System.out.println("Invalid Request");
        		}
        	}
        	else if(splitted[0] == Constants.stats)
        	{
        		if(splitted.length != 1)
        		{
        			System.out.println("Invalid Request");
        		}
        	}
        	else	//it is a plain message from the client
        	{
        		
        	}
        }

    }
}
