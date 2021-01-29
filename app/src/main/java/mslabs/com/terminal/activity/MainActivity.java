package mslabs.com.terminal.activity;

import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_TYPE;
import static uk.co.transaxiom.terminal.currency.CurrencyFormatter.getCurrencyFormatter;
import static uk.co.transaxiom.terminal.currency.CurrencyManager.getCurrencyManager;

import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

import mslabs.com.terminal.R;
import mslabs.com.terminal.Utils.AsyncClass;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.NFCActivity;
import uk.co.transaxiom.android.txandroidlib.terminal.Operations;

import uk.co.transaxiom.terminal.currency.TxCurrency;
import uk.co.transaxiom.terminal.payment.entity.Purse;
import uk.co.transaxiom.util.lw.BinaryUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends NFCActivity implements View.OnClickListener {

	public static final String terminalCurrencyCode = "INR";
	private TextView schemeNameTxtVw, terminalNameTxtVw, amountTxtVw, lastRedemptionTxtVw;
	private Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9, buttonDelete;
	private Button takePaymentBtn, lastReceiptBtn, adminInterfaceBtn;

	public static final String TERM_DATA = "TERM_DATA";
	public static final String terminalCurrency = "INR";

	boolean proceededThroughPayment = false;
	boolean onResumeCalled = false;
	boolean openedAdminInterface = false;

	private String terminalName = "";
	private String amount = "";
	Activity activity = this;
	StringBuilder amountStringBuilder;
	Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolbar=findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.wizard_icon));
		getSupportActionBar().setTitle("    "+getString(R.string.app_name));


		amountStringBuilder = new StringBuilder();

		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		terminalName = settings.getString(PREFERENCES_TERMINAL_TYPE, "N/A");

		if(terminalName.equals("N/A")){
			finish();
		}else{
			initiliaseLayout();
				updateLayout();

		}

		Log.d("STUPID_TEST", "BInaryUtils gives me back: "+BinaryUtils.encode(BinaryUtils.int2Unsigned2Bytes(152)));
	}

	private void initiliaseLayout() {

//		setContentView(R.layout.activity_main);
		/*Typeface tf_title = Typeface.createFromAsset(getAssets(), "fonts/titilliumBold.otf");
		Typeface tf_italic = Typeface.createFromAsset(getAssets(), "fonts/titilliumItalic.otf");
	    Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/titilliumLight.otf");*/

	/*	schemeNameTxtVw = (TextView) findViewById(R.id.schemeNameTxtVw);
		terminalNameTxtVw = (TextView) findViewById(R.id.terminalName_TxtVw);*/
		amountTxtVw = (TextView) findViewById(R.id.amount_TxtVw);
		lastRedemptionTxtVw = (TextView) findViewById(R.id.lastRedemption_TxtVw);

		button0 = (Button) findViewById(R.id.calcButton0);
		button1 = (Button) findViewById(R.id.calcButton1);
		button2 = (Button) findViewById(R.id.calcButton2);
		button3 = (Button) findViewById(R.id.calcButton3);
		button4 = (Button) findViewById(R.id.calcButton4);
		button5 = (Button) findViewById(R.id.calcButton5);
		button6 = (Button) findViewById(R.id.calcButton6);
		button7 = (Button) findViewById(R.id.calcButton7);
		button8 = (Button) findViewById(R.id.calcButton8);
		button9 = (Button) findViewById(R.id.calcButton9);
		buttonDelete = (Button) findViewById(R.id.calcButtonDel);

		takePaymentBtn = (Button) findViewById(R.id.takePayment_Btn);
		lastReceiptBtn = (Button) findViewById(R.id.receiptAfterSale_Btn);
		adminInterfaceBtn = (Button) findViewById(R.id.adminInterface_Btn);

//		schemeNameTxtVw.setTypeface(tf);
/*		terminalNameTxtVw.setTypeface(tf_title);
		amountTxtVw.setTypeface(tf_title);
		lastRedemptionTxtVw.setTypeface(tf_italic);*/

		SharedPreferences settings = getSharedPreferences(TERM_DATA, 0);
        String failedRedemption = settings.getString("failedRedemption", "N/A");
        if(failedRedemption.equals("N/A")){
        	lastRedemptionTxtVw.setVisibility(View.INVISIBLE);
        }else{
        	lastRedemptionTxtVw.setText("Last redemption failed on: "+failedRedemption);
        }


		/*button0.setTypeface(tf_title);
		button1.setTypeface(tf_title);
		button2.setTypeface(tf_title);
		button3.setTypeface(tf_title);
		button4.setTypeface(tf_title);
		button5.setTypeface(tf_title);
		button6.setTypeface(tf_title);
		button7.setTypeface(tf_title);
		button8.setTypeface(tf_title);
		button9.setTypeface(tf_title);
		buttonDelete.setTypeface(tf_title);

		takePaymentBtn.setTypeface(tf_title);
		lastReceiptBtn.setTypeface(tf);
		adminInterfaceBtn.setTypeface(tf);
*/
//		terminalNameTxtVw.setText(terminalName);

		button0.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				//Toast.makeText(MainActivity.this,"Sucess Click Zero",Toast.LENGTH_SHORT).show();
				addCharacter('1');
				/*if(amountStringBuilder.length() > 0){
					addCharacter('0');
				}*/
			}
		});
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this,"Sucess Click One",Toast.LENGTH_SHORT).show();
				addCharacter('1');
			}
		});

		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		char separator = dfs.getDecimalSeparator();

		TxCurrency txCurrency = getCurrencyManager().getCurrency(terminalCurrencyCode);
		String currencySymbol = getCurrencyFormatter().getSymbol(txCurrency);

