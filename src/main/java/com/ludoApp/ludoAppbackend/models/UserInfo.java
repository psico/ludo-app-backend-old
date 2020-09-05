package com.ludoApp.ludoAppbackend.models;

import java.util.List;

public class UserInfo {

    public String uid;
    public String name;
    public List<Friend> friends;

    public UserInfo(String uid, String name, List<Friend> friends) {
        this.uid = uid;
        this.name = name;
        this.friends = friends;

        System.out.println(uid);
        System.out.println(name);
        System.out.println(friends);
    }
}
