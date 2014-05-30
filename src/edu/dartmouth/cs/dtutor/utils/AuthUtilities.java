package edu.dartmouth.cs.dtutor.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import edu.dartmouth.cs.dtutor.Globals;
import edu.dartmouth.cs.dtutor.data.AuthDatabaseHelper;

public class AuthUtilities {

    private static final String TAG = Globals.TAG + ".AuthUtilities";
    
    private static String URL = Globals.SERVER_URL + "/auth.php";
    
    // JSON response node names
    public static String KEY_REQUEST_TYPE = "request_type";
    public static String KEY_SUCCESS = "success";
    public static String KEY_ERROR = "error";
    public static String KEY_ERROR_MSG = "error_msg";
    public static String KEY_USER = "user";
    public static String KEY_NAME = "name";
    public static String KEY_EMAIL = "email";
    public static String KEY_PASSWORD = "password";
    public static String KEY_CREATED_AT = "created_at";
    
    // Request types
    public static String request_type_login = "login";
    public static String request_type_register = "register";

    // Request response codes
    public static int FAILURE = 0;
    public static int SUCCESS = 1;
    
    // Error codes
    public static int NO_ERROR = 0;
    public static int REG_ERROR = 1;
    public static int REG_ERROR_USER_EXISTS = 2;
    public static int INCORRECT_EMAIL_OR_PASSWORD = 3;
    
    private AuthUtilities() {
        throw new RuntimeException("cannot instantiate AuthUtilities class");
    }

    /**
     * Perform LOGIN request.
     * 
     * @param email
     * @param password
     * */
    public static JSONObject loginUser(String email, String password) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(KEY_REQUEST_TYPE, request_type_login));
        params.add(new BasicNameValuePair(KEY_EMAIL, email));
        params.add(new BasicNameValuePair(KEY_PASSWORD, password));
        
        Log.d(TAG, "login> [" + email + ";" + password + "]");
        
        return doSend(params);
    }

    /**
     * Perform REGISTER request.
     * 
     * @param name
     * @param email
     * @param password
     * */
    public static JSONObject registerUser(String name, String email, String password) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(KEY_REQUEST_TYPE, request_type_register));
        params.add(new BasicNameValuePair(KEY_NAME, name));
        params.add(new BasicNameValuePair(KEY_EMAIL, email));
        params.add(new BasicNameValuePair(KEY_PASSWORD, password));

        Log.d(TAG, "register> [" + name + ";" + email + ";" + password + "]");
        
        return doSend(params);
    }
    
    /**
     * Send an HTTP request.
     * 
     * @param params
     * @return
     */
    private static JSONObject doSend(List<NameValuePair> params) {
        JSONObject json = AuthClient.doAuthRequest(URL, params);

        Log.i("JSONResponse=", json.toString());
        return json;
    }

    /**
     * Determine login status (i.e. if an entry exists in the table).
     * 
     * @param context
     * @return boolean flag indicated if user inStream logged in
     */
    public static boolean isUserLoggedIn(Context context) {
        AuthDatabaseHelper db = AuthDatabaseHelper.getInstance(context);
        return (db.getRowCount() > 0);
    }

    /**
     * Log the user out (reset database/table).
     * 
     * @param context
     * @return
     */
    public static boolean logoutUser(Context context) {
        AuthDatabaseHelper db = AuthDatabaseHelper.getInstance(context);
        db.resetTables();
        return true;
    }

}