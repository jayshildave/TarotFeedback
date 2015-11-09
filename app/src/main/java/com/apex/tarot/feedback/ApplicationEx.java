package com.apex.tarot.feedback;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class ApplicationEx extends Application {

    private static final String APPLICATION_ID = "cnHuQ40VRZG29F3z72NsczN2nahjJc3LpvwybSZz";

    private static final String CLIENT_KEY = "E7Oj7n2iWcJ3bjad5Gt21NwGzTSQyRI4obuOci5D";

    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.initialize(getApplicationContext(), APPLICATION_ID, CLIENT_KEY);
        ParseFacebookUtils.initialize(getApplicationContext());

//        ParseUser.enableAutomaticUser();
//
//        ParseACL defaultACL = new ParseACL();
//        ParseACL.setDefaultACL(defaultACL, true);
    }


}
