package com.kelyandev.fluxbiz.Bizzes.Models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Biz message; It is the FluxBiz equivalent of a Tweet.
 * A Biz contains an ID, content, creation time, owner's username, likes count, as well as the user's ID
 */
public class Biz {
    private String id;
    private String content;
    private long time;
    private String username;
    private int likes;
    private int rebizzes;
    private String userId;
    private double score;

    /**
     * Default constructor for creating an empty Biz instance
     */
    public Biz() {
    }

    /**
     * Create a Biz object with specified details
     * @param id The unique identifier for the Biz (generated by Firestore)
     * @param content The message content of the Biz
     * @param time The creation timestamp in milliseconds
     * @param username The username of the Biz's Author
     * @param likes The number of likes the Biz has
     * @param rebizzes The number of Rebiz the Biz has
     * @param userId The user ID of the Biz's owner
     */
    public Biz(String id, String content, long time, String username, int likes, int rebizzes, String userId) {
        this.id = id;
        this.content = content;
        this.time = time;
        this.username = username;
        this.likes = likes;
        this.rebizzes = rebizzes;
        this.userId = userId;
    }

    /**
     * Gets the ID of the Biz
     * @return The Biz's ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets a new ID for the Biz
     * @param id The new ID for the Biz
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the content of the Biz
     * @return The Biz's content
     */
    public String getContent() {
        return content;
    }

    /**
     * Updates the content of the Biz
     * @param content The new content for the Biz
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the username of the Biz's author
     * @return The username of the Biz's author
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets a new username for the Biz's author
     * @param username The username of the Biz's author
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the creation time for the Biz
     * @return The creation time in milliseconds
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets a new creation time for the Biz
     * @param time The creation time in milliseconds
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Gets the current number of likes for the Biz
     * @return The likes count
     */
    public int getLikes() {
        return likes;
    }

    /**
     * Sets a new like count for the Biz
     * @param likes The new like count
     */
    public void setLikes(int likes) {
        this.likes = likes;
    }

    /**
     * Gets the current number of rebizzes for the Biz
     * @return The rebizzes count
     */
    public int getRebizzes() {
        return rebizzes;
    }

    /**
     * Sets a new rebiz count for the Biz
     * @param rebizzes The new Biz count
     */
    public void setRebizzes(int rebizzes) {
        this.rebizzes = rebizzes;
    }

    /**
     * Gets the user ID of the Biz's owner
     * @return The user ID of the Biz's owner
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets a new user ID for the Biz's owner
     * @param userId The user ID of the Biz's owner
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Increments the like count of the Biz by 1
     */
    public void incrementLikes() {
        likes++;
    }

    /**
     * Decrements the like count of the Biz by 1, only if the like count is greater than 0
     */
    public void decrementLikes() {
        if (likes > 0) {
            likes--;
        }
    }

    /**
     * Increments the rebiz count of the Biz by 1
     */
    public void incrementRebiz() {
        rebizzes++;
    }

    /**
     * Decrements the rebiz count of the Biz by 1, only if the rebiz count is greater than 0
     */
    public void decrementRebiz() {
        if (rebizzes > 0) {
            rebizzes--;
        }
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Calculates a score for the Biz, based on its age and like count
     * This is used to rank Bizzes by actuality over time
     * @return A double representing the Biz's score
     */
    public void calculateScore() {
        long currentTime = System.currentTimeMillis();
        long ageInMillis = currentTime - time;
        long ageInDays = TimeUnit.MILLISECONDS.toDays(ageInMillis);

        double alpha = likes;

        this.score = (30 - ageInDays) * alpha;
        //return (30 - ageInDays) * alpha;
    }

    /**
     * Formats and returns the creation time of the Biz relative to the current time
     * @return A formatted string representing the age of the Biz
     */
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
        } else if (ageInDays < 30){
            return ageInDays + "d";
        } else {
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(date);
        }
    }
}
