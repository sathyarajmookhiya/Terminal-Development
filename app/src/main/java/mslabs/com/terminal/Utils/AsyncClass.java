package mslabs.com.terminal.Utils;

import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_LAST_TRANSACTION_REF;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_PASSWORD;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_USERNAME;
import static uk.co.transaxiom.android.txandroidlib.LWTranslator.JSON_VALUE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import mslabs.com.terminal.activity.ActivationActivity;
import mslabs.com.terminal.activity.AdminInterfaceActivity;
import mslabs.com.terminal.activity.MainActivity;
import mslabs.com.terminal.activity.MainMenuActivity;
import mslabs.com.terminal.activity.PaymentActivity;
import mslabs.com.terminal.activity.RegisterActivity;
import mslabs.com.terminal.activity.TransactionsActivity;
import uk.co.transaxiom.acquirer.services.lw.AppletMessageLW;
import uk.co.transaxiom.acquirer.services.lw.ProfileLW;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.BaseActivity;
import uk.co.transaxiom.android.txandroidlib.ConnectionWrapper;
import uk.co.transaxiom.android.txandroidlib.LWTranslator;
import uk.co.transaxiom.android.txandroidlib.NFCActivity;
import uk.co.transaxiom.android.txandroidlib.RestServicesWrapper;
import uk.co.transaxiom.android.txandroidlib.TxTheme;
import uk.co.transaxiom.android.txandroidlib.terminal.AndroidTerminalWrapper;
import uk.co.transaxiom.android.txandroidlib.terminal.AppletMessage;
import uk.co.transaxiom.android.txandroidlib.terminal.Operations;
import uk.co.transaxiom.android.txandroidlib.terminal.RedeemedValue;
import uk.co.transaxiom.android.txandroidlib.terminal.RequestedNCounter;

