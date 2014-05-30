package edu.dartmouth.cs.dtutor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.dartmouth.cs.dtutor.ReservableRoom.TimeBlock;
import edu.dartmouth.cs.dtutor.data.LocationsDbHelper;
import edu.dartmouth.cs.dtutor.data.MemberDbHelper;
import edu.dartmouth.cs.dtutor.data.Members;
import edu.dartmouth.cs.dtutor.utils.LocationsUtilities;
import edu.dartmouth.cs.dtutor.utils.ServerUtilities;

public class ReservationsActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = Globals.TAG + ".ReservationsActivity";

    public static final String KEY_BUILDING_NAME = "building-key";
    public static final String KEY_ROOM_NAME = "room-key";

    public static String[] TIMES =
        new String[] { 
            "06:00am", 
            "07:00am", 
            "08:00am", 
            "09:00am", 
            "10:00am",
            "11:00am", 
            "12:00pm", 
            "01:00pm", 
            "02:00pm", 
            "03:00pm", 
            "04:00pm", 
            "05:00pm", 
            "06:00pm", 
            "07:00pm", 
            "08:00pm", 
            "09:00pm", 
            "10:00pm",             
            "11:00pm" };

    private listviewAdapter adapter;
    private ArrayList<String> list;
    
    private boolean needsUpdate = false;
    private ReservableRoom rr;
    private ReservableRoom rr4Member;
    
    private DatePicker datePicker;
    private Calendar currentDayCalendar;
    
    // Listener object for DatePicker
    private DatePicker.OnDateChangedListener dateSetListener = new DatePicker.OnDateChangedListener() {
        boolean justUpdated = false;
        
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if(!justUpdated) {
                currentDayCalendar.clear();
                currentDayCalendar.set(year, monthOfYear, dayOfMonth);

                // Update list of possible entries...
                parseAllStringsToDatesAndLoadListView();
            }
            justUpdated = !justUpdated;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);
        
        /*
         * Get details about building/room
         */
        
        String building = "";
        String room = "";

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            building = extras.getString(KEY_BUILDING_NAME);
            room = extras.getString(KEY_ROOM_NAME);
        }

        // Get the actual ReservableRoom object from the database
        LocationsDbHelper locationsDb = LocationsDbHelper.getInstance(getApplicationContext());
        rr = locationsDb.fetchEntryByBuildingAndRoom(building, room);
        rr4Member = new ReservableRoom(building, room, null);

        /*
         * UI setup
         */
        
        // Reservations sub-header
        TextView tvSubHeader = (TextView) findViewById(R.id.reservationsTvSubHeader);
        tvSubHeader.setText(building + " (" + room + ")");
        
        // Bind to UI elements: DatePicker
        currentDayCalendar = Calendar.getInstance();
        currentDayCalendar.set(Calendar.MINUTE, 0);
        currentDayCalendar.set(Calendar.SECOND, 0);
        
        datePicker = (DatePicker) findViewById(R.id.reservationsDatePicker);
        datePicker.init(currentDayCalendar.get(Calendar.YEAR), currentDayCalendar.get(Calendar.MONTH), currentDayCalendar.get(Calendar.DAY_OF_MONTH), dateSetListener);
        
        // Load list view content (time ranges)
        parseAllStringsToDatesAndLoadListView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        // Get the selected item (time-range string)
        String blockString = (String) ((TextView) view).getText();
        //Log.i(TAG, "desired block: " + blockString);
        
        // Split the string on spaces
        String[] args = blockString.split(" ");
        
        // Load relevant strings as dates
        Date startDate = parseStringToDate(args[0], "hh:mmaa");
        Date endDate = parseStringToDate(args[2], "hh:mmaa");
        
        TimeBlock tb = new TimeBlock(startDate, endDate, currentDayCalendar);
        //Log.i(TAG, "try to add reservation: " + tb.toString());
                
        // Add the reservation!
        boolean result_success = rr.addReservation(tb);
        if(result_success) {
            needsUpdate = true; // reservations have been added -- update local/remote databases!
            rr4Member.addReservation(tb);
            
            String msg =  ((TextView) view).getText() + ": reservation requested!";
            //Log.i(TAG, msg);
            Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
        } else { // !result_success
            String msg =  ((TextView) view).getText() + " already reserved!";
            //Log.i(TAG, msg);
            Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();
        }
        
        final String item = (String) parent.getItemAtPosition(position);
        view.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                list.remove(item);
                adapter.notifyDataSetChanged();
                view.setAlpha(1);
            }
        });
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if(needsUpdate) {
            // (!) Make sure reservations are updated in the local database
            LocationsDbHelper locationsDb = LocationsDbHelper.getInstance(getApplicationContext());
            locationsDb.updateRoomReservations(rr);
            
            // (!) Upload changes to members table & locations table in database!
            doUpdate();
        }
        
        finish();
    }
    
    /**
     * Given a String, return a Date object.
     */
    public Date parseStringToDate(String s, String inputFormat) {
        Date date;
        SimpleDateFormat format = new SimpleDateFormat(inputFormat);
        try {
            date = format.parse(s);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Load the list of TIME Strings into the list which populates the ListView.
     */
    public void parseAllStringsToDatesAndLoadListView() {
        // Bind to UI elements: ListView
        ListView mListview = (ListView) findViewById(R.id.reservationsListView);
        
        //Log.i(TAG, "EXISTING RESERVATIONS 4 " + rr.building + " " + rr.room + ":");
        if(rr.reservations.size() == 0) {
            //Log.i(TAG, "  > no reservations");
        } else {
            for(TimeBlock tb :rr.reservations) {
                //Log.i(TAG, "  > " + tb.toString());
            }
        }
        
        // Load list!
        list = new ArrayList<String>();
        String outputFormat = "hh:mmaa";
        Date dateStart, dateEnd;
        for(int i = 0; i < TIMES.length-1; i++) {
            // Parse string into date object
            try {
                dateStart = parseStringToDate(TIMES[i], "hh:mmaa");
                dateEnd = parseStringToDate(TIMES[i+1], "hh:mmaa");
                
                TimeBlock tb = new TimeBlock(dateStart, dateEnd, currentDayCalendar);
                
                // Don't add to reservable list view if time is already reserved.
                if(!rr.isConflict(tb)) {
                    // Format date string
                    CharSequence dateStartString = DateFormat.format(outputFormat, dateStart);
                    CharSequence dateEndString = DateFormat.format(outputFormat, dateEnd);
                    String formatted = dateStartString + " - " + dateEndString;
                    //Log.d(TAG, "Date: " + formatted + " (" + String.valueOf(dateStart.getTime() < dateEnd.getTime()) +")");
                    
                    list.add(formatted);
                } else {
                    //Log.w(TAG, "conflict: not adding: " + tb.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Define the adapter
        adapter = new listviewAdapter(this, R.layout.reservations_listview_layout, list);
        mListview.setAdapter(adapter);
        
        // Setup the onClickListener
        mListview.setOnItemClickListener(this);        
    }
    
    /* ****************************************************************** *
     *                              Upload Task
     * ****************************************************************** */
    
    public void doAppend() {

        // get the member that is posting the request
//        MemberDbHelper dbHelper = new MemberDbHelper(getApplicationContext());
//        Members myProfile = dbHelper.getProfile();
//
//        JSONObject reservationsObj = new JSONObject();
//        JSONObject reservations = new JSONObject();

//        Log.e(TAG, "newReservations>>>" + rr4Member.getReservationsAsStr());
//        Log.e(TAG, "current>>>" + myProfile.getReservations());
//        if (!myProfile.getReservations().equals("null")) {// if the user already has notifications
//            try {
//                notificationsObj = new JSONObject(myProfile.getNotifications());
//            }
//            catch(JSONException e) {
//                Log.e("JSON Parser", "Error parsing data " + e.toString());
//                notificationsObj = new JSONObject();
//            }
//
//        } else {
//            notificationsObj = new JSONObject(); 
//        }
        
//        reservations = appendReservationsToJSONObject(reservationsObj, myProfile.getEmail());

        // create the new notifications string and send it to the member
//        notifications.toString();
//
//        updateTask = new UpdateMemberNotifications(getApplicationContext(), email, notifications.toString());
//        updateTask.asyncResponse = this;    // set a delegate to get the query result from the asynctask
//        updateTask.execute();
    }
    
    // convert notifications to a json object
    public JSONObject appendReservationsToJSONObject(JSONObject reservations, String email) {
        try {
            reservations.get("reserations");
            // TODO:
            Log.i(TAG, "appendReservationsToJSONObject() before:" + reservations.toString());
            Log.i(TAG, "appendReservationsToJSONObject() after:" + reservations.toString());
            return reservations;
        }
        catch (JSONException e){
            return null;
        }
    }    
    
    UpdateLocationReservationsTask mUpdateTask;

    public void doUpdate() {
        if(ServerUtilities.isNetworkAvailable(getApplicationContext())) {
            mUpdateTask = new UpdateLocationReservationsTask();
            mUpdateTask.execute((Void) null);
        } else { // no Internet connection
            Log.e(TAG, "update location/reservation data error: no internet connection...");
        }
    }
    
    /**
     * Represents an asynchronous task used to update location/reservation data on server.
     */
    public class UpdateLocationReservationsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            LocationsUtilities.updateLocationReservations(rr);
            //doAppend();
            LocationsUtilities.updateMemberReservations(getApplicationContext(), rr4Member);
            return true;
        }
                
        @Override
        protected void onPostExecute(final Boolean success) {
            mUpdateTask = null;
            Log.i(TAG, "onPostExecute(): update COMPLETE");
        }

    }
    
    /* ****************************************************************** *
     *                            Custom Adapter
     * ****************************************************************** */

    /**
     * Custom ListView Adapter for Exercise Information input UI
     */
    public class listviewAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public listviewAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            for(int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}