package com.ifyezedev.notanotebook;

public class Users {
    private String userID;
    private boolean isSecuritySet;


    public Users(){
    }

    public Users(String userID, boolean isSecuritySet) {
        this.userID = userID;
        this.isSecuritySet = isSecuritySet;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public boolean isSecuritySet() {
        return isSecuritySet;
    }

    public void setSecuritySet(boolean securitySet) {
        this.isSecuritySet = securitySet;
    }
}
