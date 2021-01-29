package mslabs.com.terminal.activity;

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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mslabs.com.terminal.R;
import mslabs.com.terminal.Utils.AsyncClass;
import mslabs.com.terminal.Utils.Params;
import mslabs.com.terminal.Utils.PermissionUtil;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.BaseActivity;
import uk.co.transaxiom.android.txandroidlib.TxTheme;
import uk.co.transaxiom.android.txandroidlib.terminal.AndroidTerminalWrapper;
import uk.co.transaxiom.android.txandroidlib.terminal.Operations;
import uk.co.transaxiom.android.txandroidlib.terminal.RequestedNCounter;

import uk.co.transaxiom.services.lw.ThemeCatalogueLW;
import uk.co.transaxiom.terminal.ncounters.TXnCounter;

import static uk.co.transaxiom.android.txandroidlib.Constants.PARAMS_APP_FLAVOUR;
import static uk.co.transaxiom.android.txandroidlib.Constants.PARAMS_URL_PREFIX;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_TAG;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_TYPE;
import static uk.co.transaxiom.android.txandroidlib.TxParams.getTxParams;

public class MainMenuActivity extends BaseActivity {

	Activity activity = this;
	boolean onResumeCalled = false;
	String terminalName = "";
	Context context = this;
	private List<RequestedNCounter> requestedNCounters;
	String themeName = "TransaXiom";

	public String getThemeName() {
		return themeName;
	}

	public List<RequestedNCounter> getRequestedNCounters() {
		return requestedNCounters;
	}

	public void setRequestedNCounters(List<RequestedNCounter> requestedNCounters) {
		this.requestedNCounters = requestedNCounters;
	}

	static {

		getTxParams().setParam(PARAMS_URL_PREFIX, Params.URL_PREFIX);
		getTxParams().setParam(PARAMS_APP_FLAVOUR, Params.APP_FLAVOUR);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		if (!PermissionUtil.hasRequiredAppPermission(this)) {
			PermissionUtil.requestAppPermission(MainMenuActivity.this);
			return;
		} else {
			initApp();
		}
	}

	private void initApp() {
		AndroidWrapper wrapper = AndroidWrapper.getInstance();
//		wrapper.setActionBarTitle(this, R.string.app_name);

		onResumeCalled = false;
		initiliaseLayout();

		theme = AndroidWrapper.getInstance().getThemeFromSharedPreferences(this);
		if (theme.getName().equals("N/A")) {
			new AsyncClass(this, Operations.GET_THEME).execute(Operations.GET_THEME);
		} else if (!isRegistered()) {
			promptUser(MainMenuActivity.this);
		} else {
			checkNCountersAvailable();
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

		TextView terminalNameTxtVw = (TextView) findViewById(R.id.terminalName_TxtVw);
		terminalNameTxtVw.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));
		terminalNameTxtVw.setTextColor(Color.parseColor(theme.getColorMain()));

		Button merchantUI_Btn = (Button) findViewById(R.id.merchantUI_Btn);
		merchantUI_Btn.setBackgroundColor(Color.parseColor(theme.getColorMain()));
		merchantUI_Btn.setTextColor(Color.parseColor(theme.getPageColor()));
		merchantUI_Btn.setOnTouchListener(new MyTouchListener());

