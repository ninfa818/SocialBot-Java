package com.hostcart.socialbot.utils;

import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PostMedia implements Serializable {

    private String userId;
    private String mediaId;

    private String content;
    private long duration;
    private String thumbImg;
    private long timestamp;
    private int type;
    private String localPath;

    public PostMedia(String userId, String mediaId, String content, long duration, String thumbImg,
                     String localPath, long timestamp, int type) {
        this.userId = userId;
        this.mediaId = mediaId;
        this.content = content;
        this.duration = duration;
        this.thumbImg = thumbImg;
        this.timestamp = timestamp;
        this.localPath = localPath;
        this.type = type;
    }

    public PostMedia() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getThumbImg() {
        return thumbImg;
    }

    public void setThumbImg(String thumbImg) {
        this.thumbImg = thumbImg;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Map toMap() {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("type", type);

        if (thumbImg != null)
            result.put("thumbImg", thumbImg);
        if (content != null)
            result.put("content", content);

        result.put("duration", duration);

        return result;
    }
}
