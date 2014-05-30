package edu.dartmouth.cs.dtutor.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import edu.dartmouth.cs.dtutor.Globals;
import edu.dartmouth.cs.dtutor.ReservableRoom;
import edu.dartmouth.cs.dtutor.utils.LocationsUtilities;

/**
 * 
    create table locations(
        uid int(11) primary key auto_increment,
        building varchar(50) not null,
        room varchar(50),
        latitude double(17,14),
        longitude double(17,14),
        reservations longtext
    );
 */
public class LocationsDbHelper extends SQLiteOpenHelper {

    private static final String TAG = Globals.TAG + ".LocationsDbHelper";

    // Database Name
    private static final String DATABASE_NAME = "locations.db";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_ROOMS = "reservableRooms";

    // Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_BUILDING_NAME = "building";
    private static final String KEY_ROOM = "room";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LONG = "longitude";
    private static final String KEY_RESERVATIONS = "reservations";
    
    /* ****************************************************************** *
     *          Database Methods: Setup/Configuring & Handling
     * ****************************************************************** */

    private static LocationsDbHelper singleton = null;

    private static final String CREATE_ROOMS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ROOMS
                                                     + "("
                                                     + KEY_ID
                                                     + " INTEGER PRIMARY KEY, "
                                                     + KEY_BUILDING_NAME
                                                     + " TEXT, "
                                                     + KEY_ROOM
                                                     + " TEXT, "
                                                     + KEY_LAT
                                                     + " REAL, "
                                                     + KEY_LONG
                                                     + " REAL, "
                                                     + KEY_RESERVATIONS
                                                     + " TEXT"
                                                     + ");";

    public synchronized static LocationsDbHelper getInstance(Context context) {
        if(singleton == null) {
            singleton = new LocationsDbHelper(context.getApplicationContext());
        }
        return(singleton);
    }

    private LocationsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate() database: " + DATABASE_NAME);
        db.execSQL(CREATE_ROOMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade() database: " + DATABASE_NAME);

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);

