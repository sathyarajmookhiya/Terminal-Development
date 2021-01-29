package mslabs.com.terminal.activity;

import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_MERCHANT_UID;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_PASSWORD;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_PROFILE_ID;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_PROFILE_UPDATE_TIMESTAMP;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_DESCRIPTION;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_LOCATION;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_TAG;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_TYPE;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_DEVICEID;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_USERNAME;
import static uk.co.transaxiom.android.txandroidlib.LWTranslator.JSON_VALUE;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import mslabs.com.terminal.R;
import mslabs.com.terminal.Utils.AsyncClass;
import mslabs.com.terminal.Utils.PermissionUtil;
import uk.co.transaxiom.acquirer.services.lw.ProfileLW;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.BaseActivity;
import uk.co.transaxiom.android.txandroidlib.LWTranslator;
import uk.co.transaxiom.android.txandroidlib.TxTheme;
import uk.co.transaxiom.android.txandroidlib.BaseActivity.MyTouchListener;
import uk.co.transaxiom.android.txandroidlib.terminal.Operations;
import uk.co.transaxiom.android.txandroidlib.terminal.RequestedNCounter;
import uk.co.transaxiom.terminal.TxTerminal;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends BaseActivity {

	private TextView profileTxtVw;
	private EditText typeEdtTxt, descriptionEdtTxt, locationEdtTxt;
	private Spinner profileSpn;
	private Toolbar toolbar;

	private String terminalType, terminalDescription, profile, imeiOrMac, terminalTag, merchantUID, terminalLocation = "";

	public List<RequestedNCounter> requestedNCounters = new ArrayList<RequestedNCounter>();
	public TxTerminal txTerminal;
	public String username, password;

	String themeName = "TransaXiom";

	public String getThemeName() {
		return themeName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public TxTerminal getTxTerminal() {
		return txTerminal;
	}

	public void setTxTerminal(TxTerminal txTerminal) {
		this.txTerminal = txTerminal;
	}

	public List<RequestedNCounter> getRequestedNCounters() {
		return requestedNCounters;
	}

	public void setRequestedNCounters(List<RequestedNCounter> requestedNCounters) {
		this.requestedNCounters = requestedNCounters;
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//AndroidWrapper wrapper = AndroidWrapper.getInstance();
//		wrapper.setActionBarTitle(this, R.string.app_name);
		if (!PermissionUtil.hasRequiredAppPermission(this)) {
			PermissionUtil.requestAppPermission(RegisterActivity.this);
			return;
		} else {
			initiliazeLayout();
		}
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		imeiOrMac = telephonyManager.getDeviceId();
		if (imeiOrMac == null) {
			//replace IMEI by MAC address
			WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = wifiManager.getConnectionInfo();
			imeiOrMac = wInfo.getMacAddress();
		}
		
		//theme = wrapper.getThemeFromSharedPreferences(this);
		//if(!theme.getName().equals("N/A")){
			//updateLayout();
		//}
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private void initiliazeLayout() {
		setContentView(R.layout.activity_register);
		
		locationEdtTxt = (EditText) findViewById(R.id.location_EdtTxt);
		profileTxtVw = (TextView) findViewById(R.id.profile_TxtVw);
		profileSpn = (Spinner) findViewById(R.id.profile_Spn);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.wizard_icon));
		getSupportActionBar().setTitle("    "+getString(R.string.app_name));

		typeEdtTxt = (EditText) findViewById(R.id.type_EdtTxt);
		descriptionEdtTxt = (EditText) findViewById(R.id.description_EdtTxt);
		
		profileTxtVw.setVisibility(View.INVISIBLE);
		profileSpn.setVisibility(View.INVISIBLE);

		Button merchantUI_Btn = (Button) findViewById(R.id.register_Btn);
		merchantUI_Btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				registerTerminal(v);
			}
		});
	}


	/*public void updateLayout(){
		super.updateLayout();
		
		View rootView = findViewById(R.id.rootLayout);
		rootView.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));
		
		Bitmap bmp = AndroidWrapper.getInstance().readImageFromFile(theme.getLogoLocation());
		ImageView schemeLogo_ImgVw = (ImageView) findViewById(R.id.schemeLogo_ImgVw);
		schemeLogo_ImgVw.setImageBitmap(bmp);
		
		TextView schemeName_TxtVw = (TextView) findViewById(R.id.schemeNameTxtVw);
		schemeName_TxtVw.setTextColor(Color.parseColor(theme.getColorMain()));
		schemeName_TxtVw.setText(theme.getName());
		
		TextView terminalNameTxtVw = (TextView) findViewById(R.id.textView1);
		terminalNameTxtVw.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));
		terminalNameTxtVw.setTextColor(Color.parseColor(theme.getColorMain()));
		
		Button merchantUI_Btn = (Button) findViewById(R.id.register_Btn);
		merchantUI_Btn.setBackgroundColor(Color.parseColor(theme.getColorMain()));
		merchantUI_Btn.setTextColor(Color.parseColor(theme.getPageColor()));
		merchantUI_Btn.setOnTouchListener(new MyTouchListener());
	}*/
	/*
	@Override
	public void buttonClicked(View view){
		registerTerminal(view);
	}*/

	class CustomOnItemSelectedListener implements OnItemSelectedListener {
		  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			  //profile = parent.getItemAtPosition(position).toString();
		  }
		 
		  @Override
		  public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		  }
	}
	
	public void registerTerminal(View view){
		readEntries();
		profile = "profile1";
		if(terminalType == null || terminalDescription == null || profile == null || terminalType.isEmpty() || terminalDescription.isEmpty() || profile.isEmpty()){
			Toast.makeText(this, "Please make sure to fill in all the fields!", Toast.LENGTH_LONG).show();
		}else{
			txTerminal = new TxTerminal();
			txTerminal.setTerminalType(terminalType);
			txTerminal.setTerminalDescription(terminalDescription);
			txTerminal.setTerminalDeviceId(imeiOrMac);
			txTerminal.setTerminalLocation(terminalLocation);
			txTerminal.setActive(true);
			txTerminal.setReplacement(true);
			
			promptUsernamePassword(RegisterActivity.this);
		}
		
	}
	
	/*private void promptUsernamePassword() {
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
	        
	        username = usernameInput.getText().toString().trim();
	        password = passwordInput.getText().toString().trim();
	        
	        executeCreateTerminal();
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
*/

	private void promptUsernamePassword(Activity activity)
	{
		final Dialog dialog=new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.text_entry);
		dialog.setCancelable(false);
		double width = (getResources().getDisplayMetrics().widthPixels * 0.90);
		double height = (getResources().getDisplayMetrics().heightPixels * 0.50);
		dialog.getWindow().setLayout((int)width, (int)height);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		final EditText usernameInput=dialog.findViewById(R.id.username_EdtTxt);
		final EditText passwordInput=dialog.findViewById(R.id.pwd_EdtTxt);

		usernameInput.setText("sekar");
		passwordInput.setText("msl@12345");

		Button dialog_login=dialog.findViewById(R.id.dialog_login);
		Button dialog_cancel=dialog.findViewById(R.id.dialog_cancel);
		dialog_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				username = usernameInput.getText().toString().trim();
				password = passwordInput.getText().toString().trim();

				executeCreateTerminal();

			}
		});
		dialog_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();

			}
		});

		dialog.show();




	}


	private void executeCreateTerminal() {

		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREFERENCES_USERNAME, username);
		editor.putString(PREFERENCES_PASSWORD, password);
		
		long timestamp = System.currentTimeMillis();
		
