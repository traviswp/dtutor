package edu.dartmouth.cs.dtutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import edu.dartmouth.cs.dtutor.data.LocationsDbHelper;

public class ReservationsBrowseActivity extends Activity {

    private static final String TAG = Globals.TAG + ".ReservationsBrowseActivity";

    // Keys
    public static final String KEY_ORIENTATION = "orientation";
    public static final String KEY_EXPAND_GROUP_TAG = "expand-group-tag";
    
    // Views
    ReservationsExpandableListAdapter listAdapter;
    ExpandableListView expandableListView;
    
    // Static data
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    
    // Dynamic data
    private String expandGroupTag = "";
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations_browse);

        Bundle extras = getIntent().getExtras();
        
        // Set orientation...
        if(extras != null) {
            int orientation = extras.getInt(KEY_ORIENTATION);
            setRequestedOrientation(orientation | ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            
            expandGroupTag = extras.getString(KEY_EXPAND_GROUP_TAG);
        }
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        viewSetup();
    }
    
    @Override
    protected void onPause() {
        super.onPause(); 
    }
        
    public void viewSetup() {
        
        expandableListView = (ExpandableListView) findViewById(R.id.expandable_list_view);

        // preparing list data
        prepareListData();

        // configure adapter
        listAdapter = new ReservationsExpandableListAdapter(this, listDataHeader, listDataChild);

        // set list adapter
        expandableListView.setAdapter(listAdapter);
        
        // for nice ui experience, expand the selected building
        if(!expandGroupTag.isEmpty()) {
            int id = 0;
            for(; id < listDataHeader.size(); id++) {
                if(expandGroupTag.equalsIgnoreCase(listDataHeader.get(id))) {
                    break;
                }
            }
            expandableListView.expandGroup(id);
        }

        /* ************************************************************** *
         *                         ListView Listeners
         * ************************************************************** */

        // Listview Group click listener
        expandableListView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }

        });

        // Listview Group expanded listener
        expandableListView.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                //Log.d(TAG, listDataHeader.get(groupPosition) + " Expanded");
            }
            
        });

        // Listview Group collasped listener
        expandableListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                //Log.d(TAG, listDataHeader.get(groupPosition) + " Collapsed");
            }
            
        });

        // Listview on child click listener
        expandableListView.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String building = listDataHeader.get(groupPosition);
                String room = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                
                //Log.d(TAG, building + ":" + room + " clicked!");

//                Toast.makeText(getApplicationContext(),
//                               listDataHeader.get(groupPosition) + " : "
//                                               + listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition),
//                               Toast.LENGTH_SHORT).show();
                
                // Start reservations UI for room in building.
                Intent i = new Intent(getApplicationContext(), ReservationsActivity.class);
                i.putExtra("building-key", building);
                i.putExtra("room-key", room);
                startActivity(i);
                
                return false;
            }
            
        });
        
    }
        
    /**
     * Query local database for info about reservable rooms -- populate list view content.
     */
    private void prepareListData() {
        LocationsDbHelper locationsDb = LocationsDbHelper.getInstance(getApplicationContext());
        ArrayList<ReservableRoom> reservableRooms = locationsDb.fetchAllEntries();
        
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        
        boolean firstHeader = true;
        String header = "";
        List<String> childElements = null;
        
        for(ReservableRoom r : reservableRooms) {
            if(r.room.isEmpty()) {
                if(!firstHeader) {
                    //Log.e(TAG, "add: " + header + "(" + childElements.size() + ")");
                    listDataChild.put(header, childElements);
                }
                firstHeader = false;
                
                header = r.building;
                listDataHeader.add(header);
                childElements = new ArrayList<String>();
                
            } else { // add elements under this header
                childElements.add(r.room);
            }
        }
        //Log.d(TAG, "add: " + header + "(" + childElements.size() + ")");
        listDataChild.put(header, childElements);
    }

}