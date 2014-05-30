package edu.dartmouth.cs.dtutor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Data class for holding information about reservable rooms on Dartmouth's campus.
 */
public class ReservableRoom {

    private static final String TAG = Globals.TAG + ".ReservableRoom";

    public static final String KEY_START_TIME = "startTime";
    public static final String KEY_END_TIME = "endTime";

    public long id;
    public String building;
    public String room;
    public LatLng latlng;
    public List<TimeBlock> reservations;

    public ReservableRoom(String building, String room, LatLng latlng) {
        this.building = building;
        this.room = room;
        this.latlng = latlng;
        this.reservations = new ArrayList<TimeBlock>();
    }

    public ReservableRoom(long id, String building, String room, LatLng latlng, String resString) {
        this.id = id;
        this.building = building;
        this.room = room;
        this.latlng = latlng;
        setReservationsFromStr(resString);
    }

    public String toString() {
        String rrString = "";

        rrString += "ID: " + id + "\n\nBuilding: " + building + "\n\nRoom: " + room + "\n\n " + latlng.toString();
        rrString += "\n\nReservations: " + getReservationsAsStr();

        return rrString;
    }

    /**
     * Add a new reservation for the room.
     * 
     * @return true is returned if the reservation can be made, false is returned if the reservation cannot be made
     *         (i.e. a conflict).
     */
    public boolean addReservation(TimeBlock newTimeBlock) {
        if(reservations == null) {
            reservations = new ArrayList<TimeBlock>();
        }

        // Determine if newTimeBlock conflicts with any existing timeBlocks...
        boolean conflict = isConflict(newTimeBlock);

        // If there are no conflicts, go ahead and add the new reservation!
        // + update local/remote database(s)...
        if(!conflict) {
            Log.i(TAG, "no conflicts -- add new reservation!");
            reservations.add(newTimeBlock);
        }

        return !conflict;
    }

    /**
     * Determine if there is a conflict with a particular timeblock and the current reservations.
     * 
     * @param timeblock
     * @return
     */
    public boolean isConflict(TimeBlock timeblock) {
        boolean conflict = false;

        for(TimeBlock tb : reservations) {
            // check if: newTimeBlock START TIME overlaps with existing reservation
            if(tb.startTime <= timeblock.startTime && timeblock.startTime < tb.endTime) {
                // Log.d(TAG, "start-time conflict: cannot book reservation!");
                conflict = true;
                break;
            }

            // check if: newTimeBlock END TIME overlaps with existing reservation
            if(tb.startTime < timeblock.endTime && timeblock.endTime <= tb.endTime) {
                // Log.d(TAG, "end-time conflict: cannot book reservation!");
                conflict = true;
                break;
            }
        }
        return conflict;
    }

    /**
     * Set the list of TimeBlocks (reservations) from a String.
     * 
     * @return
     */
    public void setReservationsFromStr(String resString) {
        if(reservations == null) {
            reservations = new ArrayList<TimeBlock>();
        }

        // Convert JSON formatted string to JSONArray for loading reservations.
        if(resString != null && !resString.isEmpty()) {
            JSONArray reservationsJson;
            try {
                reservationsJson = new JSONArray(resString);

                // Parse out start/end times to construct TimeBlock object.
                for(int i = 0; i < reservationsJson.length(); i++) {
                    JSONObject obj = (JSONObject) reservationsJson.get(i);
                    long startTime = (Long) obj.get(KEY_START_TIME);
                    long endTime = (Long) obj.get(KEY_END_TIME);

                    TimeBlock tb = new TimeBlock(startTime, endTime);
                    reservations.add(tb);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Get all current reservations as a JSON String.
     * 
     * @return
     */
    public String getReservationsAsStr() {
        JSONArray reservationsArray = new JSONArray();

        for(TimeBlock tb : reservations) {

            JSONObject obj = new JSONObject();
            try {
                // Build reservation object as JSONObject
                obj.put(KEY_END_TIME, tb.endTime);
                obj.put(KEY_START_TIME, tb.startTime);

                // Record the reservation entry
                reservationsArray.put(obj);
            } catch(JSONException e) {
                Log.e(TAG, "unable to construct reservation list item as JSON String.");
            }
        }

        return reservationsArray.toString();
    }

    public String getAsMemberReservationString() {
        JSONObject record = new JSONObject();
        try {
            record.put("building", building);
            record.put("room", room);
        } catch(JSONException e) {}

        JSONArray reservationsArray = new JSONArray();
        for(TimeBlock tb : reservations) {

            JSONObject obj = new JSONObject();
            try {
                // Build reservation object as JSONObject
                obj.put(KEY_END_TIME, tb.endTime);
                obj.put(KEY_START_TIME, tb.startTime);

                // Record the reservation entry
                reservationsArray.put(obj);
            } catch(JSONException e) {
                Log.e(TAG, "unable to construct reservation list item as JSON String.");
            }
        }

        try {
            record.put("reservations", reservationsArray);
        } catch(JSONException e) {}

        return record.toString();

    }

    /**
     * Data class for holding start/end times of reserved blocks of time.
     */
    public static class TimeBlock {

        public long startTime;
        public long endTime;

        public TimeBlock(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public TimeBlock(Calendar startTime, Calendar endTime) {
            this.startTime = startTime.getTimeInMillis();
            this.endTime = endTime.getTimeInMillis();
        }

        public TimeBlock(Date startDate, Date endDate, Calendar currentCal) {
            // Configure calendar with proper start date/time
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            startCal.set(currentCal.get(Calendar.YEAR),
                         currentCal.get(Calendar.MONTH),
                         currentCal.get(Calendar.DAY_OF_MONTH));

            // Configure calendar with proper end date/time
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            endCal.set(currentCal.get(Calendar.YEAR),
                       currentCal.get(Calendar.MONTH),
                       currentCal.get(Calendar.DAY_OF_MONTH));

            // Create TimeBlock with Calendar objects
            this.startTime = startCal.getTimeInMillis();
            this.endTime = endCal.getTimeInMillis();
        }

        public String toString() {
            Calendar c = Calendar.getInstance();

            c.setTimeInMillis(startTime);
            CharSequence startString = DateFormat.format("MM/dd/yyyy hh:mmaa", c.getTime());

            c.setTimeInMillis(endTime);
            CharSequence endString = DateFormat.format("MM/dd/yyyy hh:mmaa", c.getTime());

            return startString + " - " + endString;
        }

    }

}