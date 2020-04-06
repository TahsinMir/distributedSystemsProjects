package Identity.server;

  
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/***
 * Represents the database
 */
public class Database
{
	private	Hashtable<String, String> Users;  // login-name, uuid
	private Hashtable<String, User> UserInfo;	// uuid, all-user-information
	
	private Connection connection = null;	//connection to the database
	private Statement statement;	//The statement to be executed in the database
	Logger log;		//the logger
	
	/***
     * Creates the connection to the database
     * @param logger
     */
	public Database(Logger logger)
	{
		Users = new Hashtable<>();
		UserInfo = new Hashtable<>();
		
		
		log = logger;
		try
		{
			//established sqlite database connection
			connection = DriverManager.getConnection("jdbc:sqlite:userinfo.db");
			statement = connection.createStatement();
			statement.setQueryTimeout(500);
			log.info("Database connection established");
			
			//creates the initial table if it doesn't already exist
			statement.executeUpdate("create table if not exists user (loginName string, uuid string, password string, ipAddress string, date string, time string, realUserName string, lastChangeDate string)");
			log.info("Initial table created");
		}
		catch (SQLException e)
		{
			log.warning("SQLException during setting up database connection: " + e.getStackTrace().toString());
		}
		
	}
	
	/**
	   * inserts a new user into the database.
	   * @param LoginName - the login name of the user.
	   * @param uuid - the uuid of the user.
	   * @param password - the password hash of the user.
	   * @param ipAddress - the creation ip address of the user.
	   * @param date - the date when the user is created.
	   * @param time - the time when the user is created.
	   * @param realUserName - the real name of the user.
	   * @param lastChangeDate - the last date when the user data is changed
	   * @return A string explaining the result of the user insertion operation.
	   */
	public String Insert(String loginName, String uuid, String password, String ipAddress, String date, String time, String realUserName, String lastChangeDate)
	{
		//first we check whether information about the same loginName or UUID already exists in the HashMap
		if(Users.containsKey(loginName) || UserInfo.containsKey(uuid))
		{
			log.warning(Constants.userAlreadyExists);
			return Constants.failure + Constants.colon + Constants.userAlreadyExists;
		}
		
		try
		{
			//next we check whether information about the same loginName or UUID already exists in disk
			ResultSet checkExist = statement.executeQuery("select * from user where " + "loginName='" + loginName + "' or uuid='" + uuid + "';");
			
			//if information already exists, we do not insert another new user with the same login name or uuid
			if(checkExist.next())
			{
				log.warning(Constants.userAlreadyExists);
				return Constants.failure + Constants.colon + Constants.userAlreadyExists;
			}
		}
		catch (SQLException e)
		{
			log.warning(Constants.sqlException + Constants.dbChecking);
			return Constants.failure + Constants.colon + Constants.errorOccured + Constants.insertion;
		}
		
		//Inserting info in the HashMaps
		try
		{
			Users.put(loginName, uuid);
			//String loginName, UUID uuid, String passwordHash, String creationIpAddress, Date createdDate, Time createdTime, String realName, Date lastChangeDate
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
			User user = new User(loginName, UUID.fromString(uuid), password, ipAddress, dateFormatter.parse(date), Time.valueOf(LocalTime.parse(time)), realUserName, dateFormatter.parse(lastChangeDate));
			UserInfo.put(uuid, user);
		}
		catch (ParseException e)
		{
			log.warning(Constants.parseException + Constants.insertion + Constants.colon + e.getStackTrace());
			return Constants.failure + Constants.colon + Constants.errorOccured + Constants.insertion;
		}
		
		//Storing the information in Disk
		//Insertion sql query
		String query = "insert into user values('" + loginName + "', '" + uuid + "', '" + password + "', '" + ipAddress + "', '" + date + "', '" + time + "', '" + realUserName + "', '" + lastChangeDate + "')";
		
		try
		{
			//inserting user data
			statement.executeUpdate(query);
			log.info(Constants.dataInserted + Constants.colon + loginName);
			return Constants.success + Constants.colon + Constants.dataInserted + Constants.colon + loginName;
		}
		catch (SQLException e)
		{
			log.warning(Constants.sqlException + Constants.insertion + Constants.colon + e.getStackTrace().toString());
			return Constants.failure + Constants.colon + Constants.errorOccured + Constants.insertion;
		}
	}
	
