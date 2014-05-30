package edu.dartmouth.cs.dtutor;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import edu.dartmouth.cs.dtutor.utils.AuthUtilities;
import edu.dartmouth.cs.dtutor.utils.ServerUtilities;

public class LoginActivity extends Activity {
    
    private static final String TAG = Globals.TAG + ".LoginActivity";

    // UI elements
    private EditText mEmailView;
    private EditText mPasswordView;
    private TextView registerScreenLink;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    
    // Data
    private String mEmail;
    private String mPassword;
    private String mLoginErrorMsg;
    
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // setting default screen to activity_login.xml
        setContentView(R.layout.activity_login);
        
        // UI setup
        mEmailView = (EditText) findViewById(R.id.login_email);
        mPasswordView = (EditText) findViewById(R.id.login_password);
        
        // listening to register new account link
        registerScreenLink = (TextView) findViewById(R.id.link_to_register);
        registerScreenLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // switching to Register screen
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        
        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuitem_login_as_dev:
            Log.d(TAG, getString(R.string.ui_menu_login_as_dev));

            // load dev account
            mEmailView.setText("dtutor-dev@dartmouth.edu");
            mPasswordView.setText("123456");
            return true;
        }
        return false;
    }    
    
    /**
     * Attempts to sign in with the account specified by the login form. 
     * If there are form errors (invalid id, missing fields, etc.), the errors 
     * are presented and no actual login attempt inStream made.
     * 
     * NOTE: doLogin() registered as an onClick method in activity_login.xml
     */
    public void doLogin(View view) {
        handleLogin();
    }
    
    private void handleLogin() {
        if(mAuthTask != null) { return; }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if(TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if(mPassword.length() < 4) { // TODO: how to validate passwords?
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email.
        if(TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if(mEmail.length() < 4 || !mEmail.contains("@") || mEmail.contains(" ")) { // TODO: how to validate email?
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if(cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if(ServerUtilities.isNetworkAvailable(getApplicationContext())) {
                mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
                showProgress(true);
                mAuthTask = new UserLoginTask();
                mAuthTask.execute((Void) null);
            } else { // no Internet connection
                DtutorDialogFragment.displayDialog(getFragmentManager(),
                                                   DtutorDialogFragment.DIALOG_ID_BASIC_MESSAGE,
                                                   DtutorDialogFragment.DIALOG_ID_BASIC_MESSAGE_TAG, 
                                                   "login error:\n\nno internet connection...");
            }
        }
    }
    
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    
    /**
     * Represents an asynchronous login task used to authenticate the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // Attempt login...
            JSONObject jsonObj = AuthUtilities.loginUser(mEmail, mPassword);
            
            // Check login response...
            try {
                if(jsonObj.getString(AuthUtilities.KEY_SUCCESS) != null) {
                    mLoginErrorMsg = "";
                    String result = jsonObj.getString(AuthUtilities.KEY_SUCCESS);
                    if(Integer.parseInt(result) == AuthUtilities.SUCCESS) {
                        // extract user information
//                        JSONObject jsonUser = jsonObj.getJSONObject(AuthUtilities.KEY_USER);
                        
                        // clear any previously logged in user(s)
//                        AuthUtilities.logoutUser(getApplicationContext());
                        
//                        // add new user as logged in user
//                        AuthDatabaseHelper db = AuthDatabaseHelper.getInstance(getApplicationContext());
//                        db.addUser(jsonUser.getString(AuthUtilities.KEY_NAME), jsonUser.getString(AuthUtilities.KEY_EMAIL), jsonUser.getString(AuthUtilities.KEY_CREATED_AT));
//                        db = null;
                        
                        return true;
                    } else {
                        if(jsonObj.getString(AuthUtilities.KEY_ERROR) != null) {
                            mLoginErrorMsg = jsonObj.getString(AuthUtilities.KEY_ERROR_MSG);
                        } else {
                            mLoginErrorMsg = getString(R.string.error_incorrect_login);
                        }
                        
                        return false;
                    }
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
            
            return false;
        }
                
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if(success) {
                Log.i(TAG, "onPostExecute(): login SUCCESS");
                
                // Launch main application screen
                Intent gotoMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                 
                // Close all views before launching main activity
                gotoMainActivity.putExtra("email", mEmail);
                gotoMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(gotoMainActivity);
                 
                // Close login screen
                finish();
            } else {
                Log.i(TAG, "onPostExecute(): login FAILED");
                mPasswordView.setError(mLoginErrorMsg);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }
    
}