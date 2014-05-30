package edu.dartmouth.cs.dtutor;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import edu.dartmouth.cs.dtutor.data.AsyncResponse;
import edu.dartmouth.cs.dtutor.data.MemberDbHelper;
import edu.dartmouth.cs.dtutor.data.Members;
import edu.dartmouth.cs.dtutor.data.QueryDbForCourse;
import edu.dartmouth.cs.dtutor.utils.SessionManager;

public class SearchFragment extends Fragment implements AsyncResponse {

    private static final String TAG = Globals.TAG + ".SearchFragment";
    
    public AutoCompleteTextView dept;
    public AutoCompleteTextView courses;
    public static String courseDept = null; // Department of the course we will search for
    public static int courseNum = -1; // Course number of the course we will search for
    SessionManager session;

    public int mType; // MEMBERSHIP TYPE OF USER NAVIGATING THE APP - THIS NEEDS TO COME FROM THE SHARED PREFS EVENTUALLY
    public static final String DEPT_SPINNER_VALUE = "dept";
    public static final String COURSE_NUM_SPINNER_VALUE = "course_num";

    QueryDbForCourse queryTask; // Async Task for querying the SQL database
    MemberDbHelper dbHelper;
    static ArrayAdapter<Members> listViewAdapter;
    ListView listview;
    int searchMType;
    View v;
    ArrayList<Members> allEntries;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // get the member type
        session = SessionManager.getInstance(getActivity());
        mType = session.getSessionVarInteger(Globals.MEMBERSHIP_TYPE);

        // Toast.makeText(getActivity(), "Browsing as: " + Globals.USER_TYPES[mType], Toast.LENGTH_SHORT).show();

