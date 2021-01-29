package uk.co.transaxiom.android.txandroidlib;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;


public class HandleRequest extends AsyncTask<Void, String, Boolean> {
    /** progress dialog to show user that the backup is processing. */
    private ProgressDialog dialog;
    /** application context. */
    private Activity activity;
    private Boolean success = false;
    private long start = 0;
    private long finish = 0;
    boolean internet_ok = false;
	    
    public HandleRequest(Activity myActivity) {
        this.activity = myActivity;
        dialog = new ProgressDialog(this.activity);
        Log.d("ConnectionWrapper", "Connection initiated");
    }
    
    protected Boolean doInBackground(Void... args) {
    	this.publishProgress("Connecting...");
    	
    	try{
    		
    		internet_ok = true;
    	}catch(Exception e){
    		Log.d("ConnectionWrapper","YOU MUST BE CONNECTED!");
    		internet_ok = false;
    	}
    	if(success){
    		internet_ok = true;
    	}
      	return success;
	}
	
	protected void onPostExecute(Boolean result) {
        finish = System.currentTimeMillis();
        long elapsed = finish - start;
        
		if (dialog.isShowing()) {
             dialog.dismiss();
             Log.d("ConnectionWrapper", "Connection terminated in "+elapsed+"ms");
        }
	}
	 
	protected void onPreExecute() {
		start = System.currentTimeMillis();
		this.dialog.setMessage("Connecting...");
		this.dialog.show();
	}
	
	protected void onProgressUpdate(String... progress) {
	    String tmpStr = (String)progress[0];
	    dialog.setMessage(tmpStr);
	}
 }
