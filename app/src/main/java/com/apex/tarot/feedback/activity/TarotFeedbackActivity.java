package com.apex.tarot.feedback.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TarotFeedbackActivity extends BaseActivity {

    private List<Feedback> feedbackList;
    private CallbackManager facebookCallbackManager;
    private static final String FETCH_FEEDBACK_LIST = "https://fed-tarot.restdb.io/rest/feedback.json?q={%22moderated%22:true}&sort=_changed&dir=-1";
    private static final String ADD_FEEDBACK = "https://fed-tarot.restdb.io/rest/feedback";
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private static final String API_KEY = "3d7ee7bc98bb12651c600f57014d8a362c8d3";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarot_feedback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);

        FloatingActionButton fablogin = (FloatingActionButton) findViewById(R.id.fab_login);
        fablogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(getUsername())) {
                    facebookLogin();
                } else {
                    addFeedback(getUsername());
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
        Request request = new Request.Builder()
                .url(FETCH_FEEDBACK_LIST)
                .addHeader("x-apikey", API_KEY)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                        Toast.makeText(TarotFeedbackActivity.this, getString(R.string.update_error_1), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Headers responseHeaders = response.headers();
                    for (int i = 0; i < responseHeaders.size(); i++) {
                        Log.d("TarotFeedback", responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<Feedback>>() {
                    }.getType();
                    feedbackList = gson.fromJson(response.body().string(), type);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUIList();
                        }
                    });
                } else {
                    Log.d("TarotFeedback", "response :: " + response.isSuccessful());
                    Log.d("TarotFeedback", "response :: " + response.code());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgress();
                            Toast.makeText(TarotFeedbackActivity.this, getString(R.string.update_error_1), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    private void updateUIList() {
        dismissProgress();
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview);
        setupRecyclerView(rv);
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
        } else if (id == R.id.action_invite) {
            onInviteClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (facebookCallbackManager != null) {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void facebookLogin() {
        Log.d("TarotFeedback", "facebookLogin called");
        facebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("TarotFeedback", loginResult.getAccessToken().getUserId());
                Log.d("TarotFeedback", loginResult.getAccessToken().getApplicationId());
                Log.d("TarotFeedback", loginResult.getAccessToken().getToken());
                getUserDetails(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("TarotFeedback", "User cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TarotFeedback", "Error " + error.toString());
            }
        });
        List<String> permissions = Arrays.asList("public_profile", "email");
        LoginManager.getInstance().logInWithReadPermissions(this, permissions);
    }

    private void getUserDetails(AccessToken accessToken) {
        showProgress(getString(R.string.get_username_progress));
        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                dismissProgress();
                if (object != null) {
                    try {
                        final String username = object.getString("name");
                        Log.d("TarotFeedback", username);
                        Log.d("TarotFeedback", object.toString());
                        saveUsername(username);
                        addFeedback(username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(TarotFeedbackActivity.this, getString(R.string.get_username_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
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

    private void addFeedback(final String username) {
        Log.d("TarotFeedback", "username :: " + username);
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
                submitFeedback(comment, user, rating, feedbackFromPreference.getObjectId(), customAlertDialog);
            }
        });
        customAlertDialog.show();
    }

    private void submitFeedback(String comment, String user, double rating, String objectID, final AlertDialog customAlertDialog) {

        final Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setFeedback(comment);
        feedback.setRating(rating);

        Gson gson = new Gson();
        String requestJSON = gson.toJson(feedback);

        Log.d("TarotFeedback", "requestJSON :: " + requestJSON);

        showProgress(getString(R.string.save_progress_1));

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, requestJSON);
        Request request = null;
        if (TextUtils.isEmpty(objectID)) {
            request = new Request.Builder()
                    .url(ADD_FEEDBACK)
                    .addHeader("x-apikey", API_KEY)
                    .post(body)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(ADD_FEEDBACK + "/" + objectID)
                    .addHeader("x-apikey", API_KEY)
                    .put(body)
                    .build();
        }
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                        Toast.makeText(TarotFeedbackActivity.this, getString(R.string.update_error_1), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    Headers responseHeaders = response.headers();
                    for (int i = 0; i < responseHeaders.size(); i++) {
                        Log.d("TarotFeedback", responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }
                    Gson gson = new Gson();
                    Feedback feedback = gson.fromJson(response.body().string(), Feedback.class);
                    saveToPreference(feedback, feedback.getObjectId());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgress();
                            customAlertDialog.dismiss();
                            Toast.makeText(TarotFeedbackActivity.this, getString(R.string.save_parse_success_1), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.d("TarotFeedback", "response :: " + response.isSuccessful());
                    Log.d("TarotFeedback", "response :: " + response.code());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgress();
                            Toast.makeText(TarotFeedbackActivity.this, getString(R.string.update_error_1), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    private Feedback readFromPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("TarotAppFeedback", Context.MODE_PRIVATE);
        Feedback feedback = new Feedback();
        feedback.setFeedback(sharedPreferences.getString("feedback", ""));
        feedback.setRating(sharedPreferences.getFloat("rating", 0.0f));
        feedback.setObjectId(sharedPreferences.getString("objectID", ""));
        return feedback;
    }

    private void saveToPreference(Feedback feedback, String objectID) {
        SharedPreferences sharedPreferences = getSharedPreferences("TarotAppFeedback", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("feedback", feedback.getFeedback());
        editor.putString("user", feedback.getUser());
        editor.putFloat("rating", (float) feedback.getRating());
        editor.putString("objectID", feedback.getObjectId());
        editor.apply();
    }
}
