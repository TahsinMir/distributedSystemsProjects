package Identity.server;

public class Constants
{
	public static String loginName = "loginName";
	public static String uuid = "uuid";
	public static String password = "password";
	public static String ipAddress = "ipAddress";
	public static String date = "date";
	public static String time = "time";
	public static String realUserName = "realUserName";
	public static String lastChangeDate = "lastChangeDate";
	
	public static String success = "success";
	public static String failure = "failure";
	public static String colon = ": ";
	public static String dbChecking = "database checking";
	public static String insertion = "insertion";
	public static String deletion = "deletion";
	public static String update = "update";
	public static String userSearch = "user search";
	public static String during = "during";
	public static String space = " ";
	
	
	//non-error messages
	public static String dataInserted = "New user inserted into database";
	public static String dataDeleted = "User deleted from database";
	public static String dataUpdated = "User data updated successfully";
	public static String dataFound = "User data found";
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
	
	public static String users = "users";
	public static String uuids = "uuids";
	public static String all = "all";
}