package edu.dartmouth.cs.dtutor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.dartmouth.cs.dtutor.data.AsyncResponse;
import edu.dartmouth.cs.dtutor.data.MemberDbHelper;
import edu.dartmouth.cs.dtutor.data.Members;
import edu.dartmouth.cs.dtutor.data.QueryDbForMember;
import edu.dartmouth.cs.dtutor.data.UpdateMemberNotifications;

public class ActivitySingleEntryDisplay extends Activity implements AsyncResponse {

	private String email; // user's email
	private int mType;
	private String mName;
	UpdateMemberNotifications updateTask; //Async Task for adding a new member to the member table
	QueryDbForMember getProfileTask; //Async Task for querying the SQL database
	private String oldNotifications = "null";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_single_entry_display);

		//        String number = "tel:3334444";
		//        Intent callIntent = new Intent(Intent.ACTION_DIAL);
		//        callIntent.setData(Uri.parse(number));
		//        startActivity(callIntent);

		//get the intent
		Bundle extras = getIntent().getExtras();

		//get the email of the record being displayed
		email = extras.getString(MemberDbHelper.EMAIL);
		mType = extras.getInt(MemberDbHelper.MEMBER_TYPE);
		mName = extras.getString(MemberDbHelper.NAME);

		TextView fname = (TextView) findViewById(R.id.name_v);
		fname.setText(mName);

		String courses = extras.getString(MemberDbHelper.COURSES);
		//		courses = courses.trim().replaceAll(" ", ""); // remove all white spaces
		String[] courseL = courses.split(",");

		int count = courseL.length;
		LinearLayout ll= (LinearLayout)findViewById(R.id.add_new_single_entry);
		for(int i = 0; i<count; i++) {

			TextView n= new TextView(this);
			String mCourse = courseL[i].trim();
			if(i==0)
				mCourse = mCourse.substring(2, mCourse.length());
			if(i==count-1)
				mCourse = mCourse.substring(0, mCourse.length()-2);
			n.setText(mCourse);
			ll.addView(n);	
		}

		TextView about = (TextView) findViewById(R.id.about_v);
		about.setText(extras.getString(MemberDbHelper.ABOUT));

	}

	public void request(View v) throws JSONException {

		// get profile from the server and load it into the Profile table
		getProfileTask = new QueryDbForMember(getApplicationContext(), email); //get the member we are trying to append to 
		getProfileTask.asyncResponse = this;	// set a delegate to get the query result from the asynctask
		getProfileTask.execute();

	}

	public void updateNotifications() throws JSONException {
		// get the member that is posting the request
		MemberDbHelper dbHelper = new MemberDbHelper(getApplicationContext());
		Members myProfile = dbHelper.getProfile();
		String myEmail = myProfile.getEmail();
		
		JSONObject notificationsObj = new JSONObject();
		JSONArray notifications = new JSONArray();

		if (!oldNotifications.equals("null")) {// if the user already has notifications
			try {
				notificationsObj = new JSONObject(oldNotifications);
			}
			catch(JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
				notificationsObj = new JSONObject();
			}

		} else
			notificationsObj = new JSONObject(); 

//		Log.d("test", "Old Not:" + notificationsObj.toString());

		notifications = appendNotificationsToJSONObject(notificationsObj, myEmail, mType, -1);

		JSONObject newNot = new JSONObject();
		newNot.put("email", email);
		newNot.put("request_type", mType); //whether they want a tutor or a tutee
		newNot.put("response", -1);
		
		// create the new notifications string and send it to the member
		Log.d("test", newNot.toString());
		
		updateTask = new UpdateMemberNotifications(getApplicationContext(), email, newNot.toString());
		updateTask.asyncResponse = this;	// set a delegate to get the query result from the asynctask
		updateTask.execute();
	}
	
	//convert notifications to a json object
	public JSONArray appendNotificationsToJSONObject(JSONObject notifications, String email, int mType2, int i) throws JSONException {

		JSONObject newNot = new JSONObject();
		newNot.put("email", email);
		newNot.put("request_type", mType2); //whether they want a tutor or a tutee
		newNot.put("response", i);
		String[] names = {"email", "request_type", "response"};

		JSONArray arr = new JSONArray();

		try {
			JSONObject merged = new JSONObject(notifications, names);
			merged.put("email", newNot.get("email"));
			merged.put("request_type", newNot.get("request_type"));
			merged.put("response", newNot.get("response"));
//			Log.d("test", "NOTIFICATIONS:" + merged.toString());
			arr.put(notifications);
			arr.put(newNot);
			return arr;
		}
		catch (JSONException e){
			return null;
		}
		
		//		try {
		//			// append the new notifications on
		//			notifications.put("email", email);
		//			notifications.put("request_type", mType2); //whether they want a tutor or a tutee
		//			notifications.put("response", i);
		//			Log.d("test", "NOTIFICATIONS:" + notifications.toString());
		//			return notifications;
		//		}
		//		catch (JSONException e){
		//			return null;
		//		}

	}
	public void sendEmail(View v) {
		try {

			String subject = "DTutor: Tutoring Request"; // if we are emailing a tutor
			if (mType == 1)
				subject = "DTutor: I would like to be your tutor";

			final Intent emailIntent = new Intent(Intent.ACTION_SEND); 
			emailIntent.setType("message/rfc822");
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email}); 
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject); 
			emailIntent.putExtra(Intent.EXTRA_TEXT, ""); 
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		}
		catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it inStream present.
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// when the user clicks on the delete button, open the database and delete the record
		//open the database and fetch entries

		return true;
	}

	@Override
	public void processFinish(Integer result) {
		if(result == 1)
			Log.d("arvi", "added member successfully");
		else
			Log.d("arvi", "added member successfully");
		updateTask.cancel(true);

	}
	
	@Override
	public void processFinish(JSONArray result) throws JSONException { //after you get the user profile from the server
		if(result == null || result.length() == 0) { // Member not found
			Log.d("arvi", "MEMBER DOES NOT EXIST IN THE DATABASE");
			return;
		}

		Log.d("arvi", "result: " + result.toString());
		Members member = new Members();
		member.setMemberType(mType);

		// add the query result to the member class
		member.fromJSONObject(result.getJSONObject(0));
		Log.d("arvi", "member " + member.getName());

		// add the profile to the database
		oldNotifications = member.getNotifications();
		getProfileTask.cancel(true);		
		updateNotifications();
	}

}
