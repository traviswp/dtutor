package edu.dartmouth.cs.dtutor.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import edu.dartmouth.cs.dtutor.Globals;

public class MemberDbHelper extends SQLiteOpenHelper {

    private static final String TAG = Globals.TAG + ".MemberDbHelper";

    private static final String DATABASE_NAME = "Members.db";
    private static final int DATABASE_VERSION = 1;
    public static final String MEMBER_TABLE_NAME = "members";
    public static final String TUTOR_TABLE_NAME = "tutor";
    public static final String TUTEE_TABLE_NAME = "tutee";
    public static final String PROFILE_TABLE_NAME = "profile";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String MEMBER_TYPE = "member_type";
    public static final String COURSES = "courses";
    public static final String ABOUT = "about";
    public static final String NOTIFICATIONS = "notifications";
    public static final String RESERVATIONS = "reservations";

    // the next entries are only for the profile table - will set up better later
    public static final String TUTOR_COURSES = "tutor_courses";
    public static final String TUTOR_ABOUT = "tutor_about";
    public static final String TUTEE_COURSES = "tutee_courses";
    public static final String TUTEE_ABOUT = "tutee_about";

    // Database creation sql statement

    // create member table
    private static final String MEMBER_TABLE_CREATE = "create table if not exists " + MEMBER_TABLE_NAME
                                                      + "("
                                                      + EMAIL
                                                      + " TEXT primary key not null, "
                                                      + NAME
                                                      + " TEXT not null, "
                                                      + MEMBER_TYPE
                                                      + " TEXT not null"
                                                      // + AVAILABILITY + " BLOB not null "
                                                      + ");";

    // create tutor table
    private static final String TUTOR_TABLE_CREATE = "create table if not exists " + TUTOR_TABLE_NAME
                                                     + "("
                                                     + EMAIL
                                                     + " TEXT primary key not null, "
                                                     + TUTOR_COURSES
                                                     + " TEXT not null, "
                                                     + TUTOR_ABOUT
                                                     + " TEXT not null "
                                                     // + AVAILABILITY + " BLOB not null "
                                                     + ");";

    // create tutor table
    private static final String TUTEE_TABLE_CREATE = "create table if not exists " + TUTEE_TABLE_NAME
                                                     + "("
                                                     + EMAIL
                                                     + " TEXT primary key not null, "
                                                     + TUTEE_COURSES
                                                     + " TEXT not null, "
                                                     + TUTEE_ABOUT
                                                     + " TEXT not null "
                                                     // + AVAILABILITY + " BLOB not null "
                                                     + ");";

    // create profile table
    private static final String PROFILE_TABLE_CREATE = "create table if not exists " + PROFILE_TABLE_NAME
                                                       + "("
                                                       + ID
                                                       + " integer primary key autoincrement, "
                                                       + EMAIL
                                                       + " TEXT not null, "
                                                       + NAME
                                                       + " TEXT, "
                                                       + MEMBER_TYPE
                                                       + " TEXT, "
                                                       + NOTIFICATIONS
                                                       + " TEXT, "
                                                       + RESERVATIONS
                                                       + " TEXT, "
                                                       + TUTOR_COURSES
                                                       + " TEXT, "
                                                       + TUTOR_ABOUT
                                                       + " TEXT, "
                                                       + TUTEE_COURSES
                                                       + " TEXT, "
                                                       + TUTEE_ABOUT
                                                       + " TEXT"
                                                       // + AVAILABILITY + " BLOB not null "
                                                       + ");";

