package com.apex.tarot.feedback.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * BaseActivity
 * <p/>
 * Created by Jayshil Dave
 * 19/09/15
 * <p/>
 * © Copyright 2015 Coca-Cola, Inc. All rights reserved.
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    public void showProgress(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setCancelable(false);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
