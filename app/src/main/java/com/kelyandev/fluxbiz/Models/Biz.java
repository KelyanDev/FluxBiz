package com.kelyandev.fluxbiz.Models;

import android.util.Log;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class Biz {
    private String id;
    private String content;
    private long time;
    private String username;
    private int likeCount;
    private String userId;

    public Biz() {
    }

    public Biz(String id, String content, long time, String username, int likeCount, String userId) {
        this.id = id;
        this.content = content;
        this.time = time;
        this.username = username;
        this.likeCount = likeCount;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void incrementLikes() {
        likeCount++;
    }

    public void decrementLikes() {
        if (likeCount > 0) {
            likeCount--;
        }
    }

    public double calculateScore() {
        long currentTime = System.currentTimeMillis();
        long ageInMillis = currentTime - time;
        long ageInDays = TimeUnit.MILLISECONDS.toDays(ageInMillis);

        double alpha = (double) likeCount;

        return (30 - ageInDays) * alpha;
    }

    public String getFormattedDate() {
        long currentTime = System.currentTimeMillis();
        long ageInMillis = currentTime - time;

        long ageInSec = TimeUnit.MILLISECONDS.toSeconds(ageInMillis);
        long ageInMinutes = TimeUnit.MILLISECONDS.toMinutes(ageInMillis);
        long ageInHours = TimeUnit.MILLISECONDS.toHours(ageInMillis);
        long ageInDays = TimeUnit.MILLISECONDS.toDays(ageInMillis);

        if (ageInSec < 60) {
            return ageInSec + "sec";
        } else if (ageInMinutes < 60) {
            return ageInMinutes + "min";
        } else if (ageInHours < 24) {
            return ageInHours + "h";
        } else {
            return ageInDays + "d";
        }
    }
}
