package edu.dartmouth.cs.dtutor;

import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.dartmouth.cs.dtutor.data.LocationsDbHelper;
import edu.dartmouth.cs.dtutor.utils.ServerUtilities;

public class MapDisplayActivity extends Activity implements OnMarkerClickListener, OnMapLongClickListener { //, LocationListener {

    private static final String TAG = Globals.TAG + ".MapDisplayActivity";

    public static final String KEY_WELCOME_MESSAGE = "welcome-message";
    public static final String KEY_INITIAL_ANIMATE = "initial-animate";

    // Map objects
    private GoogleMap mMap;

    // Map should fix on a central location
    static final LatLng DARTMOUTH_BAKER_BERRY = new LatLng(43.705309, -72.288843);
    
    private List<ReservableRoom> reservableRooms;
    
    boolean welcomeMsgShown = false;
    boolean initialAnimateDone = false;

    /* ****************************************************************** *
     *                    General Activity UI Methods
     * ****************************************************************** */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_display);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        int resultCode = GooglePlayServicesUtil
                        .isGooglePlayServicesAvailable(getApplicationContext());

        String msg = "";
        if (resultCode == ConnectionResult.SUCCESS) {
            // Setup map.
            mapSetup();
            msg = getString(R.string.book_a_room_welcome_message);
            
            //Toast.makeText(getApplicationContext(), 
            //               "isGooglePlayServicesAvailable SUCCESS", Toast.LENGTH_LONG)
            //               .show();
        } else {
            msg = "Oops!\n\nGoogle Play Services seems to be unavailable...";
            //Toast.makeText(getApplicationContext(), 
            //               "isGooglePlayServicesAvailable FAILED", Toast.LENGTH_LONG)
            //               .show();
        }
        
        // Only show welcome message once...
        if(!welcomeMsgShown) {
            // Display 'welcome' message.
            //String welcomeMsg = getString(R.string.book_a_room_welcome_message);
            DtutorDialogFragment.displayDialog(getFragmentManager(),
                                               DtutorDialogFragment.DIALOG_ID_BASIC_MESSAGE,
                                               DtutorDialogFragment.DIALOG_ID_BASIC_MESSAGE_TAG,
                                               msg);
            welcomeMsgShown = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_WELCOME_MESSAGE, welcomeMsgShown);
        outState.putBoolean(KEY_INITIAL_ANIMATE, initialAnimateDone);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            welcomeMsgShown = savedInstanceState.getBoolean(KEY_WELCOME_MESSAGE);
            initialAnimateDone = savedInstanceState.getBoolean(KEY_INITIAL_ANIMATE);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    /* ****************************************************************** *
     *                        Map Interface Methods
     * ****************************************************************** */

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i(TAG, "onMarkerClick!: " + marker.getTitle());
        
        // Show overlaid activity with reservation details.
        Intent i = new Intent(getApplicationContext(), ReservationsBrowseActivity.class);
        i.putExtra(ReservationsBrowseActivity.KEY_ORIENTATION, getResources().getConfiguration().orientation);
        i.putExtra(ReservationsBrowseActivity.KEY_EXPAND_GROUP_TAG, marker.getTitle());
        startActivity(i);
        
        return true;
    }

    @Override
    public void onMapLongClick(LatLng point) {
        // TODO: This is a helper for getting Lat/Longs of places...
        //Log.i(TAG, "New marker added@" + point.toString());
        //mMap.addMarker(new MarkerOptions().position(point).title(point.toString()));
    }

    /* ****************************************************************** *
     *                          Map Helper Methods
     * ****************************************************************** */

    public void mapSetup() {
        Log.d(TAG, "mapSetup()");
        
        // Initialize map fragment
        if(mMap == null) {
            Log.d(TAG, "no map previously setup -- setting up now...");

            // Obtain map
            FragmentManager myFragmentManager = getFragmentManager();
            MapFragment myMapFragment = (MapFragment) myFragmentManager.findFragmentById(R.id.map);
            mMap = myMapFragment.getMap();

            mMap.setMyLocationEnabled(true);

            //mMap.setOnMapLongClickListener(this); // TODO: debug...
            mMap.setOnMarkerClickListener(this);

            if(!initialAnimateDone) {
                // Set initial camera location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DARTMOUTH_BAKER_BERRY, 0));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                initialAnimateDone = true;
            } else {
                // Initial camera animation already done - simply update camera
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(DARTMOUTH_BAKER_BERRY, 15);
                mMap.animateCamera(update);
            }

            // Populate server with some data...
//            doUpload(); // TODO: only uncomment this if you know what you are doing...
            
            // Load all Locations data from server...
            doSync();
        }
    }
    
    /*
     * [TESTING] Manual setup
     */    
    public void doUpload() {
        if(ServerUtilities.isNetworkAvailable(getApplicationContext())) {
            mLoadTask = new LoadLocationsTask();
            mLoadTask.execute((Void) null);
        } else { // no Internet connection
            Log.e(TAG, "load location data error: no internet connection...");
            DtutorDialogFragment.displayDialog(getFragmentManager(),
                                               DtutorDialogFragment.DIALOG_ID_BASIC_MESSAGE,
                                               DtutorDialogFragment.DIALOG_ID_BASIC_MESSAGE_TAG, 
                                               "load location data error:\n\nno internet connection...");
        }
    }
    
    public void doSync() {
        if(ServerUtilities.isNetworkAvailable(getApplicationContext())) {
            mSyncTask = new SyncLocationsTask();
            mSyncTask.execute((Void) null);
        } else { // no Internet connection
            Log.e(TAG, "sync location data error: no internet connection...");
            DtutorDialogFragment.displayDialog(getFragmentManager(),
                                               DtutorDialogFragment.DIALOG_ID_BASIC_MESSAGE,
                                               DtutorDialogFragment.DIALOG_ID_BASIC_MESSAGE_TAG, 
                                               "sync location data error:\n\nno internet connection...");
        }
    }
    
    public void show() {
        LocationsDbHelper locationsDb = LocationsDbHelper.getInstance(getApplicationContext());
        reservableRooms = locationsDb.fetchAllEntries();
        
        // Use ReservableRoom object to:
        //   (1) add geographical marker to the map, and
        //   (2) add the corresponding title to the marker.
        for(ReservableRoom r : reservableRooms) {
            if(r.room.isEmpty()) {
                Log.d(TAG, "adding building to map: " + r.building);
                mMap.addMarker(new MarkerOptions().position(r.latlng).title(r.building));
            }
        }
    }
    
    /**
     * Represents an asynchronous task used to load location data.
     */
    LoadLocationsTask mLoadTask;

    public class LoadLocationsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            LocationsDbHelper.uploadData();
            return true;
        }
                
        @Override
        protected void onPostExecute(final Boolean success) {
            mLoadTask = null;
            Log.i(TAG, "onPostExecute(): Load COMPLETE");
        }

    }
    
    /**
     * Represents an asynchronous task used to sync location data from server.
     */
    SyncLocationsTask mSyncTask;

    public class SyncLocationsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            LocationsDbHelper.syncData(getApplicationContext());
            return true;
        }
                
        @Override
        protected void onPostExecute(final Boolean success) {
            mSyncTask = null;
            Log.i(TAG, "onPostExecute(): Sync COMPLETE");
            show();
        }

    }

}