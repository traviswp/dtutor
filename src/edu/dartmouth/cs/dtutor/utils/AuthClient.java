package edu.dartmouth.cs.dtutor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import edu.dartmouth.cs.dtutor.Globals;

import android.util.Log;

public class AuthClient {

    private static final String TAG = Globals.TAG + ".AuthClient";
    
    static InputStream inStream = null;
    static JSONObject jsonObj = null;
    static String jsonStr = "";

    // constructor
    public AuthClient() {}

    /**
     * Send a POST to url with request parameters params. 
     * 
     * @param url
     * @param params
     * @return a JSON Object from JSON string response
     */
    public static JSONObject doAuthRequest(String url, List<NameValuePair> params) {

        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            inStream = httpEntity.getContent();

        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch(ClientProtocolException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        // Read response
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            inStream.close();
            jsonStr = sb.toString();
        } catch(Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try to convert the string to a JSON object
        try {
            jsonObj = new JSONObject(jsonStr);
        } catch(JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jsonObj;
    }
    
}