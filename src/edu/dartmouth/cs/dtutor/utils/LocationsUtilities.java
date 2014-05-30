package edu.dartmouth.cs.dtutor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import edu.dartmouth.cs.dtutor.Globals;
import edu.dartmouth.cs.dtutor.ReservableRoom;
import edu.dartmouth.cs.dtutor.data.MemberDbHelper;
import edu.dartmouth.cs.dtutor.data.Members;


public class LocationsUtilities {
    
    private static final String TAG = Globals.TAG + ".LocationsUtilities";
    
    private static String LOCATIONS_URL = Globals.SERVER_URL + "/locations.php";
    private static String MEMBER_UPDATE_URL = Globals.SERVER_URL + "/update_member_reservations.php";

    // JSON response node names
    public static String KEY_REQUEST_TYPE = "request_type";
    public static String KEY_SUCCESS = "success";
    public static String KEY_ERROR = "error";
    public static String KEY_ERROR_MSG = "error_msg";

    // Request response codes
    public static int FAILURE = 0;
    public static int SUCCESS = 1;
    
    // Request types
    public static String request_type_add = "add";
    public static String request_type_update = "update";
    public static String request_type_update_member = "update_member_reservations";
    public static String request_type_fetchAll = "fetchAll";
    
    // Data fields
    public static String KEY_LOCATIONS = "locations";
    public static String KEY_UID = "uid";
    public static String KEY_BUILDING = "building";
    public static String KEY_ROOM = "room";
    public static String KEY_LAT = "latitude";
    public static String KEY_LONG = "longitude";
    public static String KEY_RESERVATIONS = "reservations";
    public static String KEY_EMAIL = "email";
        
    static InputStream inStream = null;
    static JSONObject jsonObj = null;
    static String jsonStr = "";

    // constructor
    public LocationsUtilities() {}

    /* ****************************************************************** */

    /**
     * Request to add new location to database.
     */
    public static JSONObject addLocation(ReservableRoom rr) {
        
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(KEY_REQUEST_TYPE, request_type_add));
        params.add(new BasicNameValuePair(KEY_BUILDING, rr.building));
        params.add(new BasicNameValuePair(KEY_ROOM, rr.room));
        params.add(new BasicNameValuePair(KEY_LAT, String.valueOf(rr.latlng.latitude)));
        params.add(new BasicNameValuePair(KEY_LONG, String.valueOf(rr.latlng.longitude)));
        params.add(new BasicNameValuePair(KEY_RESERVATIONS, rr.getReservationsAsStr()));
        
        return doSend(LOCATIONS_URL, params);
    }

    /**
     * Request to update (add to/delete from) a location's reservations.
     */
    public static JSONObject updateLocationReservations(ReservableRoom rr) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(KEY_REQUEST_TYPE, request_type_update));
        params.add(new BasicNameValuePair(KEY_RESERVATIONS, rr.getAsMemberReservationString()));
        Log.e(TAG, "senind this res update: " + rr.getAsMemberReservationString());
        
        return doSend(LOCATIONS_URL, params);
    }
    
    /**
     * Get all known locations & their data.
     * @return
     */
    public static JSONObject fetchAllLocations() {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(KEY_REQUEST_TYPE, request_type_fetchAll));

        return doSend(LOCATIONS_URL, params);
    }    
    
    /**
     * TODO: FIX:
     * (1) Decide on format...
     * (2) THIS MAY OVERRIDE RESERVATIONS....
     * 
     * Update a member's reservations.
     */
    public static JSONObject updateMemberReservations(Context context, ReservableRoom rr) {
        // Ask user to complete the profile if they haven't done so already
        MemberDbHelper dbHelper = new MemberDbHelper(context);
        Members member = dbHelper.getProfile();
        
        if(dbHelper.doesProfileExist() && member != null) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(KEY_REQUEST_TYPE, request_type_update_member));
            params.add(new BasicNameValuePair(KEY_EMAIL, member.getEmail()));
            params.add(new BasicNameValuePair(KEY_BUILDING, rr.building));
            params.add(new BasicNameValuePair(KEY_ROOM, rr.room));
            params.add(new BasicNameValuePair(KEY_RESERVATIONS, rr.getReservationsAsStr()));
    
            return doSend(MEMBER_UPDATE_URL, params);
        } else {
            Log.e(TAG, "no member to update...");
            return null;
        }
    }
    
    /**
     * Send an HTTP POST request.
     */
    private static JSONObject doSend(String url, List<NameValuePair> params) {
        JSONObject json = doPostRequest(url, params);

        //Log.i(TAG, "doSend(): JSONResponse="+json.toString());
        
        return json;
    }
    
    /* ****************************************************************** */
    
    /**
     * Send a POST to url with request parameters params. 
     * 
     * @param url
     * @param params
     * @return a JSON Object from JSON string response
     */
    public static JSONObject doPostRequest(String url, List<NameValuePair> params) {

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

        Log.i(TAG, ">>>" + jsonStr + "<<<");
        
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
