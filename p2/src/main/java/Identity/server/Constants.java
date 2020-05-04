package Identity.server;

public class Constants
{
	//all the commands
	public static String loginName = "loginName";
	public static String uuid = "uuid";
	public static String password = "password";
	public static String ipAddress = "ipAddress";
	public static String date = "date";
	public static String time = "time";
	public static String realUserName = "realUserName";
	public static String lastChangeDate = "lastChangeDate";
	
	//command helpers
	public static String success = "success";
	public static String failure = "failure";
	public static String colon = ": ";
	public static String dbChecking = "database checking";
	public static String insertion = "insertion";
	public static String deletion = "deletion";
	public static String update = "update";
	public static String userSearch = "user search";
	public static String checkPassword = "check password";
	public static String passwordChange = "password change";
	public static String getList = "get list";
	public static String getAll = "get all";
	public static String during = "during";
	public static String space = " ";
	public static String in = "in";
	public static String disk = "disk";
	public static String memory = "memory";
	
	
	//non-error messages
	public static String dataInserted = "New user inserted into database";
	public static String dataDeleted = "User deleted from database";
	public static String dataUpdated = "User data updated successfully";
	public static String dataFound = "User data found";
	public static String passwordMatch = "Password match";
	public static String listRetrived = "List retrived of type ";
	public static String allDataRetrieved = "All data retrieved";
	public static String connectionClosed = "Database connection closed";
	public static String dataDeletedFromMemory = "Data deleted from memory";
	public static String dataDeletedFromDisk = "Data deleted from disk";
	//error messages
	public static String userAlreadyExists = "User with same login name or uuid already exists";
	public static String userNotFound = "User information not found";
	public static String userDeletionFailed = "User deletion failed";
	public static String userInsertionFailed = "User insertion failed";
	public static String userUpdateFailed = "User information update failed";
	public static String wrongPassword = "Wrong password";
	public static String sqlException = "SQLException occured during ";
	public static String errorOccured = "Error occured during ";
	public static String parseException = "ParseException occured during ";
	public static String invalidKey = "Invalid key for ";
	public static String passwordMismatch = "Password don't match";
	public static String listRetrivalFailed = "List retrival failed of type ";
	public static String connectionClosingFailure = "Failed to close connection";
	
	//type of arguments for getting user list
	public static String users = "users";
	public static String uuids = "uuids";
	public static String all = "all";
	
	//election algorithm
	public static String iAmHere = "i-am-here";
	public static String iAmCoordinator = "i-am-coordinator";
	public static String doElection = "do-election";
	public static String keepRunningElection = "keep-running-election";
	public static String True = "true";
	public static String False = "false";
	public static String nullValue = "NULL";
	public static int limit = 6;
}