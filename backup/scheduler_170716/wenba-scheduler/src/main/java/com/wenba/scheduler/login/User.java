package com.wenba.scheduler.login;

/**
 * @author zhangbo
 *
 */
public class User {

    // 成员变量
    /**
     * user name
     */
    private String userName;

    /**
     * password
     */
    private String password;

    private long lastAccessTime;

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
