package edu.dartmouth.cs.dtutor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.dartmouth.cs.dtutor.Globals;

/**
 * Helper class used to communicate with server.
 * 
 * [Modified from CS165]
 */
public final class ServerUtilities {

    private static final String TAG = Globals.TAG + ".ServerUtilities";
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2 * 1000;
    private static final Random random = new Random();
    
    private ServerUtilities() {}
        
    /* ****************************************************************** *
     *                           Helper Methods
     * ****************************************************************** */
    
    /**
     * Determine if the device has an Internet connection.
     * 
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }    
    
    public static String getUserId(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userId = prefs.getString(Globals.KEY_USER_ID, "");
        if(userId.isEmpty()) { return ""; }

        Log.i(TAG, Globals.KEY_USER_ID + ": " + userId);
        return userId;
    }    
    
    public static void storeUserId(Context context, String userId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Globals.KEY_USER_ID, userId);
        editor.commit();
    }
        
    /* ****************************************************************** *
     *                       Server Helper Methods
     * ****************************************************************** */

    public static String doSend(final Context context, final String serverUrl, Map<String, String> params) {
        Log.d(TAG, "doSend(): " + serverUrl);
        Log.d(TAG, "  * param-username: " + params.get("username"));
        Log.d(TAG, "  * param-password: " + params.get("password"));

        String response = null;
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        for(int i = 1; i <= ServerUtilities.MAX_ATTEMPTS; i++) {
            try {
                response = ServerUtilities.post(serverUrl, params);
            } catch(IOException e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                if(i == MAX_ATTEMPTS) {
                    Log.e(TAG, "attempted MAX_ATTEMPTS connections. stop trying...");
                    break;
                }
                try {
                    Log.d(TAG, "doSend() attempt " + i + ".");
                    Thread.sleep(backoff);
                } catch(InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Thread.currentThread().interrupt();
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
        
        return response;
    }

    /**
     * Issue a POST request to the server.
     * 
     * @param endpoint POST address.
     * @param params request parameters.
     * 
     * @throws IOException propagated from POST.
     */
    public static String post(String endpoint, Map<String, String> params) throws IOException {        
        URL url;
        try {
            url = new URL(endpoint);
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }

        // constructs the POST body using the parameters
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();

        while(iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
            if(iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }

        // Prepare to send POST body (string) to server
        String body = bodyBuilder.toString();
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            // Open connection and configure properties
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            // Post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            // Handle the response
            int status = conn.getResponseCode();
            if(status != 200) { throw new IOException("Post failed with error code " + status); }

            // Get Response
            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
            rd.close();
            return response.toString();

        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

}