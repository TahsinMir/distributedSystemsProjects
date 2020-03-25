package Identity.server;

import java.io.Serializable;
import java.util.Date;

// This will be passed from server to client
public class User implements Serializable {
    private static final long serialVersionUID = 8510538827054962873L;
    private String uuid;
    private String loginName;
    private String realName;
    private Date createdTime;
    private String creationIpAddress;
    private Date lastChangeDate;
    // we will not pass this data during serialization so it's transient
    private transient String passHash; // Most probabably we will not need it will remove in future

    public User(){

    }
    public User(String loginName, String realName, String uuid, String passHash, String creationIpAddress){
        this.loginName = loginName;
        this.realName = realName;
        this.uuid = uuid;
        this.passHash = passHash;
        this.creationIpAddress = creationIpAddress;
    }

    public User(String loginName, String realName, String uuid){
        this.loginName = loginName;
        this.realName = realName;
        this.uuid = uuid;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
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

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
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

    public String toString(){
        String date = (this.createdTime == null) ? this.createdTime.toString() : "Not available";
        return "User name: " + this.loginName + " UUID: " + this.uuid + " Creation time: " + date;
    }
}
//


