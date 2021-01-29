package uk.co.transaxiom.android.txandroidlib.terminal;

import static uk.co.transaxiom.android.txandroidlib.Constants.FLAVOUR_DEBUG;
import static uk.co.transaxiom.android.txandroidlib.Constants.FLAVOUR_RELEASE;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_LAST_TRANSACTION_REF;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_MERCHANT_UID;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_PROFILE_ID;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_PROFILE_UPDATE_TIMESTAMP;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_AUTOREDEEM;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_LOCATION;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_TAG;
import static uk.co.transaxiom.android.txandroidlib.Constants.PREFERENCES_TERMINAL_TYPE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.transaxiom.acquirer.services.lw.NCounterBatchLW;
import uk.co.transaxiom.acquirer.services.lw.NCounterLW;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.LWTranslator;
import uk.co.transaxiom.android.txandroidlib.Utils;
import uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu.ResponseAPDU;
import uk.co.transaxiom.terminal.TerminalProfile;
import uk.co.transaxiom.terminal.TerminalWrapper;
import uk.co.transaxiom.terminal.TxTerminal;
import uk.co.transaxiom.terminal.common.utils.BinaryUtils;
import uk.co.transaxiom.terminal.ncounters.NCountersBatch;
import uk.co.transaxiom.terminal.ncounters.TXnCounter;
import uk.co.transaxiom.terminal.payment.entity.Amount;
import uk.co.transaxiom.terminal.payment.entity.FileControlInformation;
import uk.co.transaxiom.terminal.payment.entity.PaymentCommand;
import uk.co.transaxiom.terminal.payment.entity.PaymentResult;
import uk.co.transaxiom.terminal.payment.entity.Purse;
import uk.co.transaxiom.terminal.payment.entity.TxTransaction;
import uk.transaxiom.android.txandroidlib.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.nfc.tech.IsoDep;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AndroidTerminalWrapper {

	private static AndroidTerminalWrapper instance = new AndroidTerminalWrapper();

	private AndroidTerminalWrapper() {
	}

	public static AndroidTerminalWrapper getInstance() {

		return instance;
		
	}
	
	/**
	 * Delete all the n-Counters from the database
	 * 
	 * @param parentActivity: the activity calling this method
	 */
	public void wipeNCountersDatabase(Activity parentActivity) {
		DBTXTerminalLib db = new DBTXTerminalLib(parentActivity);
		db.open();
		db.wipeNCounters();
		db.close();
	}
	
	/**
	 * Delete all records of redeemed values from the database
	 * 
	 * @param activity: the activity calling this method
	 * @return Boolean: true or false whether the operation was successful
	 */
	public boolean wipeRedeemedValuesDatabase(Activity activity){
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		List<RedeemedValue> redeemedValues = db.getAllRedeemedValues();
		
		if(redeemedValues.isEmpty()){
			return false;
		}
		db.wipeRedeemedValues();
		db.close();
		
		return true;
	}
	
	/**
	 * Delete all records of transactions from the database
	 * 
	 * @param activity: the activity calling this method
	 * @return Boolean: true or false whether the operation was successful
	 */
	public boolean wipeTransactionsDatabase(Activity activity){
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		List<TxTransaction> transactions = db.getAllTxTransactions();
		
		if(transactions.isEmpty()){
			return false;
		}
		db.wipeTransactions();
		db.close();
		
		return true;
	}
	
	/**
	 * Reset the terminal as a freshly installed terminal
	 * 
	 * @param activity: the activity calling this method
	 */
	public void deleteTerminalDetails(Activity activity){
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		db.wipeRedeemedValues();
		db.wipeTransactions();
		db.wipeRedemptions();
		db.close();
	}
	
	public void getAllMessages(Context context){
		DBTXTerminalLib db = new DBTXTerminalLib(context);
		
		db.open();
		List<AppletMessage> messages = db.getAllMessagesDB();
		db.close();
		
		for (AppletMessage appletMessage : messages) {
			Log.i("DbTestingPurposesMessages", appletMessage.toString());
		}
	}
	
	public List<AppletMessage> getDeliveredMessages(Context context){
		DBTXTerminalLib db = new DBTXTerminalLib(context);
		
		db.open();
		List<AppletMessage> messages = db.getDeliveredMessages();
		db.close();
		
		return messages;
	}
	
	public List<AppletMessage> getMessagesForASN(Context context, String appletSerialNumber){
		DBTXTerminalLib db = new DBTXTerminalLib(context);
		
		db.open();
		List<AppletMessage> messages = db.getMessagesFor(appletSerialNumber);
		db.close();
		
		return messages;
	}
	
	public AppletMessage getOldestMessage(Context context){
		DBTXTerminalLib db = new DBTXTerminalLib(context);
		
		db.open();
		AppletMessage message = db.getOldestMessage();
		db.close();
		
		return message;
	}

	public void saveNewMessagesinDB(Context context, List<AppletMessage> appletMessages){
		DBTXTerminalLib db = new DBTXTerminalLib(context);
		
		db.open();
		db.deleteAllMessages();
		db.insertAppletMessages(appletMessages);
		db.close();
	}
	
	public void updateMessagesInDb(Context context, List<AppletMessage> appletMessages){
		DBTXTerminalLib db = new DBTXTerminalLib(context);
		
		db.open();
		db.updateAppletMessages(appletMessages);
		db.close();
	}
	
	public void deleteDeliveredMessages(Context context){
		DBTXTerminalLib db = new DBTXTerminalLib(context);
		
		db.open();
		db.deleteAllDeliveredMessages();
		db.close();
	}
	
	public void wipeMessagesDB(Context context){
		DBTXTerminalLib db = new DBTXTerminalLib(context);
		
		db.open();
		db.wipeAppletMessages();
		db.close();
	}
	
	/**
	 * Display the terminal's details in a pop-up box
	 * 
	 * @param activity: the activity calling this method and displaying the pop-up box
	 * @param settings: the SharedPreferences object containing the details to be displayed
	 */
	public void displayTerminalInfo(Activity activity, SharedPreferences settings){
		LayoutInflater factory = LayoutInflater.from(activity);
		final View merchantInfoView = factory.inflate(R.layout.display_merchant_info, null);
		
		TextView merchantUIDTxtVw = (TextView) merchantInfoView.findViewById(R.id.merchantUIDTxtVw);
		TextView merchantUsernameTxtVw = (TextView) merchantInfoView.findViewById(R.id.merchantUsernameTxtVw);
		TextView terminalUIDTxtVw = (TextView) merchantInfoView.findViewById(R.id.terminalUIDTxtVw);
		TextView terminalTagTxtVw = (TextView) merchantInfoView.findViewById(R.id.terminalTagTxtVw);
		
		String username = AndroidWrapper.getInstance().getUsernameFromSharedPreferences(settings);
		TxTerminal terminal = getTerminalDetailsFromSharedPreference(settings);
		
		merchantUIDTxtVw.setText(terminal.getMerchantUID());
		merchantUsernameTxtVw.setText(username);
		terminalUIDTxtVw.setText(terminal.getTerminalTag());
		terminalTagTxtVw.setText(terminal.getTerminalType());
		
		AlertDialog dialogBox = AndroidWrapper.getInstance().createAlertDialog(activity, "About this terminal:", merchantInfoView);
		dialogBox.show();
	}

	/**
	 * Prepare for the payment by splitting the amount between the available batch of n-Counters
	 * 
	 * @param parentActivity: the activity calling this method
	 * @param amount: the amount as a string without the currency
	 * 
	 * @return List<NCountersBatch>: all the available batches of n-Counters for this payment
	 */
	public List<NCountersBatch> preparePayment(Activity parentActivity, String amount){
		
		TerminalWrapper wrapper = TerminalWrapper.getInstance();
		
		List<TXnCounter> allNCounters = getAllMyNCounters(parentActivity);
		List<NCountersBatch> availableBatches = wrapper.preparePayment(allNCounters, amount);
		
		String log = "==== PREPARE PAYMENT ==== Amount = "+amount+" - nCounters available: ";
		for(int i=0; i<availableBatches.size(); i++){
			log += availableBatches.get(i).toString()+" - ";
		}
		
		AndroidWrapper.getInstance().writeLogInFile(FLAVOUR_DEBUG, "/Transaxiom/TerminalLog/", "log.txt", log);
		
		return availableBatches;	
	}
	
	/**
	 * Get the details of the main Tx Purse from the card
	 * 
	 * @param card: the current card
	 * 
	 * @return Purse: containing all the details
	 */
	public Purse getCardPurseDetails(IsoDep card){
		Purse cardPurse = new Purse();
		
		try {
			if(!card.isConnected())
				card.connect();
			card.setTimeout(5000);
			
			cardPurse = readFCIFromCard(card);
		}catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return cardPurse;
	}
	
	/**
	 * Get the maximum amount of funds that can be accumulated on the terminal for a specific currency
	 * 
	 * @param parentActivity: the activity calling this method
	 * @param currency: the currency for which to get the max turnover
	 * 
	 * @return Max Turnover in pennies as an int
	 */
	public int getIntMaxTurnover(Activity parentActivity, String currency){
		
		int total = 0;
		
		DBTXTerminalLib db = new DBTXTerminalLib(parentActivity);
		db.open();
		NCountersBatch nCounters = db.getAllnCounters(currency);
		db.close();
		
		for(TXnCounter nCounter : nCounters){
			total += nCounter.getMerchantStepVal() * nCounter.getnCLength();
		}
		
		return total;
	}
	
	/**
	 * Get the maximum amount of funds that can be accumulated on the terminal for a specific currency
	 * 
	 * @param parentActivity: the activity calling this method
	 * @param currency: the currency for which to get the max turnover
	 * 
	 * @return Max turnover as a formatted string with currency
	 */
	public String getMaxTurnover(Activity parentActivity, String currency){
		
		int intTurnover = getIntMaxTurnover(parentActivity, currency);
		
		Amount amount = new Amount();
		amount.setCurrency(currency);
		amount.setValue(intTurnover);
		
		return amount.getFormattedValue();
	}
	
	/**
	 * Get the details of a specific Tx Purse from the card
	 * 
	 * @param card: the current card
	 * @param infix: the purse number to open
	 * 
	 * @return Purse containing all the details of the purse
	 */
	public Purse getCardSpecificPurseDetails(IsoDep card, byte infix){
		Purse cardPurse = new Purse();
		
		try {
			if(!card.isConnected())
				card.connect();
			card.setTimeout(5000);
			
			cardPurse = readSpecificFCIFromCard(card, infix);
		}catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return cardPurse;
	}
	
	
	/**
	 * Perform a payment on a card 
	 * 
	 * @param parentActivity: the activity where the payment is happening
	 * @param card: the current card
	 * @param availableNCounters: the available batchs of n-Counters for this payment (retrieved from perparePayment method)
	 * @param amount: the amount to be paid as a string without the currency
	 * @param terminal: the details of the terminal performing the payment
	 * 
	 * @return PaymentResult containing the result of the payment and the error message if any
	 */
	public PaymentResult performPayment(Activity parentActivity, IsoDep card, List<NCountersBatch> availableNCounters, String amount, TxTerminal terminal){
		long start = System.currentTimeMillis();
		PaymentResult result = new PaymentResult();
		TerminalWrapper wrapper = TerminalWrapper.getInstance();
		AndroidWrapper androidWrapper = AndroidWrapper.getInstance();
		try {
			if(!card.isConnected())
				card.connect();
			card.setTimeout(5000);
			
			Purse cardPurse = getCardPurseDetails(card);
			String log = "PERFORM PAYMENT - Terminal="+terminal.getTerminalTag()+" - Amount="+amount
					+" - Purse= [asn="+cardPurse.getStringAppletSerialNumber()+"/batchId="+BinaryUtils.encode(cardPurse.getBatchId())+"]";
			
			androidWrapper.writeLogInFile(FLAVOUR_DEBUG, "/Transaxiom/TerminalLog/", "log.txt", log);
			
			List<TXnCounter> nCounters = wrapper.selectCompatibleNCounters(availableNCounters, cardPurse);
			if(nCounters.isEmpty()){
				result.setSuccess(false);
				result.setMessage("Incompatible card.");
				log += "\n===== INCOMPATIBLE CARD ====";
				androidWrapper.writeLogInFile(FLAVOUR_RELEASE, "/Transaxiom/TerminalLog/", "log.txt", log);
				return result;
			}
			
			PaymentCommand paymentCommand = wrapper.getPaymentAPDU(cardPurse, terminal.getMerchantUIDFormatted(), nCounters, amount);
			
			if(paymentCommand.getFinalNCounters() == null || paymentCommand.getFinalNCounters().isEmpty()){
				if(paymentCommand.getPaymentAPDU() == null || paymentCommand.getPaymentAPDU().isEmpty()){
					log += "\n===== UNKNOWN ERROR ====";
					result.setMessage("UNKNOWN ERROR");
				}else{
					log += "\n===== "+paymentCommand.getPaymentAPDU()+" ====";
					result.setMessage(paymentCommand.getPaymentAPDU());
				}
				result.setSuccess(false);
				androidWrapper.writeLogInFile(FLAVOUR_RELEASE, "/Transaxiom/TerminalLog/", "log.txt", log);
				return result;
			}
			
			log += "\nnCounters=[";
			for(int i=0; i<paymentCommand.getFinalNCounters().size(); i++){
				log += BinaryUtils.encode(paymentCommand.getFinalNCounters().get(i).getnCXn())+" - "+paymentCommand.getFinalNCounters().get(i).getSteps()+"steps; ";
			}
			log += "]\n";
			Log.d("AndroidTerminalWrapper", "Payment APDU = "+paymentCommand.getPaymentAPDU()+"--"+paymentCommand.getPaymentAPDU().length());
			byte[] bytesCmd = BinaryUtils.decode(paymentCommand.getPaymentAPDU());
			
			if(!card.isConnected())
				card.connect();
			
			long temp = System.currentTimeMillis();
			
			log+="Payment command APDU = " +BinaryUtils.encode(bytesCmd) + "\n";
			byte[] paymentResponse = card.transceive(bytesCmd);
			Log.d("AndroidTerminalWrapper", "Payment response APDU = "+BinaryUtils.encode(paymentResponse));
			log+="Payment response APDU = " +BinaryUtils.encode(paymentResponse) + "\n";

			PaymentResult paymentResult = wrapper.validatePayment(BinaryUtils.encode(paymentResponse), paymentCommand.getFinalNCounters(), 
					amount, terminal, cardPurse);
			
			
			long finish = System.currentTimeMillis();
			long paymentTime = finish-temp;
			log += "Payment result = "+paymentResult.getMessage() + " " + (finish-start) + "("+paymentTime+")"+ "ms";
			
			
			androidWrapper.writeLogInFile(FLAVOUR_DEBUG, "/Transaxiom/TerminalLog/", "log.txt", log);
			result.setMessage(paymentResult.getMessage());
			
			if(paymentResult.isSuccess()){
//				((NFCActivity)parentActivity).playSuccessSound();
				result.setSuccess(true);
				result.setTransaction(paymentResult.getTransaction());
				updateNCountersDB(parentActivity, paymentResult.getUpdatedNCounters());
				saveTransactionInDB(parentActivity, paymentResult.getTransaction());
			}
			
			return result;
			
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("PerformPaymentRequest", e.getMessage());
			return null;
		}
	}
	

	/**
	 * Retrieve all transactions record from the database
	 * 
	 * @param activity: the activity calling this method
	 * @return List of Transactions
	 */
	public List<TxTransaction> getAllTransactions(Activity activity){
		
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		List<TxTransaction> transactions = db.getAllTxTransactions();
		db.close();
		
		return transactions;
	}
	
	/**
	 * Retrieve all transactions that haven't been uploaded to the server yet
	 * 
	 * @param activity: the activity calling this method
	 * @return List of Transactions
	 */
	public List<TxTransaction> getAllTransactionsToUpload(Activity activity){
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		List<TxTransaction> transactions = db.getNotUploadedTransactions();		
		db.close();
		
		return transactions;
	}
	
	
	/**
	 * Retrieve the terminal's details from the shared preferences 
	 * 
	 * @param settings: the shared preferences containing all the terminal's details
	 * @return Terminal with all its details
	 */
	public TxTerminal getTerminalDetailsFromSharedPreference(SharedPreferences settings){
		
		TxTerminal terminal = new TxTerminal();
		
		terminal.setTerminalType(settings.getString(PREFERENCES_TERMINAL_TYPE, "N/A"));
		terminal.setTerminalTag(settings.getString(PREFERENCES_TERMINAL_TAG, "N/A"));
		terminal.setTerminalLocation(settings.getString(PREFERENCES_TERMINAL_LOCATION, "N/A"));
//		terminal.setTerminalDeviceId(settings.getString(PREFERENCES_TERMINAL_DEVICEID, null));
		terminal.setMerchantUID(settings.getString(PREFERENCES_MERCHANT_UID, "N/A"));
		terminal.setActive(true);
		terminal.setLastTransasctionRef(settings.getInt(PREFERENCES_LAST_TRANSACTION_REF, 0));
		
		return terminal;
	}
	
	/**
	 * Retrieve the terminal's profile details from the shared preferences
	 * 
	 * @param settings: the shared preferences containing all the terminal's details
	 * @return TerminalProfile containing the Id of the profile and the timestamp of the last update
	 */
	public TerminalProfile getProfileFromSharedPreferences(SharedPreferences settings){
		
		TerminalProfile profile = new TerminalProfile();
		
		profile.setId(settings.getString(PREFERENCES_PROFILE_ID, "N/A"));
		profile.setLastUpdate(settings.getLong(PREFERENCES_PROFILE_UPDATE_TIMESTAMP, 0));
		
		return profile;
	}
	
	/**
	 * Set transactions in the database to "uploaded" 
	 * 
	 * @param activity: the activity calling this method
	 * @param transactions: the transactions uploaded to the server
	 */
	public void updateTransactionsToUploaded(Activity activity, List<TxTransaction> transactions){
		
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		db.updateTransactionsToUploaded(transactions);
		db.close();
	}
	
	/**
	 * Create an n-Counter to be requested from the server
	 * 
	 * @param merchantCurrency: the currency of the n-Counter
	 * @param merchantStepValue: the step value of the n-Counter
	 * @param length: the length of the n-Counter (total number of steps)
	 * @param narrative: the narrative of the n-Counter
	 * 
	 * @return RequestedNCounter object to be sent to the server
	 */
	public RequestedNCounter createRequestedNCounterBCD(String merchantCurrency, int merchantStepValue, short length, String narrative){
		byte[] merchantUnit = new byte[6];
		try {
			System.arraycopy(merchantCurrency.getBytes("US_ASCII"), 0, merchantUnit, 0, 3);
	        System.arraycopy(Utils.intToBCD3BytesArray(merchantStepValue), 0, merchantUnit, 3, 3);
	        byte[] byteNarrative = narrative.getBytes("UTF-8"); 
	        return new RequestedNCounter(merchantUnit,length, byteNarrative);
		} catch (UnsupportedEncodingException e) {
			Log.v("GenerateNCounter", "generatenCounter failed: " + e.getMessage());
			return null;
		}
	}
	
	public RequestedNCounter createRequestedNCounterLong(String merchantCurrency, long merchantStepValue, short length, String narrative){
		byte[] merchantUnit = new byte[11];
		try {
			System.arraycopy(merchantCurrency.getBytes("US_ASCII"), 0, merchantUnit, 0, 3);
	        System.arraycopy(BinaryUtils.getLongAs64Bits(merchantStepValue), 0, merchantUnit, 3, 8);
	        byte[] byteNarrative = narrative.getBytes("UTF-8"); 
	        return new RequestedNCounter(merchantUnit,length, byteNarrative);
		} catch (UnsupportedEncodingException e) {
			Log.v("GenerateNCounter", "generatenCounter failed: " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Save the n-Counters received from the server in the terminal's database
	 * 
	 * @param activity: the activity calling this method
	 * @param requestedNCounters: the list of RequestedNCounters sent to the server
	 * @param serverResponse: the response received containing the n-Counters created by the server
	 * 
	 * @return Boolean: true or false whether the operation was successful
	 */
	public boolean saveNCountersFromServer(Activity activity, List<RequestedNCounter> requestedNCounters, String serverResponse){
		boolean result = false;
		
		List<TXnCounter> finalNCounters = mergeNCounters(serverResponse, requestedNCounters);
		result = saveNCountersInDB(activity, finalNCounters);
		
		return result;
	}
	
	/**
	 * Create an n-Counter received from the server in the database
	 * 
	 * @param activity: the activity calling this method
	 * @param ncounter: the n-Counter received from the server
	 * 
	 * @return Boolean: true or false whether the operation was successful
	 */
	private boolean createNCounterInDB(Activity activity, TXnCounter ncounter){
		try{
			DBTXTerminalLib db = new DBTXTerminalLib(activity);
			db.open();			
			long res = db.insertNCounter(ncounter);
			db.close();
			Log.v("CreateNCounter", "n-Counter created in DB @ " + res + ": " + ncounter.getMerchantCurrencyCode() + ncounter.getMerchantStepVal() + "x" + ncounter.getnCLength());
			return true;
		}catch(Exception e){
			Log.e("CreateNCounter", "Exception caught:"+e);
			return false;
		}
	}
	
	/**
	 * Merge the n-Counters received from the server with the requested n-Counters sent to the server
	 * 
	 * The n-Counters received from the server do not contain all the details of an n-Counter. 
	 * These n-Counters have to be merged with the RequestedNCounters sent to the server. 
	 * 
	 * @param nCountersFromServer: the response received from the server
	 * @param requestedNCounters
	 * @return List of n-Counter ready to be saved in the database
	 */
	private List<TXnCounter> mergeNCounters(String nCountersFromServer, List<RequestedNCounter> requestedNCounters) {
		List<TXnCounter> result = new ArrayList<TXnCounter>();
		LWTranslator translator = LWTranslator.getInstance();
		Log.d("MergeNCountersFromServer", "Merging ncounters.......");
		try {
			JSONObject responseJson = new JSONObject(nCountersFromServer);
			
			JSONArray array = responseJson.getJSONArray("item");
			for (int i = 0; i < array.length(); i++) {
				
				NCounterBatchLW batchLW = translator.toNCounterBatchLW(array.getJSONObject(i));
				List<NCounterLW> listNCountersLW = batchLW.getCounters();
				
				for(int j=0; j<listNCountersLW.size(); j++){
					NCounterLW ncounterLW = listNCountersLW.get(j);
					
					ncounterLW.setMerchantUnit(BinaryUtils.encode(requestedNCounters.get(j).getMerchantUnit()));
					ncounterLW.setIssuerUnit(BinaryUtils.encode(requestedNCounters.get(j).getMerchantUnit()));
					ncounterLW.setLength(requestedNCounters.get(j).getLength());
					ncounterLW.setNarrative(new String(requestedNCounters.get(j).getNarrative(), "UTF-8"));
					
					Log.i("MainActivity", "narrative is = "+ncounterLW.getNarrative());
					
					TXnCounter ncounter = translator.fromLW(batchLW.getBatchId(), ncounterLW);
					if(ncounter == null){
						return null;
					}
					result.add(ncounter);
				}
			}
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Save the merged n-Counters to the database
	 * 
	 * @param activity: the activity calling this method
	 * @param fullNCounters: the merged n-Counters and ready to be saved in the database
	 * 
	 * @return Boolean: true or false whether the operation is successfull
	 */
	private boolean saveNCountersInDB(Activity activity, List<TXnCounter> fullNCounters) {
		if(fullNCounters != null){
			for(int i=0; i<fullNCounters.size(); i++){
				createNCounterInDB(activity, fullNCounters.get(i));
			}
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Delete the redeemed n-Counters
	 * After being redeemed an n-Counter can no longer be used as it is marked as inactive on the server
	 * 
	 * @param parentActivity: the activity calling this method
	 * @param ncounters: the redeemed n-Counters
	 * 
	 * @return  Boolean: true or false whether the operation is successful
	 */
	public boolean deleteRedeemedNCounters(Activity parentActivity, List<TXnCounter> ncounters) {
		boolean result = false;
		
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		for(int i=0; i<ncounters.size(); i++){
			int ncID =(ncounters.get(i).getnCId());
			
			result = wrapper.deleteNCounter(parentActivity, ncID);
		}
		return result;
	}
	
	/**
	 * Retrieve all the n-Counters from the database
	 * 
	 * @param activity: the activity calling this method
	 * @return List of n-Counters
	 */
	public List<TXnCounter> getAllTXNcounters(Activity activity){
		List<TXnCounter> allNCounters = new ArrayList<TXnCounter>();
		
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		NCountersBatch listOfNCounters = db.getAllnCounters();
		if(!listOfNCounters.isEmpty()){
			for (int i=0; i<listOfNCounters.size(); i++){
				TXnCounter nCounter = listOfNCounters.get(i);
				allNCounters.add(nCounter);
			}
		}
		db.close();

		if(allNCounters.isEmpty()){
			return null;
		}
		return allNCounters;
	}
	
	
	/**
	 * Parse an n-Counter into a Requested n-Counter to be sent to the server
	 * 
	 * @param nCounter: the n-Counter to be parsed
	 * @return RequestedNCounter to be sent to the server
	 */
	public RequestedNCounter parseIntoRequestedNCounter(TXnCounter nCounter) {
		RequestedNCounter result = new RequestedNCounter(
				nCounter.getnCMerchantUnit(), 
				nCounter.getnCLength(), 
				nCounter.getNarrative());
		
		return result;
	}
	
	/**
	 * Delete a specific n-Counter from the database
	 * 
	 * @param activity: the activity calling this method
	 * @param _id: the Id of the n-Counter in the database
	 * 
	 * @return Boolean: true or false whether the operation is successful
	 */
	private boolean deleteNCounter(Activity activity, int _id){
		try{
			DBTXTerminalLib db = new DBTXTerminalLib(activity);
			db.open();
			
			TXnCounter nCounter = db.getSinglenCounters(_id);
			if(nCounter != null){
				db.deleteNCounter(""+_id);	
				
				Log.v("DeleteNCounter", "OPERATION SUCCESSFULL");
			}
			db.close();
			return true;
		}catch(Exception e){
			Log.e("DeleteNCounter", "Exception caught:"+e);
			return false;
		}
	}
	
	/**
	 * Get all the n-Counters as a map of details to be displayed on the screen
	 * 
	 * @param activity: the activity calling this method
	 * 
	 * @return List of a map containing the n-Counters details
	 */
	public ArrayList<HashMap<String,Object>> getNCountersStatus(Activity activity){
		ArrayList<HashMap<String,Object>> array = new ArrayList<HashMap<String,Object>>(); //arrayList containing the n-counters
		HashMap<String,Object> map ; 
		
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		NCountersBatch listOfNCounters = db.getAllnCounters();
		if(!listOfNCounters.isEmpty()){
			
			for(int i=0; i< listOfNCounters.size(); i++){
				Log.d("GetNCounterStatus", "index = "+i);
				map = new HashMap<String,Object>();
				
				
				map.put("_id", listOfNCounters.get(i).getnCId());
				map.put("issuerKeyID",  Utils.getHexString(listOfNCounters.get(i).getnCIssuerKeyID(), listOfNCounters.get(i).getnCIssuerKeyID().length));
				map.put("seed", Utils.getHexString(listOfNCounters.get(i).getnCSeed(), listOfNCounters.get(i).getnCSeed().length));
				map.put("issuerUnits", Utils.getHexString(listOfNCounters.get(i).getnCIssuerUnit(), listOfNCounters.get(i).getnCIssuerUnit().length));
				map.put("merchantUnits", Utils.getHexString(listOfNCounters.get(i).getnCMerchantUnit(), listOfNCounters.get(i).getnCMerchantUnit().length));
				map.put("merchantCurrency", listOfNCounters.get(i).getMerchantCurrencyCode());
				map.put("length", listOfNCounters.get(i).getnCLength());
				map.put("stepsUsed", listOfNCounters.get(i).getnCStepsUsed());
				String currentVal = Utils.getHexString(listOfNCounters.get(i).getnCValue(), listOfNCounters.get(i).getnCValue().length);
				map.put("currentValue", currentVal);
				map.put("stepsRedeemed", (int) listOfNCounters.get(i).getnCRedeemedSteps());
				
				Log.d("GetNCounterStatus", "id= "+listOfNCounters.get(i).getnCId());
				Log.d("GetNCounterStatus", "issuerID= "+Utils.getHexString(listOfNCounters.get(i).getnCIssuerKeyID(), listOfNCounters.get(i).getnCIssuerKeyID().length));
				Log.d("GetNCounterStatus", "seed= "+Utils.getHexString(listOfNCounters.get(i).getnCSeed(), listOfNCounters.get(i).getnCSeed().length));
				Log.d("GetNCounterStatus", "issuerUnits= "+Utils.getHexString(listOfNCounters.get(i).getnCIssuerUnit(), listOfNCounters.get(i).getnCIssuerUnit().length));
				Log.d("GetNCounterStatus", "merchantUnits= "+Utils.getHexString(listOfNCounters.get(i).getnCMerchantUnit(), listOfNCounters.get(i).getnCMerchantUnit().length));
				Log.d("GetNCounterStatus", "length= "+listOfNCounters.get(i).getnCLength());
				Log.d("GetNCounterStatus", "stepsUsed= "+listOfNCounters.get(i).getnCStepsUsed());
				Log.d("GetNCounterStatus", "listofncounters currentValue= "+currentVal);
				Log.d("GetNCounterStatus", "stepsRedeemed= "+listOfNCounters.get(i).getnCRedeemedSteps());
				
				array.add(map);
			}
		}
		db.close();
		return array;
	}
	
	/**
	 * Retrieve a specific n-Counter from the database
	 * 
	 * @param activity: the activity calling this method
	 * @param rowID: the Id of the n-Counter in the database
	 * 
	 * @return the n-Counter 
	 */
	public TXnCounter getSingleNCounter(Activity activity, int rowID){
		
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		TXnCounter result = db.getSinglenCounters(rowID);
		db.close();
		
		return result;
	}
	
	/**
	 * Retrieve all records of redeemed values from the database
	 * 
	 * @param activity: the activity calling this method
	 * 
	 * @return List of redeemed value
	 */
	public List<RedeemedValue> getRedeemedValues(Activity activity){
		
		List<RedeemedValue> values = new ArrayList<RedeemedValue>();
		
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		values = db.getAllRedeemedValues();
		db.close();
		
		return values;
	}
	
	/**
	 * Retrieve the list of all n-Counters with value on them
	 * 
	 * @param activity: the activity calling this method
	 * 
	 * @return List of n-Counter
	 */
	public ArrayList<TXnCounter> getAllRedeemableTXNcounters(Activity activity){
		ArrayList<TXnCounter> allNCounters = new ArrayList<TXnCounter>();
		
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		//1: get all the n-counters
		NCountersBatch listOfNCounters = db.getAllnCounters();
		if(!listOfNCounters.isEmpty()){
			for (int i=0; i<listOfNCounters.size(); i++){
				TXnCounter nCounter = listOfNCounters.get(i);
				if(nCounter.getnCStepsUsed() > 0){
					allNCounters.add(nCounter);
				}
			}
		}
		if(allNCounters.isEmpty()){
			db.close();
			return null;
		}
		db.close();
		return allNCounters;
	}
	
	
	public List<TXnCounter> getFullNCounters(Activity activity){
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		NCountersBatch listOfNCounters = db.getAllnCounters();
		db.close();
		
		List<TXnCounter> nCountersToRedeem = TerminalWrapper.getInstance().findFullNCounters(listOfNCounters);
		return nCountersToRedeem;
	}
	/**
	 * Save the value that has been redeemed in the database
	 * 
	 * @param activity: the activity calling this method
	 * @param ncountersRedeemed: the list of n-Counters that have been redeemed on the server
	 * 
	 * @return RedeemedValue saved in the database
	 */
	public RedeemedValue saveRedeemedValues(Activity activity, List<TXnCounter> ncountersRedeemed){
		
		RedeemedValue value = new RedeemedValue();
		value.parseIntoValue(ncountersRedeemed);
		
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		db.insertRedeemedValue(value);
		db.close();
		
		return value;
	}
	
	
	/**
	 * Update the n-Counters in the database after a successful payment
	 * 
	 * @param activity: the activity calling this method
	 * @param updatedNCounters: the list of n-Counters used for the last payment
	 */
	private void updateNCountersDB(Activity activity, List<TXnCounter> updatedNCounters) {
		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		if(!updatedNCounters.isEmpty()){
			for(int i=0; i< updatedNCounters.size(); i++){
				if(updatedNCounters.get(i).getSteps() > 0){
					short oldSteps = updatedNCounters.get(i).getnCStepsUsed();
					short stepsToAdd = updatedNCounters.get(i).getSteps();
					byte[] ncValue = updatedNCounters.get(i).getExpectedValue();
					
					short newStepsUsed = (short) (oldSteps + stepsToAdd);
					int newIntLocation = updatedNCounters.get(i).getnCLength() - newStepsUsed;
					updatedNCounters.get(i).setnCLoc(((Integer) newIntLocation).byteValue());
					updatedNCounters.get(i).setnCStepsUsed(newStepsUsed);
					updatedNCounters.get(i).setSteps((short) 0);
					updatedNCounters.get(i).setExpectedValue(new byte[8]);
					
					Log.d("TXTerminalWrapper", "oldValue = "+BinaryUtils.encode(updatedNCounters.get(i).getnCValue()));
					Log.d("TXTerminalWrapper", "newValue = "+BinaryUtils.encode(ncValue));
					updatedNCounters.get(i).setnCValue(ncValue);
					
					db.updateNcounter(updatedNCounters.get(i));
				}
			}
		}
		db.close();
	}
	/**
	 * Save a transaction in the database after a successful payment
	 * 
	 * @param activity: the activity calling this method
	 * @param txTransaction: the transaction to be saved in the database
	 */
	@SuppressLint("SimpleDateFormat")
	private void saveTransactionInDB(Activity activity, TxTransaction txTransaction){

		DBTXTerminalLib db = new DBTXTerminalLib(activity);
		db.open();
		db.insertTransaction(txTransaction);
		db.close();
	}
	
	/**
	 * Open the main purse on the card
	 * 
	 * @param card: the current card 
	 * 
	 * @return Purse containing the details of the main purse
	 * @throws IOException
	 */
	private Purse readFCIFromCard(IsoDep card) throws IOException{
		Purse result = new Purse();
		TerminalWrapper wrapper = TerminalWrapper.getInstance();
		
		String stringCmd = wrapper.getOpenAppletCmd();
		
		byte[] bytesCmd = BinaryUtils.decode(stringCmd);
		Log.d("ANDROID_TERMINAL_WRAPPER", "Send:"+BinaryUtils.encode(bytesCmd));
		byte[] bytesResp = card.transceive(bytesCmd);
		Log.d("ANDROID_TERMINAL_WRAPPER", "Recv:"+BinaryUtils.encode(bytesResp));
		ResponseAPDU response = new ResponseAPDU(bytesResp);
		
		if(response.getErrorCode().isOk()){
			result = wrapper.getNewPurseDetails(BinaryUtils.encode(response.getData()));
			
			String readPayerDetailsCmd = wrapper.getPayerDetailsCmd();
			Log.d("ANDROID_TERMINAL_WRAPPER", "Send:"+readPayerDetailsCmd);
			byte[] resp = card.transceive(BinaryUtils.decode(readPayerDetailsCmd));
			Purse payerDetails = wrapper.getPayerDetails(BinaryUtils.encode(resp));
			Log.d("ANDROID_TERMINAL_WRAPPER", "Recv:"+BinaryUtils.encode(resp));
			
			result.setAppletSerialNumber(payerDetails.getAppletSerialNumber());
			result.setBatchId(payerDetails.getBatchId());
			Log.d("ANDROID_TERMINAL_WRAPPER", "BATCH ID = "+BinaryUtils.encode(result.getBatchId()));
			
			return result;
		}
		
		return null;
	}
	
	public FileControlInformation getPurseFCI(IsoDep card, byte infix) throws IOException{
		FileControlInformation fci = null;
		
		TerminalWrapper wrapper = TerminalWrapper.getInstance();
		
		String stringCmd = wrapper.getOpenSpecificAppletCmd(BinaryUtils.encode(infix));
		
		byte[] bytesCmd = BinaryUtils.decode(stringCmd);
		Log.d("ANDROID_TERMINAL_WRAPPER", "Send:"+BinaryUtils.encode(bytesCmd));
		byte[] bytesResp = card.transceive(bytesCmd);
		Log.d("ANDROID_TERMINAL_WRAPPER", "Recv:"+BinaryUtils.encode(bytesResp));
		ResponseAPDU response = new ResponseAPDU(bytesResp);
		
		if(response.getErrorCode().isOk()){
			
			fci = wrapper.getPayerFCI(BinaryUtils.encode(bytesResp));
			
			return fci;
		}
		
		return null;
	}
	
	/**
	 * Open a specific Purse on the card
	 * 
	 * @param card: the current card
	 * @param infix: the purse number to open
	 * 
	 * @return Purse containing the details of the purse to be opened
	 * @throws IOException
	 */
	private Purse readSpecificFCIFromCard(IsoDep card, byte infix) throws IOException{
		Purse result = new Purse();
		TerminalWrapper wrapper = TerminalWrapper.getInstance();
		
		String stringCmd = wrapper.getOpenSpecificAppletCmd(BinaryUtils.encode(infix));
		
		byte[] bytesCmd = BinaryUtils.decode(stringCmd);
		Log.d("ANDROID_TERMINAL_WRAPPER", "Send:"+BinaryUtils.encode(bytesCmd));
		byte[] bytesResp = card.transceive(bytesCmd);
		Log.d("ANDROID_TERMINAL_WRAPPER", "Recv:"+BinaryUtils.encode(bytesResp));
		ResponseAPDU response = new ResponseAPDU(bytesResp);
		
		if(response.getErrorCode().isOk()){
			result = wrapper.getNewPurseDetails(BinaryUtils.encode(response.getData()));
			
			if(result != null){
				
				String readPayerDetailsCmd = wrapper.getPayerDetailsCmd();
				Log.d("ANDROID_TERMINAL_WRAPPER", "Send:"+readPayerDetailsCmd);
				byte[] resp = card.transceive(BinaryUtils.decode(readPayerDetailsCmd));
				Purse payerDetails = wrapper.getPayerDetails(BinaryUtils.encode(resp));
				Log.d("ANDROID_TERMINAL_WRAPPER", "Recv:"+BinaryUtils.encode(resp));
				
				result.setAppletSerialNumber(payerDetails.getAppletSerialNumber());
				result.setBatchId(payerDetails.getBatchId());
				
				return result;
			}
		}
		
		return null;
	}
	
	/**
	 * Retrieve all the n-Counters from the database
	 * 
	 * @param parentActivity: the activity calling this method
	 * @return
	 */
	private List<TXnCounter> getAllMyNCounters(Activity parentActivity){
		
		List<TXnCounter> result = new ArrayList<TXnCounter>();
		DBTXTerminalLib db = new DBTXTerminalLib(parentActivity);
		db.open();
		NCountersBatch allNCounters = db.getAllnCounters();
		db.close();
		for(TXnCounter ncounter : allNCounters){
			result.add(ncounter);
		}
		
		return result;
	}

	public boolean isAutoRedeemOn(SharedPreferences settings) {
		boolean autoRedeem = settings.getBoolean(PREFERENCES_TERMINAL_AUTOREDEEM, true);
		
		return autoRedeem;
	}
	
	public void setAutoRedeemStatus(SharedPreferences settings, boolean autoRedeem){
		Editor edit = settings.edit();
		edit.putBoolean(PREFERENCES_TERMINAL_AUTOREDEEM, autoRedeem);
		edit.commit();
	}
}
