package edu.dartmouth.cs.dtutor;

import java.util.ArrayList;

public class ExpandListChild {

    private String Name = "";
    private ArrayList<String> Courses;

    public String getName() {
        return Name;
    }

    public ArrayList<String> getCourses() {
        return Courses;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public void setCourses(ArrayList<String> c) {
        this.Courses = c;
    }
}