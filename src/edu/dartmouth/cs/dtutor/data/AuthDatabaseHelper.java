package edu.dartmouth.cs.dtutor.data;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import edu.dartmouth.cs.dtutor.Globals;

/**
 * AuthDatabaseHelper inStream a small database helper class for managing the current logged in user for the system. At
 * most one user will be in the table when a user inStream logged into the system, and the table should be deleted/reset
 * upon a system logout.
 * 
 * This database/table reflects the server-side database setup as follows:
 * 
 *  create table users(
 *      uid int(11) primary key auto_increment,
 *      name varchar(50) not null,
 *      email varchar(100) not null unique,
 *      encrypted_password varchar(80) not null,
 *      salt varchar(10) not null,
 *      created_at datetime
 *  );
 */
public class AuthDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = Globals.TAG + ".AuthDatabaseHelper";

    // Database Name
    private static final String DATABASE_NAME = "auth.db";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Login table name
    private static final String TABLE_LOGIN = "login";

    // Login Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_CREATED_AT = "created_at";

    /* ****************************************************************** *
     *          Database Methods: Setup/Configuring & Handling
     * ****************************************************************** */

    private static AuthDatabaseHelper singleton = null;

    private static final String CREATE_LOGIN_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LOGIN
                                                     + "("
                                                     + KEY_ID
                                                     + " INTEGER PRIMARY KEY,"
                                                     + KEY_NAME
                                                     + " TEXT,"
                                                     + KEY_EMAIL
                                                     + " TEXT UNIQUE,"
                                                     + KEY_CREATED_AT
                                                     + " TEXT"
                                                     + ");";

    public synchronized static AuthDatabaseHelper getInstance(Context context) {
        if(singleton == null) {
            singleton = new AuthDatabaseHelper(context.getApplicationContext());
        }
        return(singleton);
    }

    private AuthDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate() database: " + DATABASE_NAME);

        db.execSQL(CREATE_LOGIN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade() database: " + DATABASE_NAME);

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);

        // Create tables again
        onCreate(db);
    }

    /* ****************************************************************** *
     *          Database Methods: Setup/Configuring & Handling
     * ****************************************************************** */

    /**
     * Storing user details in database.
     * 
     * @param name
     * @param email
     * @param created_at
     */
    public void addUser(String name, String email, String created_at) {
        Log.d(TAG, "addUser> [" + name + ";" + email + ";" + created_at + "]");
        
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);             // Name
        values.put(KEY_EMAIL, email);           // Email
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_LOGIN, null, values);
        db.close();
    }

    /**
     * Getting user data from database as HashMap.
     * 
     * @return
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("created_at", cursor.getString(3));
        }
        cursor.close();
        db.close();

        // return user
        return user;
    }
    
    /**
     * Getting user data from database as String.
     * @return
     */
    public String getUserDetailsStr() {
        HashMap<String, String> user = getUserDetails();
        if(!user.isEmpty()) {
            String userString = "";
            for(String key : user.keySet()) {
                userString += "[" + key + ":" + user.get(key) + "]";
            }
            return userString;
        }
        return null;
    }

    /**
     * Getting user login status return true if rows are there in table
     * 
     * @return
     */
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    /**
     * Recreate database:delete all tables and create them again
     */
    public void resetTables() {
        Log.d(TAG, "local user delete!");
        
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOGIN, null, null);
        db.close();
    }

}