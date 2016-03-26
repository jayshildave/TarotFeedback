package com.apex.tarot.feedback;

import android.app.Application;

import com.facebook.FacebookSdk;

public class ApplicationEx extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
    }


}
