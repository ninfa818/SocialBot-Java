package com.hostcart.socialbot.model;

import java.io.Serializable;
import java.util.HashMap;

public class FriendSystem implements Serializable {

    private HashMap<String,Object> friends;
    private HashMap<String,Object> invite;
    private HashMap<String,Object> invited;

    public HashMap<String, Object> getFriends() {
        return friends;
    }

    public void setFriends(HashMap<String, Object> friends) {
        this.friends = friends;
    }

    public HashMap<String, Object> getInvite() {
        return invite;
    }

    public void setInvite(HashMap<String, Object> invite) {
        this.invite = invite;
    }

    public HashMap<String, Object> getInvited() {
        return invited;
    }

    public void setInvited(HashMap<String, Object> invited) {
        this.invited = invited;
    }
}
