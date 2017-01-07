package com.dadabit.ourbudget.customClasses;



public class UserMetadata {

    private String uid;
    private String userName;
    private String user2id;

    public UserMetadata() {
    }

    public UserMetadata(String uid, String userName, String user2id) {
        this.uid = uid;
        this.userName = userName;
        this.user2id = user2id;
    }

    public String getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public String getUser2id() {
        return user2id;
    }
}
