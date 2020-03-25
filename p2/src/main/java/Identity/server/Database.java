package Identity.server;

  
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.sql.*;

public class Database
{
	private Connection connection = null;
	private Statement statement;
	
	public Database()
	{
		try
		{
			connection = DriverManager.getConnection("jdbc:sqlite:userinfo.db");
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			
			statement.executeUpdate("create table if not exists user (loginName string, uuid string, password string, ipAddress string, date string, time string, realUserName string, lastChangeDate string)");
			
		}
		catch (SQLException e)
		{
			System.out.println("SQLException during setting up database connection: " + e.getStackTrace());
		}
		
	}
	private boolean Insert(String loginName, String uuid, String password, String ipAddress, String date, String time, String realUserName, String lastChangeDate)
	{
		try
		{
			ResultSet checkExist = statement.executeQuery("select * from user where " + "loginName='" + loginName + "' or uuid='" + uuid + "';");
			
			if(checkExist.next())
			{
				System.out.println("user with same login name or uuid already exists");
				return false;
			}
		}
		catch (SQLException e)
		{
			System.out.println("Error occured during database checking: " + e.getStackTrace());
			return false;
		}
		
		String query = "insert into user values('" + loginName + "', '" + uuid + "', '" + password + "', '" + ipAddress + "', '" + date + "', '" + time + "', '" + realUserName + "', '" + lastChangeDate + "')";
		
		try
		{
			statement.executeUpdate(query);
			return true;
		}
		catch (SQLException e)
		{
			System.out.println("SQLException during insertion: " + e.getStackTrace());
			return false;
		}
	}
	
	private boolean Delete(String key, String value)
	{
		if(!(key.equals(Constants.loginName) || key.equals(Constants.uuid)))
		{
			System.out.println("Invalid key for deletion");
			return false;
		}
		String query = "delete from user where " + key + "='" + value + "';";
		
		try
		{
			statement.executeUpdate(query);
			return true;
		}
		catch (SQLException e)
		{
			System.out.println("SQLException during deletion: " + e.getStackTrace());
			return false;
		}
	}
	private boolean Update(String keyType, String keyValue, String changeType, String changeValue)
	{
		if(!(keyType.equals(Constants.loginName) || keyType.equals(Constants.uuid)))
		{
			System.out.println("Invalid key for update");
			return false;
		}
		
		try
		{
			ResultSet result = statement.executeQuery("select * from user where " + keyType + "='" + keyValue + "';");
			
			if(!result.next())
			{
				System.out.println("user information not found while updating");
				return false;
			}
			
			
			String loginName = result.getString(Constants.loginName);
			String uuid = result.getString(Constants.uuid);
			String password = result.getString(Constants.password);
			String ipAddress = result.getString(Constants.ipAddress);
			String date = result.getString(Constants.date);
			String time = result.getString(Constants.time);
			String realUserName = result.getString(Constants.realUserName);
			String lastChangeDate = result.getString(Constants.lastChangeDate);
			
			boolean deletionResult = Delete(Constants.loginName, loginName);
			if(!deletionResult)
			{
				System.out.println("Deletion of old data failed during update");
				return false;
			}

			boolean addResult = true;
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
			
			if(!addResult)
			{
				System.out.println("Insertion of new data failed during update");
				return false;
			}
		}
		catch(SQLException e)
		{
			System.out.println("SQLException occured during updating: " + e.getStackTrace());
			return false;
		}
		
		return true;
	}
	private ResultSet GetAll()
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
	
	private boolean CloseDB()
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
	
	private void printAll(ResultSet response)
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
		Database b = new Database();
		
		b.Insert("Mike", "1234", "Mike Password", "127.0.0.1", "March 25", "4 pm", "Michael", "March 25");
		b.Insert("Tyson", "1235", "Tyson Password", "127.0.0.1", "March 25", "4 pm", "Michael", "March 25");
		b.Insert("Ashley", "1236", "Ashley Password", "127.0.0.1", "March 25", "4 pm", "Michael", "March 25");
		
		ResultSet res = b.GetAll();
		
		System.out.println("printing all");
		b.printAll(res);
		
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
		
		b.CloseDB();
	}
}