	/**
	   * deletes user into from the memory and the database.
	   * @param key - the type of key to be used to delete the user.
	   * @param value - the value of the key type to be used to delete the user.
	   * @return A string explaining the result of the user deletion operation.
	   */
	public String Delete(String key, String value)
	{
		if(!(key.equals(Constants.loginName) || key.equals(Constants.uuid)))
		{
			System.out.println(Constants.invalidKey +  Constants.deletion);
			return Constants.failure + Constants.colon + Constants.invalidKey +  Constants.deletion;
		}
		
		//chacking whether the data already exists or not for deletion
		try
		{
			ResultSet checkExist = statement.executeQuery("select * from user where " + key + "='" + value + "';");
			
			if(!checkExist.next())
			{
				log.warning(Constants.userNotFound + Constants.space + Constants.during + Constants.space + Constants.deletion);
				return Constants.failure + Constants.colon + Constants.userNotFound + Constants.space + Constants.during + Constants.space + Constants.deletion;
			}
		}
		catch (SQLException e)
		{
			log.warning(Constants.sqlException + Constants.dbChecking);
			return Constants.failure + Constants.colon + Constants.errorOccured + Constants.deletion;
		}
		
		//delete user from disk and HashMap
		String query = "delete from user where " + key + "='" + value + "';";
		
		try
		{
			//from disk
			statement.executeUpdate(query);
			log.info(Constants.dataDeletedFromDisk);
			
			//from memory
			try
			{
				if(key.equals(Constants.loginName))
				{
					String uuid = Users.get(value);
					Users.remove(value);
					UserInfo.remove(uuid);
					log.info(Constants.dataDeletedFromMemory);
				}
				else if(key.equals(Constants.uuid))
				{
					User user = UserInfo.get(value);
					Users.remove(user.getLoginName());
					UserInfo.remove(value);
					log.info(Constants.dataDeletedFromMemory);
				}
			}
			catch(NullPointerException e)
			{
				log.info(Constants.userNotFound + Constants.space + Constants.during + Constants.space + Constants.deletion);
			}
			
			return Constants.success + Constants.colon + Constants.dataDeleted + Constants.colon + value;
		}
		catch (SQLException e)
		{
			log.warning(Constants.sqlException + Constants.deletion + Constants.colon + e.getStackTrace().toString());
			return Constants.failure + Constants.colon + Constants.errorOccured + Constants.deletion;
		}
	}
	
