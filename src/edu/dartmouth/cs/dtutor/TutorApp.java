package edu.dartmouth.cs.dtutor;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import edu.dartmouth.cs.dtutor.data.AsyncResponse;
import edu.dartmouth.cs.dtutor.data.InsertMemberDb;
import edu.dartmouth.cs.dtutor.data.Members;

public class TutorApp extends Activity implements AsyncResponse {

    // used to keep track of number of courses added in this application
    int courseCount;

    ImageButton addButton;
    Button saveButton;
    Button cancelButton;
    
    // link to the linear layout that contains the add button
    LinearLayout course_layout;
    
    // link to the overall layout
    LinearLayout layout;
    InsertMemberDb insertTask; // Async Task for querying the SQL database
    int memberType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_app);
        layout = (LinearLayout) findViewById(R.id.tutor_application_view);

        Bundle extras = getIntent().getExtras();

        // get the row id of the record being displayed
        memberType = extras.getInt("member_type");

        if(savedInstanceState == null) {
            // set the number of courses to zero initially
            courseCount = 0;
            BtnClickHandler handler = new BtnClickHandler();
            course_layout = (LinearLayout) findViewById(R.id.add_new);
            addButton = (ImageButton) findViewById(R.id.add_button);
            addButton.setOnClickListener(handler);
            saveButton = (Button) findViewById(R.id.save_button);
            saveButton.setOnClickListener(handler);
            cancelButton = (Button) findViewById(R.id.cancel_button);
            cancelButton.setOnClickListener(handler);

        }
    }

    @Override
    public void processFinish(Integer result) {
        if(result == null) { // Query produces no results --> just clear the list view
            Log.d("arvi", "Do you come here");
            return;
        }

        Toast.makeText(getApplicationContext(), "Member Added to db", Toast.LENGTH_SHORT).show();
        insertTask.cancel(true);
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
        case R.id.add_button: {
            course_layout.addView(addCourseView());
            break;
        }
        case R.id.save_button: {
            save_data();
            break;
        }
        case R.id.cancel_button: {
            finish();
            break;
        }
        }
    }

    public void save_data() {
        Members member = new Members();

        ArrayList<String> tutor_courses = new ArrayList<String>();
        EditText fname = (EditText) findViewById(R.id.name);
        member.setName(fname.getText().toString());

        EditText student_id = (EditText) findViewById(R.id.email);
        member.setEmail(student_id.getText().toString());

        int count = course_layout.getChildCount();
        for(int i = 0; i < count; i++) {
            View v = course_layout.getChildAt(i);
            EditText course_entry = (EditText) v.findViewById(R.id.new_course);
            tutor_courses.add(course_entry.getText().toString());
        }

        member.setTutorCourses(tutor_courses);
        Log.d("arvi", "Tutor COURSE: " + tutor_courses.toString());

        EditText about = (EditText) findViewById(R.id.about);
        member.setTutorAbout(about.getText().toString());

        String email = member.getEmail();
        String name = member.getName();

        ArrayList<String> courses = member.getTutorCourses();
        String aboutT = member.getTutorAbout();
        insertTask = new InsertMemberDb(this, email, name, courses, aboutT, memberType);
        insertTask.asyncResponse = this;	// set a delegate to get the query result from the asynctask
        insertTask.execute();
        finish();

    }

    public View addCourseView() {
        LayoutInflater layoutInflater =
            (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it inStream present.
        getMenuInflater().inflate(R.menu.tutor_app, menu);
        return true;
    }

    @Override
    public void processFinish(JSONArray result) throws JSONException {
    }

}
