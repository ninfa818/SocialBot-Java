package com.hostcart.socialbot.model;

import java.io.Serializable;
import java.util.HashMap;

public class Post implements Serializable {
    private String id;
    private String text;
    private HashMap<String,String> likes;
    private HashMap<String,Object> comments;
    private int shares;
    private HashMap<String,Object> medias;
    private long time;
    private String uid;
    private String displayName;
    private String photoUri;
    private String latlng;
    private int type;
    private boolean isShared;

    public Post() {
    }

    public Post(String id, String text, HashMap<String, String> likes, HashMap<String, Object> comments,
                int shares, HashMap<String, Object> medias, long time, String uid, String displayName,
                String photoUri, String latlng, int type, boolean isShared) {
        this.id = id;
        this.text = text;
        this.likes = likes;
        this.comments = comments;
        this.shares = shares;
        this.medias = medias;
        this.time = time;
        this.uid = uid;
        this.displayName = displayName;
        this.photoUri = photoUri;
        this.latlng = latlng;
        this.type = type;
        this.isShared = isShared;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public HashMap<String,Object> getMedias() {
        return medias;
    }

    public void setMedias(HashMap<String,Object> medias) {
        this.medias = medias;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public HashMap<String, String> getLikes() {
        return likes;
    }

    public void setLikes(HashMap<String, String> likes) {
        this.likes = likes;
    }

    public HashMap<String, Object> getComments() {
        return comments;
    }

    public void setComments(HashMap<String, Object> comments) {
        this.comments = comments;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public int getType() { return type; }

    public void setType( int type ) {
        this.type = type;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

}
