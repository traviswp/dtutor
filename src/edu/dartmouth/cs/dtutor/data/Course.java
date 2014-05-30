package edu.dartmouth.cs.dtutor.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class for storing data about courses that the student wants to tutor for...
 */
public class Course {

    public String courseSubject;
    public int courseNumber;
    public String courseProfessor;

    public Course() {}

    public String toString() {
        return courseSubject + " " + courseNumber + " [" + courseProfessor + "]";
    }

    public static Course fromJSONObject(JSONObject obj) throws JSONException {
        Course c = new Course();
        c.courseNumber = obj.getInt("courseNumber");
        c.courseProfessor = obj.getString("courseProfessor");
        c.courseSubject = obj.getString("courseSubject");

        return c;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jnew = new JSONObject();
        jnew.put("courseNumber", courseNumber);
        jnew.put("courseProfessor", courseProfessor);
        jnew.put("courseSubject", courseSubject);
        return jnew;
    }

}