	/**
	   * searches user into from the memory and the database.
	   * @param key - the type of key to be used to search the user.
	   * @param value - the value of the key type to be used to search the user.
	   * @return User info except password.
	   */
	public User Search(String key, String value)
	{
		// check key compatibility
		if(!(key.equals(Constants.loginName) || key.equals(Constants.uuid)))
		{
			log.warning(Constants.invalidKey +  Constants.userSearch);
			User user = new User(Constants.failure + Constants.colon + Constants.invalidKey +  Constants.userSearch);
			return user;
		}
		
		//check in memory
		if(key.equals(Constants.loginName))
		{
			String uuid = Users.get(value);
			
			if(uuid != null)
			{
				User user = UserInfo.get(uuid);
				
				if(user != null)
				{
					//String loginName, UUID uuid, String creationIpAddress, Date createdDate, Time createdTime, String realName, Date lastChangeDate, String modificationMessage
					User result = new User(user.getLoginName(), user.getUuid(), user.getCreationIpAddress(), user.getCreatedDate(), user.getCreatedTime(), user.getRealName(), user.getLastChangeDate(), Constants.success + Constants.colon + Constants.dataFound);
					log.info(Constants.dataFound + Constants.space + Constants.in + Constants.space + Constants.memory);
					
					return result;
				}
			}
		}
		else if(key.equals(Constants.uuid))
		{
			User user = UserInfo.get(value);
			
			if(user != null)
			{
				//String loginName, UUID uuid, String creationIpAddress, Date createdDate, Time createdTime, String realName, Date lastChangeDate, String modificationMessage
				User result = new User(user.getLoginName(), user.getUuid(), user.getCreationIpAddress(), user.getCreatedDate(), user.getCreatedTime(), user.getRealName(), user.getLastChangeDate(), Constants.success + Constants.colon + Constants.dataFound);
				log.info(Constants.dataFound + Constants.space + Constants.in + Constants.space + Constants.memory);
				
				return result;
			}
		}
		
		//if not found in memory, check in disk
		try
		{
			ResultSet result = statement.executeQuery("select * from user where " + key + "='" + value + "';");
			
			if(!result.next())
			{
				log.warning(Constants.userNotFound);
				User user = new User(Constants.failure + Constants.colon + Constants.userNotFound);
				return user;
			}
			
			String loginName = result.getString(Constants.loginName);
			String uuidStr = result.getString(Constants.uuid);
			UUID uuid = UUID.fromString(uuidStr);
			String creationIpAddress = result.getString(Constants.ipAddress);
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
			Date createdDate = dateFormatter.parse(result.getString(Constants.date));
			LocalTime createTime = LocalTime.parse(result.getString(Constants.time));
			Time createTimeSqlTime = Time.valueOf(createTime);
			String realName = result.getString(Constants.realUserName);
			Date lastChangeDate = dateFormatter.parse(result.getString(Constants.lastChangeDate));
			User user = new User(loginName, uuid, creationIpAddress, createdDate, createTimeSqlTime, realName, lastChangeDate, Constants.success + Constants.colon + Constants.dataFound);	//String loginName, String uuid, String creationIpAddress, Date createdDate, Time createdTime, String realName, Date lastChangeDate
			
			log.info(Constants.dataFound + Constants.space + Constants.in + Constants.space + Constants.disk);
			return user;
		}
		catch (SQLException e)
		{
			log.warning(Constants.sqlException + Constants.userSearch + Constants.colon + e.getStackTrace().toString());
			User user = new User(Constants.failure + Constants.colon + Constants.errorOccured + Constants.userSearch);
			return user;
		}
		catch(ParseException e)
		{
			log.warning(Constants.parseException + Constants.userSearch + Constants.colon + e.getStackTrace());
			User user = new User(Constants.failure + Constants.colon + Constants.errorOccured + Constants.userSearch);
			return user;
		}
	}
	
	
	/**
	   * updates user into in the memory and the database.
	   * @param keyType - the type of key to be used to update the user.
	   * @param keyValue - the value of the key type to be used to update the user.
	   * @param changeType - the type of the field to be used to update the user.
	   * @param changeValue - the value of the change type field to be used to update the user.
	   * @return A string explaining the result of the user update operation..
	   */
	public String Update(String keyType, String keyValue, String changeType, String changeValue)
	{
		// check key compatibility
		if(!(keyType.equals(Constants.loginName) || keyType.equals(Constants.uuid)))
		{
			log.warning(Constants.invalidKey +  Constants.update);
			return Constants.failure + Constants.colon + Constants.invalidKey +  Constants.update;
		}
		
		try
		{
			ResultSet result = statement.executeQuery("select * from user where " + keyType + "='" + keyValue + "';");
			
			if(!result.next())
			{
				log.warning(Constants.userNotFound + Constants.during + Constants.update);
				return Constants.failure + Constants.colon  + Constants.userNotFound + Constants.during + Constants.update;
			}
			
			//update in memory and disk			
			String loginName = result.getString(Constants.loginName);
			String uuid = result.getString(Constants.uuid);
			String password = result.getString(Constants.password);
			String ipAddress = result.getString(Constants.ipAddress);
			String date = result.getString(Constants.date);
			String time = result.getString(Constants.time);
			String realUserName = result.getString(Constants.realUserName);
			String lastChangeDate = result.getString(Constants.lastChangeDate);
			
			String deletionResult = Delete(Constants.loginName, loginName);
			if(deletionResult.startsWith(Constants.failure))
			{
				log.warning(Constants.userDeletionFailed + Constants.space + Constants.during + Constants.space + Constants.update);
				return Constants.failure + Constants.colon + Constants.userUpdateFailed;
			}

			String addResult;
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
			
			if(changeType.equals(Constants.loginName))
			{
				addResult = Insert(changeValue, uuid, password, ipAddress, date, time, realUserName, lastChangeDate);
				Users.put(changeValue, uuid);
				UserInfo.put(uuid, new User(changeValue, UUID.fromString(uuid), password, ipAddress, dateFormatter.parse(date), Time.valueOf(LocalTime.parse(time)), realUserName, dateFormatter.parse(lastChangeDate)));
			}
			else if(changeType.equals(Constants.uuid))
			{
				addResult = Insert(loginName, changeValue, password, ipAddress, date, time, realUserName, lastChangeDate);
				Users.put(loginName, changeValue);
				UserInfo.put(changeValue, new User(loginName, UUID.fromString(changeValue), password, ipAddress, dateFormatter.parse(date), Time.valueOf(LocalTime.parse(time)), realUserName, dateFormatter.parse(lastChangeDate)));
			}
			else if(changeType.equals(Constants.password))
			{
				addResult = Insert(loginName, uuid, changeValue, ipAddress, date, time, realUserName, lastChangeDate);
				Users.put(loginName, uuid);
				UserInfo.put(uuid, new User(loginName, UUID.fromString(uuid), changeValue, ipAddress, dateFormatter.parse(date), Time.valueOf(LocalTime.parse(time)), realUserName, dateFormatter.parse(lastChangeDate)));
			}
			else if(changeType.equals(Constants.ipAddress))
			{
				addResult = Insert(loginName, uuid, password, changeValue, date, time, realUserName, lastChangeDate);
				Users.put(loginName, uuid);
				UserInfo.put(uuid, new User(loginName, UUID.fromString(uuid), password, changeValue, dateFormatter.parse(date), Time.valueOf(LocalTime.parse(time)), realUserName, dateFormatter.parse(lastChangeDate)));
			}
			else if(changeType.equals(Constants.date))
			{
				addResult = Insert(loginName, uuid, password, ipAddress, changeValue, time, realUserName, lastChangeDate);
				Users.put(loginName, uuid);
				UserInfo.put(uuid, new User(loginName, UUID.fromString(uuid), password, ipAddress, dateFormatter.parse(changeValue), Time.valueOf(LocalTime.parse(time)), realUserName, dateFormatter.parse(lastChangeDate)));
			}
			else if(changeType.equals(Constants.time))
			{
				addResult = Insert(loginName, uuid, password, ipAddress, date, changeValue, realUserName, lastChangeDate);
				Users.put(loginName, uuid);
				UserInfo.put(uuid, new User(loginName, UUID.fromString(uuid), password, ipAddress, dateFormatter.parse(date), Time.valueOf(LocalTime.parse(changeValue)), realUserName, dateFormatter.parse(lastChangeDate)));
			}
			else if(changeType.equals(Constants.realUserName))
			{
				addResult = Insert(loginName, uuid, password, ipAddress, date, time, changeValue, lastChangeDate);
				Users.put(loginName, uuid);
				UserInfo.put(uuid, new User(loginName, UUID.fromString(uuid), password, ipAddress, dateFormatter.parse(date), Time.valueOf(LocalTime.parse(time)), changeValue, dateFormatter.parse(lastChangeDate)));
			}
			else if(changeType.equals(Constants.lastChangeDate))
			{
				addResult = Insert(loginName, uuid, password, ipAddress, date, time, realUserName, changeValue);
				Users.put(loginName, uuid);
				UserInfo.put(uuid, new User(loginName, UUID.fromString(uuid), password, ipAddress, dateFormatter.parse(date), Time.valueOf(LocalTime.parse(time)), realUserName, dateFormatter.parse(changeValue)));
			}
			else
			{
				addResult = Constants.failure;
			}
			
			if(addResult.startsWith(Constants.failure))
			{
				log.warning(Constants.userInsertionFailed + Constants.space + Constants.during + Constants.update);
				return Constants.failure + Constants.colon + Constants.userUpdateFailed;
			}
		}
		catch(SQLException e)
		{
			log.warning(Constants.sqlException + Constants.update + Constants.colon + e.getStackTrace().toString());
			return Constants.failure + Constants.colon + Constants.errorOccured + Constants.update;
		}
		catch (ParseException e)
		{
			log.warning(Constants.parseException + Constants.update + Constants.colon + e.getStackTrace());
			return Constants.failure + Constants.colon + Constants.errorOccured + Constants.update;
		}
		
		log.info(Constants.dataUpdated);
		return Constants.success + Constants.colon + Constants.dataUpdated;
	}
	
