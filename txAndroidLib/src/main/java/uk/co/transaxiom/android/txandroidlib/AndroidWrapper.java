package uk.co.transaxiom.android.txandroidlib;

import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_USERNAME;
import static uk.co.transaxiom.android.txandroidlib.TxParams.getTxParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.transaxiom.terminal.Config;
import uk.transaxiom.android.txandroidlib.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AndroidWrapper {

	private static AndroidWrapper instance = new AndroidWrapper();
	private static int appFlavour = 3; 
	
	private AndroidWrapper() {
	}

	public static AndroidWrapper getInstance() {

		appFlavour = getTxParams().getParam(Constants.PARAMS_APP_FLAVOUR);
		
		return instance;
	}
	
	/**
	 * Set the title to be displayed in the ActionBar from a String resource 
	 * 
	 * @param activity: the activity calling this method
	 * @param resId: the Id of the String resource
	 */
	public void setActionBarTitle(AppCompatActivity activity, int resId){
		View actionBarView = activity.getSupportActionBar().getCustomView();
		TextView actionBarTitle = (TextView) actionBarView.findViewById(R.id.action_bar_title);
		actionBarTitle.setText(resId);
	}
	
	/**
	 * Set the Image to be displayed in the ActionBar from a drawable resource
	 * 
	 * @param activity: the activity calling this method
	 * @param resId: the Id of the drawable resource
	 */
	public void setActionBarImage(AppCompatActivity activity, int resId){
		View actionBarView = activity.getSupportActionBar().getCustomView();
		ImageView actionBarImage = (ImageView) actionBarView.findViewById(R.id.appIconImgVw);
		actionBarImage.setImageResource(resId);
	}
	
	/**
	 * Set the Image to be displayed in the ActionBar from a bitmap image
	 * 
	 * @param activity: the activity calling this method
	 * @param bitmap: the Bitmap to be displayed
	 */
	public void setActionBarImage(AppCompatActivity activity, Bitmap bitmap){
		View actionBarView = activity.getSupportActionBar().getCustomView();
		ImageView actionBarImage = (ImageView) actionBarView.findViewById(R.id.appIconImgVw);
		actionBarImage.setImageBitmap(bitmap);
	}
	
	public void setActionBarImage(AppCompatActivity activity, Bitmap bitmap, TxTheme theme){
		View actionBarView = activity.getSupportActionBar().getCustomView();
		ImageView actionBarImage = (ImageView) actionBarView.findViewById(R.id.appIconImgVw);
		
		RelativeLayout imageBackground = (RelativeLayout) actionBarView.findViewById(R.id.imageBackground);
		imageBackground.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));
		
		actionBarImage.setImageBitmap(bitmap);
	}
	
	public void setActionBarImage(AppCompatActivity activity, String path, TxTheme theme){
		Bitmap bmp = readImageFromFile(path);
		setActionBarImage(activity, bmp, theme);
	}
	/**
	 * Set the Title to be displayed in the ActionBar from a specified string
	 * 
	 * @param activity: the activity calling this method
	 * @param title: the title to be displayed
	 */
	public void setActionBarTitle(AppCompatActivity activity, String title){
		View actionBarView = activity.getSupportActionBar().getCustomView();
		TextView actionBarTitle = (TextView) actionBarView.findViewById(R.id.action_bar_title);
		actionBarTitle.setText(title);
	}
	
	/**
	 * Display the version of the App, the Android Library and the Terminal Library in a pop up box
	 * 
	 * @param activity: the activity calling this method
	 */
	public void displayVersions(Activity activity){
		LayoutInflater factory = LayoutInflater.from(activity);
		final View versionView = factory.inflate(R.layout.display_versions, null);
		
		String terminalLibVersion = getTerminalLibVersion();
		String androidLibVersion = getAndroidLibVersionNumber();
		String appVersion = getAppVersionNumber(activity);
		
		TextView terminalLibVersionTxtVw = (TextView) versionView.findViewById(R.id.terminalLibVersion_TxtVw);
		TextView androidLibVersionTxtVw = (TextView) versionView.findViewById(R.id.androidLibVersion_TxtVw);
		TextView appVersionTxtVw = (TextView) versionView.findViewById(R.id.appVersion_TxtVw);
		
		appVersionTxtVw.setText(appVersion);
		androidLibVersionTxtVw.setText(androidLibVersion);
		terminalLibVersionTxtVw.setText(terminalLibVersion);
		
		AlertDialog dialogBox = createAlertDialog(activity, "Version:", versionView);
		dialogBox.show();
	}
	
	/**
	 * Display the version of the App and the Android Library in a pop up box
	 * 
	 * @param activity: the activity calling this method
	 */
	public void displayVersionsNoTerminal(Activity activity){
		LayoutInflater factory = LayoutInflater.from(activity);
		final View versionView = factory.inflate(R.layout.display_versions, null);
		
		String androidLibVersion = getAndroidLibVersionNumber();
		String appVersion = getAppVersionNumber(activity);
		
		TextView terminalLibTxtVw = (TextView) versionView.findViewById(R.id.textView1);
		TextView terminalLibVersionTxtVw = (TextView) versionView.findViewById(R.id.terminalLibVersion_TxtVw);
		TextView androidLibVersionTxtVw = (TextView) versionView.findViewById(R.id.androidLibVersion_TxtVw);
		TextView appVersionTxtVw = (TextView) versionView.findViewById(R.id.appVersion_TxtVw);
		
		terminalLibTxtVw.setVisibility(View.GONE);
		terminalLibVersionTxtVw.setVisibility(View.GONE);
		
		appVersionTxtVw.setText(appVersion);
		androidLibVersionTxtVw.setText(androidLibVersion);
		
		AlertDialog dialogBox = createAlertDialog(activity, "Version:", versionView);
		dialogBox.show();
	}
	
	/**
	 * Retrieve the username stored in the shared preferences
	 * Returns "N/A" if no username was specified in the shared preferences
	 * 
	 * @param settings: the shared Preferences holding the username
	 * @return
	 */
	public String getUsernameFromSharedPreferences(SharedPreferences settings){
		
		String username = settings.getString(PREFERENCES_USERNAME, "N/A");
		return username;
	}
	
	/**
	 * Create an alert dialog box (pop up box) to display message or a view.
	 * Can only be closed
	 * 
	 * @param activity: the activity calling this method
	 * @param title: the title of the pop up box
	 * @param view: the view to be displayed inside the pop up box
	 * 
	 * @return AlertDialog to be displayed
	 */
	public AlertDialog createAlertDialog(Activity activity, String title, View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(title)
		.setView(view)
		.setPositiveButton("Close", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		AlertDialog dialogBox = builder.create();
		return dialogBox;
	}
	
	/**
	 * Retrieve the terminal library version number
	 * 
	 * @return Version number as a string to be displayed
	 */
	private String getTerminalLibVersion() {
		String version = "";
		
		version = Config.getVersionNumber(); 
		
		return version;
	}

	/**
	 * Retrieve the Android library version number
	 * 
	 * @return Version number as a string to be displayed
	 */
	public String getAndroidLibVersionNumber(){
		String version = AndroidConfig.getVersionNumber();
		
		return version;
	}
	
	/**
	 * Retrieve the app version number
	 * 
	 * @param activity: the activity calling this method
	 * 
	 * @return App version number as a string to be displayed 
	 */
	public String getAppVersionNumber(Activity activity){
		String version = "";
		try {
			version = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
			version += "." + activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;	
	}
	
	public void saveThemeInSharedPreferences(Activity parentActivity, TxTheme theme){
		SharedPreferences settings = parentActivity.getSharedPreferences(TxTheme.THEME_PREFERENCES, 0);
		
		Editor editor = settings.edit();
		editor.putString(TxTheme.THEME_NAME, theme.getName());
		editor.putString(TxTheme.COLOR_MAIN, theme.getColorMain());
		editor.putString(TxTheme.COLOR_HIGHLIGHT, theme.getColorHighlight());
		editor.putString(TxTheme.COLOR_DARK, theme.getColorDark());
		editor.putString(TxTheme.COLOR_BACKGROUND, theme.getBackgroundColor());
		editor.putString(TxTheme.COLOR_PAGE, theme.getPageColor());
		editor.putString(TxTheme.LOGO_LOCATION, theme.getLogoLocation());
		editor.commit();
	}
	
	public TxTheme getThemeFromSharedPreferences(Activity parentActivity){
		SharedPreferences settings = parentActivity.getSharedPreferences(TxTheme.THEME_PREFERENCES, 0);

		TxTheme theme = new TxTheme();
		
		theme.setName(settings.getString(TxTheme.THEME_NAME, "N/A"));
		theme.setColorMain(settings.getString(TxTheme.COLOR_MAIN, "N/A"));
		theme.setColorHighlight(settings.getString(TxTheme.COLOR_HIGHLIGHT, "N/A"));
		theme.setColorDark(settings.getString(TxTheme.COLOR_DARK, "N/A"));
		theme.setBackgroundColor(settings.getString(TxTheme.COLOR_BACKGROUND, "N/A"));
		theme.setPageColor(settings.getString(TxTheme.COLOR_PAGE, "N/A"));
		theme.setLogoLocation(settings.getString(TxTheme.LOGO_LOCATION, "N/A"));
		return theme;
	}
	
	public void eraseThemeFromSharedPreferences(Activity parentActivity){
		SharedPreferences settings = parentActivity.getSharedPreferences(TxTheme.THEME_PREFERENCES, 0);
		
		Editor editor = settings.edit();
		editor.putString(TxTheme.THEME_NAME, "N/A");
		editor.putString(TxTheme.COLOR_MAIN, "N/A");
		editor.putString(TxTheme.COLOR_HIGHLIGHT, "N/A");
		editor.putString(TxTheme.COLOR_DARK, "N/A");
		editor.putString(TxTheme.COLOR_BACKGROUND, "N/A");
		editor.putString(TxTheme.COLOR_PAGE, "N/A");
		editor.putString(TxTheme.LOGO_LOCATION, "N/A");
		editor.commit();
	}
	
	public void saveBitmapToLocation(Bitmap bitmap, String location, String fileName) throws IOException{
		
		File directory = new File(location);
		directory.mkdirs();
		
		File file = new File(location+fileName);
		file.createNewFile();
		
		FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
		bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		
		if(out != null){
			out.close();
		}
	}
	
	public Bitmap readImageFromFile(String path){
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;

		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
	
		return bitmap;
	}
	
	@SuppressLint("SimpleDateFormat")
	public void writeLogInFile(int logLevel, String location, String fileName, String log){
		
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timestamp = dateFormat.format(now);
		
//		if(logLevel <= appFlavour){
			String directory = Environment.getExternalStorageDirectory().getAbsolutePath()+location;
			File fileDir = new File(directory);
			fileDir.mkdirs();
			
			File myFile = new File(directory+fileName);
			
			try {
				myFile.createNewFile();
				
		       FileOutputStream fOut = new FileOutputStream(myFile, true);
		       OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
		       
		       myOutWriter.append("\n\n === " + timestamp + " === " + log);
		       myOutWriter.close();
		       fOut.close();
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
//		}
	}
}
