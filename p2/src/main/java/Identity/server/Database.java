package Identity.server;

  
import Identity.client.IdClient;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/***
 * Represents the database
 */
public class Database
{
	private Connection connection = null;	//connection to the database
	private Statement statement;	//The statement to be executed in the database
	Logger log;		//the logger
	
	/***
     * Creates the connection to the database
     * @param logger
     */
	public Database(Logger logger)
	{
		log = logger;
		try
		{
			//established sqlite database connection
			connection = DriverManager.getConnection("jdbc:sqlite:userinfo.db");
			statement = connection.createStatement();
			statement.setQueryTimeout(300);
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
	public String Insert(String loginName, String uuid, String password, String ipAddress, String date, String time, String realUserName, String lastChangeDate)
	{
		try
		{
			//first we check whether information about the same loginName or UUID already exists
			ResultSet checkExist = statement.executeQuery("select * from user where " + "loginName='" + loginName + "' or uuid='" + uuid + "';");
			
			//if information already exists, we do not insert another new use with the same login name or uuid
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
	
	public String Delete(String key, String value)
	{
		if(!(key.equals(Constants.loginName) || key.equals(Constants.uuid)))
		{
			System.out.println(Constants.invalidKey +  Constants.deletion);
			return Constants.failure + Constants.colon + Constants.invalidKey +  Constants.deletion;
		}
		
		
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
		
		String query = "delete from user where " + key + "='" + value + "';";
		
		try
		{
			statement.executeUpdate(query);
			return Constants.success + Constants.colon + Constants.dataDeleted + Constants.colon + value;
		}
		catch (SQLException e)
		{
			log.warning(Constants.sqlException + Constants.deletion + Constants.colon + e.getStackTrace().toString());
			return Constants.failure + Constants.colon + Constants.errorOccured + Constants.deletion;
		}
	}
	public User Search(String key, String value)
	{
		if(!(key.equals(Constants.loginName) || key.equals(Constants.uuid)))
		{
			log.warning(Constants.invalidKey +  Constants.userSearch);
			User user = new User(Constants.failure + Constants.colon + Constants.invalidKey +  Constants.userSearch);
			return user;
		}
		
		
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
	public String Update(String keyType, String keyValue, String changeType, String changeValue)
	{
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
			if(changeType.equals(Constants.loginName))
			{
				addResult = Insert(changeValue, uuid, password, ipAddress, date, time, realUserName, lastChangeDate);
			}
			else if(changeType.equals(Constants.uuid))
			{
				addResult = Insert(loginName, changeValue, password, ipAddress, date, time, realUserName, lastChangeDate);
			}
			else if(changeType.equals(Constants.password))
			{
				addResult = Insert(loginName, uuid, changeValue, ipAddress, date, time, realUserName, lastChangeDate);
			}
			else if(changeType.equals(Constants.ipAddress))
			{
				addResult = Insert(loginName, uuid, password, changeValue, date, time, realUserName, lastChangeDate);
			}
			else if(changeType.equals(Constants.date))
			{
				addResult = Insert(loginName, uuid, password, ipAddress, changeValue, time, realUserName, lastChangeDate);
			}
			else if(changeType.equals(Constants.time))
			{
				addResult = Insert(loginName, uuid, password, ipAddress, date, changeValue, realUserName, lastChangeDate);
			}
			else if(changeType.equals(Constants.realUserName))
			{
				addResult = Insert(loginName, uuid, password, ipAddress, date, time, changeValue, lastChangeDate);
			}
			else if(changeType.equals(Constants.lastChangeDate))
			{
				addResult = Insert(loginName, uuid, password, ipAddress, date, time, realUserName, changeValue);
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
		
		return Constants.success + Constants.colon + Constants.dataUpdated;
	}
	public boolean CheckPassword(String keyType, String keyValue, String password)
	{
		if(!(keyType.equals(Constants.loginName) || keyType.equals(Constants.uuid)))
		{
			System.out.println("Invalid key type for checking password");
			return false;
		}
		
		try
		{
			ResultSet result = statement.executeQuery("select * from user where " + keyType + "='" + keyValue + "';");
			
			if(!result.next())
			{
				System.out.println("user information not found while checking password");
				return false;
			}
			
			String passwordFromDB = result.getString(Constants.password);
			
			if(passwordFromDB.equals(password))
			{
				return true;
			}
			return false;
		}
		catch (SQLException e)
		{
			System.out.println("SQLException during user password check: " + e.getStackTrace());
			return false;
		}
	}
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
				
				return all;
			}
			else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException during retriving data: " + e.getStackTrace());
			return null;
		}
	}
	public ResultSet GetAll()
	{
		try
		{
			ResultSet result = statement.executeQuery("select * from user");
			return result;
		}
		catch (SQLException e)
		{
			System.out.println("SQLException during retriving data: " + e.getStackTrace());
			return null;
		}
	}
	
	public boolean CloseDB()
	{
		try
		{
			if(connection != null)
		          connection.close();
			
			return true;
		}
		catch(SQLException e)
		{
			return false;
		}
	}
	
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
	
	public static void main( String args[] ) throws ClassNotFoundException
	{
		Logger log = Logger.getLogger(IdClient.class.getName());
		Database b = new Database(log);
		
		UUID one = UUID.randomUUID();
		UUID two = UUID.randomUUID();
		UUID three = UUID.randomUUID();
		String oneStr = one.toString();
		String twoStr = two.toString();
		String threeStr = three.toString();
		LocalDate dateNow = LocalDate.now();
   		String dateNowString = dateNow.toString();
   		LocalTime timeNow = LocalTime.now();
   		String timeNowString = timeNow.toString();
		b.Insert("Mike", oneStr, "Mike Password", "127.0.0.1", dateNowString, timeNowString, "Michael", dateNowString);
		b.Insert("Tyson", twoStr, "Tyson Password", "127.0.0.1", dateNowString, timeNowString, "Michael", dateNowString);
		b.Insert("Ashley", threeStr, "Ashley Password", "127.0.0.1", dateNowString, timeNowString, "Michael", dateNowString);
		
		ResultSet res = b.GetAll();
		
		System.out.println("printing all");
		b.printAll(res);
		
		System.out.println("Getting all loginName:");
		List<String> allLoginName = b.GetList(Constants.users);
		for(int i=0;i<allLoginName.size();i++)
		{
			System.out.println(allLoginName.get(i));
		}
		
		System.out.println("Getting all uuid:");
		List<String> allUUID = b.GetList(Constants.uuids);
		for(int i=0;i<allUUID.size();i++)
		{
			System.out.println(allUUID.get(i));
		}
		
		System.out.println("Getting all info of all users:");
		List<String> allInfo = b.GetList(Constants.all);
		for(int i=0;i<allInfo.size();i++)
		{
			System.out.println(allInfo.get(i));
		}
		
		System.out.println("Deleting tyson");
		b.Delete(Constants.loginName, "Tyson");
		
		ResultSet res2 = b.GetAll();
		
		System.out.println("printing all again");
		b.printAll(res2);
		
		System.out.println("Deleting Ashley");
		b.Delete(Constants.loginName, "Ashley");
		
		ResultSet res3 = b.GetAll();
		
		System.out.println("printing all again");
		b.printAll(res3);
		
		
		b.Update(Constants.loginName, "Mike", Constants.ipAddress, "new ip");
		
		ResultSet res4 = b.GetAll();
		
		System.out.println("printing all again");
		b.printAll(res4);
		
		System.out.println("searching for mike's info:");
		User user = b.Search(Constants.loginName, "Mike");
		
		if(user == null)
		{
			System.out.println("user not found");
		}
		else
		{
			System.out.println("user found!!");
			//String loginName, String uuid, String creationIpAddress, Date createdDate, Time createdTime, String realName, Date lastChangeDate
			System.out.println(user.getLoginName() + ", " + user.getUuid() + ", " + user.getCreationIpAddress() + ", " + user.getCreatedDate() + ", " + user.getCreatedTime() + ", " + user.getRealName() + ", " + user.getLastChangeDate());
		}
		
		b.CloseDB();
	}
}