	/**
	   * checks password of user.
	   * @param keyType - the type of key to be used to check the user.
	   * @param keyValue - the value of the key type to be used to check the user.
	   * @param password - the password hash of the user.
	   * @return A boolean value indicating the result of the user password check operation.
	   */
	public boolean CheckPassword(String keyType, String keyValue, String password)
	{
		// check key compatibility
		if(!(keyType.equals(Constants.loginName) || keyType.equals(Constants.uuid)))
		{
			log.warning(Constants.invalidKey +  Constants.checkPassword);
			return false;
		}
		
		try
		{
			ResultSet result = statement.executeQuery("select * from user where " + keyType + "='" + keyValue + "';");
			
			if(!result.next())
			{
				log.warning(Constants.userNotFound + Constants.space + Constants.during + Constants.space + Constants.checkPassword);
				return false;
			}
			
			String passwordFromDB = result.getString(Constants.password);
			
			if(passwordFromDB.equals(password))
			{
				log.info(Constants.passwordMatch);
				return true;
			}
			log.warning(Constants.passwordMismatch);
			return false;
		}
		catch (SQLException e)
		{
			log.warning(Constants.sqlException + Constants.checkPassword + Constants.colon + e.getStackTrace().toString());
			return false;
		}
	}
	
	/**
	   * gets user list.
	   * @param listType - the type of list being requested.
	   * @return A list containing user information.
	   */
	public List<String> GetList(String listType)
	{
		try
		{
			if(listType.equals(Constants.users))
			{
				ResultSet result = statement.executeQuery("select loginName from user");
				
				List<String> users = new ArrayList<String>();
				while(result.next())
				{
					users.add(result.getString(Constants.loginName));
				}
				
				log.info(Constants.listRetrived + listType);
				return users;
			}
			else if(listType.equals(Constants.uuids))
			{
				ResultSet result = statement.executeQuery("select uuid from user");
				
				List<String> uuids = new ArrayList<String>();
				while(result.next())
				{
					uuids.add(result.getString(Constants.uuid));
				}
				log.info(Constants.listRetrived + listType);
				return uuids;
			}
			else if(listType.equals(Constants.all))
			{
				ResultSet result = statement.executeQuery("select * from user");
				
				List<String> all = new ArrayList<String>();
				while(result.next())
				{
					String loginName = result.getString(Constants.loginName);
					String uuid = result.getString(Constants.uuid);
					String ipAddress = result.getString(Constants.ipAddress);
					String date = result.getString(Constants.date);
					String time = result.getString(Constants.time);
					String realUserName = result.getString(Constants.realUserName);
					String lastChangeDate = result.getString(Constants.lastChangeDate);
					
					String oneUser = Constants.loginName + ": " + loginName + ", " + Constants.uuid + ": " + uuid + ", " + Constants.ipAddress + ": " + ipAddress + ", " + Constants.date + ": " + date + ", " + Constants.time + ": " + time + ", " + Constants.realUserName + ": " + realUserName + ", " + Constants.lastChangeDate + ": " + lastChangeDate;
					all.add(oneUser);
				}
				
				log.info(Constants.listRetrived + listType);
				return all;
			}
			else
			{
				log.warning(Constants.listRetrivalFailed + listType);
				return null;
			}
		}
		catch (SQLException e)
		{
			log.warning(Constants.sqlException + Constants.getList + Constants.colon + e.getStackTrace().toString());
			return null;
		}
	}
	
