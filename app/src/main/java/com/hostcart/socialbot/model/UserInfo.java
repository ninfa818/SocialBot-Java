package com.hostcart.socialbot.model;

import java.io.Serializable;
import java.util.HashMap;

public class UserInfo implements Serializable {

    private String uid;
    private String birthDate;
    private String email;
    private String name;
    private String phone;
    private String photo;
    private String gender;
    private String status;
    private String surname;
    private String thumbImg;
    private String ver;

    private FriendSystem friendSystem;
    private HashMap<String,Object> notificationTokens;
    private HashMap<String,Object> posts;

    public HashMap<String, Object> getNotificationTokens() {
        return notificationTokens;
    }

    public void setNotificationTokens(HashMap<String, Object> notificationTokens) {
        this.notificationTokens = notificationTokens;
    }

    public HashMap<String, Object> getPosts() {
        return posts;
    }

    public void setPosts(HashMap<String, Object> posts) {
        this.posts = posts;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getThumbImg() {
        return thumbImg;
    }

    public void setThumbImg(String thumbImg) {
        this.thumbImg = thumbImg;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String version) {
        this.ver = version;
    }

    public FriendSystem getFriendSystem() {
        return friendSystem;
    }

    public void setFriendSystem(FriendSystem friendSystem) {
        this.friendSystem = friendSystem;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
