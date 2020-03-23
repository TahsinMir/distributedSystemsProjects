import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Option;
import java.io.PrintWriter;

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



    public static Options makeOption(){
        Options options = new Options();
        //Adding the command line options
        options.addRequiredOption("s", "server", true, "The name of the server host");
        options.addRequiredOption("n", "numport", true, "The port to be connected with the server");

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

    public void extractOptions(Options options, String[] args){
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
                formatter.printHelp("IdClient", options);
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
                if(getOptionsValue.equals("users") || getOptionsValue.equals("UUID") || getOptionsValue.equals("all")){
                    getCommand = getOptionsValue;
                }else{
                    System.err.println("get can have either of these value: users | UUID | all");
                }
            }

        } catch (ParseException e){
            System.err.println("Error during parsing " + e.toString());
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Options options = makeOption();

        IdClient client = new IdClient();
        client.extractOptions(options, args);


    }
}
