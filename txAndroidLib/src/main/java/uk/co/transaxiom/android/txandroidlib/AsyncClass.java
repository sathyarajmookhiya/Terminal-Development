package uk.co.transaxiom.android.txandroidlib;

import uk.co.transaxiom.android.txandroidlib.cardmanagement.applet.filesystem.Operations;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class AsyncClass extends AsyncTask<Operations, String, Boolean> {

	private Activity parentActivity;
	private int operation;
	private ProgressDialog dialog;
	
	//Constructor
	public AsyncClass(Activity parentActivity, int operation){
		this.parentActivity = parentActivity;
		this.operation = operation;
		
		dialog = new ProgressDialog(this.parentActivity);
	}
	
	//Preparation before executing the operations
	protected void onPreExecute() {
		this.dialog.setMessage("Reading card...");
		this.dialog.show();
	}

	//Update the message displayed on the screen
	protected void onProgressUpdate(String... progress) {
		String tmpStr = (String) progress[0];
		dialog.setMessage(tmpStr);
	}

	//Where the operations are executed in the background
	@Override
	protected Boolean doInBackground(Operations... params) {
		boolean result = false;
		
		switch(operation){
		case Operations.WRITE_DATA:
			//	...
			break;
		
		case Operations.READ_DATA:
			//	...
			break;
			
		default:
			break;
		}
		return result;
	}

	//What to do when the execution is over
	protected void onPostExecute(Boolean result) {
		if(dialog.isShowing()){
			dialog.dismiss();
		}
		
		switch(operation){
		case Operations.WRITE_DATA:
			//	...
			break;
		
		case Operations.READ_DATA:
			//	...
			break;
			
		default:
			break;
		}
	}
}