    // Constructor
    public MemberDbHelper(Context context) {
        // DATABASE_NAME inStream, of course the name of the database, which inStream defined as a string constant
        // DATABASE_VERSION inStream the version of database, which inStream defined as an integer constant
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create table schema if not exists
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create the sql tables (the database inStream create automatically if it doesn't exist
        db.execSQL(MEMBER_TABLE_CREATE);
        db.execSQL(TUTOR_TABLE_CREATE);
        db.execSQL(TUTEE_TABLE_CREATE);
        db.execSQL(PROFILE_TABLE_CREATE);
    }

    public boolean doesProfileExist() {

        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT COUNT(*) AS count FROM " + PROFILE_TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        boolean profileExists = true;

        int numRecords = cursor.getInt(cursor.getColumnIndex("count"));

        if(numRecords == 0)
            profileExists = false;
        else if(numRecords > 0)
            profileExists = true;
        cursor.close();
        db.close();

        return profileExists;
    }

    // Insert a new member
    public long insertEntry(Members member) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues memValues = new ContentValues();
        memValues.put(EMAIL, member.getEmail());
        memValues.put(NAME, member.getName());
        memValues.put(MEMBER_TYPE, member.getMemberType());

        // Insert user into Members table
        long row_id = db.insert(MemberDbHelper.MEMBER_TABLE_NAME, null, memValues);

        Log.v("dbDebugging", "User added to Members table with row_id = " + row_id);

        // add user to tutor table
        if(member.getMemberType() == 0 || member.getMemberType() == 2) { // if the user is a tutor
            ContentValues tValues = new ContentValues();
            tValues.put(EMAIL, member.getEmail());
            tValues.put(TUTOR_COURSES, member.getTutorCourses().toString());
            tValues.put(TUTOR_ABOUT, member.getTutorAbout().toString());
            long row_id_tutor = db.insert(MemberDbHelper.TUTOR_TABLE_NAME, null, tValues); // add a row to the tutor table
            Log.v("dbDebugging", "User added to Tutor table with row_id = " + row_id_tutor);
        }

        // add user to tutee table
        if(member.getMemberType() == 1 || member.getMemberType() == 2) { // if the user is a tutee
            ContentValues tValues = new ContentValues();
            tValues.put(EMAIL, member.getEmail());
            tValues.put(TUTEE_COURSES, member.getTuteeCourses().toString());
            tValues.put(TUTEE_ABOUT, member.getTuteeAbout().toString());
            long row_id_tutee = db.insert(MemberDbHelper.TUTEE_TABLE_NAME, null, tValues); // add a row to the tutee table
            Log.v("dbDebugging", "User added to Tutee table with row_id = " + row_id_tutee);
        }

        db.close();
        return row_id;
    }

    public void deleteProfile() {
        SQLiteDatabase db = this.getWritableDatabase();

        // delete the old profile first
        db.delete(PROFILE_TABLE_NAME, null, null);

        db.close();
    }

    // Add user profile
    public long addProfile(Members member) {

        SQLiteDatabase db = this.getWritableDatabase();

        // delete the old profile first
        db.delete(PROFILE_TABLE_NAME, null, null);

        // now add in the new member values
        ContentValues memValues = new ContentValues();
        memValues.put(EMAIL, member.getEmail());
        memValues.put(NAME, member.getName());
        memValues.put(MEMBER_TYPE, member.getMemberType());

        // By default, the members class contains empty course and about lists
        memValues.put(TUTOR_COURSES, member.getTutorCourses().toString());
        memValues.put(TUTOR_ABOUT, member.getTutorAbout());

        memValues.put(TUTEE_COURSES, member.getTuteeCourses().toString());
        memValues.put(TUTEE_ABOUT, member.getTuteeAbout());
        memValues.put(NOTIFICATIONS, member.getNotifications());
        memValues.put(RESERVATIONS, member.getReservations());

        // add user to the Profile table
        long row_id = db.insert(MemberDbHelper.PROFILE_TABLE_NAME, null, memValues);

        Log.v("dbDebugging", "added profile, row_id " + row_id);
        db.close();
        return row_id;
    }

    // Get the users profile
    public Members getProfile() {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursorProfile = db.query(MemberDbHelper.PROFILE_TABLE_NAME, null, null, null, null, null, null);
        cursorProfile.moveToFirst();

        Members member_entry = null;

        Log.i(TAG, "cursor: row count: " + cursorProfile.getCount());
        if(cursorProfile.getCount() > 0) {
            member_entry = new Members();

            member_entry.setEmail(cursorProfile.getString(cursorProfile.getColumnIndex(EMAIL)));
            member_entry.setName(cursorProfile.getString(cursorProfile.getColumnIndex(NAME)));
            member_entry.setMemberType(cursorProfile.getInt(cursorProfile.getColumnIndex(MEMBER_TYPE)));
            member_entry.setNotifications(cursorProfile.getString(cursorProfile.getColumnIndex(NOTIFICATIONS)));
            member_entry.setReservations(cursorProfile.getString(cursorProfile.getColumnIndex(RESERVATIONS)));

            // when the user doesn't have courses or about, those fields will be returned with empty strings
            member_entry.setTutorCoursesFromString(cursorProfile.getString(cursorProfile.getColumnIndex(TUTOR_COURSES)));
            member_entry.setTutorAbout(cursorProfile.getString(cursorProfile.getColumnIndex(TUTOR_ABOUT)));
            member_entry.setTuteeCoursesFromString(cursorProfile.getString(cursorProfile.getColumnIndex(TUTEE_COURSES)));
            member_entry.setTuteeAbout(cursorProfile.getString(cursorProfile.getColumnIndex(TUTEE_ABOUT)));
        }

        // close the cursors and the database
        cursorProfile.close();

        db.close();

        return member_entry;
    }