        // Create tables again
        onCreate(db);
    }

    /* ****************************************************************** *
     *          Database Methods: Setup/Configuring & Handling
     * ****************************************************************** */

    public void addRoom(ReservableRoom r) {
//        Log.d(TAG, "addRoom> " + r.building + ":" + r.room);
        
        ContentValues values = new ContentValues();
        values.put(KEY_BUILDING_NAME, r.building);
        values.put(KEY_ROOM, r.room);
        values.put(KEY_LAT, r.latlng.latitude);
        values.put(KEY_LONG, r.latlng.longitude);
        values.put(KEY_RESERVATIONS, r.getReservationsAsStr());

        // Inserting Row
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_ROOMS, null, values);
        db.close();
    }
    
    public void updateRoomReservations(ReservableRoom r) {
//        Log.d(TAG, "updateRoom> " + r.building + ":" + r.room);
        
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_RESERVATIONS, r.getReservationsAsStr());

        // Inserting Row
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_ROOMS, newValues, KEY_ID + "=" + r.id, null);
        db.close();
    }
    
    /*
     * TODO: Update entry.... (add/delete reservation)
     * 
     * add works but we need delete....
     */
    
    public ReservableRoom fetchEntryByIndex(long rowId) {
//        Log.d(TAG, "fetching entry: " + rowId);

        // Get readable database
        SQLiteDatabase database = getReadableDatabase();

        // Query for rowId
        Cursor cursor =
            database.query(TABLE_ROOMS,
                           null,
                           " " + KEY_ID + " = ?",
                           new String[] { String.valueOf(rowId) },
                           null,
                           null,
                           null,
                           null);

        // Set cursor
        if(cursor != null)
            cursor.moveToFirst();

        // Construct entry from database
        ReservableRoom entry = cursorToReservableRoom(cursor);

        // Clean up
        cursor.close();
        database.close();

//        Log.d(TAG, "entry(" + rowId + ")\n");

        return entry;
    }
    
    public ReservableRoom fetchEntryByBuildingAndRoom(String buildingName, String roomName) {
//        Log.d(TAG, "fetching entry: " + buildingName + ":" + roomName);

        // Get readable database
        SQLiteDatabase database = getReadableDatabase();

        // Query for rowId
        Cursor cursor =
            database.query(TABLE_ROOMS,
                           null,
                           " " + KEY_BUILDING_NAME + " = ? AND " + KEY_ROOM + " = ?",
                           new String[] { buildingName, roomName },
                           null,
                           null,
                           null,
                           null);

        // Set cursor
        if(cursor != null)
            cursor.moveToFirst();

        // Construct entry from database
        ReservableRoom entry = cursorToReservableRoom(cursor);

        // Clean up
        cursor.close();
        database.close();

        return entry;
    }
    
    public ArrayList<ReservableRoom> fetchAllEntries() {
        ArrayList<ReservableRoom> entries = new ArrayList<ReservableRoom>();
        
        // Get readable database
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(TABLE_ROOMS, null, null, null, null, null, null);

        // Load all database entries into a list
        ReservableRoom r = null;

        if(cursor.moveToFirst()) {
            do {
                r = cursorToReservableRoom(cursor);
                entries.add(r);
            } while(cursor.moveToNext());
        }

        // Clean up
        cursor.close();
        database.close();

//        Log.d(TAG, "fetched " + entries.size() + " entries!");
        return (ArrayList<ReservableRoom>) entries;
    }
    
    private static ReservableRoom cursorToReservableRoom(Cursor cursor) {
        long id = cursor.getLong(0);
        String building = cursor.getString(1);
        String room = cursor.getString(2);
        LatLng latlng = new LatLng(cursor.getDouble(3), cursor.getDouble(4));
        String resString = cursor.getString(5);
        
        return new ReservableRoom(id, building, room, latlng, resString);
    }
    
    /**
     * Recreate database: delete all tables and create them again
     */
    public void resetTables() {
//        Log.d(TAG, "delete locations table!");
        
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROOMS, null, null);
        db.close();
    }
    
    /**
     * PLEASE KEEP:
     * 
     * This should only be called when we need to re-populate the server database...
     */
    public static void uploadData() {
//        ArrayList<ReservableRoom> rrList = new ArrayList<ReservableRoom>();
        
//        final LatLng SUDIKOFF_LATLNG = new LatLng(43.70673044346993, -72.2871831804514);
//        final LatLng LIFE_SCIENCES_LATLNG = new LatLng(43.70908183628646, -72.28394877165556);
//        final LatLng BAKER_BERRY_LATLNG = new LatLng(43.705414864144274, -72.28876031935215);
//        final LatLng THAYER_LATLNG = new LatLng(43.70460703371793, -72.29502830654383);
//        final LatLng KEMENY_HALL_LATLNG = new LatLng(43.70640518686137, -72.28912677615881);
//        final LatLng WILDER_LATLNG = new LatLng(43.70534142546446,-72.28625413030386);
//        final LatLng THORTON_LATLNG = new LatLng(43.70338642079971,-72.28704169392586);
//        final LatLng COLLIS_LATLNG = new LatLng(43.702740474797196,-72.29000922292471);
                
        // 114, 115, 212, 213, 214
//        rrList.add(new ReservableRoom("Sudikoff", "", SUDIKOFF_LATLNG));
//        rrList.add(new ReservableRoom("Sudikoff", "SUDI 114", SUDIKOFF_LATLNG));
//        rrList.add(new ReservableRoom("Sudikoff", "SUDI 115", SUDIKOFF_LATLNG));
//        rrList.add(new ReservableRoom("Sudikoff", "SUDI 212", SUDIKOFF_LATLNG));
//        rrList.add(new ReservableRoom("Sudikoff", "SUDI 213", SUDIKOFF_LATLNG));
//        rrList.add(new ReservableRoom("Sudikoff", "SUDI 214", SUDIKOFF_LATLNG));
        
        // 100, 105, 200, 201, 205
//        rrList.add(new ReservableRoom("Life Sciences", "", LIFE_SCIENCES_LATLNG));
//        rrList.add(new ReservableRoom("Life Sciences", "LSC 100", LIFE_SCIENCES_LATLNG));
//        rrList.add(new ReservableRoom("Life Sciences", "LSC 105", LIFE_SCIENCES_LATLNG));
//        rrList.add(new ReservableRoom("Life Sciences", "LSC 200", LIFE_SCIENCES_LATLNG));
//        rrList.add(new ReservableRoom("Life Sciences", "LSC 201", LIFE_SCIENCES_LATLNG));
//        rrList.add(new ReservableRoom("Life Sciences", "LSC 205", LIFE_SCIENCES_LATLNG));
        
        // Baker 201, Baker 213, BerryL 178A, BerryL 277, BerryL 365, BerryL 370, BerryL 371
//        rrList.add(new ReservableRoom("Baker Berry Library", "", BAKER_BERRY_LATLNG));
//        rrList.add(new ReservableRoom("Baker Berry Library", "Baker 201", BAKER_BERRY_LATLNG));
//        rrList.add(new ReservableRoom("Baker Berry Library", "Baker 213", BAKER_BERRY_LATLNG));
//        rrList.add(new ReservableRoom("Baker Berry Library", "BerryL 178A", BAKER_BERRY_LATLNG));
//        rrList.add(new ReservableRoom("Baker Berry Library", "BerryL 277", BAKER_BERRY_LATLNG));
//        rrList.add(new ReservableRoom("Baker Berry Library", "BerryL 365", BAKER_BERRY_LATLNG));
//        rrList.add(new ReservableRoom("Baker Berry Library", "BerryL 370", BAKER_BERRY_LATLNG));
//        rrList.add(new ReservableRoom("Baker Berry Library", "BerryL 371", BAKER_BERRY_LATLNG));
        
        // Cummings 102, Cummings 105, MacLean 101, Murdough 335
//        rrList.add(new ReservableRoom("Thayer", "", THAYER_LATLNG));
//        rrList.add(new ReservableRoom("Thayer", "Cummings 102", THAYER_LATLNG));
//        rrList.add(new ReservableRoom("Thayer", "Cummings 105", THAYER_LATLNG));
//        rrList.add(new ReservableRoom("Thayer", "MacLean 101", THAYER_LATLNG));
//        rrList.add(new ReservableRoom("Thayer", "Murdough 335", THAYER_LATLNG));
        
        // 006, 007, 008, 105, 108
//        rrList.add(new ReservableRoom("Kemeny Hall", "", KEMENY_HALL_LATLNG));
//        rrList.add(new ReservableRoom("Kemeny Hall", "006", KEMENY_HALL_LATLNG));
//        rrList.add(new ReservableRoom("Kemeny Hall", "007", KEMENY_HALL_LATLNG));
//        rrList.add(new ReservableRoom("Kemeny Hall", "008", KEMENY_HALL_LATLNG));
//        rrList.add(new ReservableRoom("Kemeny Hall", "105", KEMENY_HALL_LATLNG));
//        rrList.add(new ReservableRoom("Kemeny Hall", "108", KEMENY_HALL_LATLNG));
        
        // 102, 104, 111, 115
//        rrList.add(new ReservableRoom("Wilder", "", WILDER_LATLNG));
//        rrList.add(new ReservableRoom("Wilder", "102", WILDER_LATLNG));
//        rrList.add(new ReservableRoom("Wilder", "104", WILDER_LATLNG));
//        rrList.add(new ReservableRoom("Wilder", "111", WILDER_LATLNG));
//        rrList.add(new ReservableRoom("Wilder", "115", WILDER_LATLNG));
        
        // 101, 103, 104, 105, 107
//        rrList.add(new ReservableRoom("Thorton", "", THORTON_LATLNG));
//        rrList.add(new ReservableRoom("Thorton", "101", THORTON_LATLNG));
//        rrList.add(new ReservableRoom("Thorton", "103", THORTON_LATLNG));
//        rrList.add(new ReservableRoom("Thorton", "104", THORTON_LATLNG));
//        rrList.add(new ReservableRoom("Thorton", "105", THORTON_LATLNG));
//        rrList.add(new ReservableRoom("Thorton", "107", THORTON_LATLNG));
        
        // 209, 212, 218, 219, 221, 222, 223, 301C
//        rrList.add(new ReservableRoom("Collis Center", "", COLLIS_LATLNG));
//        rrList.add(new ReservableRoom("Collis Center", "209", COLLIS_LATLNG));
//        rrList.add(new ReservableRoom("Collis Center", "212", COLLIS_LATLNG));
//        rrList.add(new ReservableRoom("Collis Center", "218", COLLIS_LATLNG));
//        rrList.add(new ReservableRoom("Collis Center", "219", COLLIS_LATLNG));
//        rrList.add(new ReservableRoom("Collis Center", "221", COLLIS_LATLNG));
//        rrList.add(new ReservableRoom("Collis Center", "222", COLLIS_LATLNG));
//        rrList.add(new ReservableRoom("Collis Center", "223", COLLIS_LATLNG));
//        rrList.add(new ReservableRoom("Collis Center", "301C", COLLIS_LATLNG));
        
        // Send each location to server...
//        for(ReservableRoom rr : rrList) {
//            Log.i(TAG, "adding...");
//            JSONObject jsonObj = LocationsUtilities.addLocation(rr);
//            Log.i(TAG, "add " + rr.building + ": " + rr.room + " -- response="+jsonObj);
//        }
        
    }
    
    /**
     * Sync locations data from server.
     */
    public static void syncData(Context context) {
        // Fetch list of rooms from server...
        JSONObject allLocationsJsonObj = LocationsUtilities.fetchAllLocations();

        // Drop current locations table...
        LocationsDbHelper db = LocationsDbHelper.getInstance(context);
        db.resetTables();
        
        // Add each to local database...
        try {
            JSONArray locationsArray = allLocationsJsonObj.getJSONArray(LocationsUtilities.KEY_LOCATIONS);
            Log.d(TAG, "# of locations = "+locationsArray.length());
            
            for(int i = 0; i < locationsArray.length(); i++) {
                JSONObject jsonObj = locationsArray.getJSONObject(i);
                long uid = jsonObj.getLong(LocationsUtilities.KEY_UID);
                String building = jsonObj.getString(LocationsUtilities.KEY_BUILDING);
                String room = jsonObj.getString(LocationsUtilities.KEY_ROOM);
                double latitude = jsonObj.getDouble(LocationsUtilities.KEY_LAT);
                double longitude = jsonObj.getDouble(LocationsUtilities.KEY_LONG);
                String reservationsString = jsonObj.getString(LocationsUtilities.KEY_RESERVATIONS);
                
                // Configure room & add to local database.
                ReservableRoom rr = new ReservableRoom(uid, building, room, new LatLng(latitude, longitude), reservationsString);
                db.addRoom(rr);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
        db = null;
        
    }
    
}