package Identity.client;

import Identity.server.User;
import Identity.server.IdServerInterface;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Option;

import java.io.InputStream;
import java.util.logging.Logger;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/***
 * Represents the IdClient
 */
public class IdClient {
    private String serverHost;
    private int port;
    private String createLoginName;
    private String realName;
    private String password;
    private String lookUpQuery;
    private String reverseLookUpQuery;
    private String oldLoginName;
    private String newLoginName;
    private String deleteLoginName;
    private String getCommand;

    public IdClient()
    {
       
    }



    /**
	   * makes all the option for command line.
	   * @param nothing.
	   * @return An Option instance.
	   */
    public static Options makeOption(){
        Options options = new Options();
        //Adding the command line options
        options.addOption("s", "server", true, "The name of the server host");
        options.addOption("n", "numport", true, "The port to be connected with the server");

        options.addOption("p", "password", true, "Password for the user");
        options.addOption("l", "lookup", true, "look up for user using the userName");
        options.addOption("rl", "reverse-lookup", true, "look up for user using the UUID");

        options.addOption("d", "delete", true, "Delete user from the server <UserName>");
        options.addOption("g", "get", true, "get users | UUID | all");
        options.addOption("h", "help", false, "Print this message");
        Option create = new Option("c", "create", true, "Create a new login name. pass login name and real name <login_name> [<real_name>]");
        create.setArgs(2); // First one is for login name the rest are for full name
        create.setArgName("loginName");
        options.addOption(create);

        Option modify = new Option("m", "modify", true, "Modify username <previousUserName> <newUserName> ");
        modify.setArgs(2);
        options.addOption(modify);
        return options;
    }

    /**
	   * extracts all the options from command line.
	   * @param options - Options instance.
	   * @param args - the arguments.
	   * @return nothing.
	   */
    private void extractOptions(Options options, String[] args){
        //We will validate the options here
        try{
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse( options, args);

            HelpFormatter formatter = new HelpFormatter();

            if(cmd.hasOption("help")){
                formatter.printHelp("IdClient", options);
            }

            if(cmd.hasOption("server") && cmd.hasOption("numport")){
                serverHost = cmd.getOptionValue("server");
                port = Integer.parseInt(cmd.getOptionValue("numport"));
            }else{
                System.err.println("Server host name and port is required");
                System.exit(0);
            }

            if(cmd.hasOption("password")){
                password = cmd.getOptionValue("password");
            }

            if(cmd.hasOption("create")){
                // If client wants to create new login name he/she has to pass the loginname and password
                String[] createValue = cmd.getOptionValues("create");
                createLoginName = createValue[0];
                if(createValue.length == 2)
                    realName = createValue[1];
            }

            if(cmd.hasOption("lookup")){
                lookUpQuery = cmd.getOptionValue("lookup");
            }
            if(cmd.hasOption("reverse-lookup")){
                reverseLookUpQuery = cmd.getOptionValue("reverse-lookup");
            }
            if(cmd.hasOption("delete")){
                //password is required for this operation
                if(!cmd.hasOption("password")){
                    System.err.println("A valid password is required for delete operation");
                }else{
                    deleteLoginName = cmd.getOptionValue("delete");
                }
            }
            if(cmd.hasOption("get")){
                // This argument can have these three value: users | UUID | all
                String getOptionsValue = cmd.getOptionValue("get");
                if(getOptionsValue.equals("users") || getOptionsValue.equals("uuids") || getOptionsValue.equals("all")){
                    getCommand = getOptionsValue;
                }else{
                    System.err.println("get can have either of these value: users | uuids | all");
                }
            }
            if(cmd.hasOption("modify")){
                String[] modifyOptions = cmd.getOptionValues("modify");

                if(modifyOptions.length == 2){
                    oldLoginName = modifyOptions[0];
                    newLoginName = modifyOptions[1];
                }else{
                    System.err.println("Modified user name is required");
                }
            }

        } catch (ParseException e){
            System.err.println("Error during parsing: " + e.getStackTrace());
        }
    }

    /**
	   * establishes the connection to the server.
	   * @param nothing.
	   * @return nothing.
	   */
    private void connectServer(){
        try{
            Registry registry = LocateRegistry.getRegistry(serverHost, port);
            IdServerInterface stub = (IdServerInterface) registry.lookup("IdServer");
            executeCommand(stub);
        } catch (Exception e){
        	System.err.println("Failed to connect to the server: " + e.getStackTrace());
        	e.printStackTrace();
        }
    }

    /**
	   * generates the password hash.
	   * @param password - the original password.
	   * @return password hash.
	   */
    private String getHash(String password)  {
        MessageDigest md;
        try{
            md = MessageDigest.getInstance("SHA-256");
        } catch(NoSuchAlgorithmException e){
            return "-1";
        }

        byte[] passbyte = md.digest(password.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, passbyte);
        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32){
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }

    /**
	   * executes the possible commands with server.
	   * @param stub - IdServerInterface instance.
	   * @return nothing.
	   * @exception RemoteException
	   * @see RemoteException
	   */
    private void executeCommand(IdServerInterface stub) throws RemoteException{
        //createLoginName is saved so we need to create a user
        String serverResponse;
        if (createLoginName != null){
            serverResponse = stub.create(createLoginName, realName, getHash(password), serverHost);
            System.out.println(serverResponse);
        }
        if (lookUpQuery != null){
            // This is a lookup query
            // Lookupquery should contain the loginName
            User searchedUser = stub.lookup(lookUpQuery);
            System.out.println(searchedUser);
        }
        if (reverseLookUpQuery != null){
            // This is a lookup query
            // Lookupquery should contain the loginName
            User searchedUser = stub.reverseLookUp(reverseLookUpQuery);
            System.out.println(searchedUser);
        }
        if(oldLoginName != null){
            // This is modify command
            serverResponse = stub.modify(oldLoginName, newLoginName, getHash(password));
            System.out.println(serverResponse);
        }
        if(deleteLoginName != null){
             serverResponse = stub.delete(deleteLoginName, getHash(password));
             System.out.println(serverResponse);
        }
        if(getCommand != null){
            List<String> ServerResponses = stub.get(getCommand);
            ServerResponses.forEach(System.out::println);
        }

    }

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        //System.out.println(System.getProperty("user.dir"));
        System.setProperty("javax.net.ssl.trustStore", "/p2/security/Client_Truststore");
        System.setProperty("java.security.policy", "/p2/security/mysecurity.policy");
        System.setProperty("javax.net.ssl.trustStorePassword", "test123");

        Options options = makeOption();

        IdClient client = new IdClient();
        client.extractOptions(options, args);
        client.connectServer();
    }
}
