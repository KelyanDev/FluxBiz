package com.kelyandev.fluxbiz.Models;

public class Biz {
    private String content;
    private long time;
    private String username;
    private int likeCount;
    private String userId;

    public Biz() {
    }

    public Biz(String content, long time, String username, int likeCount, String userId) {
        this.content = content;
        this.time = time;
        this.username = username;
        this.likeCount = likeCount;
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getLikes() {
        return likeCount;
    }

    public void setLikes(int likeCount) {
        this.likeCount = likeCount;
    }

    private String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
