package edu.dartmouth.cs.dtutor;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExpandListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<ExpandListGroup> groups;

    public ExpandListAdapter(Context context, ArrayList<ExpandListGroup> groups) {
        this.context = context;
        this.groups = groups;

    }

    public void addItem(ExpandListChild item, ExpandListGroup group) {

        if(!groups.contains(group)) {
            groups.add(group);
        }

        int index = groups.indexOf(group);
        ArrayList<ExpandListChild> ch = groups.get(index).getItems();
        ch.add(item);
        groups.get(index).setItems(ch);

    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<ExpandListChild> chList = groups.get(groupPosition).getItems();
        return chList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ExpandListChild child = (ExpandListChild) getChild(groupPosition, childPosition);

        if(view == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.expandlist_child, null);
        }

        TextView tv = (TextView) view.findViewById(R.id.course_expand_child_textview);
        LinearLayout expand_course = (LinearLayout) view.findViewById(R.id.course_expand_child);
        String courses = child.getCourses().toString();
        String[] courseL = courses.split(",");
        int count = courseL.length;

        for(int i = 0; i < count; i++) {

            TextView n = new TextView(context);
            String mCourse = courseL[i];
            // mCourse.replace("[", "").replace("]", "");
            if(i == 0)
                mCourse = mCourse.substring(2, mCourse.length());
            if(i == count - 1)
                mCourse = mCourse.substring(0, mCourse.length() - 2);
            n.setText(mCourse);
            expand_course.addView(n);
        }
        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<ExpandListChild> chList = groups.get(groupPosition).getItems();
        return chList.size();
    }

    @Override
    public Object getGroup(int arg0) {
        return groups.get(arg0);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int arg0) {
        return arg0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view, ViewGroup parent) {

        ExpandListGroup group = (ExpandListGroup) getGroup(groupPosition);
        if(view == null) {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.expandlist_group, null);
        }

        TextView tv = (TextView) view.findViewById(R.id.group_name_textview);
        tv.setText(group.getName());
        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }

}
