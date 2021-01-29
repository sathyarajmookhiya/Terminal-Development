package mslabs.com.terminal.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mslabs.com.terminal.R;
import uk.co.transaxiom.acquirer.services.lw.TerminalLW;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.TxTheme;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class TerminalAdapter extends BaseExpandableListAdapter {
	 
    private Context context;
    private List<TerminalLW> terminals = new ArrayList<TerminalLW>(); // header titles
    private Activity parentActivity;

    // child data in format of header title, child title
 
    public TerminalAdapter(Context context, List<TerminalLW> terminals, Activity parentActivity) {
        this.context = context;
        this.terminals = terminals;
        this.parentActivity = parentActivity;
    }
 
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.terminals.get(groupPosition);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @SuppressLint("SimpleDateFormat")
	@Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 
    	TerminalLW terminal = terminals.get(groupPosition);
 
    	Date stamp = new Date(terminal.getStamp().getValue());
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm");
    	String timestamp = "created on: "+dateFormat.format(stamp);
    	
    	String active = "No"; 
    	if(terminal.getActive()){
    		active = "Yes";
    	}
    	
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.display_terminal_details, null);
        }
 
        TextView descripionTxtVw = (TextView) convertView.findViewById(R.id.description_TxtVw);
        TextView timestampTxtVw = (TextView) convertView.findViewById(R.id.timestamp_TxtVw);
        TextView locationTxtVw = (TextView) convertView.findViewById(R.id.location_TxtVw);
        TextView activeTxtVw = (TextView) convertView.findViewById(R.id.active_TxtVw);
 
        descripionTxtVw.setText(terminal.getDescription());
        timestampTxtVw.setText(timestamp);
        locationTxtVw.setText(terminal.getLocation());
        activeTxtVw.setText("Active: "+active);
        
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this.terminals.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this.terminals.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
       
    	TerminalLW terminal = terminals.get(groupPosition);
    	
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.display_terminal, null);
        }
 
        TextView typeTxtVw = (TextView) convertView.findViewById(R.id.type_TxtVw);
        TextView tagTxtVw = (TextView) convertView.findViewById(R.id.tag_TxtVw);
        
        TxTheme theme = AndroidWrapper.getInstance().getThemeFromSharedPreferences(parentActivity);
		if(!theme.getName().equals("N/A")){
        	convertView.setBackgroundColor(Color.parseColor(theme.getColorMain()));
        	typeTxtVw.setTextColor(Color.parseColor(theme.getPageColor()));
        	tagTxtVw.setTextColor(Color.parseColor(theme.getPageColor()));
		}
		else{
        	convertView.setBackgroundColor(context.getResources().getColor(R.color.Tx_Prim_Light_Blue));
		}
        
        typeTxtVw.setText(terminal.getTerminalId().getType());
        tagTxtVw.setText(terminal.getTerminalId().getTag());
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