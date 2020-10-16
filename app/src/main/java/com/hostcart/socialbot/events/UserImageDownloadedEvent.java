package com.hostcart.socialbot.events;

public class UserImageDownloadedEvent {
    String path;

    public String getPath() {
        return path;
    }

    public UserImageDownloadedEvent(String path) {
        this.path = path;
    }
}
