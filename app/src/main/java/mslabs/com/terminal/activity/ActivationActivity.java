package mslabs.com.terminal.activity;

import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_MERCHANT_UID;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_PASSWORD;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_DESCRIPTION;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_DEVICEID;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_LOCATION;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_TAG;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_TYPE;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_USERNAME;
import static uk.co.transaxiom.android.txandroidlib.LWTranslator.JSON_VALUE;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mslabs.com.terminal.R;
import mslabs.com.terminal.Utils.AsyncClass;
import mslabs.com.terminal.Utils.TerminalAdapter;
import uk.co.transaxiom.acquirer.services.lw.TerminalLW;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.BaseActivity;
import uk.co.transaxiom.android.txandroidlib.LWTranslator;
import uk.co.transaxiom.android.txandroidlib.BaseActivity.MyTouchListener;
import uk.co.transaxiom.android.txandroidlib.terminal.AndroidTerminalWrapper;
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
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;

public class ActivationActivity extends BaseActivity {

	private Button seeMoreBtn;
	private int lastExpandedPosition = -1;
	private String username = "";
	private String password = "";

	private int nextTerminalIndex = 0;
	private int totalNumberOfTerminals = -1;

	private Context context = this;

	private List<RequestedNCounter> requestedNCounters = new ArrayList<RequestedNCounter>();
	private List<TerminalLW> terminalsLW = new ArrayList<TerminalLW>();

	public List<RequestedNCounter> getRequestedNCounters() {
		return requestedNCounters;
	}

	public int getTotalNumberOfTerminals() {
		return totalNumberOfTerminals;
	}

	public void setTotalNumberOfTerminals(int totalNumberOfTerminals) {
		this.totalNumberOfTerminals = totalNumberOfTerminals;
	}

	public int getNextTerminalIndex() {
		return nextTerminalIndex;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}


	Toolbar toolbar;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activation);

	/*	AndroidWrapper wrapper = AndroidWrapper.getInstance();
		wrapper.setActionBarTitle(this, R.string.app_name);*/
		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().
				setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.wizard_icon));
		getSupportActionBar().setTitle("    " + getString(R.string.app_name));

		seeMoreBtn = (Button) findViewById(R.id.seeMore_Btn);
		updateLayout();
		/*
		theme = wrapper.getThemeFromSharedPreferences(this);
		if(!theme.getName().equals("N/A")){
			updateLayout();
		}
		*/
		promptUsernamePassword(ActivationActivity.this);
	}

	public void updateLayout() {
		super.updateLayout();

		TextView terminalNameTxtVw = (TextView) findViewById(R.id.select_TxtVw);
		/*terminalNameTxtVw.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));
		terminalNameTxtVw.setTextColor(Color.parseColor(theme.getColorMain()));*/

		Button seeMoreBtn = (Button) findViewById(R.id.seeMore_Btn);
		seeMoreBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				seeMoreTerminals(v);
			}
		});
	/*	seeMoreBtn.setBackgroundColor(Color.parseColor(theme.getColorMain()));
		seeMoreBtn.setTextColor(Color.parseColor(theme.getPageColor()));
		seeMoreBtn.setOnTouchListener(new MyTouchListener());*/
	}

	@Override
	public void buttonClicked(View view) {
		seeMoreTerminals(view);
	}