    // Find a member using the email
    public Members fetchEntryByEmail(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        // query the member profile
        Cursor cursorMember =
            db.query(MemberDbHelper.MEMBER_TABLE_NAME,
                     null,
                     MemberDbHelper.EMAIL + " = " + email,
                     null,
                     null,
                     null,
                     null);
        cursorMember.moveToFirst();

        // query the student's tutor profile
        Cursor cursorTutor =
            db.query(MemberDbHelper.TUTOR_TABLE_NAME,
                     null,
                     MemberDbHelper.EMAIL + " = " + email,
                     null,
                     null,
                     null,
                     null);
        cursorTutor.moveToFirst();

        // query the student's tutee profile
        Cursor cursorTutee =
            db.query(MemberDbHelper.TUTEE_TABLE_NAME,
                     null,
                     MemberDbHelper.EMAIL + " = " + email,
                     null,
                     null,
                     null,
                     null);
        cursorTutee.moveToFirst();

        Members member_entry = new Members();
        member_entry = cursorToMemberEntry(cursorMember, cursorTutor, cursorTutee);

        // close the cursors and the database
        cursorMember.close();
        cursorTutor.close();
        cursorTutee.close();

        db.close();

        return member_entry;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    private Members cursorToMemberEntry(Cursor cursorMember, Cursor cursorTutor, Cursor cursorTutee) {
        Members member_entry = new Members();
        // add info from the member table
        member_entry.setEmail(cursorMember.getString(cursorMember.getColumnIndex(EMAIL)));
        member_entry.setName(cursorMember.getString(cursorMember.getColumnIndex(NAME)));
        member_entry.setMemberType(cursorMember.getInt(cursorMember.getColumnIndex(MEMBER_TYPE)));

        // add info from the tutor table if it exists
        if(cursorTutor != null) {
            member_entry.setTutorCoursesFromString(cursorTutor.getString(cursorMember.getColumnIndex(TUTOR_COURSES)));
            member_entry.setTutorAbout(cursorTutor.getString(cursorMember.getColumnIndex(TUTOR_ABOUT)));
        }

        // add info from the tutee table if it exists
        if(cursorTutee != null) {
            member_entry.setTuteeCoursesFromString(cursorTutee.getString(cursorMember.getColumnIndex(TUTEE_COURSES)));
            member_entry.setTuteeAbout(cursorTutee.getString(cursorMember.getColumnIndex(TUTEE_ABOUT)));
        }

        return member_entry;
    }

    public ArrayList<Members> fetchAllEntries(int mType) { // mType specifies whether we want to fetch tutor data
                                                           // or tutee data

        Log.d("LOOKING", "GETTING ALL RECORDS FOR ROLE: " + String.valueOf(mType));
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Members> records = new ArrayList<Members>();

        String selectQuery = null;
        if(mType == 0) // get tutor records
            selectQuery =
                "SELECT * FROM " + MEMBER_TABLE_NAME + " mem, " + TUTOR_TABLE_NAME + " tut where mem.email=tut.email";
        else if(mType == 1) // get tutee records
            selectQuery =
                "SELECT * FROM " + MEMBER_TABLE_NAME + " mem, " + TUTEE_TABLE_NAME + " te where mem.email=te.email";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) { // if there are records in the table
            while(!cursor.isAfterLast()) {
                // store the current row into an exercise entry object
                Members member = new Members();
                member = cursorToMemberEntry(cursor, mType);
                records.add(member); // add the current row to the records array list
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return records;
    }

    private Members cursorToMemberEntry(Cursor cursor, int mType) {

        Members member = new Members();
        member.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL)));
        member.setName(cursor.getString(cursor.getColumnIndex(NAME)));
        member.setMemberType(cursor.getInt(cursor.getColumnIndex(MEMBER_TYPE)));
        if(mType == 0 || mType == 2) { // tutor
            try {
                member.setTutorCoursesFromString(cursor.getString(cursor.getColumnIndex(TUTOR_COURSES)));
                member.setTutorAbout(cursor.getString(cursor.getColumnIndex(TUTOR_ABOUT)));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(mType == 1 || mType == 2) { // tutee
            try {
                member.setTuteeCoursesFromString(cursor.getString(cursor.getColumnIndex(TUTEE_COURSES)));
                member.setTuteeAbout(cursor.getString(cursor.getColumnIndex(TUTEE_ABOUT)));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(member.getMemberType() == 2) {
            // Log.d("cursorToMemberEntry", "member: " + member.getEmail() + ", tuteeCourses: "
            // +cursor.getString(cursor.getColumnIndex(TUTEE_COURSES)) + ", tutorCourses: " +
            // member.getTutorCourses().toString());
        }

        return member;

    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MEMBER_TABLE_NAME, null, null);
        db.delete(TUTOR_TABLE_NAME, null, null);
        db.delete(TUTEE_TABLE_NAME, null, null);
        db.close();

    }
}
