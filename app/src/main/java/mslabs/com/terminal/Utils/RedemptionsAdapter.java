package mslabs.com.terminal.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mslabs.com.terminal.R;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.TxTheme;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;


public class RedemptionsAdapter extends BaseExpandableListAdapter {
	 
    private Context context;
    private Activity parentActivity;
    private List<String> listDataHeader = new ArrayList<String>(); // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> listDataChild;
 
    public RedemptionsAdapter(Context context, Activity parentActivity, List<String> listDataHeader,
            HashMap<String, List<String>> listChildData) {
        this.context = context;
        this.parentActivity = parentActivity;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }
 
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosititon);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 
        final String childText = (String) getChild(groupPosition, childPosition);
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.display_redemption, null);
        }
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.anyRedemption_TxtVw);
 
        txtListChild.setText(childText);
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
       
    	String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.last_redemption, null);
        }
 
        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lastRedemption_TxtVw);
        lblListHeader.setText(headerTitle);
        
        TxTheme theme = AndroidWrapper.getInstance().getThemeFromSharedPreferences(parentActivity);
		if(!theme.getName().equals("N/A")){
			convertView.setBackgroundColor(Color.parseColor(theme.getColorMain()));
			lblListHeader.setBackgroundColor(Color.parseColor(theme.getColorMain()));
			lblListHeader.setTextColor(Color.parseColor(theme.getPageColor()));
		}
 
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}