/*
	private void promptUsernamePassword() {
		LayoutInflater factory = LayoutInflater.from(this);

	    final View textEntryView = factory.inflate(R.layout.text_entry, null);
	       //text_entry is an Layout XML file containing two text field to display in alert dialog

	    final EditText usernameInput = (EditText) textEntryView.findViewById(R.id.username_EdtTxt);
	    final EditText passwordInput = (EditText) textEntryView.findViewById(R.id.pwd_EdtTxt);

	    usernameInput.setText("", TextView.BufferType.EDITABLE);
	    passwordInput.setText("", TextView.BufferType.EDITABLE);

	    final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setTitle(
	      "Please identify yourself:")
	      .setView(textEntryView)
	      .setCancelable(false)
	      .setPositiveButton("OK",new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,
	         int whichButton) {

	        username = usernameInput.getText().toString().trim();
	        password = passwordInput.getText().toString().trim();

	        loginAndGetTerminals();
	       }
	      }).setNegativeButton("Cancel",
	      new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog, int whichButton) {
	    	   closeActivity();
	    	   dialog.cancel();
	       }
	      });
	    alert.show();
	}
*/

	private void promptUsernamePassword(Activity activity) {
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.text_entry);
		dialog.setCancelable(false);
		double width = (getResources().getDisplayMetrics().widthPixels * 0.90);
		double height = (getResources().getDisplayMetrics().heightPixels * 0.50);
		dialog.getWindow().setLayout((int) width, (int) height);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		final EditText usernameInput = dialog.findViewById(R.id.username_EdtTxt);
		final EditText passwordInput = dialog.findViewById(R.id.pwd_EdtTxt);
		Button dialog_login = dialog.findViewById(R.id.dialog_login);
		Button dialog_cancel = dialog.findViewById(R.id.dialog_cancel);
		dialog_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				username = usernameInput.getText().toString().trim();
				password = passwordInput.getText().toString().trim();

				loginAndGetTerminals();
			}
		});
		dialog_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeActivity();
				dialog.cancel();

			}
		});

		dialog.show();


	}


	public void closeActivity() {
		failToUpdate();
		finish();
		Intent intent = new Intent(context, MainMenuActivity.class);
		startActivity(intent);
	}

	private void loginAndGetTerminals() {

		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREFERENCES_USERNAME, username);
		editor.putString(PREFERENCES_PASSWORD, password);
		editor.commit();


		new AsyncClass(this, Operations.GET_NUMBER_TERMINALS_FILTERED).execute(Operations.GET_NUMBER_TERMINALS_FILTERED);
	}

	public void seeMoreTerminals(View view) {
		new AsyncClass(this, Operations.GET_TERMINALS_FILTERED).execute(Operations.GET_TERMINALS_FILTERED);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activation, menu);
		return true;
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


	public void parseResponseIntoTerminals(String response) {
		if (totalNumberOfTerminals - nextTerminalIndex > 5) {
			nextTerminalIndex += 5;
		} else {
			nextTerminalIndex = totalNumberOfTerminals;
		}

		if (nextTerminalIndex == totalNumberOfTerminals) {
			seeMoreBtn.setVisibility(View.INVISIBLE);
		}

		try {
			JSONObject json = new JSONObject(response);
			JSONArray array = json.getJSONArray("item");
			LWTranslator translator = LWTranslator.getInstance();
			List<TerminalLW> terminals = translator.toTerminalsLW(array);

			terminalsLW.addAll(terminals);
			displayList(terminalsLW);
		} catch (JSONException e) {
			Toast.makeText(context, "Something went wrong... Please try again later.", Toast.LENGTH_SHORT).show();
			finish();
			e.printStackTrace();
		}
	}


	private void displayList(List<TerminalLW> terminals) {
		final ExpandableListView terminalsLstVw = (ExpandableListView) findViewById(R.id.terminals_LstVw);
		TerminalAdapter adatper = new TerminalAdapter(this, terminals, this);

		terminalsLstVw.setAdapter(adatper);

		terminalsLstVw.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				if (lastExpandedPosition != -1
						&& groupPosition != lastExpandedPosition) {
					terminalsLstVw.collapseGroup(lastExpandedPosition);
				}
				lastExpandedPosition = groupPosition;
			}
		});

		terminalsLstVw.setOnChildClickListener((new OnChildClickListener() {


			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
										int groupPosition, int childPosition, long id) {
				TerminalLW terminal = (TerminalLW) terminalsLstVw.getItemAtPosition(groupPosition);
				promptUserToSelectTerminal(terminal);

				return true;
			}
		}));

	}

	private void promptUserToSelectTerminal(final TerminalLW terminal) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(terminal.getTerminalId().getType());
		adb.setMessage(
				"Are you sure you want to activate this terminal?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								updateTerminal(terminal);
							}

						})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});

		AlertDialog alertDialog = adb.create();
		alertDialog.show();

	}


	protected void updateTerminal(TerminalLW terminal) {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

			return;
		}
		String imeiOrMac = telephonyManager.getDeviceId();
		if (imeiOrMac == null) {
			//replace IMEI by MAC address
			WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			WifiInfo wInfo = wifiManager.getConnectionInfo();
			imeiOrMac = wInfo.getMacAddress();
		}
		
		terminal.setDeviceId(imeiOrMac);
		LWTranslator translator = LWTranslator.getInstance();
		
		TxTerminal txTerminal = translator.fromLW(terminal);
		
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREFERENCES_TERMINAL_TAG, txTerminal.getTerminalTag());
		editor.putString(PREFERENCES_TERMINAL_TYPE, txTerminal.getTerminalType());
		editor.putString(PREFERENCES_TERMINAL_DESCRIPTION, txTerminal.getTerminalDescription());
		editor.putString(PREFERENCES_TERMINAL_LOCATION, txTerminal.getTerminalLocation());
		editor.putString(PREFERENCES_TERMINAL_DEVICEID, txTerminal.getTerminalDeviceId());
		editor.putString(PREFERENCES_USERNAME, username);
		editor.putString(PREFERENCES_PASSWORD, password);
		editor.commit();
		
		createRequestedNCounters();
		
		new AsyncClass(this, Operations.UPDATE_TERMINAL).execute(Operations.UPDATE_TERMINAL);
	}

	private void createRequestedNCounters() {
		
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		String narrative = "Thanks for paying with the Universal-Terminal-App";

		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 100, (short) 100, narrative));
		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 100, (short) 100, narrative));
		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 10, (short) 100, narrative));
		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 10, (short) 100, narrative));
		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 1, (short) 100, narrative));
		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 1, (short) 100, narrative));
	}

	public void activateTerminal(String response) {
		
		String merchantUID = getUIDFromResponse(response);
		
		SharedPreferences settingsTerm = getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settingsTerm.edit();
		
		if(merchantUID == null){
			Toast.makeText(context, "Please try again!", Toast.LENGTH_SHORT).show();
			editor.putString(PREFERENCES_TERMINAL_TAG, "N/A");
			editor.putString(PREFERENCES_TERMINAL_TYPE, "N/A");
			editor.putString(PREFERENCES_TERMINAL_DESCRIPTION, "N/A");
			editor.putString(PREFERENCES_TERMINAL_LOCATION, "N/A");
			editor.putString(PREFERENCES_USERNAME, "N/A");
			editor.putString(PREFERENCES_PASSWORD, "N/A");
			editor.commit();
			return;
		}
		
		editor.putString(PREFERENCES_MERCHANT_UID, merchantUID);
		editor.commit();
		
		Toast.makeText(this, "Terminal activated successfully and ready to be used!", Toast.LENGTH_SHORT).show();
		
		finish();
	}

	private String getUIDFromResponse(String response) {
		try {
			JSONObject json = new JSONObject(response);
			JSONObject merchID = json.getJSONObject("merchantId");
			String merchantUID = merchID.getString(JSON_VALUE);
			Log.d("RegisterActivity", "My merchant uuid is: " + merchantUID);

			return merchantUID;
		} catch (JSONException e) {
			Toast.makeText(this, "Something went wrong while activating the terminal...",	Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return null;
		}
	}

	public void failToUpdate() {
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString(PREFERENCES_TERMINAL_TAG, "N/A");
		editor.putString(PREFERENCES_TERMINAL_TYPE, "N/A");
		editor.putString(PREFERENCES_TERMINAL_DESCRIPTION, "N/A");
		editor.putString(PREFERENCES_TERMINAL_LOCATION, "N/A");
		editor.putString(PREFERENCES_USERNAME, "N/A");
		editor.putString(PREFERENCES_PASSWORD, "N/A");
		editor.commit();
		
	}


}
