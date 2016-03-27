package com.apex.tarot.feedback.entity;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Feedback {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy h:m:ss - a");

    @SerializedName("feedback")
    private String feedback;

    @SerializedName("user")
    private String user;

    @SerializedName("rating")
    private double rating;

    @SerializedName("_id")
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
}
