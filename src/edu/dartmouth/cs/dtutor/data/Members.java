package edu.dartmouth.cs.dtutor.data;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


//data structure to hold the work out data.  
public class Members {

	private String email;
	private String name;
	private int memberType;
	public ArrayList<String> tutorCourses = new ArrayList<String>();
	public ArrayList<String> tuteeCourses = new ArrayList<String>();
	public String tutorAbout;
	public String tuteeAbout;
	public String notifications;
    public String reservations;
	public int req_type;
	public int resp;
	
	public Members() {
		email = null;
		name = null;
		memberType = -1;
		tutorAbout = "";
		tuteeAbout="";
		notifications="";
        reservations="";
		req_type=-1; // no request has been made
		resp=-1; // no response for request
		
	}

	public String getEmail() { return email; }
	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() { return name; }
	public void setName(String name) {
		this.name = name;
	}
	
	public int getMemberType() { return memberType; }
	public void setMemberType(int memberType) {
		this.memberType = memberType;
	}
	
	public ArrayList<String> getTutorCourses() { return tutorCourses; }
	public void setTutorCourses(ArrayList<String> tutorCourses) {
		this.tutorCourses = tutorCourses;
	}
	
	public ArrayList<String> getTuteeCourses() { return tuteeCourses; }
	public void setTuteeCourses(ArrayList<String> tuteeCourses) {
		this.tuteeCourses = tuteeCourses;
	}

	public String getTutorAbout() { return tutorAbout; }
	public void setTutorAbout(String tutorAbout) {
		this.tutorAbout = tutorAbout;
	}

	public String getTuteeAbout() { return tuteeAbout; }
	public void setTuteeAbout(String tuteeAbout) {
		this.tuteeAbout = tuteeAbout;
	}
	
	public String getNotifications() { return notifications; }
	public void setNotifications(String notifications) {
		this.notifications = notifications;
	}
	
    public String getReservations() { return reservations; }
    public void setReservations(String reservations) {
        this.reservations = reservations;
    }
	
	public int getRequestType() { return req_type; }
	public void setRequestType(int req_type) {
		this.req_type = req_type;
	}
	
	public int getResponse() { return resp; }
	public void setResponse(int resp) {
		this.resp = resp;
	}
	
	// convert a member to a jsonStr object
	public JSONObject toJSONObject() throws JSONException {

		try {
			JSONObject json_obj = new JSONObject();
			json_obj.put("email", email);
			json_obj.put("name", name);
			json_obj.put("member_type", memberType);

			if(memberType==0) { //Tutor
				json_obj.put("courses", tutorCourses.toString());
				json_obj.put("about", tutorAbout);
			}
			else if(memberType==1) { //Tutee
				json_obj.put("courses", tuteeCourses.toString());
				json_obj.put("about", tuteeAbout);
			}
			
			return json_obj;
		}
		catch (JSONException e){
			return null;
		}
	}
	
	//convert notifications to a json object
	public JSONObject notificationToJSONObject() throws JSONException {
		try {
			JSONObject json_obj = new JSONObject();
			json_obj.put("email", email);
			json_obj.put("request_type", req_type); //whether they want a tutor or a tutee
			json_obj.put("response", resp);
			return json_obj;
		}
		catch (JSONException e){
			return null;
		}
	}
	
	public void setTutorCoursesFromString(String str) {
		// parse the string of courses into an array list of strings
		ArrayList<String> tutorCourseList = new ArrayList<String>(Arrays.asList(str.split(",")));
		setTutorCourses(tutorCourseList);
	}
	
	public void setTuteeCoursesFromString(String str) {
		// parse the string of courses into an array list of strings
		ArrayList<String> tuteeCourseList = new ArrayList<String>(Arrays.asList(str.split(",")));
		setTuteeCourses(tuteeCourseList);
	}
	
	// converts a member to a jsonStr object
		public void fromJSONObjectForMtype(JSONObject obj, int mType) throws JSONException { // mType here indicates whether we want the tutor profile or the tutee profile

			try {
				email = (String) obj.getString("email");
				name = obj.getString("name");
				memberType = mType;
				
				Log.d("arvi2", "MEMBERSHIP TYPE:" + String.valueOf(mType));
				if(mType==0) { //Tutor
					String coursesStr = obj.getString("tutor_courses");
					tutorCourses = new ArrayList<String>(Arrays.asList(coursesStr.split(",")));	
					tutorAbout = obj.getString("tutor_about");
				}
				if(mType==1) { //Tutee
					String coursesStr = obj.getString("tutee_courses");
					tuteeCourses = new ArrayList<String>(Arrays.asList(coursesStr.split(",")));
					tuteeAbout = obj.getString("tutee_about");
				}
			}
			catch (JSONException e){
			}
		}
	
	// converts a member to a jsonStr object
	public void fromJSONObject(JSONObject obj) throws JSONException {

		try {
			email = (String) obj.getString("email");
			name = obj.getString("name");
			memberType = obj.getInt("member_type");
			notifications = obj.getString("notifications");
			reservations = obj.getString("reservations");
			if(memberType==0 || memberType == 2) { //Tutor
				String coursesStr = obj.getString("tutor_courses");
				tutorCourses = new ArrayList<String>(Arrays.asList(coursesStr.split(",")));	
				tutorAbout = obj.getString("tutor_about");
			}
			if(memberType==1 || memberType == 2) { //Tutee
				String coursesStr = obj.getString("tutee_courses");
				tuteeCourses = new ArrayList<String>(Arrays.asList(coursesStr.split(",")));
				tuteeAbout = obj.getString("tutee_about");
			}
		}
		catch (JSONException e){
}
    }
}
