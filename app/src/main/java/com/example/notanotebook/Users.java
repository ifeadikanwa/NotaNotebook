package com.example.notanotebook;

public class Users {
    private String userID;

    public Users(){

    }

    public Users(String userID){
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