	/**
	   * gets user list, not used by the server, used for testing purposes.
	   * @param nothing.
	   * @return All information.
	   */
	public ResultSet GetAll()
	{
		try
		{
			ResultSet result = statement.executeQuery("select * from user");
			log.info(Constants.allDataRetrieved);
			return result;
		}
		catch (SQLException e)
		{
			log.warning(Constants.sqlException + Constants.getAll + Constants.colon + e.getStackTrace().toString());
			return null;
		}
	}
	
	/**
	   * closes the db connection.
	   * @param nothing.
	   * @return A boolean value indicating the result of the db connection closing operation..
	   */
	public boolean CloseDB()
	{
		try
		{
			if(connection != null)
		          connection.close();
			
			log.info(Constants.connectionClosed);
			return true;
		}
		catch(SQLException e)
		{
			log.warning(Constants.connectionClosingFailure);
			return false;
		}
	}
	
	/**
	   * prints information.
	   * @param response - all the response info.
	   * @return nothing.
	   */
	public void printAll(ResultSet response)
	{
		try {
			while(response.next())
			{
				try
				{
					System.out.print(response.getString("loginName") + ", ");
					
					System.out.print(response.getString("uuid") + ", ");
					
					System.out.print(response.getString("password") + ", ");
					
					System.out.print(response.getString("ipAddress") + ", ");
					
					System.out.print(response.getString("date") + ", ");
					
					System.out.print(response.getString("time") + ", ");
					
					System.out.print(response.getString("realUserName") + ", ");
					
					System.out.print(response.getString("lastChangeDate"));
					
					System.out.println();
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
				
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void SaveAllToDisk()
	{
		Set<String> allKeys = UserInfo.keySet();
		
		for (String key : allKeys)
		{
	        User user = UserInfo.get(key);
	        
	        try
			{
				//next we check whether information about the same loginName or UUID already exists in disk
				ResultSet checkExist = statement.executeQuery("select * from user where " + "loginName='" + user.getLoginName() + "' or uuid='" + user.getUuid() + "';");
				
				//if information already exists, we do not insert another new user with the same login name or uuid
				if(checkExist.next())
				{
					log.warning(Constants.userAlreadyExists);
					continue;
				}
			}
			catch (SQLException e)
			{
				log.warning(Constants.sqlException + Constants.dbChecking);
				continue;
			}
	        
	        
	        //Storing the information in Disk
			//Insertion sql query
			String query = "insert into user values('" + user.getLoginName() + "', '" + user.getUuid() + "', '" + user.getPassHash() + "', '" + user.getCreationIpAddress() + "', '" + user.getCreatedDate() + "', '" + user.getCreatedTime() + "', '" + user.getRealName() + "', '" + user.getLastChangeDate() + "')";
			
			try
			{
				//inserting user data
				statement.executeUpdate(query);
				log.info(Constants.dataInserted + Constants.colon + user.getLoginName());
			}
			catch (SQLException e)
			{
				log.warning(Constants.sqlException + Constants.insertion + Constants.colon + e.getStackTrace().toString());
			}
	    }
	}
}
