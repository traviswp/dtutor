package edu.dartmouth.cs.dtutor.utils;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import edu.dartmouth.cs.dtutor.Globals;

public class SessionManager {

    private static final String TAG = Globals.TAG + ".SessionManager";

    // Session variables stored on disk as...
    private static final String SESSION_PREFS = "my_session";

    // Session objects
    private Context context;
    private SharedPreferences preferences;
    private Editor editor;
    
    private static SessionManager singleton;

    /**
     * Create a new session.
     * 
     * @param context
     */
    public synchronized static SessionManager getInstance(Context context) {
        if(singleton == null) {
            singleton = new SessionManager(context.getApplicationContext());
        }
        return(singleton);
    }

    private SessionManager(Context context) {
        Log.d(TAG, "create session.");

        this.context = context;
        preferences = this.context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * Completely clear out all session variables.
     */
    public void destroySession() {
        Log.d(TAG, "destroy session.");

        editor.clear();
        editor.commit();
    }
    
    public void addSessionVarBoolean(String key, boolean value) {
        Log.d(TAG, "add session variable: {" + key + "=" + value + "}");
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    public void addSessionVarString(String key, String value) {
        Log.d(TAG, "add session variable: {" + key + "=" + value + "}");
        editor.putString(key, value);
        editor.commit();
    }
    
    public void addSessionVarInteger(String key, int value) {
        Log.d(TAG, "add session variable: {" + key + "=" + value + "}");
        editor.putInt(key, value);
        editor.commit();
    }
    
    public void addSessionVarFloat(String key, float value) {
        Log.d(TAG, "add session variable: {" + key + "=" + value + "}");
        editor.putFloat(key, value);
        editor.commit();
    }
    
    public void addSessionVarLong(String key, long value) {
        Log.d(TAG, "add session variable: {" + key + "=" + value + "}");
        editor.putLong(key, value);
        editor.commit();
    }
    
    public boolean getSessionVarBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public String getSessionVarString(String key) {
        return preferences.getString(key, "");
    }
    
    public int getSessionVarInteger(String key) {
        return preferences.getInt(key, -1);
    }
    
    public float getSessionVarFloat(String key) {
        return preferences.getFloat(key, (float) -1.0);
    }
    
    public long getSessionVarLong(String key) {
        return preferences.getLong(key, -1);
    }
    
    /**
     * Remove a specific session variable from the session.
     * 
     * @param key
     */
    public void deleteSessionVar(String key) {
        editor.remove(key);
        editor.commit();
    }
    
    public void dumpPrefs() {
        Map<String, ?> allPrefs = preferences.getAll();

        Log.i(TAG, "PREFERENCES DUMP:");
        for(String key : allPrefs.keySet()) {
            Log.i(TAG, key + ": " + allPrefs.get(key) + "\n");
        }
    }

}