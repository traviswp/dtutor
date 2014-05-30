package edu.dartmouth.cs.dtutor;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import edu.dartmouth.cs.dtutor.data.AsyncResponse;
import edu.dartmouth.cs.dtutor.data.InsertMemberDb;
import edu.dartmouth.cs.dtutor.utils.AuthUtilities;

public class AddFakeRecords extends Activity implements AsyncResponse {
	InsertMemberDb insertTask; //Async Task for adding a new member to the member table
	private UserRegisterTask mAuthTask = null;
	
	// Data
    private String mRegisterErrorMsg;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_add_fake_records);

		// fake records 
		String[] names={"Andrew T. Campbell", "Alexander Tsu", "Douglas M. Crockatt", "Eva W. Xiao", "Janice Yip", "Kimberly A. Strauch", "Yongfu Lou", "Qiuhan Wang", "Janet Liu", "Xinran Xiao", "Matthew L. Krantz", "Nathaniel J. Lewin", "Max R. Gibson", "Robert W. Meyer", "Matthew G. Marcus", "Hongyi Jia", "Jiaming Jiang", "Joanne Zhao", "William T. McConnell", "Paul L. Champeau", "Lisa Luo", "Arvind C. Senthil Kumaran", "Travis W. Peters", "Haider Syed", "Cameron J. Price", "Patricia T. Neckowicz", "Andrew D. Pillsbury", "Barry Y. Chen", "Faizan N. Kanji", "James E. Verhagen", "Yondon Fu", "Michael S. Levine", "James A. Brofos", "Sayeh Gorjifard", "Rukmini Goswami", "Adenugbe A. Vormawor", "Laura E. Goodfellow", "Jordan K. Hall", "Jun Yang", "Richard M. Palomino", "Cristian Caraballo Jr.", "Vibhu Yadav", "William M. Wang", "Alexander P. Welton", "Kyle E. Tessier-Lavigne", "Tianlong Yun", "Xiaohong Qiu", "Mengjia Kong", "Mohammad Z. Akmal", "Patrick R. Yukman", "Wesley K. Thompson"};
		String[] emails= new String[names.length];
		
        //copy values
        for(int i =0;i < names.length;i++)
            emails[i] = names[i];
		
		int numCourses = 1;
		int deptInd = 0;
		int courseNum = 1; // course number
		
		Random randNum = new Random();
		// GENERATE SOME TUTORS : Half the class are tutors
		ArrayList<String> courses = new ArrayList<String> ();
		String[] about = {"I have been tutoring for 2 years with great success.", "I always love working with students and am very flexible with meeting times", 
				"I can help you", "I have been tutoring for 3 years and usually have good success with students", "I have a proven track-record of success and have tutoring over 10 students in the past 2 years",
				"I love teaching and I'm very passionate about the courses I teach."};
		
		for(int i=0; i<names.length/2; i++) {
			emails[i] = emails[i].replaceAll("\\-",""); // remove all dashes
			emails[i] = emails[i].replaceAll("\\.",""); // remove all the dots
			emails[i] = emails[i].replaceAll("\\s",".");// replace all spaces with .'s now
			emails[i] = emails[i] + "@dartmouth.edu";

			numCourses = randInteger(randNum, 1, 7); // generate upto 10 courses per person
			courses.clear(); // delete the old courses
			int c=0; // count to count the number of cs courses we add for each person
			for (int j=0; j < numCourses; j++) {
				deptInd = randInteger(randNum, 0, 50); // index of department in the Globals dept list
				courseNum = randInteger(randNum, 1, 50); // upto 10 courses per person
				
				if(deptInd!=15) // don't add cs courses
					courses.add(Globals.DEPARTMENTS[deptInd] + " " + String.valueOf(courseNum));
				
				// add cs courses for everyone manually
				if(c <= 5) {
					courses.add(Globals.DEPARTMENTS[15] + " " + String.valueOf(courseNum)); // add in at least one cosc course per person
					c=c+1;
				}
			}
			
			int aboutIndex = randInteger(randNum, 0, about.length-1); // pick an about we want to include
			insertTask = new InsertMemberDb(getApplicationContext(), emails[i], names[i], courses, about[aboutIndex], 0); //add the tutor to the database
			insertTask.asyncResponse = this; // set a delegate to get the query result from the asynctask
			try {
				insertTask.execute().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			
			//add the user to the users table 
			mAuthTask = new UserRegisterTask(emails[i], names[i], "9101019");
            try {
				mAuthTask.execute().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		// GENERATE SOME TUTEEs: Half the class are tutees
		courses = new ArrayList<String> ();
		String[] about_me = {"I am a Junior looking for a tutor to help me with weekly assignments", "I am a Freshman and I need consistent help throughout the term", 
				"I am a Senior mostly looking to meet up with someone for an hour a week to make sure I stay on track with the course material.", 
				"I am looking for a friendly tutor to help me prepare for exams", "I usually don't get tutors but I'm short on time this term and need someone to help me on weekly assignments",
				"Hi guys, I'd like an experienced tutor who can help me out for about 2 hours a week."};
		
		for(int i=names.length/2; i<names.length; i++) {
			emails[i] = emails[i].replaceAll("\\-",""); // remove all dashes
			emails[i] = emails[i].replaceAll("\\.",""); // remove all the dots
			emails[i] = emails[i].replaceAll("\\s",".");// replace all spaces with .'s now
			emails[i] = emails[i] + "@dartmouth.edu";

			numCourses = randInteger(randNum, 1, 2); // generate upto 10 courses per person
			courses.clear(); // delete the old courses
			for (int j=0; j< numCourses; j++) {
				deptInd = randInteger(randNum, 0, 50); // index of department in the Globals dept list
				courseNum = randInteger(randNum, 1, 50); // upto 10 courses per person
				if(deptInd!=15) // don't add cs courses
					courses.add(Globals.DEPARTMENTS[deptInd] + " " + String.valueOf(courseNum));
				// add cs courses for everyone manually
				courses.add(Globals.DEPARTMENTS[15] + " " + String.valueOf(courseNum)); // add in at least one cosc course per person
			}

			int about_index = randInteger(randNum, 0, about.length-1); // pick an about we want to include
			insertTask = new InsertMemberDb(getApplicationContext(), emails[i], names[i], courses, about_me[about_index], 1); //add the user to them members table
			insertTask.asyncResponse = this; // set a delegate to get the query result from the asynctask
			try {
				insertTask.execute().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			

			//add the user to the users table 
			mAuthTask = new UserRegisterTask(emails[i], names[i], "9101019");
            try {
				mAuthTask.execute().get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	public static int randInteger(Random randNum, int minVal, int maxVal) {
		// +1 makes sure you can generate values upto/including the max value
		int randomNum = randNum.nextInt((maxVal - minVal) + 1) + minVal;
		return randomNum;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public void processFinish(JSONArray result) throws JSONException {
	}
	
	@Override
	public void processFinish(Integer result) {
		if(result == 1)
			Log.d("arvi", "Added fake member to server members table successfully");
		else
			Log.d("arvi", "Could not add member to server members table successfully");
		insertTask.cancel(true);		
	}
	
	/**
	 * Represents an asynchronous register task used to sign-up the user.
	 */
	public class UserRegisterTask extends AsyncTask<String, String, Boolean> {
		
		private String mName;
	    private String mEmail;
	    private String mPassword;
	    
		public UserRegisterTask(String email, String name, String password) {
			this.mName = name;
			this.mEmail = email;
			this.mPassword = password;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// Attempt register...
			JSONObject jsonObj = AuthUtilities.registerUser(mName, mEmail, mPassword);

			// Check register response...
			try {
				if(jsonObj.getString(AuthUtilities.KEY_SUCCESS) != null) {
					mRegisterErrorMsg = "";
					String result = jsonObj.getString(AuthUtilities.KEY_SUCCESS);
					if(Integer.parseInt(result) == AuthUtilities.SUCCESS) {
						return true;
					} else {
						if(jsonObj.getString(AuthUtilities.KEY_ERROR) != null) {
							mRegisterErrorMsg = jsonObj.getString(AuthUtilities.KEY_ERROR_MSG);
						} else {
							mRegisterErrorMsg = getString(R.string.error_failed_registration);
						}

						return false;
					}
				}
			} catch(JSONException e) {
				e.printStackTrace();
			}

			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;

			if(success) {
				Log.i("Registration", "onPostExecute(): register SUCCESS");

			} else {
				Log.i("REGISTRATION", "onPostExecute(): register FAILED");
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}

	}

}
