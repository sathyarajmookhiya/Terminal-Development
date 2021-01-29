package mslabs.com.terminal.activity;

import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_PASSWORD;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_USERNAME;
import static uk.co.transaxiom.terminal.currency.CurrencyFormatter.getCurrencyFormatter;
import static uk.co.transaxiom.terminal.currency.CurrencyManager.getCurrencyManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.conn.scheme.SchemeRegistry;

import mslabs.com.terminal.R;
import mslabs.com.terminal.Utils.AsyncClass;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.NFCActivity;
import uk.co.transaxiom.android.txandroidlib.TxTheme;
import uk.co.transaxiom.android.txandroidlib.terminal.AndroidTerminalWrapper;
import uk.co.transaxiom.android.txandroidlib.terminal.Operations;
import uk.co.transaxiom.android.txandroidlib.terminal.RedeemedValue;

import uk.co.transaxiom.terminal.currency.TxCurrency;
import uk.co.transaxiom.terminal.ncounters.NCountersBatch;
import uk.co.transaxiom.terminal.ncounters.TXnCounter;
import uk.co.transaxiom.terminal.payment.entity.TxTransaction;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentActivity extends NFCActivity implements View.OnClickListener{

	public String amount = "";

	List<NCountersBatch> availableNCouters = new ArrayList<NCountersBatch>();
	List<TXnCounter> toRedeemNCs = new ArrayList<TXnCounter>();
	private List<TxTransaction> transactions = new ArrayList<TxTransaction>();
	
	public List<TxTransaction> getTransactions() {
		return transactions;
	}
	public List<TXnCounter> getToRedeemNCs() {
		return toRedeemNCs;
	}

	public String getAmount() {
		return amount;
	}
	
	public List<NCountersBatch> getAvailableNCouters() {
		return availableNCouters;
	}

	Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);
		
		/*AndroidWrapper wrapper = AndroidWrapper.getInstance();
		wrapper.setActionBarTitle(this, R.string.app_name);*/
		
		//Typeface tf_title = Typeface.createFromAsset(getAssets(), "fonts/titilliumBold.otf");
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.wizard_icon));
		getSupportActionBar().setTitle("    "+getString(R.string.app_name));
		Intent intent = getIntent();
		
		amount = intent.getStringExtra("amount");
		
		TxCurrency txCurrency = getCurrencyManager().getCurrency(MainActivity.terminalCurrencyCode);
		String currencySymbol = getCurrencyFormatter().getSymbol(txCurrency);
		
		TextView amountTxtVw = (TextView) findViewById(R.id.amount_TxtVw);
		//amountTxtVw.setTypeface(tf_title);
		
		double amountToPay = Double.parseDouble(amount);
		if(amountToPay > 0) {
			NumberFormat formatNumber = NumberFormat.getInstance();
			formatNumber.setMinimumFractionDigits(2);
			formatNumber.setMaximumFractionDigits(2);

			amount = formatNumber.format(amountToPay);
			amountTxtVw.setText("Amount to pay: " + currencySymbol + amount);

			new AsyncClass(this, Operations.PREPARE_NCOUNTERS).execute(Operations.PREPARE_NCOUNTERS);

			updateLayout();

		}
	}

	public void updateLayout() {
		super.updateLayout();
		
		/*View rootView = findViewById(R.id.rootLayout);
		rootView.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));
		
		Bitmap bmp = AndroidWrapper.getInstance().readImageFromFile(theme.getLogoLocation());
		ImageView schemeLogo_ImgVw = (ImageView) findViewById(R.id.schemeLogo_ImgVw);
		schemeLogo_ImgVw.setImageBitmap(bmp);
		
		TextView schemeName_TxtVw = (TextView) findViewById(R.id.schemeNameTxtVw);
		schemeName_TxtVw.setTextColor(Color.parseColor(theme.getColorMain()));
		schemeName_TxtVw.setText(theme.getName());		
		*/
		TextView amountTxtVw = (TextView) findViewById(R.id.amount_TxtVw);
		/*amountTxtVw.setTextColor(Color.parseColor(theme.getColorMain()));
		amountTxtVw.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));*/
		
		Button cancelBtn = (Button) findViewById(R.id.cancelBtn);

		cancelBtn.setOnClickListener(this);
	}

	public void rejectNoNCounters() {
		Toast.makeText(this, "Sorry! Couldn't split payment for the available n-Counters!", Toast.LENGTH_SHORT).show();
		
		Toast.makeText(this, "Try redeeming your n-Counters before accepting a payment.", Toast.LENGTH_SHORT).show();
		finish();
		
	}

	public void preparePayment(List<NCountersBatch> availableNCounters) {
		availableNCouters = availableNCounters;
	}
	
	public void acknowledgePayment() {
		saveLastReceipt();
		
		checkNCountersStatus();
	}
	
	@SuppressLint("SimpleDateFormat")
	private void saveLastReceipt() {
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		
		TxCurrency txCurrency = getCurrencyManager().getCurrency(MainActivity.terminalCurrencyCode);
		String currencySymbol = getCurrencyFormatter().getSymbol(txCurrency);
		
		String timestamp = format.format(now);
		String lastReceipt = timestamp + " - "+ currencySymbol + amount;
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("lastReceipt", lastReceipt);
		editor.commit();
	}

	
	public void checkNCountersStatus(){
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		
		boolean autoRedeem = wrapper.isAutoRedeemOn(settings);
		
    	List<TXnCounter> fullNCounters = getAllFullNCounters();
		if(!fullNCounters.isEmpty()){
			if(autoRedeem){
				toRedeemNCs = fullNCounters;
				new AsyncClass(this, Operations.REDEEM_NCOUNTERS).execute(Operations.REDEEM_NCOUNTERS);
			}
			else{
//				Toast.makeText(this, "Maybe time for a redemption?", Toast.LENGTH_LONG).show();
				finish();
			}
		}
		else{
			finish();
		}
    }
	
	private List<TXnCounter> getAllFullNCounters() {
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		
		List<TXnCounter> fullNCounters = new ArrayList<TXnCounter>();
		List<TXnCounter> tmp = wrapper.getAllTXNcounters(this);
		for(int i=0; i<tmp.size(); i++){
			short stepsUsed = tmp.get(i).getnCStepsUsed();
			short length = tmp.get(i).getnCLength();
			Log.d("CheckNCounterStatus", "steps used / length = "+(stepsUsed*100/length));
			if((stepsUsed*100/length) >= 80){
				Log.d("CheckNCounterStatus", "Added an n-Counter to get redeemed!");
				fullNCounters.add(tmp.get(i));
			}
		}
		return fullNCounters;
	}

	public void saveLatestRedemption(RedeemedValue value){
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("failedRedemption", "N/A");
		editor.putString("lastRedemption", value.toString());
		editor.commit();
		
		Toast.makeText(this, "Funds successfully redeemed. Login to your admin UI to see your redemptions", Toast.LENGTH_LONG).show();
		new AsyncClass(this, Operations.GET_MESSAGES_AFTER).execute(Operations.GET_MESSAGES_AFTER);
	}
	
	@Override
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();       
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
			Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String techs[] = tagFromIntent.getTechList();
			if (techs[0].contains("IsoDep")) {
				Log.d("NFCEvent", "card detected!");
				
				currentCard = IsoDep.get(tagFromIntent);
				currentCard.setTimeout(5000);
				
				new AsyncClass(this, Operations.READ_PAYER_FCI).execute(Operations.READ_PAYER_FCI);
				
			} else {
				Toast.makeText(this, "Card not supported yet...", Toast.LENGTH_SHORT).show();
			}
        }
	}

	public void cancel(View view){
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.payment, menu);
		return true;
	}

	public void displayMassiveBreachMessage(String message) {
		AlertDialog.Builder adb= new AlertDialog.Builder(this);
		adb.setTitle("WARNING!");
		adb.setMessage(message)
		.setCancelable(false)
		.setPositiveButton("I understand",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.dismiss();
			}
		  });	
		   AlertDialog alertDialog = adb.create();
		   alertDialog.show();	
		
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
	        case R.id.about:
	        	displayAbout();
	            return true;
			case android.R.id.home:
				onBackPressed();
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void startPayment() {

		new AsyncClass(this, Operations.PERFORM_PAYMENT).execute(Operations.PERFORM_PAYMENT);
	}

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
	        
	        checkNCountersStatus();
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

	@Override
	public void onClick(View view) {

		int id = view.getId();
		switch (id) {
			case R.id.cancelBtn:
				cancel(view);
				break;
		}
	}
}