		Button terminalUI_Btn = (Button) findViewById(R.id.terminalUI_Btn);
		terminalUI_Btn.setBackgroundColor(Color.parseColor(theme.getColorMain()));
		terminalUI_Btn.setTextColor(Color.parseColor(theme.getPageColor()));
		terminalUI_Btn.setOnTouchListener(new MyTouchListener());*/
		CardView merchantUI_Btn = (CardView) findViewById(R.id.merchantUI_Btn);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.wizard_icon));
		getSupportActionBar().setTitle("    "+getString(R.string.app_name));
		CardView terminalUI_Btn = (CardView) findViewById(R.id.terminalUI_Btn);
		merchantUI_Btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*Intent intent = new Intent(MainMenuActivity.this, AdminActivity.class);
				startActivity(intent);*/
				Intent intent = new Intent(MainMenuActivity.this, AdminInterfaceActivity.class);
				startActivity(intent);
			}
		});
		terminalUI_Btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*Intent intent = new Intent(MainMenuActivity.this, TakePaymentActivity.class);
				startActivity(intent);*/
				/*Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
				startActivity(intent);*/
				Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});

	}


	private void checkNCountersAvailable() {
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		List<TXnCounter> allNCounters = wrapper.getAllTXNcounters(this);

		if (allNCounters == null) {
			alertUser();
		}
	}

	private void alertUser() {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("WARNING!");
		adb.setMessage(
				"Your terminal has been misconfigured! You won't be able to receive any payments." +
						"\nPlease make sure you have an internet connection to get your terminal fixed!")
				.setCancelable(false)
				.setPositiveButton("Fix it!",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

								getNewNCounters();
							}
						});

		AlertDialog alertDialog = adb.create();
		alertDialog.show();

	}

	protected void getNewNCounters() {
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		String narrative = "Thanks for paying with the Universal-Terminal-App";

		requestedNCounters = new ArrayList<RequestedNCounter>();

		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 100, (short) 100, narrative));
		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 100, (short) 100, narrative));
		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 10, (short) 100, narrative));
		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 10, (short) 100, narrative));
		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 1, (short) 100, narrative));
		requestedNCounters.add(wrapper.createRequestedNCounterLong("INR", 1, (short) 100, narrative));

		new AsyncClass(this, Operations.GET_NCOUNTERS).execute(Operations.GET_NCOUNTERS);

	}
	private void promptUser(Activity activity)
	{
		final Dialog dialog=new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(false);
		dialog.setContentView(R.layout.dialog_dashbord);
		double width = (getResources().getDisplayMetrics().widthPixels * 0.90);
		double height = (getResources().getDisplayMetrics().heightPixels * 0.60);
		dialog.getWindow().setLayout((int)width, (int)height);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		Button dialog_new_terminal=dialog.findViewById(R.id.dialog_new_terminal);
		Button dialog_existing_terminal=dialog.findViewById(R.id.dialog_existing_terminal);
		dialog_new_terminal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, RegisterActivity.class);
				startActivity(intent);
				dialog.dismiss();

			}
		});
		dialog_existing_terminal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ActivationActivity.class);
				startActivity(intent);
				dialog.dismiss();

			}
		});

		dialog.show();




	}


	private void initiliaseLayout() {
		setContentView(R.layout.activity_main_menu);

		//TextView terminalNameTxtVw = (TextView) findViewById(R.id.terminalName_TxtVw);
//		terminalNameTxtVw.setText(terminalName);

		theme = AndroidWrapper.getInstance().getThemeFromSharedPreferences(this);
		if (!theme.getName().equals("N/A")) {
			updateLayout();
		}
	}

	/*public void nextActivity(View view) {
		int id = view.getId();

		if (id == R.id.merchantUI_Btn) {
			Intent intent = new Intent(this, AdminInterfaceActivity.class);
			startActivity(intent);
		} else if (id == R.id.terminalUI_Btn) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
	}
*/

	private boolean isRegistered() {
		boolean result = false;
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		String terminalTag = settings.getString(PREFERENCES_TERMINAL_TAG, "N/A");
		Log.v("MainActivity", "terminalTag is - " + terminalTag);

		if (terminalTag.equals("N/A")) {
			result = false;
		} else {
			terminalName = settings.getString(PREFERENCES_TERMINAL_TYPE, "Terminal type");
			result = true;
		}
		return result;
	}

	public void selectThemeName(ThemeCatalogueLW catalogue) {

		List<HashMap<String, String>> finalCatalogue = new ArrayList<HashMap<String, String>>();

		List<String> list = catalogue.getThemeNames();
		for (int i = 0; i < list.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("name", list.get(i));

			finalCatalogue.add(map);
		}

		LayoutInflater factory = LayoutInflater.from(this);
		final View view = factory.inflate(R.layout.display_theme_catalogue, null);

		final ListView catalogueLstVw = (ListView) view.findViewById(R.id.catalogue_LstVw);
		SimpleAdapter adapter = new SimpleAdapter(this, finalCatalogue, R.layout.display_theme_name,
				new String[]{"name"}, new int[]{R.id.themeName_TxtVw});

		catalogueLstVw.setAdapter(adapter);
		final AlertDialog dialogBox = AndroidWrapper.getInstance().createAlertDialog(this, "Pick a theme:", view);
		dialogBox.show();

		catalogueLstVw.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				@SuppressWarnings("unchecked")
				HashMap<String, String> tmp = (HashMap<String, String>) catalogueLstVw.getItemAtPosition(position);
				dialogBox.cancel();
				themeName = tmp.get("name");
				new AsyncClass(activity, Operations.GET_THEME).execute(Operations.GET_THEME);
			}
		});
	}

	public void onResume() {
		super.onResume();
		if (onResumeCalled) {
			boolean tmp = isRegistered();
			if (!tmp) {
				finish();
			} else {
				initiliaseLayout();
			}
		}
		onResumeCalled = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.skins) {
			getThemeCatalogue();
			return true;
		}
		if (id == R.id.revert) {
			revertToInstalledLayout();
			return true;
		}
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


	private void getThemeCatalogue() {

		new AsyncClass(this, Operations.GET_THEME_CATALOGUE).execute(Operations.GET_THEME_CATALOGUE);
	}


	public void saveNewTheme(TxTheme theme) {
		this.theme = theme;
		this.theme.setName(themeName);
		AndroidWrapper.getInstance().saveThemeInSharedPreferences(this, this.theme);
		updateLayout();
		if (!isRegistered()) {
			promptUser(MainMenuActivity.this);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		boolean flag = true;
		for (int i = 0; i < grantResults.length; i++) {
			flag = flag && grantResults[i] == PackageManager.PERMISSION_GRANTED;
		}
		if (flag) {
			initApp();
		}
	}
}