import uk.co.transaxiom.services.errors.lw.TxExceptionLW;
import uk.co.transaxiom.services.lw.ThemeCatalogueLW;
import uk.co.transaxiom.services.lw.ThemeLW;
import uk.co.transaxiom.terminal.TerminalProfile;
import uk.co.transaxiom.terminal.TxTerminal;
import uk.co.transaxiom.terminal.common.utils.BinaryUtils;
import uk.co.transaxiom.terminal.ncounters.NCountersBatch;
import uk.co.transaxiom.terminal.ncounters.TXnCounter;
import uk.co.transaxiom.terminal.payment.applet.Errors;
import uk.co.transaxiom.terminal.payment.entity.PaymentResult;
import uk.co.transaxiom.terminal.payment.entity.Purse;
import uk.co.transaxiom.terminal.payment.entity.TxTransaction;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class AsyncClass extends AsyncTask<Operations, String, Boolean> {	
    /** progress dialog to show user that the backup is processing. */
    private ProgressDialog dialog;
    long start = 0;
    long finish = 0;

    TxTheme theme = new TxTheme();
    
    String loadsText="";
    /** The activity launching this thread */
    private Activity parentActivity;
    private IsoDep currentCard;
    private Operations operation;
	public boolean payment_success;
	
	ConnectionWrapper client;
	boolean success = false;
	boolean internet_ok = false;
	public boolean operationDone = false;
	
	private boolean operationSuccess = false;
	private boolean redemptionCompletelySuccessful = false;
	
    public boolean isOperationDone() {
		return operationDone;
	}
    
    private String responseFromServer = "";
    private int responseCodeFromServer = 0;
    
    Purse purse;
    ProfileLW profileLW; 
    List<RequestedNCounter> nCountersToRequest = new ArrayList<RequestedNCounter>();
    
    private List<NCountersBatch> availableNCounters = new ArrayList<NCountersBatch>();
    private RedeemedValue redeemedValue = new RedeemedValue();
	private String errorMsg;
    
	public AsyncClass(Activity activity, Operations operation) {
    	operationDone = false;
        this.parentActivity = activity; 
        this.operation = operation;
        
        availableNCounters = new ArrayList<NCountersBatch>();
        dialog = new ProgressDialog(this.parentActivity); 
    }
    
    //runs on the UI thread
    protected void onPreExecute() {
            //launch the progress dialog on the UI thread;
            //it will run until we tell it to stop
    	start = System.currentTimeMillis();
        this.dialog.setMessage("Loading...");
        this.dialog.show();
    }
   
	protected void onProgressUpdate(String... progress) {
        //you need to cast the params to the type you want!
	    String tmpStr = (String)progress[0];
	        //now update the progress dialog
	    dialog.setMessage(tmpStr);
	    Log.v("HandleApplet", tmpStr);
	}
	
	protected void onPostExecute(Boolean result) {
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		RestServicesWrapper restWrapper = RestServicesWrapper.getInstance();
		
		finish = System.currentTimeMillis();
		operationDone = true;
		Log.v("HandleApplet", "on post execute started");
		if(result)
		{
			
			switch (operation){
				
			case GET_MESSAGES_AFTER:
				if(this.dialog.isShowing()){
					dialog.dismiss();
				}
				if(operationSuccess){
					if(parentActivity instanceof AdminInterfaceActivity){
			        	((AdminInterfaceActivity)parentActivity).uploadTransactionsIfSelected();
			        } else if(parentActivity instanceof PaymentActivity){
			        	((PaymentActivity)parentActivity).uploadTransactions();
			        }
				}
				
				break;
			
				case READ_PROFILE:
					if(operationSuccess){
						((RegisterActivity) parentActivity).getNCounters(profileLW);
					}
					else {
						TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
						if(exception != null && !exception.getLocalizedMessage().isEmpty()){
							errorMsg = exception.getLocalizedMessage();
							Toast.makeText(parentActivity, "ERROR: " + errorMsg, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(parentActivity, "Connection has failed... Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
						}
					}
				break;
			
				case GET_NUMBER_TERMINALS_FILTERED:
					if(operationSuccess){
						((ActivationActivity) parentActivity).parseResponseIntoTerminals(responseFromServer);
					}
					else if(((ActivationActivity)parentActivity).getTotalNumberOfTerminals() == 0){
						Toast.makeText(parentActivity, "You have no unregistered terminals... Please create a new one.", Toast.LENGTH_LONG).show();
						((ActivationActivity) parentActivity).closeActivity();
					}
					else {
						TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
						if(exception != null && !exception.getLocalizedMessage().isEmpty()){
							errorMsg = exception.getLocalizedMessage();
							Toast.makeText(parentActivity, "ERROR: " + errorMsg, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(parentActivity, "Connection has failed... Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
						}
						((ActivationActivity) parentActivity).closeActivity();
					}
					break;
			
				case GET_TERMINALS_FILTERED:
					if(operationSuccess){
						((ActivationActivity) parentActivity).parseResponseIntoTerminals(responseFromServer);
					}else{
						TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
						if(exception != null && !exception.getLocalizedMessage().isEmpty()) {
							errorMsg = exception.getLocalizedMessage();
							Toast.makeText(parentActivity, "ERROR: " + errorMsg, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(parentActivity, "Connection has failed... Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
						}
						((ActivationActivity) parentActivity).closeActivity();
					}
					break;
					
				case UPDATE_TERMINAL:
					if(operationSuccess){
						((ActivationActivity) parentActivity).activateTerminal(responseFromServer);
					}else{
						TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
						if(exception != null && !exception.getLocalizedMessage().isEmpty()){
							errorMsg = exception.getLocalizedMessage();
							Toast.makeText(parentActivity, "ERROR: " + errorMsg, Toast.LENGTH_LONG).show();
							((ActivationActivity) parentActivity).failToUpdate();
						}else{
							Toast.makeText(parentActivity, "Connection has failed... Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
							((ActivationActivity) parentActivity).closeActivity();
						}
					}
					break;
					
				case DELETE_TERMINAL:
					if(operationSuccess){
						((AdminInterfaceActivity)parentActivity).resetTerminalConfirmed();
					}
					else{
						TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
						if(exception != null && !exception.getLocalizedMessage().isEmpty()){
							errorMsg = exception.getLocalizedMessage();
							Toast.makeText(parentActivity, "ERROR: " + errorMsg, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(parentActivity, "Connection has failed... Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
						}
					}
					
					
					break;
					
				case CREATE_TERMINAL:
					Log.v("HandleApplet", "Post execute CREATE_TERMINAL");
					if (operationSuccess) {
						Log.v("HandleApplet", "Successful!");
						((RegisterActivity) parentActivity).saveTerminalTag(responseFromServer);
					} 
					else{
						TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
						if(exception != null && !exception.getLocalizedMessage().isEmpty()){
							errorMsg = exception.getLocalizedMessage();
							Toast.makeText(parentActivity, "ERROR: " + errorMsg, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(
									parentActivity,
									"Connection has failed... Please check your internet connection and try again.",
									Toast.LENGTH_LONG).show();
						}
					}
					break;
				
				case READ_MERCHANT:
						Log.v("HandleApplet", "Post execute CREATE_TERMINAL");
						if(operationSuccess){
							Log.v("HandleApplet", "Successful!");
							((RegisterActivity) parentActivity).saveMerchantUID(responseFromServer);
						}
						else{
							TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
							if(exception != null && !exception.getLocalizedMessage().isEmpty()){
								errorMsg = exception.getLocalizedMessage();
								Toast.makeText(parentActivity, "ERROR: "+errorMsg, Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(parentActivity, "Connection has failed... Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
							}
						}
					break;
					
				case GET_NCOUNTERS:
					Log.v("HandleApplet", "Post execute GET_NCOUNTERS");
						if(operationSuccess){
							
							
							if(parentActivity instanceof RegisterActivity){
								boolean savingResult = wrapper.saveNCountersFromServer(
										parentActivity, 
										((RegisterActivity)parentActivity).getRequestedNCounters(), 
										responseFromServer);
								((RegisterActivity) parentActivity).saveNCounters(savingResult);
							}
							if(parentActivity instanceof MainMenuActivity){
								boolean savingResult = wrapper.saveNCountersFromServer(
										parentActivity, 
										((MainMenuActivity)parentActivity).getRequestedNCounters(), 
										responseFromServer);
								Toast.makeText(parentActivity, "Terminal fixed and ready to be receive payment again!", Toast.LENGTH_SHORT).show();
							}
						}
						else{
							TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
							if(exception != null && !exception.getLocalizedMessage().isEmpty()){
								errorMsg = exception.getLocalizedMessage();
								Toast.makeText(parentActivity, "ERROR: "+errorMsg, Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(parentActivity, "Connection has failed... Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
							}
						}
					break;
					
				case REDEEM_NCOUNTERS:
						if(redemptionCompletelySuccessful){
							if(parentActivity instanceof AdminInterfaceActivity){
					        	((AdminInterfaceActivity)parentActivity).saveLatestRedemption(redeemedValue);
					        	
					        } else if(parentActivity instanceof PaymentActivity){
					        	((PaymentActivity)parentActivity).saveLatestRedemption(redeemedValue);
					        }
						}
						else{
							TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
							if(exception != null && !exception.getLocalizedMessage().isEmpty()){
								saveFailedRedemptionWithException(exception);
								errorMsg = exception.getLocalizedMessage();
								Toast.makeText(parentActivity, "Redemption failed... ERROR: "+errorMsg, Toast.LENGTH_LONG).show();
							}
							else{
								Toast.makeText(parentActivity, "Redemption failed... Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
							}
							saveFailedRedemption();
						}
					break;
					
				case READ_PAYER_FCI:
					
					if(purse != null){
						if(parentActivity instanceof PaymentActivity){
							((PaymentActivity) parentActivity).startPayment();
						}
						else if(parentActivity instanceof MainActivity){
							Toast.makeText(parentActivity, purse.getStringAppletVersion(), Toast.LENGTH_SHORT).show();
							((MainActivity) parentActivity).displayBalance(purse);
						}
					}else{
						Toast.makeText(parentActivity, "Sorry... something went wrong while opening the card.", Toast.LENGTH_SHORT).show();
					}
					break;
					
				case PERFORM_PAYMENT:
					if(payment_success){
						Toast.makeText(parentActivity, "Payment Successful! ", Toast.LENGTH_LONG).show();
						((PaymentActivity) parentActivity).acknowledgePayment();
					}
					else if(errorMsg.equals(Errors.MASSIVE_BREACH)){
						((PaymentActivity) parentActivity).displayMassiveBreachMessage(errorMsg);
						((NFCActivity)parentActivity).playFailSound();
					}
					
					else{
						Toast.makeText(parentActivity, "Payment Failed... "+errorMsg, Toast.LENGTH_LONG).show();
						((NFCActivity)parentActivity).playFailSound();
					}
					break;
					
				case PREPARE_NCOUNTERS:
					if(availableNCounters == null || availableNCounters.isEmpty()){
						((PaymentActivity) parentActivity).rejectNoNCounters();
					}else{
						((PaymentActivity) parentActivity).preparePayment(availableNCounters);
					}
					break;
					
				case UPLOAD_TRANSACTIONS:
					if(parentActivity instanceof TransactionsActivity){
						if(operationSuccess){
							wrapper.updateTransactionsToUploaded(parentActivity, ((TransactionsActivity)parentActivity).getTransactions());
							Toast.makeText(parentActivity, "Transactions successfully uploaded!", Toast.LENGTH_SHORT).show();
							((BaseActivity)parentActivity).refreshScreen();
						}else{
							TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
							if(exception != null && !exception.getLocalizedMessage().isEmpty()){
								errorMsg = exception.getLocalizedMessage();
								Toast.makeText(parentActivity, "ERROR: " + errorMsg, Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(parentActivity, "Connection has failed... Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
							}
						}
					}
					else if(parentActivity instanceof AdminInterfaceActivity){
						if(operationSuccess){
							wrapper.updateTransactionsToUploaded(parentActivity, ((AdminInterfaceActivity)parentActivity).getTransactions());
							Toast.makeText(parentActivity, "Transactions successfully uploaded!", Toast.LENGTH_SHORT).show();
						}
						else{
							TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
							if(exception != null && !exception.getLocalizedMessage().isEmpty()){
								errorMsg = exception.getLocalizedMessage();
								Toast.makeText(parentActivity, "ERROR: " + errorMsg, Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(parentActivity, "Connection has failed... Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
							}
						}
						((AdminInterfaceActivity)parentActivity).refreshScreen();
					}
					
					
					break;
					
				case GET_THEME_CATALOGUE:
					if(operationSuccess){
						ThemeCatalogueLW catalogue = LWTranslator.getInstance().toThemeCatalogueLW(responseFromServer);
						((MainMenuActivity)parentActivity).selectThemeName(catalogue);
					}else{
						TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
						if(exception != null && !exception.getLocalizedMessage().isEmpty()){
							String errorMsg = exception.getLocalizedMessage();
							Toast.makeText(parentActivity, "Error: "+errorMsg, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(parentActivity, "Something went wrong... Please try again!", Toast.LENGTH_LONG).show();
						}
					}
					
					break;
				case GET_THEME:
					if(operationSuccess){
						((MainMenuActivity)parentActivity).saveNewTheme(theme);
					}else{
						TxExceptionLW exception = restWrapper.getTxExceptionFromMessage(responseFromServer);
						if(exception != null && !exception.getLocalizedMessage().isEmpty()){
							String errorMsg = exception.getLocalizedMessage();
							Toast.makeText(parentActivity, "Error: "+errorMsg, Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(parentActivity, "Something went wrong... Please try again!", Toast.LENGTH_LONG).show();
						}
					}
					break;
					
				default:
					break;
					
			}
			
			Log.v("HandleApplet", "SUCCESS!");
		}
		else
		{
			Toast.makeText(parentActivity, "Operation Failed...", Toast.LENGTH_LONG).show();
			Log.v("HandleApplet", "Async operation failed!");
			if(parentActivity instanceof MainMenuActivity){
				parentActivity.finish();
			}
		}
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
	}


	@Override
	protected Boolean doInBackground(Operations... params) {
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		operationDone = false;
		
		Operations currentOp = params[0];
		
		SharedPreferences settings = parentActivity.getSharedPreferences(MainActivity.TERM_DATA, 0);
		RestServicesWrapper restWrapper = RestServicesWrapper.getInstance();
		client = new ConnectionWrapper(parentActivity);
		responseFromServer = "";
		
		String username = settings.getString(PREFERENCES_USERNAME, "N/A");
		String password = settings.getString(PREFERENCES_PASSWORD, "N/A");
		boolean login = false;
		
		try
		{	
			switch (currentOp) {
			
			case GET_MESSAGES_AFTER:
				publishProgress("Loading, please wait...");

				login = restWrapper.loginToAcquirer(client, username, password);
				responseFromServer = client.getResponse();
				responseCodeFromServer = client.getResponseCode();
				
				
				if(login){
					operationSuccess = true;
					TxTerminal terminal = wrapper.getTerminalDetailsFromSharedPreference(settings);
					List<AppletMessage> messagesToUpload = wrapper.getDeliveredMessages(parentActivity);
					if(!messagesToUpload.isEmpty()){
						operationSuccess = restWrapper.requestSyncFinishedMessages(client, terminal, messagesToUpload);
						responseFromServer = client.getResponse();
						if(operationSuccess){
							wrapper.deleteDeliveredMessages(parentActivity);
						}
					}
					if(operationSuccess){
						AppletMessage oldestMessage = wrapper.getOldestMessage(parentActivity);
						operationSuccess = restWrapper.requestGetActiveMessagesAfter(client, oldestMessage.getSeqNumber());
						responseFromServer = client.getResponse();
						
						if(operationSuccess){
							List<AppletMessageLW> newMessagesLW = LWTranslator.getInstance().toAppletMessageLWs(responseFromServer);
							if(!newMessagesLW.isEmpty()){
								List<AppletMessage> appletMessages = LWTranslator.getInstance().fromLWs(newMessagesLW);
								wrapper.saveNewMessagesinDB(parentActivity, appletMessages);
							}
						}
					}
					
					restWrapper.logoutFromAcquirer(client);
				}
				
				break;
			
			
			case READ_PROFILE:
				
				publishProgress("Loading, please wait...");
				login = restWrapper.loginToAcquirer(client, username, password);
				responseFromServer = client.getResponse();
				responseCodeFromServer = client.getResponseCode();
				
				if(login){
					TerminalProfile profile = wrapper.getProfileFromSharedPreferences(settings);
					
					operationSuccess = restWrapper.readProfile(client, profile.getId());
					responseFromServer = client.getResponse();
					responseCodeFromServer = client.getResponseCode();
					
					if(operationSuccess){
						LWTranslator translator = LWTranslator.getInstance();
						JSONObject jsonResponse = new JSONObject(responseFromServer);
						profileLW = translator.toProfileLW(jsonResponse);
						nCountersToRequest = translator.fromLW(profileLW);
						
						restWrapper.logoutFromAcquirer(client);
					}
				}
				
				
				break;
			
				case DELETE_TERMINAL:
					publishProgress("Loading, please wait...");
					login = restWrapper.loginToAcquirer(client, username, password);
					responseFromServer = client.getResponse();
					responseCodeFromServer = client.getResponseCode();
					
					if(login){
						
						boolean redeemed = true;
						TxTerminal terminal = wrapper.getTerminalDetailsFromSharedPreference(settings);
						List<TXnCounter> ncounters = ((AdminInterfaceActivity)parentActivity).getToRedeemNCs();
						
						if(ncounters != null && !(ncounters.isEmpty())){
							
							TerminalProfile profile = wrapper.getProfileFromSharedPreferences(settings);
							
							redeemed = restWrapper.redeemNCounters(client, parentActivity, profile, terminal, ncounters);
							responseFromServer = client.getResponse();
							responseCodeFromServer = client.getResponseCode();
						}
						
						if(redeemed){
							List<TxTransaction> transactionsToUpload = wrapper.getAllTransactionsToUpload(parentActivity);
							
							operationSuccess = true;
							
							if(transactionsToUpload != null && !(transactionsToUpload.isEmpty())){
								operationSuccess = restWrapper.uploadTransactions(client, transactionsToUpload);
								responseFromServer = client.getResponse();
								responseCodeFromServer = client.getResponseCode();
							}
						}
						restWrapper.logoutFromAcquirer(client);
					}
					
				
					break;
			
				case GET_TERMINALS_FILTERED:
					publishProgress("Loading, please wait...");
					login = restWrapper.loginToAcquirer(client, username, password);
					responseFromServer = client.getResponse();
					responseCodeFromServer = client.getResponseCode();
					
					if(login){
						
						int nextIndex = ((ActivationActivity)parentActivity).getNextTerminalIndex();
						int nbTotal = ((ActivationActivity)parentActivity).getTotalNumberOfTerminals();
						
						operationSuccess = restWrapper.getTerminalsFiltered(client, "deviceId", "", nextIndex, nbTotal);
						responseFromServer = client.getResponse();
						responseCodeFromServer = client.getResponseCode();
						
						restWrapper.logoutFromAcquirer(client);
					}
					
					break;
					
				case GET_NUMBER_TERMINALS_FILTERED:
					publishProgress("Loading, please wait...");
					login = restWrapper.loginToAcquirer(client, username, password);
					responseFromServer = client.getResponse();
					responseCodeFromServer = client.getResponseCode();
					
					if(login){
						boolean getNBTerminals = restWrapper.getNumberOfTerminalFiltered(client, "deviceId", "");
						responseFromServer = client.getResponse();
						responseCodeFromServer = client.getResponseCode();
						
						if(getNBTerminals){
							
							int numberOfAvailableTerminals = parseResponseToGetNumber(client.getResponse());
							((ActivationActivity)parentActivity).setTotalNumberOfTerminals(numberOfAvailableTerminals);
							
							if(numberOfAvailableTerminals > 0){
								
								int nextIndex = ((ActivationActivity)parentActivity).getNextTerminalIndex();
								
								operationSuccess = restWrapper.getTerminalsFiltered(client, "deviceId", "", nextIndex, numberOfAvailableTerminals);
								responseFromServer = client.getResponse();
								responseCodeFromServer = client.getResponseCode();
							}
						}
						restWrapper.logoutFromAcquirer(client);
					}
					
					break;
					
				case UPDATE_TERMINAL:
					publishProgress("Updating terminal, please wait...");
					login = restWrapper.loginToAcquirer(client, username, password);
					responseFromServer = client.getResponse();
					responseCodeFromServer = client.getResponseCode();
					
					if(login){
						
						TxTerminal terminal = wrapper.getTerminalDetailsFromSharedPreference(settings);
						boolean updateTerminal = restWrapper.updateTerminal(client, terminal);
						responseFromServer = client.getResponse();
						responseCodeFromServer = client.getResponseCode();
						
						if(updateTerminal){
							
							List<RequestedNCounter> requestedNCounters = ((ActivationActivity)parentActivity).getRequestedNCounters();
							boolean requestNCounters = restWrapper.getNCounters(client, terminal, requestedNCounters);
							responseFromServer = client.getResponse();
							responseCodeFromServer = client.getResponseCode();
							
							if(requestNCounters){
								
								boolean saveNCounters = wrapper.saveNCountersFromServer(parentActivity, requestedNCounters, client.getResponse());
								if(saveNCounters){
									operationSuccess = restWrapper.readMerchant(client);
									responseFromServer = client.getResponse();
									responseCodeFromServer = client.getResponseCode();
								}
							}
						}
						restWrapper.logoutFromAcquirer(client);
					}
					
					break;
					
				case CREATE_TERMINAL: 
					publishProgress("Logging in...");
					login = restWrapper.loginToAcquirer(client, username, password);
					responseFromServer = client.getResponse();
					responseCodeFromServer = client.getResponseCode();
					
					if(login){
						
						TxTerminal terminal = ((RegisterActivity)parentActivity).getTxTerminal();
						
						operationSuccess = restWrapper.createTerminal(client, terminal);
						responseFromServer = client.getResponse();
						responseCodeFromServer = client.getResponseCode();
						
						restWrapper.logoutFromAcquirer(client);
					}
					break;
					
				case READ_MERCHANT:
					publishProgress("Retrieving your details from server...");
					login = restWrapper.loginToAcquirer(client, username, password);
					responseFromServer = client.getResponse();
					responseCodeFromServer = client.getResponseCode();
					
					if(login){
						
						operationSuccess = restWrapper.readMerchant(client);
						responseFromServer = client.getResponse();
						responseCodeFromServer = client.getResponseCode();
						
						restWrapper.logoutFromAcquirer(client);
					}
					break;
					
				case GET_NCOUNTERS: 
					publishProgress("Installing terminal...");
					login = restWrapper.loginToAcquirer(client, username, password);
					responseFromServer = client.getResponse();
					responseCodeFromServer = client.getResponseCode();
					
					if(login){
						TxTerminal terminal = new TxTerminal();
						List<RequestedNCounter> requestedNCounters = new ArrayList<RequestedNCounter>();
						
						if(parentActivity instanceof RegisterActivity){
							terminal = ((RegisterActivity)parentActivity).getTxTerminal();
							requestedNCounters = ((RegisterActivity)parentActivity).getRequestedNCounters();
						}
						if(parentActivity instanceof MainMenuActivity){
							terminal = wrapper.getTerminalDetailsFromSharedPreference(settings);
							requestedNCounters = ((MainMenuActivity)parentActivity).getRequestedNCounters();
						}
						
						operationSuccess = restWrapper.getNCounters(client, terminal, requestedNCounters);
						responseFromServer = client.getResponse();
						responseCodeFromServer = client.getResponseCode();
						
						restWrapper.logoutFromAcquirer(client);
					}
					break;
					
				case REDEEM_NCOUNTERS:
					publishProgress("Redeeming funds...");
					if(restWrapper.loginToAcquirer(client, username, password)){
						
						TxTerminal terminal = wrapper.getTerminalDetailsFromSharedPreference(settings);
						List<TXnCounter> ncounters = new ArrayList<TXnCounter>();
				        if(parentActivity instanceof AdminInterfaceActivity){
				        	ncounters = ((AdminInterfaceActivity)parentActivity).getToRedeemNCs();
				        }
				        else if(parentActivity instanceof PaymentActivity){
				        	ncounters = ((PaymentActivity)parentActivity).getToRedeemNCs();
				        }
						
						redeemedValue.parseIntoValue(ncounters);
						
						TerminalProfile profile = wrapper.getProfileFromSharedPreferences(settings);
						
						redemptionCompletelySuccessful = restWrapper.redeemNCounters(client, parentActivity, profile, terminal, ncounters);
						responseFromServer = client.getResponse();
						responseCodeFromServer = client.getResponseCode();

						restWrapper.logoutFromAcquirer(client);
					}
					break;
					
				case READ_PAYER_FCI:
					currentCard = ((NFCActivity)parentActivity).getCurrentCard();
					
					purse = wrapper.getCardPurseDetails(currentCard);
					break;
					
				case PERFORM_PAYMENT:
					currentCard = ((PaymentActivity) parentActivity).getCurrentCard();
					publishProgress("Opening card...");
					sendAllAsyncMessagesToApplet();
					
						payment_success = false;
						publishProgress("Making payment from card...");
						
						TxTerminal terminal = wrapper.getTerminalDetailsFromSharedPreference(settings);
						
						PaymentResult paymentResult = wrapper.performPayment(parentActivity, 
								currentCard, 
								((PaymentActivity)parentActivity).getAvailableNCouters(), 
								((PaymentActivity)parentActivity).getAmount(),
								terminal);
						
						errorMsg = paymentResult.getMessage();
						
						if(paymentResult.isSuccess()){
							payment_success = true;
							saveLastTransactionRef(paymentResult.getTransaction());
						}
					break;

				case PREPARE_NCOUNTERS:
					publishProgress("Preparing payment, please wait...");
					availableNCounters = wrapper.preparePayment(parentActivity, ((PaymentActivity)parentActivity).getAmount());
					break;
					
				case UPLOAD_TRANSACTIONS:
					publishProgress("Uploading transactions to server, please wait...");
					if(restWrapper.loginToAcquirer(client, username, password)){
						
						List<TxTransaction> transactions = new ArrayList<TxTransaction>();
						if(parentActivity instanceof TransactionsActivity){
							transactions = ((TransactionsActivity)parentActivity).getTransactions();
						}else if(parentActivity instanceof AdminInterfaceActivity){
							transactions = ((AdminInterfaceActivity)parentActivity).getTransactions();
						}else if(parentActivity instanceof PaymentActivity){
							transactions = ((PaymentActivity)parentActivity).getTransactions();
						}
						
						operationSuccess = restWrapper.uploadTransactions(client, transactions);
						responseFromServer = client.getResponse();
						responseCodeFromServer = client.getResponseCode();

						restWrapper.logoutFromAcquirer(client);
					}
					
					break;
					
				case GET_THEME_CATALOGUE:
					publishProgress("Loading theme catalogue...");
					operationSuccess = restWrapper.requestGetThemeCatalogAcquirer(client);
					responseFromServer = client.getResponse();
					responseCodeFromServer = client.getResponseCode();
					
					break;
					
				case GET_THEME:
					publishProgress("Loading theme, please wait...");
					String themeName = ((MainMenuActivity)parentActivity).getThemeName();
					
					operationSuccess = restWrapper.requestGetThemeAcquirer(client, themeName);
					responseFromServer = client.getResponse();
					responseCodeFromServer = client.getResponseCode();
					if(operationSuccess){
						ThemeLW themeLW = LWTranslator.getInstance().toThemeLW(responseFromServer);
						String iconUrl = Params.URL_PREFIX + Params.DOMAIN +"ui/javax.faces.resource/"+ themeLW.getIconURL()+"?ln=frags";
						
						Bitmap bmp = BitmapFactory.decodeStream((InputStream) new URL(iconUrl).getContent());
						
						String location = Environment.getExternalStorageDirectory().getAbsolutePath()+"/terminalApp/";
						String fileName = "logo.png";
						
						Log.d("DOWNLOADINGIMAGE", "path:"+location);
						AndroidWrapper.getInstance().saveBitmapToLocation(bmp, location, fileName);
						theme = LWTranslator.getInstance().fromLW(themeLW);
						theme.setLogoLocation(location+fileName);
					}
					
					break;
					
				default:
					Log.v("HandleApplet", "Unknown command");
					break;
			}
	    }
				    	
		catch (Exception e)
		{
			Log.v("HandleApplet", "Exception caught 111: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}  
	

	private void sendAllAsyncMessagesToApplet() {
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		Purse purse = wrapper.getCardPurseDetails(currentCard);
		String asn = purse.getStringAppletSerialNumber();
		String appletAsn = asn.replace("-", "");
		List<AppletMessage> asyncMessages = wrapper.getMessagesForASN(parentActivity, appletAsn);
		
		for (AppletMessage appletMessage : asyncMessages) {
			try {
				byte[] response = currentCard.transceive(appletMessage.getCommandApduBytes());
				appletMessage.setResponseApdu(BinaryUtils.encode(response));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!asyncMessages.isEmpty()){
			wrapper.updateMessagesInDb(parentActivity, asyncMessages);
		}		
	}

	private void saveLastTransactionRef(TxTransaction transaction) {
		SharedPreferences settings = parentActivity.getSharedPreferences(MainActivity.TERM_DATA, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PREFERENCES_LAST_TRANSACTION_REF, transaction.getReference());
		editor.commit();
		
	}

	private int parseResponseToGetNumber(String response) throws JSONException {
		JSONObject json = new JSONObject(response);
		int number = json.getInt(JSON_VALUE);
		return number;
	}
	
	private void saveFailedRedemption(){
		if (parentActivity instanceof MainActivity) {
			((MainActivity) parentActivity).failedRedemption();
		} else if (parentActivity instanceof PaymentActivity) {
			((PaymentActivity) parentActivity).failedRedemption();
			parentActivity.finish();
		}
	}
	
	private void saveFailedRedemptionWithException(TxExceptionLW exception){
		if (parentActivity instanceof AdminInterfaceActivity) {
			if(exception.getCodeAsString().equals("1012")){
				((AdminInterfaceActivity) parentActivity).promptUserForNewPassword();
			}else{
				((AdminInterfaceActivity) parentActivity).failedRedemption();
			}
		} else if (parentActivity instanceof PaymentActivity) {
			if(exception.getCodeAsString().equals("1012")){
				((PaymentActivity) parentActivity).promptUserForNewPassword();
			}else{
				((PaymentActivity) parentActivity).failedRedemption();
				parentActivity.finish();
			}
		}
	}
	
}
