package com.apex.tarot.feedback.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.apex.tarot.feedback.R;
import com.apex.tarot.feedback.entity.Feedback;
import com.apex.tarot.feedback.view.CommentEditText;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;


public class TarotFeedbackActivity extends BaseActivity {

    private List<Feedback> feedbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarot_feedback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(getUsername())) {
                    loginUsingFacebook();
                } else {
                    Feedback feedback = readFromPreference();
                    addFeedback(feedback.getUser());
                }
            }
        });
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
        updateList();
    }

    private void updateList() {
        showProgress(getString(R.string.feedback_progress));
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Feedback");
        query.addDescendingOrder("updatedAt");
        query.whereEqualTo("moderated", true);
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List list, ParseException e) {
                dismissProgress();
                if (e == null) {
                    feedbackList = Feedback.parseResponse(list);

                    RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview);
                    setupRecyclerView(rv);
                    // object will be your game score
                } else {
                    Toast.makeText(TarotFeedbackActivity.this, getString(R.string.update_error_1), Toast.LENGTH_SHORT).show();
                    // something went wrong
                }
            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView) {

        recyclerView.setAdapter(new RecyclerViewAdapter(this));
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View view;
            public final TextView feedback;
            public final TextView user;
            public final RatingBar ratingBar;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                feedback = (TextView) view.findViewById(R.id.feedback);
                user = (TextView) view.findViewById(R.id.user);
                ratingBar = (RatingBar) view.findViewById(R.id.rating);
            }
        }

        public String getValueAt(int position) {
            return feedbackList.get(position).getFeedback();
        }

        public RecyclerViewAdapter(Context context) {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.feedback.setText(feedbackList.get(position).getFeedback());
            holder.user.setText(feedbackList.get(position).getUser());
            holder.ratingBar.setRating((float) feedbackList.get(position).getRating());
        }

        @Override
        public int getItemCount() {
            return feedbackList.size();
        }
    }

    private void loginUsingFacebook() {
        List<String> permissions = Arrays.asList("public_profile", "email");
        showProgress("Logging in...");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                dismissProgress();
                if (user == null) {
                    return;
                } else if (user.isNew()) {
                    getUserDetails(user);
                } else {
                    getUserDetails(user);
                }
            }
        });
    }

    private void getUserDetails(final ParseUser parseUser) {
        showProgress(getString(R.string.get_username_progress));
        GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {

                if (jsonObject != null) {
                    try {
                        final String username = jsonObject.getString("name");
                        JSONObject userProfile = new JSONObject();
                        userProfile.put("name", username);
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        currentUser.put("profile", userProfile);
                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Feedback");
                                    query.whereEqualTo("user", username);
                                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                                        public void done(ParseObject object, ParseException e) {
                                            if (object == null) {

                                            } else {
                                                Feedback feedback = Feedback.parseResponseObject(object);
                                                saveToPreference(feedback, object.getObjectId());
                                            }
                                            dismissProgress();
                                            saveUsername(username);
                                            addFeedback(username);
                                        }
                                    });
                                } else {
                                    dismissProgress();
                                    Toast.makeText(TarotFeedbackActivity.this, getString(R.string.get_username_error), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    dismissProgress();
                    Toast.makeText(TarotFeedbackActivity.this, getString(R.string.get_username_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }

    private void addFeedback(final String username) {
        View dialogLayout = LayoutInflater.from(this).inflate(R.layout.feedback_alert_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setView(dialogLayout);
        EditText usernameEditText = (EditText) dialogLayout.findViewById(R.id.user_edittext);
        usernameEditText.setText(username);
        final CommentEditText commentEditText = (CommentEditText) dialogLayout.findViewById(R.id.feedback_edittext);
        final RatingBar feedbackRating = (RatingBar) dialogLayout.findViewById(R.id.rating);

        final AlertDialog customAlertDialog = builder.create();
        final Button saveButton = (Button) dialogLayout.findViewById(R.id.feedback_button);

        final Feedback feedbackFromPreference = readFromPreference();
        commentEditText.setText(feedbackFromPreference.getFeedback());
        feedbackRating.setRating((float) feedbackFromPreference.getRating());

        if (!TextUtils.isEmpty(feedbackFromPreference.getObjectId())) {
            saveButton.setText(R.string.update_button_label);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment = commentEditText.getText().toString();
                String user = username;
                float rating = feedbackRating.getRating();
                if (TextUtils.isEmpty(comment)) {
                    Toast.makeText(TarotFeedbackActivity.this, getString(R.string.save_error_2), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (rating == 0.0f) {
                    Toast.makeText(TarotFeedbackActivity.this, getString(R.string.save_error_1), Toast.LENGTH_SHORT).show();
                    return;
                }

                final Feedback feedback = new Feedback();
                feedback.setUser(user);
                feedback.setFeedback(comment);
                feedback.setRating(rating);


                if (!TextUtils.isEmpty(feedbackFromPreference.getObjectId())) {
                    showProgress(getString(R.string.update_feedback_progress));
                    final ParseObject updateFeedbackParseObject = Feedback.createParseObject(feedback);
                    updateFeedbackParseObject.setObjectId(feedbackFromPreference.getObjectId());
                    updateFeedbackParseObject.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            dismissProgress();
                            if (e == null) {
                                customAlertDialog.dismiss();
                                saveToPreference(feedback, updateFeedbackParseObject.getObjectId());
                                Toast.makeText(TarotFeedbackActivity.this, getString(R.string.save_parse_success_1), Toast.LENGTH_SHORT).show();
                                updateList();
                            } else {
                                Toast.makeText(TarotFeedbackActivity.this, getString(R.string.save_parse_error_1), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {

                    showProgress(getString(R.string.save_progress_1));


                    final ParseObject feedbackParseObject = Feedback.createParseObject(feedback);
                    feedbackParseObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            dismissProgress();
                            if (e == null) {
                                customAlertDialog.dismiss();
                                saveToPreference(feedback, feedbackParseObject.getObjectId());
                                Toast.makeText(TarotFeedbackActivity.this, getString(R.string.save_parse_success_1), Toast.LENGTH_SHORT).show();
                                updateList();
                            } else {
                                Toast.makeText(TarotFeedbackActivity.this, getString(R.string.save_parse_error_1), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        customAlertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void saveToPreference(Feedback feedback, String objectID) {
        SharedPreferences sharedPreferences = getSharedPreferences("TarotAppFeedback", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("feedback", feedback.getFeedback());
        editor.putString("user", feedback.getUser());
        editor.putFloat("rating", (float) feedback.getRating());
        editor.putString("objectID", objectID);
        editor.apply();
    }

    private void saveUsername(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("TarotAppFeedback", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user", username);
        editor.apply();
    }

    private String getUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("TarotAppFeedback", Context.MODE_PRIVATE);
        return sharedPreferences.getString("user", "");
    }

    private Feedback readFromPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("TarotAppFeedback", Context.MODE_PRIVATE);
        Feedback feedback = new Feedback();
        feedback.setFeedback(sharedPreferences.getString("feedback", ""));
        feedback.setUser(sharedPreferences.getString("user", ""));
        feedback.setRating(sharedPreferences.getFloat("rating", 0.0f));
        feedback.setObjectId(sharedPreferences.getString("objectID", ""));
        return feedback;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            Intent intent = new Intent(TarotFeedbackActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_renew) {
            updateList();
            return true;
        } else if (id == R.id.action_share) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.apex.tarot.feedback");
            startActivity(Intent.createChooser(i, "Share and spread the word"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
