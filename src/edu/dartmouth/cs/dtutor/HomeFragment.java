package edu.dartmouth.cs.dtutor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import edu.dartmouth.cs.dtutor.data.AsyncResponse;
import edu.dartmouth.cs.dtutor.data.InsertMemberDb;
import edu.dartmouth.cs.dtutor.data.MemberDbHelper;
import edu.dartmouth.cs.dtutor.data.Members;
import edu.dartmouth.cs.dtutor.data.QueryDbForMember;
import edu.dartmouth.cs.dtutor.utils.ServerUtilities;
import edu.dartmouth.cs.dtutor.utils.SessionManager;

public class HomeFragment extends Fragment implements AsyncResponse {

	private static final String TAG = Globals.TAG + ".HomeFragment";

	private Context mContext;
	private static Switch memberSwitch;
	private static TextView profileMsg; 
	private static LinearLayout notifications; 
	private static int mType = 0;
	private static TextView nText;

	private static View view;


	MemberDbHelper dbHelper;
	InsertMemberDb insertTask; //Async Task for adding a new member to the member table
	QueryDbForMember getProfileTask; //Async Task for querying the SQL database
	SessionManager session;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		view = inflater.inflate(R.layout.homefragment, container, false);

		// Set context of the fragment
		mContext = getActivity();
		dbHelper = new MemberDbHelper(getActivity());

		session = SessionManager.getInstance(mContext);
		if(session.getSessionVarInteger(Globals.MEMBERSHIP_TYPE) >= 0) {
			mType = session.getSessionVarInteger(Globals.MEMBERSHIP_TYPE);
		} else {
			session.addSessionVarInteger(Globals.MEMBERSHIP_TYPE, mType);
		}

		/*
		 * Additional UI setup
		 */

		BtnClickHandler handler = new BtnClickHandler();

		profileMsg = (TextView) view.findViewById(R.id.completeProfileMsg);
		nText = (TextView) view.findViewById(R.id.notifications_present);
		notifications=(LinearLayout)view.findViewById(R.id.notifications);

		Button bookings = (Button) view.findViewById(R.id.book);
		bookings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(mContext, MapDisplayActivity.class);
				startActivity(intent);
			}

		});

		memberSwitch = (Switch) view.findViewById(R.id.SwitchID);
		memberSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if(isChecked){
					mType = 1;
				}
				else{
					mType = 0;
				}
				session.addSessionVarInteger(Globals.MEMBERSHIP_TYPE, mType);
			}
		});

		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MemberDbHelper dbHelper = new MemberDbHelper(getActivity());

		// if the user exists in the database and you have internet; retrieve the profile from the server
		if(dbHelper.doesProfileExist() && ServerUtilities.isNetworkAvailable(getActivity())) { //update the profile
			Members member = dbHelper.getProfile();
			String email = member.getEmail();

			// get profile from the server and load it into the Profile table
			getProfileTask = new QueryDbForMember(getActivity(), email); //get the complete user profile (tutor and tutee...)
			getProfileTask.asyncResponse = this;	// set a delegate to get the query result from the asynctask
			getProfileTask.execute();

		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if(mType==0)  // restore the switch when you rotate
			memberSwitch.setChecked(false);
		else
			memberSwitch.setChecked(true);

		// Ask user to complete the profile if they haven't done so already
		Members member = dbHelper.getProfile();
		profileMsg = (TextView) view.findViewById(R.id.completeProfileMsg);
		if(member != null && member.getMemberType() == -1) { //user has not signed up as a tutor or a tutee
			profileMsg.setText(getString(R.string.complete_profile_message));
		} else if(member != null) 
			profileMsg.setText("Welcome " + member.getName() + "!");

		JSONObject notificationsObj = new JSONObject();

		if(member != null && member.getMemberType() !=-1) {
			Log.d("no", member.getNotifications());
			if(member.getNotifications().equals("null")) {
				nText.setText("You do not have any notifications");
			}
			else { // the member has notifications stored; show them on the screen
				try {
					notificationsObj = new JSONObject(member.getNotifications());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				nText.setVisibility(View.INVISIBLE);
				//				notifications.addView(addNotifications(notificationsObj));			
			}
		}
	}

	//USED TO DYNAMICALLY ADD NOTIFICATIONS TO THE UI
	public View addNotifications(JSONObject notificationsObj)
	{
		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View newView = layoutInflater.inflate(R.layout.notification_entry, null);

		//SET THE COUNT HERE
		int count=notificationsObj.length();

		for(int i=0;i<count;i++)
		{

			TextView newCourse = (TextView) newView.findViewById(R.id.new_notifications);
			//***********************************************************
			//set the value of the notifications in this gap
			//			notificationsObj.g
			//***********************************************************

			ImageButton acceptButton = (ImageButton) newView.findViewById(R.id.accept_button);
			//the reason the onclicklistener for the delete button inStream defined seperately here
			//inStream because the one above uses the wrong view. This one will use the view that we have just created.
			acceptButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					((LinearLayout)newView.getParent()).removeView(newView);
					Toast.makeText(mContext, "REQUEST ACCEPTED!!", Toast.LENGTH_SHORT);
				}});
			ImageButton declineButton = (ImageButton) newView.findViewById(R.id.decline_button);
			declineButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					((LinearLayout)newView.getParent()).removeView(newView);
				}});
		}

		return newView;
	}

	/* ****************************************************************** *
	 *                     UI Listener Classes/Handlers
	 * ****************************************************************** */

	/**
	 * MyRunsBtnClickHandler inStream a single listener that can be shared for any button -- onClickBtn() inStream then called
	 * with the view of the button that registers the click and will handle that click appropriately based on the
	 * view.
	 */
	class BtnClickHandler implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			onClickBtn(v);
		}
	}

	/**
	 * Use the input view to determine how to properly handle the user's request.
	 * 
	 * @param view of the button that was clicked
	 */
	public void onClickBtn(View view) {
	}

	@Override
	public void processFinish(Integer result) { // After adding users to members table finishes

		if(result == 1)
			Log.d("arvi", "added member successfully");
		else
			Log.d("arvi", "added member successfully");
		insertTask.cancel(true);
	}

	@Override
	public void processFinish(JSONArray result) throws JSONException { //after you get the user profile from the server
		if(result == null || result.length() == 0) { // Member not found
			Log.d("arvi", "MEMBER DOES NOT EXIST IN THE DATABASE");
			return;
		}

//		Log.d("arvi", "result: " + result.toString());
		Members member = new Members();
		member.setMemberType(mType);

		// add the query result to the member class
		member.fromJSONObject(result.getJSONObject(0));
		Log.d("arvi", "member " + member.getName());

		// add the profile to the database
		dbHelper.addProfile(member);
//		profileMsg.setText("Welcome " + member.getName() + "! Please");

		getProfileTask.cancel(true);		
	}
}