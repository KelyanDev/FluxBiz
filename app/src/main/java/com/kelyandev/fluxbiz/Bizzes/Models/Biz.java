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
    private String author;
    private int likes, rebizzes, replies;
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
     * @param author The Biz's Author
     * @param likes The number of likes the Biz has
     * @param rebizzes The number of Rebiz the Biz has
     * @param replies The number of replies the Biz has
     * @param userId The user ID of the Biz's owner
     */
    public Biz(String id, String content, long time, String author, int likes, int rebizzes, int replies, String userId) {
        this.id = id;
        this.content = content;
        this.time = time;
        this.author = author;
        this.likes = likes;
        this.rebizzes = rebizzes;
        this.replies = replies;
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
    public String getAuthor() {
        return author;
    }

    /**
     * Sets a new username for the Biz's author
     * @param author The username of the Biz's author
     */
    public void setAuthor(String author) {
        this.author = author;
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
     * @param rebizzes The new rebiz count
     */
    public void setRebizzes(int rebizzes) {
        this.rebizzes = rebizzes;
    }

    /**
     * Gets the current number of replies for the Biz
     * @return The replies count
     */
    public int getReplies() {
        return replies;
    }

    /**
     * Sets a new reply count for the Biz
     * @param replies The new reply count
     */
    public void setReplies(int replies) {
        this.replies = replies;
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

    /**
     * Get the Biz score
     * @return The biz score
     */
    public double getScore() {
        return score;
    }

    /**
     * Set the Biz score
     * @param score The new Biz score
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Calculates a score for the Biz, based on its age and like count
     * This is used to rank Bizzes by actuality over time
     */
    public void calculateScore() {
        long currentTime = System.currentTimeMillis();
        long ageInMillis = currentTime - time;

        // Hyperparameters
        double beta = 0.9; // Likes weight
        double alpha = 1.2; // Rebizzes weight
        double delta = 1.4; // Decreasing linked to age
        double epsilon = 1e-6;

        // Calculating likes and rebizzes contribution
        double likesContribution = Math.pow(likes, beta);
        double rebizzesContribution = Math.pow(rebizzes, alpha);

        // Total popularity score
        double popularity = likesContribution + rebizzesContribution;

        // Calculating penalty (related to age)
        double agePenalty = Math.pow(ageInMillis / 1000.0, delta) + epsilon;

        setScore(popularity/agePenalty);
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
