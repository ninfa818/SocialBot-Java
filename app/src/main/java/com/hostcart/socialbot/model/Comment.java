package com.hostcart.socialbot.model;

import java.util.ArrayList;
import java.util.List;

public class Comment {

    private String photoUrl;
    private String userName;
    private String userid;
    private String content;
    private String review;
    private String time;
    private List<Review> reviews = new ArrayList<>();
    private List<Comment> replies = new ArrayList<>();


    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void updateReviews(int index, Review review) {
        this.reviews.set(index, review);
    }

    public void addReviews(Review review) {
        this.reviews.add(review);
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }

    public void addReplies(Comment reply) {
        this.replies.add(reply);
    }

}
