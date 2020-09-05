package com.ludoApp.ludoAppbackend.models;

public class Friend {

    public String uid;
    public String name;

    public Friend(String uid, String name) {
        this.uid = uid;
        this.name = name;

        System.out.println(uid);
        System.out.println(name);
    }
}