        // FOR SOME REASON, THE QUERY STARTS FAILING ONCE YOU CHANGE THE USER TYPE 
        // SO WE JUST LEAVE IT AS TUTOR FOR NOW
        // mType = 1;

        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.searchfragment, container, false);

        // SEARCH SPINNERS FOR DEPT AND COURSE NUMBER
        dept = (AutoCompleteTextView) v.findViewById(R.id.departments);
        courses = (AutoCompleteTextView) v.findViewById(R.id.courses);

        ArrayAdapter<String> adapter =
            new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, Globals.DEPARTMENTS);
        dept.setAdapter(adapter);
        dept.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                dept.showDropDown();
            }
        });
        // get the selected course dept.
        dept.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
                courseDept = ((TextView) view).getText().toString();
                queryDbForCourse(courseDept, courseNum); // query the database
                Log.i("Course dept: ", courseDept);
            }
        });
        // if the user deletes the dept value, relaunch a query using null dept and the selected course num
        dept.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if((s.toString()).equals("")) {
                    Log.d("test", "dept field was deleted");
                    courseDept = null;
                    queryDbForCourse(courseDept, courseNum); // query the database
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int start, int count, int after) {}
        });

        // generate the supported course numbers
        final String[] nums = new String[51];
        nums[0] = "All";
        for(int i = 1; i < 51; i++) {
            nums[i] = String.valueOf(i);
        }

        ArrayAdapter<String> courseAdapter =
            new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, nums);
        courses.setAdapter(courseAdapter);
        courses.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                courses.showDropDown();
            }
        });

        // get the selected course number
        courses.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
                String courseNumStr = ((TextView) view).getText().toString();
                try {
                    courseNum = Integer.parseInt(courseNumStr);
                } catch(NumberFormatException ne) {
                    Log.e(TAG, ne.getMessage());
                    courseNum = -1;
                }
                queryDbForCourse(courseDept, courseNum); // query the database
                Log.i("Course Number: ", String.valueOf(courseNum));
            }
        });

        // if the user deletes the course number, relaunch a query using null dept and the selected course num
        courses.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if((s.toString()).equals("")) {
                    courseNum = -1;
                    Log.d("deleted coursenum",
                          "deleted coursenum; dept= " + courseDept + " courseNum= " + String.valueOf(courseNum));
                    queryDbForCourse(courseDept, courseNum); // query the database
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int start, int count, int after) {}
        });

        searchMType = 0; // Search results of this member type
        if(mType == 0)
            searchMType = 1;

        // populate the listview with search results
        dbHelper = new MemberDbHelper(getActivity());
        allEntries = dbHelper.fetchAllEntries(searchMType);
        listViewAdapter = new CustomAdapter(getActivity(), R.layout.single_entry, allEntries); // array adapter for
                                                                                               // displaying search
                                                                                               // results
        listview = (ListView) v.findViewById(R.id.listview); // get the listview of the search fragment

        listview.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> l, View v, int position, long id) {

                Members member = allEntries.get(position);
                Intent intent = new Intent(getActivity(), ActivitySingleEntryDisplay.class);
                intent.putExtra(MemberDbHelper.NAME, member.getName());
                intent.putExtra(MemberDbHelper.EMAIL, member.getEmail());
                intent.putExtra(MemberDbHelper.MEMBER_TYPE, searchMType);

                if(searchMType == 0) { // we are looking for tutors
                    Log.d("courses", member.getTutorCourses().toString());
                    intent.putExtra(MemberDbHelper.COURSES, member.getTutorCourses().toString());
                    intent.putExtra(MemberDbHelper.ABOUT, member.getTutorAbout());
                } else {
                    Log.d("courses", member.getTuteeCourses().toString());
                    intent.putExtra(MemberDbHelper.COURSES, member.getTuteeCourses().toString());
                    intent.putExtra(MemberDbHelper.ABOUT, member.getTuteeAbout());
                }

                startActivity(intent);
            }
        });

        listview.setAdapter(listViewAdapter);
        listViewAdapter.notifyDataSetChanged();

        if((courseDept != null)) {
            if(!courseDept.equals("")) {
                dept.setAdapter(null);
                dept.setText(courseDept);
                dept.setAdapter(adapter);
            }
        }

        if((courseNum != -1)) {
            courses.setAdapter(null);
            courses.setText(String.valueOf(courseNum));
            courses.setAdapter(courseAdapter);
        }

        if(savedInstanceState != null) {
            courseDept = savedInstanceState.getString(DEPT_SPINNER_VALUE);
            String courseNumStr = savedInstanceState.getString(COURSE_NUM_SPINNER_VALUE);
            if(courseNumStr.equals(nums[0]) || courseNumStr.equals(""))
                courseNum = -1;
            else
                courseNum = Integer.parseInt(courseNumStr);
            Log.d("arvind", courseDept);
            // queryDbForCourse(courseDept, courseNum); //query the database
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DEPT_SPINNER_VALUE, dept.getText().toString());
        outState.putString(COURSE_NUM_SPINNER_VALUE, courses.getText().toString());
    }

    @Override
    public void processFinish(JSONArray result) throws JSONException { // After we complete the search; we come in
                                                                       // here - ADD RESULTS TO THE DATABASE

        if(result == null) { // Query produces no results --> just clear the list view
            listViewAdapter.clear();
            listViewAdapter.notifyDataSetChanged();
            return;
        }

//        Log.d("arviRESULT", "Result: " + result.toString());

        // Add the query results to the local database
        for(int i = 0; i < result.length(); i++) { // add each record from the query result to the database
            Members member = new Members();
            member.fromJSONObjectForMtype(result.getJSONObject(i), searchMType);

            if(member.getMemberType() == 2)
                Log.d("cursorToMemberEntry", "member: " + member.getEmail()
                                             + ", tuteeCourses: "
                                             + member.getTuteeCourses().toString()
                                             + ", tutorCourses: "
                                             + member.getTutorCourses().toString());

            Log.d("arviMEMBER", "member " + member.getName() + ", email:" + member.getEmail());
            dbHelper.insertEntry(member);

        }

        // get all the entries from the database
        final ArrayList<Members> Entries = dbHelper.fetchAllEntries(searchMType);

        Log.d("arviIamMember", "num of records found: " + Entries.size());
        listViewAdapter.clear();
        listViewAdapter.addAll(Entries);
        listViewAdapter.notifyDataSetChanged();
        queryTask.cancel(true);

    }

    public void queryDbForCourse(String courseDept, int courseNum) { // search the database for the course

        TextView t = (TextView) v.findViewById(R.id.search_type_textview);
        t.setVisibility(View.INVISIBLE);
        View s = (View) v.findViewById(R.id.blue_bar);
        s.setVisibility(View.INVISIBLE);
        // delete all the entries from the database before adding any new results
        dbHelper.deleteAll();

        queryTask = new QueryDbForCourse(getActivity(), mType, courseDept, courseNum); // query the database
        queryTask.asyncResponse = this;	// set a delegate to get the query result from the asynctask
        queryTask.execute();

        listViewAdapter.setNotifyOnChange(true);
    }

    public class CustomAdapter extends ArrayAdapter<Members> {

        public CustomAdapter(Context context, int txtViewResId, ArrayList<Members> allEntries) {
            super(context, txtViewResId, allEntries);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

//            Log.d("arvi", "WE ARE UPDATING THE LISTVIEW");
            View bar = (View) v.findViewById(R.id.blue_bar);
            bar.setVisibility(View.VISIBLE);
            if(mType == 0) {
                TextView search_type = (TextView) v.findViewById(R.id.search_type_textview);
                search_type.setVisibility(View.VISIBLE);

                search_type.setText("Available Tutees:");

            } else if(mType == 1) {
                TextView search_type = (TextView) v.findViewById(R.id.search_type_textview);
                search_type.setVisibility(View.VISIBLE);

                search_type.setText("Available Tutors:");

            }

            Members member;

            // Inflate the layout, in each row
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View row = inflater.inflate(R.layout.single_entry, parent, false);

            // get views for single entry
            TextView nameV = (TextView) row.findViewById(R.id.memberName);
            TextView aboutV = (TextView) row.findViewById(R.id.memberAbout);

            member = getItem(position);

            // Add member info to the list views
            nameV.setText(member.getName());

            if(mType == 0) // user searching is a tutor, so pull up tutee info
                aboutV.setText(member.getTuteeAbout());
            else if(mType == 1) // user searching is a tutee, so pull up tutor info
                aboutV.setText(member.getTutorAbout());

            // Return the completed view to render on screen
            return row;
        }
    }

    @Override
    public void processFinish(Integer result) {}

    @Override
    public void onResume() {
        super.onResume();

    }
}