//		String currency = "$";//dfs.getCurrencySymbol();
		amountTxtVw.setText(currencySymbol+"0" + separator + "00");
	}


	public void updateLayout(){
		super.updateLayout();


	   	button0.setOnClickListener(this);

		button1.setOnClickListener(this);

		button2.setOnClickListener(this);

		button3.setOnClickListener(this);

		button4.setOnClickListener(this);

		button5.setOnClickListener(this);

		button6.setOnClickListener(this);

		button7.setOnClickListener(this);

		button8.setOnClickListener(this);

		button9.setOnClickListener(this);

		buttonDelete.setOnClickListener(this);

		adminInterfaceBtn.setOnClickListener(this);

		lastReceiptBtn.setOnClickListener(this);

		takePaymentBtn.setOnClickListener(this);
	}



	public void deleteLastChar (View view){
		if(amountStringBuilder.length() > 0){
			amountStringBuilder.deleteCharAt(amountStringBuilder.length()-1);
			splitAmount();
		}
	}
	
	private void addCharacter(char c) {
		if(amountStringBuilder.length() < 5){
			amountStringBuilder.append(c);
			
			splitAmount();
		}
	}

	private void splitAmount() {
		if(amountStringBuilder.length() == 1){
			String tmp2 = amountStringBuilder.toString();
			
			updateAmount("0", "0"+tmp2);
		}
		else if(amountStringBuilder.length() > 2){
			String tmp1 = amountStringBuilder.substring(0, amountStringBuilder.length()-2);
			String tmp2 = amountStringBuilder.substring(amountStringBuilder.length()-2, amountStringBuilder.length());
			
			updateAmount(tmp1, tmp2);
		} 
		else if(amountStringBuilder.length() == 0){
			updateAmount("0", "00");
		}
		else{
			String tmp2 = amountStringBuilder.substring(0, amountStringBuilder.length());
			updateAmount("0", tmp2);
		}
		
	}
	

	private void updateAmount(String tmp1, String tmp2) {
		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		char separator = dfs.getDecimalSeparator();
		
		TxCurrency txCurrency = getCurrencyManager().getCurrency(terminalCurrencyCode);
		String currencySymbol = getCurrencyFormatter().getSymbol(txCurrency);
		
		
		amount = tmp1 + separator + tmp2;
		amountTxtVw.setText(currencySymbol+amount);
	}

	public void takePayment(View view){
		TxCurrency currency = getCurrencyManager().getCurrency(terminalCurrencyCode);
		String currencySymbol = getCurrencyFormatter().getSymbol(currency);
		
		if(!amount.isEmpty()){
			Float tmp = Float.valueOf(amount);
			if(tmp <= 0){
				Toast.makeText(this, "Please enter a positive amount.", Toast.LENGTH_LONG).show();
			}
//			else if(tmp > 10000.00){
//				Toast.makeText(this, "You cannot make payments for more than "+currencySymbol+"1000.00", Toast.LENGTH_LONG).show();
//			}
			else{
				proceededThroughPayment = true;
				Intent intent = new Intent(this, PaymentActivity.class);
				intent.putExtra("amount", amount);
				startActivity(intent);
			}
		}else{
			Toast.makeText(this, "Please enter a positive amount.", Toast.LENGTH_LONG).show();
		}
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
	
	public void displayLastReceipt(View view){
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		String lastReceipt = settings.getString("lastReceipt", "N/A");
		if(!lastReceipt.equals("N/A")){

			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Receipt after sale:");
			adb.setMessage(
					lastReceipt + "\nThanks for shopping with us!")
					.setCancelable(false)
					.setPositiveButton("Close",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.dismiss();
								}
							});
			AlertDialog alertDialog = adb.create();
			alertDialog.show();
		}else{
			Toast.makeText(this, "No transaction has been made yet...", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void openAdminInterface(View view){
		openedAdminInterface = true;
		Intent intent = new Intent(this, AdminInterfaceActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
		
		refreshScreen();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(openedAdminInterface || proceededThroughPayment){
			refreshScreen();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.about) {
			displayAbout();
			return true;
		}
		if (id == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public void displayBalance(Purse purse) {
		
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Purse Details:");
		adb.setMessage(
				"You have "+purse.getCurrencySymbol()+purse.getStringBalance()+
						" of credit left on your card.")
				.setPositiveButton("Close",
						new DialogInterface.OnClickListener() {
					
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
							
						});
		
		AlertDialog alertDialog = adb.create();
		alertDialog.show();	
		
	}

	@Override
	public void onClick(View view) {

		int id = view.getId();
		switch(id){
			case R.id.calcButton0:
				if(amountStringBuilder.length() > 0){
					addCharacter('0');
				}
				break;

			case R.id.calcButton1:
				addCharacter('1');
				break;

			case R.id.calcButton2:
				addCharacter('2');
				break;

			case R.id.calcButton3:
				addCharacter('3');
				break;

			case R.id.calcButton4:
				addCharacter('4');
				break;

			case R.id.calcButton5:
				addCharacter('5');
				break;

			case R.id.calcButton6:
				addCharacter('6');
				break;

			case R.id.calcButton7:
				addCharacter('7');
				break;

			case R.id.calcButton8:
				addCharacter('8');
				break;

			case R.id.calcButton9:
				addCharacter('9');
				break;

			case R.id.calcButtonDel:
				deleteLastChar(view);
				break;

			case R.id.adminInterface_Btn:
				openAdminInterface(view);
				break;

			case R.id.receiptAfterSale_Btn:
				displayLastReceipt(view);
				break;

			case R.id.takePayment_Btn:
				takePayment(view);
				break;
		}
		Log.d("MainActivity", "amount entered: "+amountStringBuilder.toString());
	}
}
