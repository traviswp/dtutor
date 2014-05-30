package edu.dartmouth.cs.dtutor;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.dartmouth.cs.dtutor.data.AsyncResponse;
import edu.dartmouth.cs.dtutor.data.InsertMemberDb;
import edu.dartmouth.cs.dtutor.data.MemberDbHelper;
import edu.dartmouth.cs.dtutor.data.Members;
import edu.dartmouth.cs.dtutor.utils.SessionManager;

public class ProfileFragment extends Fragment implements AsyncResponse {

    public static int mType;
    public static ImageButton addButton;
    public static Button saveButton;
    public static Button cancelButton;

    public static LinearLayout course_layout;

    public static EditText email;
    public static EditText name;
    public static EditText courses;
    public static EditText about;
    public static TextView courses_title;
    public static TextView about_title;

    InsertMemberDb insertTask; // Async Task for querying the SQL database
    MemberDbHelper dbHelper;
    SessionManager session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profilefragment, container, false);

        session = SessionManager.getInstance(getActivity());
        mType = session.getSessionVarInteger(Globals.MEMBERSHIP_TYPE);

        BtnClickHandler handler = new BtnClickHandler();
        course_layout = (LinearLayout) view.findViewById(R.id.add_new_profile);

        addButton = (ImageButton) view.findViewById(R.id.add_button_profile);
        addButton.setOnClickListener(handler);

        name = (EditText) view.findViewById(R.id.name_val);
        email = (EditText) view.findViewById(R.id.email_val);
        about = (EditText) view.findViewById(R.id.about_val_p);
        courses_title = (TextView) view.findViewById(R.id.courses_title);
        about_title = (TextView) view.findViewById(R.id.about_title);

        // Don't allow users to change their email - it cannot be changed. Still it was implemented as a textview
        // so that the 'look' of all the fields is the same
        email.setKeyListener(null);

        // save button
        saveButton = (Button) view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(handler);

        // save button
        saveButton = (Button) view.findViewById(R.id.cancel_button);
        saveButton.setOnClickListener(handler);

        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

        // load in the user's profile into the UI
        loadUserProfile();
    }

    public void cancel() {
        loadUserProfile(); // re-load the last-saved profile
        Toast.makeText(getActivity(), "Your changes have been discarded", Toast.LENGTH_SHORT).show();
    }

    public void saveData() {
        // get the courses from the UI
        int count = course_layout.getChildCount();
        ArrayList<String> mCourses = new ArrayList<String>();
        for(int i = 0; i < count; i++) {
            View v = course_layout.getChildAt(i);
            EditText course_entry = (EditText) v.findViewById(R.id.new_course);
            mCourses.add(course_entry.getText().toString());
        }

        // get the profile info
        String mName = name.getText().toString();
        String mEmail = email.getText().toString(); // email is currently not changeable since this is the user id
                                                    // you used to create the account
        String mAbout = about.getText().toString();

        // Add the changes to the locally-stored profile table (in the db)
        Members member = new Members();
        member = dbHelper.getProfile(); // get current profile and get changes for the fields that are currently
                                        // displayed
        member.setName(mName);

        if(mType == 0) {
            member.setTutorAbout(mAbout);
            member.setTutorCourses(mCourses);
        } else if(mType == 1) {
            member.setTuteeAbout(mAbout);
            member.setTuteeCourses(mCourses);
        }

        // add the updated profile to the local database
        dbHelper.addProfile(member);

        // update the record on the server
        insertTask = new InsertMemberDb(getActivity(), mEmail, mName, mCourses, mAbout, mType);
        insertTask.asyncResponse = this;	// set a delegate to get the query result from the asynctask
        insertTask.execute();

    }

    public void loadUserProfile() {

        // get the profile from the local database
        dbHelper = new MemberDbHelper(getActivity());
        Members member = new Members();
        member = dbHelper.getProfile();

        Log.d("profileFrag", "GETTING THE PROFILE FROM THE LOCAL DATABASE");

        // populate the UI fields using the values from the database
        email.setText(member.getEmail());
        name.setText(member.getName());

        if(mType == 0) { // display courses that the user tutors
            String tutorCourses = member.getTutorCourses().toString();

            // add courses
            courses_title.setText("Courses You Are Tutoring (Click '+' to Add)");
            addCoursesToUI(tutorCourses);

            // add about
            about_title.setText("Your Tutor Info");
            about.setText(" " + member.getTutorAbout());
        } else if(mType == 1) {
            String tuteeCourses = member.getTuteeCourses().toString();

            // add courses
            courses_title.setText("Courses You Are Tutored In (Click '+' to Add)");
            addCoursesToUI(tuteeCourses);

            // add about
            about_title.setText("Your Tutee Info");
            about.setText(" " + member.getTuteeAbout());
        }

    }

    public void addCoursesToUI(String courses) {

        int numCoursesCurrent = course_layout.getChildCount(); // Get the number of courses we are 'currently'
                                                               // displaying

        // load the courses into textviews
        String regex = "\\[|\\]";
        courses = courses.replaceAll(regex, ""); // remove all brackets from the string
        String[] courseL = courses.split(","); // separate the courses into a list
        int count = courseL.length; // Get the number of courses we will be restoring to the view
        for(int i = 0; i < count; i++) {
            if(i >= numCoursesCurrent) // we need to add more 'course views' in the case where the user deleted
                                       // some courses and is now restoring them
                course_layout.addView(addCourseView());

            View v = course_layout.getChildAt(i);
            EditText course_entry = (EditText) v.findViewById(R.id.new_course);
            course_entry.setText(courseL[i].trim()); // add the course into the view
        }
    }

    /**
     * BtnClickHandler inStream a single listener that can be shared for any button -- onClickBtn() inStream then
     * called with the view of the button that registers the click and will handle that click appropriately based
     * on the view.
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

        switch (view.getId()) {
        case R.id.add_button_profile: {
            course_layout.addView(addCourseView());
            break;
        }
        case R.id.save_button: {
            saveData();
            break;
        }
        case R.id.cancel_button: {
            cancel();
            break;
        }
        }
    }

    public View addCourseView() {
        LayoutInflater layoutInflater =
            (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View newView = layoutInflater.inflate(R.layout.row_entry, null);
        EditText newCourse = (EditText) newView.findViewById(R.id.new_course);
        newCourse.setHint("Enter new Course");
        ImageButton deleteButton = (ImageButton) newView.findViewById(R.id.delete_button);
//        BtnClickHandler inner_handler = new BtnClickHandler();

        // the reason the onclicklistener for the delete button inStream defined seperately here
        // inStream because the one above uses the wrong view. This one will use the view that we have just
        // created.
        deleteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((LinearLayout) newView.getParent()).removeView(newView);
            }
        });
        return newView;
    }

    @Override
    public void processFinish(Integer result) {
        if(result == null) { // Query produces no results --> just clear the list view
            Log.d("arvi", "No result from updating");
            return;
        }

        Toast.makeText(getActivity(), "Your profile has been updated", Toast.LENGTH_SHORT).show();
        insertTask.cancel(true);
    }

    @Override
    public void processFinish(JSONArray result) throws JSONException {
        if(result == null || result.length() == 0) { // Member not found
            Log.d("arvi", "MEMBER DOES NOT EXIST IN THE DATABASE");
            return;
        }
    }

}