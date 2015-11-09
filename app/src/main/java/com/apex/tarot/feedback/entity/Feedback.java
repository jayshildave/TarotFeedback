package com.apex.tarot.feedback.entity;

import android.util.Log;

import com.parse.Parse;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Feedback
 * <p/>
 * Created by Jayshil Dave
 * 19/09/15
 * <p/>
 * © Copyright 2015 Coca-Cola, Inc. All rights reserved.
 */
public class Feedback {

    private String feedback;

    private String user;

    private double rating;

    private String objectId;

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public static List<Feedback> parseResponse(List<ParseObject> parseObjects) {
        List<Feedback> feedbackList = new ArrayList<>();

        for (ParseObject parseObject:parseObjects) {
            Feedback feedback = parseResponseObject(parseObject);
            feedbackList.add(feedback);
        }

        return feedbackList;
    }

    public static Feedback parseResponseObject(ParseObject parseObject) {
        Feedback feedback = new Feedback();
        feedback.setFeedback(parseObject.getString("feedback"));
        feedback.setUser(parseObject.getString("user"));
        feedback.setRating(parseObject.getInt("rating"));
        feedback.setObjectId(parseObject.getString("objectId"));
        return feedback;
    }

    public static ParseObject createParseObject(Feedback feedback) {
        ParseObject parseObject = new ParseObject("Feedback");
        parseObject.put("feedback", feedback.getFeedback());
        parseObject.put("user", feedback.getUser());
        parseObject.put("rating", feedback.getRating());
        parseObject.put("moderated", false);
        return parseObject;

    }
}
