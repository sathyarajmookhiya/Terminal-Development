package mslabs.com.terminal.Utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mslabs.com.terminal.R;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.TxTheme;
import uk.co.transaxiom.terminal.payment.entity.TxTransaction;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class TransactionAdapter extends BaseExpandableListAdapter {

	private Context context;
    private List<TxTransaction> transactions = new ArrayList<TxTransaction>(); 
    private Activity parentActivity;
    // child data in format of header title, child title
 
    public TransactionAdapter(Context context, List<TxTransaction> transactions, Activity parentActivity) {
        this.context = context;
        this.transactions = transactions;
        this.parentActivity = parentActivity;
    }
 
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.transactions.get(groupPosition);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @SuppressLint("SimpleDateFormat")
	@Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 
    	TxTransaction transaction = transactions.get(groupPosition);
    	
    	String uploaded = "No"; 
    	if(transaction.isUploaded()){
    		uploaded = "Yes";
    	}
    	
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.display_transaction_detail, null);
        }
 
        TextView terminalLocationTxtVw = (TextView) convertView.findViewById(R.id.terminalLocation_TxtVw);
        TextView terminalTagTxtVw = (TextView) convertView.findViewById(R.id.terminalTag_TxtVw);
        TextView appletSNTxtVw = (TextView) convertView.findViewById(R.id.appletSN_TxtVw);
        TextView newBalanceTxtVw = (TextView) convertView.findViewById(R.id.newBalance_TxtVw);
        TextView uploadedTxtVw = (TextView) convertView.findViewById(R.id.uploaded_TxtVw);
 
        terminalLocationTxtVw.setText(transaction.getTerminalLocation());
        terminalTagTxtVw.setText(transaction.getTerminalTag());
        appletSNTxtVw.setText("Applet: "+transaction.getAppletSerialNumber());
        newBalanceTxtVw.setText(transaction.getCurrencySymbol()+transaction.getNewBalance());
        uploadedTxtVw.setText("Uploaded: "+uploaded);
        
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this.transactions.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this.transactions.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @SuppressLint("SimpleDateFormat")
	@Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
       
    	TxTransaction transaction = transactions.get(groupPosition);
    	
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.display_transaction, parent, false);
        }

        TextView transactionRefTxtVw = (TextView) convertView.findViewById(R.id.transactionRef_TxtVw);
        TextView timestampTxtVw = (TextView) convertView.findViewById(R.id.timestamp_TxtVw);
        TextView transactionAmountTxtVw = (TextView) convertView.findViewById(R.id.transactionAmount_TxtVw);
        

        if(transaction.isUploaded()){
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            timestampTxtVw.setTextColor(ContextCompat.getColor(context, R.color.white));
            transactionAmountTxtVw.setTextColor(ContextCompat.getColor(context, R.color.white));

        }else{
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            timestampTxtVw.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            transactionAmountTxtVw.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        BigDecimal bdAmount = new BigDecimal(transaction.getAmount());
        NumberFormat formatNumber = NumberFormat.getInstance();
        formatNumber.setMinimumFractionDigits(2);
        formatNumber.setMaximumFractionDigits(2);
        
        
        Date date = new Date(transaction.getTimestamp());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	String timestamp = dateFormat.format(date);
        
        transactionRefTxtVw.setText("Ref: #"+transaction.getReference());
        timestampTxtVw.setText(timestamp);
        transactionAmountTxtVw.setText(transaction.getCurrencySymbol() + formatNumber.format(bdAmount.doubleValue()));
        
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
