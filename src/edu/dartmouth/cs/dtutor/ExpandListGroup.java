package edu.dartmouth.cs.dtutor;

import java.util.ArrayList;

public class ExpandListGroup {

    private String Name = "";
    private String About = "";
    private ArrayList<ExpandListChild> Items;

    public String getName() {
        return Name;
    }

    public String getAbout() {
        return About;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public void setAbout(String about) {
        this.About = about;
    }

    public ArrayList<ExpandListChild> getItems() {
        return Items;
    }

    public void setItems(ArrayList<ExpandListChild> Items) {
        this.Items = Items;
    }
}