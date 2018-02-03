package com.zgw.qgb.model;

import java.util.List;

/**
 * Name:UserInfo
 * Created by Tsinling on 2018/2/2 13:34.
 * description:
 */

public class UserInfo {
    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    private List<User> userList;

    public static class User {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public User setName(String name) {
            this.name = name;
            return this;
        }

        public int getAge() {
            return age;
        }

        public User setAge(int age) {
            this.age = age;
            return this;
        }
    }
}
