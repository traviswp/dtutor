package edu.dartmouth.cs.dtutor;

import java.util.ArrayList;

import org.json.JSONArray;
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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import edu.dartmouth.cs.dtutor.data.AsyncResponse;
import edu.dartmouth.cs.dtutor.data.InsertMemberDb;
import edu.dartmouth.cs.dtutor.utils.AuthUtilities;
import edu.dartmouth.cs.dtutor.utils.ServerUtilities;

public class RegisterActivity extends Activity implements AsyncResponse {
    
    private static final String TAG = Globals.TAG + ".RegisterActivity";
    
    // UI elements
    private EditText mNameView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private TextView loginScreenLink;
    private View mRegisterFormView;
    private View mRegisterStatusView;
    private TextView mRegisterStatusMessageView;
    
    // Data
    private String mName;
    private String mEmail;
    private String mPassword;
    private String mRegisterErrorMsg;
    
	InsertMemberDb insertTask; //Async Task for adding a new member to the member table
    
    // TODO: Keep track of tutor type...
    
    /**
     * Keep track of the register task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // setting default screen to activity_register.xml
        setContentView(R.layout.activity_register);
        
        // UI setup
        mNameView = (EditText) findViewById(R.id.reg_fullname);
        mEmailView = (EditText) findViewById(R.id.reg_email);
        mPasswordView = (EditText) findViewById(R.id.reg_password);
        
        // listening to login link
        loginScreenLink = (TextView) findViewById(R.id.link_to_login);
        loginScreenLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // switching to Log in screen
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        
        mRegisterFormView = findViewById(R.id.register_form);
        mRegisterStatusView = findViewById(R.id.register_status);
        mRegisterStatusMessageView = (TextView) findViewById(R.id.register_status_message);
    }
    
    /**
     * Attempts to register the account specified by the register form. 
     * If there are form errors (invalid id, missing fields, etc.), the errors 
     * are presented and no actual register attempt inStream made.
     * 
     * NOTE: doRegister() registered as an onClick method in activity_register.xml
     */
    public void doRegister(View view) {
        if(mAuthTask != null) { return; }

        // Reset errors.
        mNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the register attempt.
        mName = mNameView.getText().toString().trim();
        mEmail = mEmailView.getText().toString().trim();
        mPassword = mPasswordView.getText().toString().trim();

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

        // Check for a valid name.
        if(TextUtils.isEmpty(mName)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if(mName.length() < 4) { // TODO: how to validate a name?
            mNameView.setError(getString(R.string.error_invalid_name));
            focusView = mNameView;
            cancel = true;
        }
        
        if(cancel) {
            // There was an error; don't attempt registration and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            if(ServerUtilities.isNetworkAvailable(getApplicationContext())) {
                mRegisterStatusMessageView.setText(R.string.login_progress_signing_up);
                showProgress(true);
                mAuthTask = new UserRegisterTask();
                mAuthTask.execute((Void) null);
            } else { // no Internet connection
                DtutorDialogFragment.displayDialog(getFragmentManager(),
                                                   DtutorDialogFragment.DIALOG_ID_BASIC_MESSAGE,
                                                   DtutorDialogFragment.DIALOG_ID_BASIC_MESSAGE_TAG, 
                                                   "registration error:\n\nno internet connection...");
            }
        }
    }

    public void insertMemberOnRegister() {
        // add user to the members table on the server
        insertTask = new InsertMemberDb(getApplicationContext(), mEmail, mName, new ArrayList<String>(), "", -1); //add the user to them members table 
        insertTask.asyncResponse = this; // set a delegate to get the query result from the asynctask
        insertTask.execute();
    }
    
    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterStatusView.setVisibility(View.VISIBLE);
            mRegisterStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            mRegisterFormView.setVisibility(View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    
    /**
     * Represents an asynchronous register task used to sign-up the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            // Attempt register...
            JSONObject jsonObj = AuthUtilities.registerUser(mName, mEmail, mPassword);
            
            // Check register response...
            try {
                if(jsonObj.getString(AuthUtilities.KEY_SUCCESS) != null) {
                    mRegisterErrorMsg = "";
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
                        
//                        // Profile table will just contain the user's email and name since they have not registered as tutor/tutee yet
//                        Member member = new Member();
//                        member.setEmail(jsonUser.getString(AuthUtilities.KEY_EMAIL));
//                        member.setName(jsonUser.getString(AuthUtilities.KEY_NAME));
//                        member.setMemberType(-1); // Pass in a member type of -1 since they have not registered as a member yet
//                        
//                     // Add new user to the local profile table
//                        MemberDbHelper MemberDbHelper = new MemberDbHelper(getApplicationContext());
//                        MemberDbHelper.addProfile(member); 
                        
                        return true;
                    } else {
                        if(jsonObj.getString(AuthUtilities.KEY_ERROR) != null) {
                            mRegisterErrorMsg = jsonObj.getString(AuthUtilities.KEY_ERROR_MSG);
                        } else {
                            mRegisterErrorMsg = getString(R.string.error_failed_registration);
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
                Log.i(TAG, "onPostExecute(): register SUCCESS");
                                
                insertMemberOnRegister();
                
                // Launch main application screen
                Intent gotoMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                 
                // Close all views before launching main activity
                gotoMainActivity.putExtra("email", mEmail);
                gotoMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(gotoMainActivity);
                 
                // Close registration screen
                finish();
            } else {
                Log.i(TAG, "onPostExecute(): register FAILED");
                mPasswordView.setError(mRegisterErrorMsg);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

    }

	@Override
	public void processFinish(JSONArray result) throws JSONException {
	}

	@Override
	public void processFinish(Integer result) {
		if(result == 1)
			Log.d("arvi", "Added member to server members table successfully");
		else
			Log.d("arvi", "Could not add member to server members table successfully");
		insertTask.cancel(true);		
	}
    
}