package mslabs.com.terminal.activity;

import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_PASSWORD;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_USERNAME;
import static uk.co.transaxiom.terminal.currency.CurrencyFormatter.getCurrencyFormatter;
import static uk.co.transaxiom.terminal.currency.CurrencyManager.getCurrencyManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mslabs.com.terminal.R;
import mslabs.com.terminal.Utils.AsyncClass;
import mslabs.com.terminal.Utils.CustomListAdapter;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.BaseActivity;
import uk.co.transaxiom.android.txandroidlib.terminal.AndroidTerminalWrapper;
import uk.co.transaxiom.android.txandroidlib.terminal.Operations;
import uk.co.transaxiom.android.txandroidlib.terminal.RedeemedValue;

import uk.co.transaxiom.terminal.currency.TxCurrency;
import uk.co.transaxiom.terminal.ncounters.TXnCounter;
import uk.co.transaxiom.terminal.payment.entity.TxTransaction;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AdminInterfaceActivity extends BaseActivity {

	List<TXnCounter> toRedeemNCs = new ArrayList<TXnCounter>();
	private Context context = this;
	
	private boolean uploadWithRedemption = false;
	
	public List<TXnCounter> getToRedeemNCs() {
		return toRedeemNCs;
	}
	
	private List<TxTransaction> transactions = new ArrayList<TxTransaction>();
	
	public List<TxTransaction> getTransactions() {
		return transactions;
	}

	Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_interface);

		/*AndroidWrapper wrapper = AndroidWrapper.getInstance();
		wrapper.setActionBarTitle(this, R.string.app_name);*/

		toolbar=findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.wizard_icon));
		getSupportActionBar().setTitle("    "+getString(R.string.app_name));
		
		displayCurrentTurnover();
		displayRedemptions();

		Button seeTransactionsBtn = (Button) findViewById(R.id.seeTransactionsBtn);
		seeTransactionsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				seeTransactions(v);

			}
		});

		Button redeemBtn = (Button) findViewById(R.id.redeemBtn);
		redeemBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				redeemFunds(v);

			}
		});
		initialiseAutoRedeemCheckBox();
		

	}

	private void initialiseAutoRedeemCheckBox() {
		CheckBox autoRedeemBox = (CheckBox) findViewById(R.id.autoRedeemBox);
		final AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		
		final SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		boolean isAutoRedeemOn = wrapper.isAutoRedeemOn(settings);
		
		autoRedeemBox.setChecked(isAutoRedeemOn);
		autoRedeemBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				wrapper.setAutoRedeemStatus(settings, isChecked);
			}
		});
	}

	/*public void updateLayout() {
		super.updateLayout();

		TextView turnoverTxtVw = (TextView) findViewById(R.id.turnover_TxtVw);

		TextView maxTurnoverTxtVw = (TextView) findViewById(R.id.maxTurnover_TxtVw);

		TextView redemptionsTxtVw = (TextView) findViewById(R.id.myRedemptions_TxtVw);

		*//*Button seeTransactionsBtn = (Button) findViewById(R.id.seeTransactionsBtn);
		seeTransactionsBtn.setTextColor(Color.parseColor(theme.getPageColor()));
		seeTransactionsBtn.setBackgroundColor(Color.parseColor(theme.getColorMain()));
		seeTransactionsBtn.setOnTouchListener(new MyTouchListener());
		
		Button redeemBtn = (Button) findViewById(R.id.redeemBtn);
		redeemBtn.setTextColor(Color.parseColor(theme.getPageColor()));
		redeemBtn.setBackgroundColor(Color.parseColor(theme.getColorMain()));
		redeemBtn.setOnTouchListener(new MyTouchListener());*//*
	}
*/
	private void displayRedemptions() {
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		List<RedeemedValue> values = wrapper.getRedeemedValues(this);
		
		String lastRedemption = "N/A";
		if(values.size() > 0){
			lastRedemption = values.get(0).toString();
		}
		
		ListView expandable = (ListView) findViewById(R.id.redemptions_ExpLst);
		
		if(lastRedemption.equals("N/A")){
			TextView myRedemptionsTxtVw = (TextView) findViewById(R.id.myRedemptions_TxtVw);
			myRedemptionsTxtVw.setText("No funds have been redeemed yet.");
			TextView myRedemption = (TextView) findViewById(R.id.myredimtion);
             myRedemption.setVisibility(View.GONE);
			expandable.setVisibility(View.INVISIBLE);
		}else{
			TextView myRedemptions_TxtVw = (TextView) findViewById(R.id.myRedemptions_TxtVw);
			myRedemptions_TxtVw.setVisibility(View.GONE);

			List<String> header = new ArrayList<String>();
			header.add(lastRedemption);
			
			List<String> redeemedValues = new ArrayList<String>();
			redeemedValues.add("No previous redemptions.");
			
			if(values.size() > 1){
				redeemedValues = populateList(values);
			}
			
			HashMap<String, List<String>> redemptionsItems = new HashMap<String, List<String>>();
			for(int i=0;i<header.size();i++) {
				redemptionsItems.put(header.get(i), redeemedValues);
			}
			/*RedemptionsAdapter listAdapter = new RedemptionsAdapter(this, this, header, redemptionsItems);
			expandable.setAdapter(listAdapter);*/
			CustomListAdapter adapter=new CustomListAdapter(AdminInterfaceActivity.this,values);
			expandable.setAdapter(adapter);
		}
	}

	public void seeTransactions(View view){
		Intent intent = new Intent(this, TransactionsActivity.class);
		startActivity(intent);
	}
	
	private List<String> populateList(List<RedeemedValue> values) {
		List<String> redemptions = new ArrayList<String>();
		
		for(int i=1; i<values.size(); i++){
			redemptions.add(values.get(i).toString());
		}
		
		return redemptions;
	}

	private void displayCurrentTurnover() {
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		
		TextView turnoverTxtVw = (TextView) findViewById(R.id.turnover_TxtVw);
		TextView maxTurnoverTxtVw = (TextView) findViewById(R.id.maxTurnover_TxtVw);
		
		TxCurrency txCurrency = getCurrencyManager().getCurrency(MainActivity.terminalCurrencyCode);
		String currencySymbol = getCurrencyFormatter().getSymbol(txCurrency);
		
		String turnover = currencySymbol+"0.00";
		List<TXnCounter> redeemableNCounters = wrapper.getAllRedeemableTXNcounters(this);
		if(redeemableNCounters != null && !redeemableNCounters.isEmpty()){
			RedeemedValue value = new RedeemedValue();
			value.parseIntoValue(redeemableNCounters);
			
			turnover = value.getCurrencySymbol() + value.getAmount();
		}
		
		turnoverTxtVw.setText("Current turnover: "+turnover);
		maxTurnoverTxtVw.setText("Max turnover: "+wrapper.getMaxTurnover(this, MainActivity.terminalCurrency));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.admin_interface, menu);
		return true;
	}

	public void saveLatestRedemption(RedeemedValue redeemedValue){
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("failedRedemption", "N/A");
		editor.putString("lastRedemption", redeemedValue.toString());
		editor.commit();
		
		Toast.makeText(this, "Funds successfully redeemed.", Toast.LENGTH_LONG).show();
		new AsyncClass(this, Operations.GET_MESSAGES_AFTER).execute(Operations.GET_MESSAGES_AFTER);
	}
	
	public void refreshScreen(){
		uploadWithRedemption = false;
		
		Intent intent = getIntent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
	    finish();
	    overridePendingTransition(0, 0);
	    startActivity(intent);
	}
	
	public void uploadTransactions(){
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		List<TxTransaction> transactionsToUpload = wrapper.getAllTransactionsToUpload(this);
		
		if(transactionsToUpload.isEmpty()){
			Toast.makeText(this, "You have no transactions to upload!", Toast.LENGTH_SHORT).show();
			refreshScreen();
		}else{
			transactions = transactionsToUpload;
			new AsyncClass(this, Operations.UPLOAD_TRANSACTIONS).execute(Operations.UPLOAD_TRANSACTIONS);
		}
	}
	
	public void redeemFunds(View view){
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		List<TxTransaction> transactionsToUpload = wrapper.getAllTransactionsToUpload(this);
		
		if(!transactionsToUpload.isEmpty()){
			uploadWithRedemption = true;
  		  	performRedemption();
		}else{
			performRedemption();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.about) {
			displayAbout();
			return true;
		}
		if (id == R.id.resetRedemptions) {
			promptUserForReset();
			return true;
		}
		if(id == R.id.resetTerminal){
			promptUserForDelete();
			return true;
		}
		if(id == R.id.merchantInfo){
			displayMerchantInfo();
		}
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void displayMerchantInfo(){
		
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		
		
		wrapper.displayTerminalInfo(this, settings);
	}
	
	private void promptUserForDelete(){
		LayoutInflater factory = LayoutInflater.from(this);

	    final View textEntryView = factory.inflate(R.layout.text_entry, null);
	       //text_entry is an Layout XML file containing two text field to display in alert dialog

	    final EditText usernameInput = (EditText) textEntryView.findViewById(R.id.username_EdtTxt);
	    final EditText passwordInput = (EditText) textEntryView.findViewById(R.id.pwd_EdtTxt);
	    
	    usernameInput.setText("", TextView.BufferType.EDITABLE);
	    passwordInput.setText("", TextView.BufferType.EDITABLE);
	    
	    final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle(
	      "Please identify yourself:").setView(
	      textEntryView).setPositiveButton("OK",
	      new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,
	         int whichButton) {
	        
	        String username = usernameInput.getText().toString().trim();
	        String password = passwordInput.getText().toString().trim();
	        
	        SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
	        String realUsername = settings.getString(PREFERENCES_USERNAME, "N/A");
	        String realPassword = settings.getString(PREFERENCES_PASSWORD, "N/A");
	        
	        if(username.equals(realUsername) && password.equals(realPassword)){
	        	deleteTerminal();
	        }
	        else{
	        	Toast.makeText(context, "Wrong username or password. If you forgot your username or password, please visit your admin web UI.", Toast.LENGTH_LONG).show();
	        }
	       }
	      }).setNegativeButton("Cancel",
	      new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,
	         int whichButton) {
	         dialog.cancel();
	       }
	      });
	    alert.show();
	}
	
	protected void deleteTerminal() {
		
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		toRedeemNCs = new ArrayList<TXnCounter>();
		toRedeemNCs = wrapper.getAllRedeemableTXNcounters(this);
		
		new AsyncClass(this, Operations.DELETE_TERMINAL).execute(Operations.DELETE_TERMINAL);
	}

	private void promptUserForReset() {
		LayoutInflater factory = LayoutInflater.from(this);

	    final View textEntryView = factory.inflate(R.layout.text_entry, null);
	       //text_entry is an Layout XML file containing two text field to display in alert dialog

	    final EditText usernameInput = (EditText) textEntryView.findViewById(R.id.username_EdtTxt);
	    final EditText passwordInput = (EditText) textEntryView.findViewById(R.id.pwd_EdtTxt);
	    usernameInput.setText("");
		passwordInput.setText("");

	    usernameInput.setText("", TextView.BufferType.EDITABLE);
	    passwordInput.setText("", TextView.BufferType.EDITABLE);
	    
	    final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle(
	      "Please identify yourself:").setView(
	      textEntryView).setPositiveButton("OK",
	      new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,
	         int whichButton) {
	        
	        String username = usernameInput.getText().toString().trim();
	        String password = passwordInput.getText().toString().trim();
	        
	        SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
	        String realUsername = settings.getString(PREFERENCES_USERNAME, "N/A");
	        String realPassword = settings.getString(PREFERENCES_PASSWORD, "N/A");
	        
	        if(username.equals(realUsername) && password.equals(realPassword)){
	        	resetRedemptions();
	        }
	        else{
	        	Toast.makeText(context, "Wrong username or password. If you forgot your username or password, please visit your admin web UI.", Toast.LENGTH_LONG).show();
	        }
	       }
	      }).setNegativeButton("Cancel",
	      new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,
	         int whichButton) {
	         dialog.cancel();
	       }
	      });
	    alert.show();
	}

	
	private void promptUserForUpload() {
		LayoutInflater factory = LayoutInflater.from(this);

	    final View textEntryView = factory.inflate(R.layout.text_entry, null);
	       //text_entry is an Layout XML file containing two text field to display in alert dialog

	    final EditText usernameInput = (EditText) textEntryView.findViewById(R.id.username_EdtTxt);
	    final EditText passwordInput = (EditText) textEntryView.findViewById(R.id.pwd_EdtTxt);
	    
	    usernameInput.setText("", TextView.BufferType.EDITABLE);
	    passwordInput.setText("", TextView.BufferType.EDITABLE);
	    
	    final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle(
	      "Upload transactions:")
	      .setMessage("Do you want to upload your transactions at the same time?")
	      .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    	  public void onClick(DialogInterface dialog, int whichButton) {
	    		  uploadWithRedemption = true;
	    		  performRedemption();
	       }
	      })
	      .setNegativeButton("No", new DialogInterface.OnClickListener() {
	    	  public void onClick(DialogInterface dialog, int whichButton) {
	    	   		uploadWithRedemption = false;
	    	   		performRedemption();
	       }
	      });
	    alert.show();
	}
	
	private void resetRedemptions(){
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		if(wrapper.wipeRedeemedValuesDatabase(this)){
			SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("lastRedemption", "N/A");
			editor.commit();
			Toast.makeText(this, "Redemptions erased successfully!", Toast.LENGTH_SHORT).show();
			finish();
		}
		else{
			Toast.makeText(this, "You have no redeemed values to erase...", Toast.LENGTH_SHORT).show();
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	public void failedRedemption() {
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String date = format.format(now);
		
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("failedRedemption", date);
		editor.commit();
	}
/*

	@Override
	public void buttonClicked(View view) {
		int id = view.getId();
		
		switch(id){
		
		case R.id.redeemBtn:
			redeemFunds(view);
			break;
			
		case R.id.seeTransactionsBtn:
			seeTransactions(view);
			break;
		}
		
	}
*/

	public void promptUserForNewPassword() {
		LayoutInflater factory = LayoutInflater.from(this);

	    final View textEntryView = factory.inflate(R.layout.text_entry, null);
	       //text_entry is an Layout XML file containing two text field to display in alert dialog

	    final EditText usernameInput = (EditText) textEntryView.findViewById(R.id.username_EdtTxt);
	    final EditText passwordInput = (EditText) textEntryView.findViewById(R.id.pwd_EdtTxt);
	    
	    final SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
	    String oldusername = settings.getString(PREFERENCES_USERNAME, "");
	    
	    usernameInput.setText(oldusername, TextView.BufferType.EDITABLE);
	    passwordInput.setText("", TextView.BufferType.EDITABLE);
	    
	    final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle(
	      "Please identify yourself:").setView(
	      textEntryView).setPositiveButton("OK",
	      new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,
	         int whichButton) {
	        
	    	   
	        String username = usernameInput.getText().toString().trim();
	        String password = passwordInput.getText().toString().trim();
	        
			SharedPreferences.Editor editor = settings.edit();
			
			editor.putString(PREFERENCES_USERNAME, username);
			editor.putString(PREFERENCES_PASSWORD, password);
			editor.commit();
	        
			performRedemption();
	       }
	      }).setNegativeButton("Cancel",
	      new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,
	         int whichButton) {
	         dialog.cancel();
	       }
	      });
	    alert.show();
		
	}

	protected void performRedemption() {
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		toRedeemNCs = new ArrayList<TXnCounter>();
		toRedeemNCs = wrapper.getAllRedeemableTXNcounters(this);
		if(toRedeemNCs != null && !toRedeemNCs.isEmpty()){
			new AsyncClass(this, Operations.REDEEM_NCOUNTERS).execute(Operations.REDEEM_NCOUNTERS);
		}else{
			Toast.makeText(this, "You have no funds to redeem!", Toast.LENGTH_SHORT).show();
		}
	}

	public void resetTerminalConfirmed() {
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		wrapper.deleteTerminalDetails(this);
		
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("lastRedemption", "N/A");
		editor.putString("lastReceipt", "N/A");
		editor.commit();
		
		finish();
		Intent intent = new Intent(this, MainMenuActivity.class);
		startActivity(intent);
	}
	
	public void uploadTransactionsIfSelected() {
		if(uploadWithRedemption){
			uploadTransactions();
		}else{
			refreshScreen();
		}
	}
}
