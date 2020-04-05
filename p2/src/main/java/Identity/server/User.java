package Identity.server;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;
import java.util.UUID;

// This will be passed from server to client
public class User implements Serializable {
    private static final long serialVersionUID = 8510538827054962873L;
    private String loginName;
    private UUID uuid;
    private String creationIpAddress;
    private Date createdDate;
    private Time createdTime;
    private String realName;
    private Date lastChangeDate;
    private String modificationMessage;
    // we will not pass this data during serialization so it's transient
    private transient String passHash; // Most probabably we will not need it will remove in future

    public User(){

    }
    
    public User(String loginName, UUID uuid, String creationIpAddress, Date createdDate, Time createdTime, String realName, Date lastChangeDate, String modificationMessage)
    {
    	this.loginName = loginName;
        this.uuid = uuid;
        this.creationIpAddress = creationIpAddress;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.realName = realName;
        this.lastChangeDate = lastChangeDate;
        this.modificationMessage = modificationMessage;
    }
    
    public User(String loginName, UUID uuid, String passwordHash, String creationIpAddress, Date createdDate, Time createdTime, String realName, Date lastChangeDate)
    {
    	this.loginName = loginName;
        this.uuid = uuid;
        this.passHash = passwordHash;
        this.creationIpAddress = creationIpAddress;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.realName = realName;
        this.lastChangeDate = lastChangeDate;
    }
    
    public User(String modificationMessage)
    {
    	this.modificationMessage = modificationMessage;
    }

    public User(String loginName, UUID uuid, String realName){
        this.loginName = loginName;
        this.uuid = uuid;
        this.realName = realName;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public Time getCreatedTime()
    {
    	return createdTime;
    }
    
    public void setCreatedTime(Time createdTime)
    {
    	this.createdTime = createdTime;
    }

    public String getCreationIpAddress() {
        return creationIpAddress;
    }

    public void setCreationIpAddress(String creationIpAddress) {
        this.creationIpAddress = creationIpAddress;
    }

    public Date getLastChangeDate() {
        return lastChangeDate;
    }

    public void setLastChangeDate(Date lastChangeDate) {
        this.lastChangeDate = lastChangeDate;
    }

    public String getPassHash() {
        return passHash;
    }

    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }
    
    public String getModificationMessage()
    {
    	return modificationMessage;
    }
    
    public void setModificationMessage(String modificationMessage)
    {
    	this.modificationMessage = modificationMessage;
    }

    public String toString()
    {
    	if(this.modificationMessage.startsWith(Constants.failure))
    	{
    		return this.modificationMessage;
    	}
        return "User name: " + this.loginName + ", UUID: " + this.uuid + ", real name: " + this.realName + ", Creation IP address: " + this.creationIpAddress + ", Creation date: " + this.createdDate + ", Creation time: " + this.createdTime + ", last change date: " + this.lastChangeDate;
    }
}
//