//		editor.putString(PREFERENCES_PROFILE_ID, "B31A-FBF6-39BE-D7BC"); //transaxiomdemo
	      editor.putString(PREFERENCES_PROFILE_ID, "F35A-FFF6-39FE-D31E"); //jeeves
		//editor.putString(PREFERENCES_PROFILE_ID, "EC5D-F4FA-63D2-E7AE"); //Cub Digital
		//editor.putString(PREFERENCES_PROFILE_ID, "90EE-1E71-38EB-EFE4"); //jeeves
		editor.putLong(PREFERENCES_PROFILE_UPDATE_TIMESTAMP, timestamp);
		editor.commit();
		
		new AsyncClass(this, Operations.CREATE_TERMINAL).execute(Operations.CREATE_TERMINAL);
	}

	private void readEntries() {
		
		terminalType = typeEdtTxt.getText().toString().trim();
		terminalLocation = locationEdtTxt.getText().toString().trim();
		terminalDescription = descriptionEdtTxt.getText().toString().trim();
	}

	public void saveTerminalTag(String response) {
		try {
			JSONObject json = new JSONObject(response);
			terminalTag = json.getString("tag");
			txTerminal.setTerminalTag(terminalTag);
			
			new AsyncClass(this, Operations.READ_MERCHANT).execute(Operations.READ_MERCHANT);
		} catch (JSONException e) {
			Toast.makeText(this, "Something went wrong while reading terminal ID!", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
	}

	public void saveMerchantUID(String response){
		try {
			JSONObject json = new JSONObject(response);
			JSONObject merchID = json.getJSONObject("merchantId");
			merchantUID = merchID.getString(JSON_VALUE);
			Log.d("RegisterActivity", "My merchant uuid is: "+merchantUID);
			
			new AsyncClass(this, Operations.READ_PROFILE).execute(Operations.READ_PROFILE);
			//getNCounters();
		} catch (JSONException e) {
			Toast.makeText(this, "Something went wrong while reading the terminal ID!", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	public void getNCounters(ProfileLW profileLW){
		
		LWTranslator translator = LWTranslator.getInstance();
		
		try {
			requestedNCounters = translator.fromLW(profileLW);
			new AsyncClass(this, Operations.GET_NCOUNTERS).execute(Operations.GET_NCOUNTERS);
		} catch (UnsupportedEncodingException e) {
			Toast.makeText(this, "Sorry, something went wrong while reading the profile... Please try again later", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
//		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
//		String narrative1 = "www.transaxiom.co.uk";
//		String narrative2 = "Please visit www.transaxiom.co.uk";
//		
//		requestedNCounters.add(wrapper.createRequestedNCounter("GBP", 100, (short) 100, narrative1));
//		requestedNCounters.add(wrapper.createRequestedNCounter("GBP", 100, (short) 100, narrative1));
//		requestedNCounters.add(wrapper.createRequestedNCounter("GBP", 10, (short) 100, narrative1));
//		requestedNCounters.add(wrapper.createRequestedNCounter("GBP", 10, (short) 100, narrative2));
//		requestedNCounters.add(wrapper.createRequestedNCounter("GBP", 1, (short) 100, narrative2));
//		requestedNCounters.add(wrapper.createRequestedNCounter("GBP", 1, (short) 100, narrative2));
//		
//		new AsyncClass(this, Operations.GET_NCOUNTERS).execute(Operations.GET_NCOUNTERS);
	}
	
	public void saveNCounters(boolean savingResult) {
		Log.d("MainActivity", "GetNCounters is successful? --> "+savingResult);
		if(savingResult){
			
			SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(PREFERENCES_TERMINAL_TAG, terminalTag);
			editor.putString(PREFERENCES_TERMINAL_TYPE, terminalType);
			editor.putString(PREFERENCES_TERMINAL_DESCRIPTION, terminalDescription);
			editor.putString(PREFERENCES_TERMINAL_LOCATION, terminalLocation);
			editor.putString(PREFERENCES_TERMINAL_DEVICEID, imeiOrMac);
			editor.putString(PREFERENCES_MERCHANT_UID, merchantUID);
			editor.putString(PREFERENCES_USERNAME, username);
			editor.putString(PREFERENCES_PASSWORD, password);
			editor.commit();
			
			Toast.makeText(this, "Terminal created successfully and ready to be used!", Toast.LENGTH_SHORT).show();
			
			finish();
		}else{
			Toast.makeText(this, "Something went wrong... Please contact your administrator.", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
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

}
