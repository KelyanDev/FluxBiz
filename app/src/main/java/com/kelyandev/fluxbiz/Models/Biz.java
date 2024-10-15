package com.kelyandev.fluxbiz.Models;

public class Biz {
    private String content;
    private long time;
    private String username;
    private int likes;

    public Biz() {
    }

    public Biz(String content, long time, String username, int likes) {
        this.content = content;
        this.time = time;
        this.username = username;
        this.likes = likes;
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
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }


}
