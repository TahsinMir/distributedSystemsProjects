import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import java.io.PrintWriter;

public class IdClient {

    public static Options makeOption(){
        Options options = new Options();
        //Adding the command line options
        options.addRequiredOption("s", "server", true, "The name of the server host");
        options.addRequiredOption("p", "port", true, "The port to be connected with the server");
        options.addOption("c", "create", true, "Create a new login name. pass login name and real name <login_name> [<real_name>]");
        options.addOption("p", "password", true, "Password for the user");
        options.addOption("l", "lookup", true, "look up for user using the userName");
        options.addOption("rl", "reverse-lookup", true, "look up for user using the UUID");
        options.addOption("m", "modify", true, "Modify username <previousUserName> <newUserName> ");
        options.addOption("d", "delete", true, "Delete user from the server <UserName>");
        options.addOption("g", "get", true, "get users | UUID | all");
        options.addOption("h", "help", false, "Print this message");
        return options;
    }

    public static boolean validateOptions(Options options){
        //We will validate the options here
        return true;
    }

    public static void main(String args[]){
        Options options = makeOption();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("IdClient", options);
    }
}
