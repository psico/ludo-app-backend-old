package com.ludoApp.ludoAppbackend;

public class UserInfo {

    public String uid;
    public String name;

    public UserInfo(String uid, String name) {
        this.uid = uid;
        this.name = name;

        System.out.println(uid);
        System.out.println(name);
    }
}
