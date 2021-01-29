package uk.co.transaxiom.android.txandroidlib;

import static uk.co.transaxiom.acquirer.services.rest.lw.RESTConstants.NCOUNTERS_PARAM;
import static uk.co.transaxiom.acquirer.services.rest.lw.RESTConstants.REDEMPTION_TYPE_PARAM;
import static uk.co.transaxiom.acquirer.services.rest.lw.RESTConstants.TERMINAL_PARAM;
import static uk.co.transaxiom.android.txandroidlib.TxParams.getTxParams;
import static uk.co.transaxiom.parkingoperator.services.rest.lw.RESTConstants.AMOUNT_PARAM;
import static uk.co.transaxiom.parkingoperator.services.rest.lw.RESTConstants.FCI_PARAM;
import static uk.co.transaxiom.services.rest.lw.RESTConstants.JSON_UTF8;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.transaxiom.acquirer.services.lw.AppletMessageLW;
import uk.co.transaxiom.acquirer.services.lw.NCounterLW;
import uk.co.transaxiom.acquirer.services.lw.ProfileLW;
import uk.co.transaxiom.acquirer.services.lw.RedemptionTypeLW;
import uk.co.transaxiom.acquirer.services.lw.TerminalIdLW;
import uk.co.transaxiom.acquirer.services.lw.TerminalLW;
import uk.co.transaxiom.acquirer.services.lw.TransactionLW;
import uk.co.transaxiom.acquirer.services.rest.lw.AppObjectList;
import uk.co.transaxiom.acquirer.services.rest.lw.AppObjectMap;
import uk.co.transaxiom.android.txandroidlib.card.DisplayTransaction;
import uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu.ResponseAPDU;
import uk.co.transaxiom.android.txandroidlib.terminal.AndroidTerminalWrapper;
import uk.co.transaxiom.android.txandroidlib.terminal.AppletMessage;
import uk.co.transaxiom.android.txandroidlib.terminal.RequestedNCounter;
import uk.co.transaxiom.issuer.services.lw.AppletLW;
import uk.co.transaxiom.issuer.services.lw.CardLW;
import uk.co.transaxiom.issuer.services.lw.ConsumerLW;
import uk.co.transaxiom.issuer.services.lw.LoadCommandRequestLW;
import uk.co.transaxiom.issuer.services.lw.LoadRequestLW;
import uk.co.transaxiom.issuer.services.lw.LoadResultLW;
import uk.co.transaxiom.issuer.services.lw.LoadStateLW;
import uk.co.transaxiom.parkingoperator.services.lw.AmountLW;
import uk.co.transaxiom.services.errors.lw.TxExceptionLW;
import uk.co.transaxiom.services.lw.ApduLW;
import uk.co.transaxiom.services.lw.ByteLW;
import uk.co.transaxiom.services.lw.FciLW;
import uk.co.transaxiom.services.lw.FilterLW;
import uk.co.transaxiom.services.lw.IdLW;
import uk.co.transaxiom.services.lw.IndexLW;
import uk.co.transaxiom.services.lw.PageLW;
import uk.co.transaxiom.services.lw.PasswordLW;
import uk.co.transaxiom.services.lw.StringLW;
import uk.co.transaxiom.services.lw.TimestampLW;
import uk.co.transaxiom.services.lw.UserAuthenticationLW;
import uk.co.transaxiom.services.lw.UserTypeLW;
import uk.co.transaxiom.terminal.TerminalProfile;
import uk.co.transaxiom.terminal.TxTerminal;
import uk.co.transaxiom.terminal.common.utils.BinaryUtils;
import uk.co.transaxiom.terminal.ncounters.TXnCounter;
import uk.co.transaxiom.terminal.payment.entity.Purse;
import uk.co.transaxiom.terminal.payment.entity.TxTransaction;
import android.app.Activity;
import android.util.Log;

public class RestServicesWrapper {

	private static RestServicesWrapper instance = new RestServicesWrapper();
	
	private static String url_prefix;
	
	private RestServicesWrapper (){
		
	}
	
	public static RestServicesWrapper getInstance(){
		
		url_prefix = getTxParams().getParam(Constants.PARAMS_URL_PREFIX);
		
		return instance;
	}
	
	/**
	 * Login to the Acquirer System as a regular user
	 * 
	 * @param client: the HTTP client performing the request
	 * @param username: the username of the user
	 * @param password: the password of the user
	 * 
	 * @return Boolean true or false whether the request was successful
	 */
	public boolean loginToAcquirer(ConnectionWrapper client, String username, String password){
		String URL = url_prefix + ConnectionWrapper.LOGIN_ACQUIRER_URL;
		client.setUrl(URL);
		
		UserAuthenticationLW authenLW = new UserAuthenticationLW();
		
		PasswordLW passLW = new PasswordLW();
		passLW.setData(password);
		authenLW.setPassword(passLW);
		authenLW.setUserName(username);
		
		return performPostRequest(client, authenLW);
	}
	
	/**
	 * Login to the Issuer System as an Administrator 
	 * 
	 * 
	 * @param client: the HTTP client performing the request
	 * @param username: the username of the admin
	 * @param password: the password of the admin
	 * 
	 * @return
	 */
	public boolean loginToIssuerAsAdmin(ConnectionWrapper client, String username, String password){
		String URL = url_prefix + ConnectionWrapper.LOGIN_ASTYPE_ISSUER_URL;
		client.setUrl(URL);
		
		UserAuthenticationLW authenLW = new UserAuthenticationLW();
		
		PasswordLW passLW = new PasswordLW();
		passLW.setData(password);
		authenLW.setPassword(passLW);
		authenLW.setUserName(username);
		
		AppObjectMap holder = new AppObjectMap();
		holder.put("userauthentication", authenLW);
		holder.put("usertype", UserTypeLW.ADMINISTRATOR_TYPE);
		
		return performPostRequest(client, holder);
	}
	
	/**
	 * Login to the Issuer as a regular consumer
	 * 
	 * @param client: the HTTP client performing the request
	 * @param username: the username of the consumer
	 * @param password: the password of the consumer
	 * 
	 * @return
	 */
	public boolean loginToIssuer(ConnectionWrapper client, String username, String password){
		String URL = url_prefix + ConnectionWrapper.LOGIN_ISSUER_URL;
		client.setUrl(URL);
		
		UserAuthenticationLW authenLW = new UserAuthenticationLW();
		PasswordLW passLW = new PasswordLW();
		passLW.setData(password);
		authenLW.setPassword(passLW);
		authenLW.setUserName(username);
		
		return performPostRequest(client, authenLW);
	}
	
	/**
	 * Login to the Parking Operator system as a regular user
	 * 
	 * @param client: the HTTP client performing the request
	 * @param username: the username of the user
	 * @param password: the password of the user
	 * 
	 * @return
	 */
	public boolean loginToParkingOperator(ConnectionWrapper client, String username, String password){
		String URL = url_prefix + ConnectionWrapper.LOGIN_PARKING_URL;
		client.setUrl(URL);
		
		UserAuthenticationLW authenLW = new UserAuthenticationLW();
		PasswordLW passLW = new PasswordLW();
		passLW.setData(password);
		authenLW.setPassword(passLW);
		authenLW.setUserName(username);
		
		return performPostRequest(client, authenLW);
	}
	
	/**
	 * Read a terminal profile details
	 * 
	 * @param client: the HTTP client performing the request
	 * @param stringId: the Id of the profile to read
	 * 
	 * @return
	 */
	public boolean readProfile(ConnectionWrapper client, String stringId){
		String URL = url_prefix + ConnectionWrapper.READ_PROFILE_URL;
		client.setUrl(URL);
		
		IdLW idLW = new IdLW();
		idLW.setValue(stringId);
		
		return performPostRequest(client, idLW);
	}
	
	/**
	 * Read a terminal profile details if changed since the last update
	 * 
	 * @param client: the client calling the server
	 * @param stringId: the Id of the profile to read
	 * @param timestamp: the timestamp of the last update
	 * 
	 * @return
	 */
	public boolean readProfileIfChanged(ConnectionWrapper client, String stringId, long timestamp){
		String URL = url_prefix + ConnectionWrapper.READ_PROFILEIFCHANGED_URL;
		client.setUrl(URL);
		
		IdLW idLW = new IdLW();
		idLW.setValue(stringId);
		
		TimestampLW timestampLW = new TimestampLW(timestamp);
		
		AppObjectMap holder = new AppObjectMap();
		holder.put("id", idLW);
		holder.put("timestamp", timestampLW);
		
		return performPostRequest(client, holder);
	}
	
	/**
	 * Get the Transaction Frontier for transactions upload from the card
	 * 
	 * @param client: the HTTP client performing the request
	 * @param appletSerialNumber: the applet serial number
	 * 
	 * @return 
	 */
	public boolean requestGetTransactionFrontier(ConnectionWrapper client, String appletSerialNumber){
		String URL = url_prefix + ConnectionWrapper.GET_TRANSACTION_FRONTIER;
		client.setUrl(URL);
		
		StringLW stringLW = new StringLW(appletSerialNumber);
		
		return performPostRequest(client, stringLW);
	}
	
	/**
	 * Send the load result received from the card
	 * 
	 * @param client: the HTTP client performing the request
	 * @param response: the response encoded as a byte array
	 * @param conversationId: the conversation Id used for the load request
	 * 
	 * @return
	 */
	public boolean requestSetLoadResult(ConnectionWrapper client, byte[] response, String conversationId){
		String URL = url_prefix + ConnectionWrapper.SET_LOADRESULT_URL;
		client.setUrl(URL);
		
		ResponseAPDU responseAPDU = new ResponseAPDU(response);
		
		LoadResultLW loadResult = new LoadResultLW();
		
		ApduLW apdu = new ApduLW();
		apdu.setData(BinaryUtils.encode(responseAPDU.getData()));
		loadResult.setApdu(apdu);
		loadResult.setConversationId(conversationId);
		
		if(performPostRequest(client, loadResult)){
			LoadStateLW loadState = LWTranslator.getInstance().toLW(client.getResponse());
			if(loadState.equals(LoadStateLW.COMPLETE)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Get the payment APDU from the Parking Operator system
	 * 
	 * @param client: the HTTP client performing the request
	 * @param fci: the FCI of the applet encoded as a byte array
	 * @param amount: the amount to be paid
	 * 
	 * @return
	 */
	public boolean requestPreparePayment(ConnectionWrapper client, byte[] fci, String amount){
		String url = url_prefix + ConnectionWrapper.PREPARE_PAYMENT_URL;
		client.setUrl(url);
		
		FciLW fciLW = new FciLW();
		fciLW.setData(BinaryUtils.encode(fci));
		
		AmountLW amountLW = new AmountLW();
		amountLW.setData(amount);
		
		AppObjectMap holder = new AppObjectMap();
		holder.put(FCI_PARAM, fciLW);
		holder.put(AMOUNT_PARAM, amountLW);
		
		return performPostRequest(client, holder);
	}
	
	/**
	 * Consume payment on the Parking Operator system
	 * 
	 * @param client: the HTTP client performing the request
	 * @param responseApdu: the response after payment command from the card
	 * 
	 * @return
	 */
	public boolean requestConsumePayment(ConnectionWrapper client, byte[] responseApdu){
		String url = url_prefix + ConnectionWrapper.CONSUME_URL;
		client.setUrl(url);
		
		ApduLW apduLW = new ApduLW();
		apduLW.setData(BinaryUtils.encode(responseApdu));
		
		return performPostRequest(client, apduLW);
	}
	
	/**
	 * Read the details of the merchant currently logged in
	 * 
	 * @param client: the HTTP client performing the request
	 * 
	 * @return
	 */
	public boolean readMerchant(ConnectionWrapper client){
		String url = url_prefix + ConnectionWrapper.READMERCHANT_URL;
		client.setUrl(url);
		
		return performGetRequest(client);
	}
	
	/**
	 * Get the open applet command from the Parking Operator system
	 * 
	 * @param client: the HTTP client performing the request
	 * @return
	 */
	public boolean requestOpenAppletForParkingCommand(ConnectionWrapper client){
		String URL = url_prefix + ConnectionWrapper.GET_OPEN_APPLET_URL;
		client.setUrl(URL);
		
		return performGetRequest(client);
	}
	
	/**
	 * Get the open applet command for load from Issuer System
	 * 
	 * @param client: the HTTP client performing the request
	 * 
	 * @return
	 */
	public boolean requestOpenAppletForLoadCommand(ConnectionWrapper client, byte purseNumber) {
		String URL = url_prefix + ConnectionWrapper.GET_OPENPURSE_FORLOAD_URL;
		client.setUrl(URL);
		
		ByteLW byteLW = new ByteLW(purseNumber);
		
		return performPostRequest(client, byteLW);
	}
	
	public boolean requestReadLoad(ConnectionWrapper client, String conversationId) {
		String URL = url_prefix + ConnectionWrapper.READ_LOAD_RESULT;
		client.setUrl(URL);
		
		IdLW idLW = new IdLW(conversationId);
		
		return performPostRequest(client, idLW);
	}
	
//	/**
//	 * Get the authentication template from Issuer System
//	 * 
//	 * @param client: the HTTP client performing the request
//	 * 
//	 * @return
//	 */
//	public Boolean requestGetAuthenticationTemplate(ConnectionWrapper client) {
//		String URL = url_prefix + ConnectionWrapper.GETAUTHENTICATETEMPLATE_URL;
//		client.setUrl(URL);
//				
//		FciLW fciLW = new FciLW();
//		DeviceLW deviceLW = new DeviceLW();
//		deviceLW.setFci(fciLW);
//		
//		return performPostRequest(client, deviceLW);
//	}
	
	public boolean startLoadConversation(ConnectionWrapper client, Purse purse, String loadAmount, String treasuryName){
		String URL = url_prefix + ConnectionWrapper.START_LOAD_CONVERSATION_URL;
		client.setUrl(URL);
		
		double doubleAmount = Double.parseDouble(loadAmount);
		double tmp = doubleAmount * Math.pow(10, purse.getCurrency().getFractionDigits());
		int intAmount = (int) tmp;
		
		
		LoadRequestLW loadRequest = new LoadRequestLW();
		loadRequest.setAmount(intAmount);
		loadRequest.setSerialNumber(purse.getStringAppletSerialNumber());
		loadRequest.setTreasuryName(treasuryName);
		
		return performPostRequest(client, loadRequest);
	}
	
	public boolean requestCreateLoadCommand(ConnectionWrapper client, byte[] openAppletResponse, Purse purse, String conversationId){
		String URL = url_prefix + ConnectionWrapper.CREATE_LOAD_COMMAND_URL;
		client.setUrl(URL);
		
		FciLW fci = new FciLW();
		if(openAppletResponse != null){
			fci.setData(BinaryUtils.encode(openAppletResponse));
		}
		
		LoadCommandRequestLW commandRequest = new LoadCommandRequestLW();
		commandRequest.setConversationId(conversationId);
		commandRequest.setSerialNumber(purse.getStringAppletSerialNumber());
		commandRequest.setFci(fci);
		
		return performPostRequest(client, commandRequest);
	}
	
	
	/**
	 * Authorize a load on the Issuer System
	 * 
	 * @param client: the HTTP client performing the request
	 * @param purse: the purse requesting the load
	 * @param openAppletResponse: the response from the card after the open applet command
	 * @param loadAmount: the amount to load
	 * 
	 * @return
	 */
//	public Boolean requestAuthorize(ConnectionWrapper client, Purse purse, byte[] openAppletResponse, String loadAmount) {
//		String URL = url_prefix + ConnectionWrapper.AUTHORIZE_URL;
//		client.setUrl(URL);
//		
//		double doubleAmount = Double.parseDouble(loadAmount);
//		double tmp = doubleAmount * Math.pow(10, purse.getCurrency().getFractionDigits());
//		int intAmount = (int) tmp;
//		
//		FciLW fci = new FciLW();
//		if(openAppletResponse != null){
//			fci.setData(BinaryUtils.encode(openAppletResponse));
//		}
//		
//		LoadRequestLW loadReqLW = new LoadRequestLW();
//		loadReqLW.setAmount(intAmount);
//		loadReqLW.setFci(fci);
//		loadReqLW.setSerialNumber(purse.getStringAppletSerialNumber());
//		
//		return performPostRequest(client, loadReqLW);
//	}
	
	/**
	 * Redeem the funds accumulated on the terminal
	 * 
	 * @param client: the HTTP client performing the request
	 * @param parentActivity: the activity performing the request
	 * @param terminalProfile: the profile of the terminal redeeming its funds
	 * @param terminal: the terminal's details
	 * @param ncounters: the list of n-Counters containing values to redeem
	 * 
	 * @return
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws JSONException
	 */
	public boolean redeemNCounters(ConnectionWrapper client, Activity parentActivity, TerminalProfile terminalProfile, TxTerminal terminal, List<TXnCounter> ncounters) throws UnsupportedEncodingException, JSONException{
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		String url = url_prefix + ConnectionWrapper.REDEEMNC_URL;
		client.setUrl(url);
		
		TerminalIdLW idLw = new TerminalIdLW();
		idLw.setType(terminal.getTerminalType());
		idLw.setTag(terminal.getTerminalTag());
		
		TerminalLW terminalLW = new TerminalLW();
		terminalLW.setTerminalId(idLw);
		terminalLW.setDescription(terminal.getTerminalDescription());
		terminalLW.setLocation(terminal.getTerminalLocation());
        
		LWTranslator translator = LWTranslator.getInstance();
        AppObjectList<NCounterLW> ncountersLW = new AppObjectList<NCounterLW>();
        
        List<RequestedNCounter> ncountersToReplace = new ArrayList<RequestedNCounter>();
        
    	for(int i=0; i<ncounters.size(); i++){
    		ncountersToReplace.add(parseIntoRequestedNCounter(ncounters.get(i)));
	    	Log.d("ServerCallWrapper","A new nCounter is being transformed into NCounterLW");
	    	ncountersLW.add(translator.toLW(ncounters.get(i)));
	    }
    	
    	AppObjectMap holder = new AppObjectMap();
		holder.put(NCOUNTERS_PARAM, ncountersLW);
		holder.put(REDEMPTION_TYPE_PARAM, RedemptionTypeLW.FULL);
		
		boolean redeemSuccess = performPostRequest(client, holder);
		
		if(redeemSuccess){
			
			wrapper.saveRedeemedValues(parentActivity, ncounters);
			if(wrapper.deleteRedeemedNCounters(parentActivity, ncounters)){
				
				return checkForUpdate(parentActivity, client, terminalProfile, terminal, ncountersToReplace);
			}
		}
		return false;
	}
	
	public boolean redeemNCountersForDelete(ConnectionWrapper client, Activity activity, List<TXnCounter> ncounters){
		String url = url_prefix + ConnectionWrapper.REDEEMNC_URL;
		client.setUrl(url);
		
		LWTranslator translator = LWTranslator.getInstance();
        AppObjectList<NCounterLW> ncountersLW = translator.toLW(ncounters);
		
		AppObjectMap holder = new AppObjectMap();
		holder.put(NCOUNTERS_PARAM, ncountersLW);
		holder.put(REDEMPTION_TYPE_PARAM, RedemptionTypeLW.FULL);
		
		boolean redeemSuccess = performPostRequest(client, holder);
		if(redeemSuccess){
			AndroidTerminalWrapper.getInstance().saveRedeemedValues(activity, ncounters);
		}
		
		return redeemSuccess;
	}
	
	/**
	 * Check if the terminal needs to be updated (after a redemption)
	 * 
	 * @param parentActivity: the activity calling this method
	 * @param client: the HTTP client performing the request
	 * @param terminalProfile: the terminal's Profile
	 * @param terminal: the terminal's details
	 * @param ncountersToReplace: the lsit of n-Counters to replace
	 * 
	 * @return
	 * 
	 * @throws JSONException
	 * @throws UnsupportedEncodingException
	 */
	public boolean checkForUpdate(Activity parentActivity, ConnectionWrapper client, TerminalProfile terminalProfile, TxTerminal terminal, List<RequestedNCounter> ncountersToReplace) throws JSONException, UnsupportedEncodingException{
		String URL = url_prefix + ConnectionWrapper.READ_PROFILEIFCHANGED_URL;
		client.setUrl(URL);
		boolean isClearToDeleteDB = false;
		if(readProfileIfChanged(client, terminalProfile.getId(), terminalProfile.getLastUpdate())){
			
			String profileChangeResponse = client.getResponse();
			int responseCode = client.getResponseCode();
			if(responseCode == 200){
				
				isClearToDeleteDB = true;
				List<TXnCounter> usedNCounters = AndroidTerminalWrapper.getInstance().getAllRedeemableTXNcounters(parentActivity);
				if(usedNCounters != null){
					isClearToDeleteDB  = redeemNCountersForDelete(client, parentActivity, usedNCounters);
				}
				
				if(!isClearToDeleteDB){
					return false;
				}
				else{
					AndroidTerminalWrapper.getInstance().wipeNCountersDatabase(parentActivity);
					
					LWTranslator translator = LWTranslator.getInstance();
					JSONObject jsonResponse = new JSONObject(profileChangeResponse);
					ProfileLW profileLW = translator.toProfileLW(jsonResponse);
					List<RequestedNCounter> newNCounters = translator.fromLW(profileLW);
					
					return replaceNCounters(parentActivity, client, terminal, newNCounters);
				}
			}else{
				return replaceNCounters(parentActivity, client, terminal, ncountersToReplace);
			}
		}else{
			return replaceNCounters(parentActivity, client, terminal, ncountersToReplace);
		}
	}
	
	/**
	 * Request new n-Counters from the server
	 * 
	 * @param parentActivity: the activity calling this method
	 * @param client: the HTTP client performing this request
	 * @param terminal: the terminal's details
	 * @param ncounters: the n-Counters to request
	 * 
	 * @return
	 */
	private boolean replaceNCounters(Activity parentActivity, ConnectionWrapper client, TxTerminal terminal, List<RequestedNCounter> ncounters){
		
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		if(getNCounters(client, terminal, ncounters)){
			
			boolean savingResult = wrapper.saveNCountersFromServer(parentActivity, ncounters, client.getResponse());
			return savingResult;
		}else{
			wrapper.wipeNCountersDatabase(parentActivity);
		}
		return false;
	}
	
	/**
	 * Register a new consumer to the Issuer System
	 * 
	 * @param client: the HTTP client performing the request
	 * @param consumerDetails: the consumer details
	 * @param consumerAuthentication: the consumer username and password
	 * 
	 * @return
	 */
	public boolean registerConsumer(ConnectionWrapper client, ConsumerLW consumerDetails, UserAuthenticationLW consumerAuthentication) {
		String URL = url_prefix + ConnectionWrapper.REGSITER_CONSUMER_URL;
		client.setUrl(URL);
		
		AppObjectMap holder = new AppObjectMap();
		holder.put("userauthentication", consumerAuthentication);
		holder.put("consumer", consumerDetails);
		
		return performPostRequest(client, holder);
	}
	
	/**
	 * Update a card's details on the Issuer System:
	 * \t -registering a new applet Id on this card
	 * \t -linking the card to a specific consumer
	 * 
	 * @param client: the HTTP client performing this request
	 * @param cardLW: the card object with the updated values
	 * 
	 * @return
	 */
	public boolean updateCard(ConnectionWrapper client, CardLW cardLW) {
		String URL = url_prefix + ConnectionWrapper.UPDATECARD_URL;
		client.setUrl(URL);
		
		return performPostRequest(client, cardLW);
	}

	/**
	 * Read cards with a specific attribute
	 * 
	 * @param client: the HTTP client performing this request
	 * @param cardSerialNumber: the serial number of the card to read
	 * @param numberOfCards: the number of cards exepcting to get back
	 * 
	 * @return
	 */
	public boolean readCardFiltered(ConnectionWrapper client, String cardSerialNumber, int numberOfCards) {
		String URL = url_prefix + ConnectionWrapper.READCARDS_FILTERED_URL;
		client.setUrl(URL);
		
		PageLW page = new PageLW();
		page.setFrom(0);
		page.setSize(numberOfCards);
		
		FilterLW filter = new FilterLW();
		filter.setAttribute("serialNumber");
		filter.setValue(cardSerialNumber);
		
		AppObjectMap holder = new AppObjectMap();
		holder.put("page", page);
		holder.put("filter", filter);
		
		return performPostRequest(client, holder);
	}
	
	/**
	 * Get the number of cards with a specific attribute
	 * 
	 * @param client: the HTTP client performing this request
	 * @param cardSerialNumber: the card serial number to search for
	 * 
	 * @return
	 */
	public boolean getNumberCardsFiltered(ConnectionWrapper client, String cardSerialNumber) {
		String URL = url_prefix + ConnectionWrapper.GETNUMBERCARDSFILTERED_URL;
		client.setUrl(URL);
		
		FilterLW filter = new FilterLW();
		filter.setAttribute("serialNumber");
		filter.setValue(cardSerialNumber);
		
		return performPostRequest(client, filter);
	}
	
	/**
	 * Get n-Counters from the server
	 * 
	 * @param client: the HTTP client performing this request
	 * @param terminal: the terminal requesting the n-Counters
	 * @param requestedNCounters: the list of requested n-Counters
	 * 
	 * @return 
	 */
	public boolean getNCounters(ConnectionWrapper client, TxTerminal terminal, List<RequestedNCounter> requestedNCounters){
		String url = url_prefix + ConnectionWrapper.GETNCOUNTERS_URL;
		client.setUrl(url);
		
		LWTranslator translator = LWTranslator.getInstance();
		TerminalLW terminalLW = translator.toLW(terminal);
		
		AppObjectList<NCounterLW> ncounters = new AppObjectList<NCounterLW>();
		for(int i=0; i<requestedNCounters.size(); i++){
			NCounterLW ncLW = translator.toLW(requestedNCounters.get(i));
			ncounters.add(ncLW);
		}

		AppObjectMap holder = new AppObjectMap();
		holder.put(TERMINAL_PARAM, terminalLW);
		holder.put(NCOUNTERS_PARAM, ncounters);
		
		boolean firstRequest = performPostRequest(client, holder);
		if(!firstRequest){
			
			TxExceptionLW exception = getTxExceptionFromMessage(client.getResponse());
			if(exception.getCodeAsString().equals("1021") || exception.getMessage().contains("XNUNIQUE")){
				
				return performPostRequest(client, holder);
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
	
	
	/**
	 * Get the number of terminal with a specific attribute
	 * 
	 * @param client: the HTTP client performing this request
	 * @param attribute: the attribute to search for
	 * @param fragment: the value of the attribute to search for
	 * @return
	 */
	public boolean getNumberOfTerminalFiltered(ConnectionWrapper client, String attribute, String fragment){
		String url = url_prefix + ConnectionWrapper.GETNUMBERTERMINALSFILTERED_URL;
		client.setUrl(url);
		
		FilterLW filter = new FilterLW();
		filter.setAttribute(attribute);
		filter.setValue(fragment);
		
		return performPostRequest(client, filter);
	}
	
	/**
	 * Get the terminals with a specific attribute
	 * 
	 * @param client: the HTTP client performing this request
	 * @param attribute: the attribute to search for
	 * @param fragment: the value of the attribute to search for
	 * @param from: the index of the first terminal to get
	 * @param to: the index of the last terminal to get
	 * 
	 * @return
	 */
	public boolean getTerminalsFiltered(ConnectionWrapper client, String attribute, String fragment, int from, int to) {
		String url = url_prefix + ConnectionWrapper.READTERMINALSFILTERED_URL;
		client.setUrl(url);
		
		FilterLW filter = new FilterLW();
		filter.setAttribute(attribute);
		filter.setValue(fragment);
		
		PageLW page = new PageLW();
		page.setFrom(from);
		if(to - from > 5){
			page.setSize(5);
		}else{
			page.setSize(to - from);
		}
		
		AppObjectMap holder = new AppObjectMap();
		holder.put("page", page);
		holder.put("filter", filter);
		
		return performPostRequest(client, holder);
	}
	
	/**
	 * Update a terminal on the Acquirer System
	 * 
	 * @param client: the HTTP client performing this request
	 * @param terminal: the terminal to update with its new values
	 * 
	 * @return
	 */
	public boolean updateTerminal(ConnectionWrapper client, TxTerminal terminal) {
		String url = url_prefix + ConnectionWrapper.UPDATETERMINAL_URL;
		client.setUrl(url);
		
		LWTranslator translator = LWTranslator.getInstance();
		TerminalLW terminalLW = translator.toLW(terminal);
		
//		AppObjectMap holder = new AppObjectMap();
//		holder.put("terminalId", terminalLW.getTerminalId());
//		holder.put("deviceId", terminalLW.getDeviceId());

		return performPostRequest(client, terminalLW);
	}
	
	/**
	 * Get the error message from the response sent back by the server
	 * 
	 * @param clientResponse: the response sent back by the server
	 * 
	 * @return TxExceptionLW containing the error message
	 */
	public TxExceptionLW getTxExceptionFromMessage(String clientResponse){
		TxExceptionLW exception = new TxExceptionLW("");
		
		try {
			JSONObject respJson = new JSONObject(clientResponse);
			String response = respJson.getString("message");
			exception = new TxExceptionLW(response);
			Log.d("AsyncClass", "Exception = "+exception.getMessage() + " -*- "+exception.getLocalizedMessage());
			
			return exception;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Upload transactions from the terminal to the Acquirer System
	 * 
	 * @param client: the HTTP client performing this request
	 * @param transactions: the list of transactions to upload
	 * 
	 * @return
	 */
	public boolean uploadTransactions(ConnectionWrapper client, List<TxTransaction> transactions) {
		String url = url_prefix + ConnectionWrapper.UPLOADTRANSACTIONS_URL;
		client.setUrl(url);
		
		LWTranslator translator = LWTranslator.getInstance();
		AppObjectList<TransactionLW> transactionsLW = new AppObjectList<TransactionLW>();
		for(TxTransaction transaction : transactions){
			TransactionLW transLW = translator.toLW(transaction);
			transactionsLW.add(transLW);
		}
				
		return performPostRequest(client, transactionsLW);
	}
	
	/**
	 * Create a new terminal on the Acquirer System
	 * 
	 * @param client: the HTTP client performing this request
	 * @param terminal: the new terminal to create
	 * 
	 * @return
	 */
	public boolean createTerminal(ConnectionWrapper client, TxTerminal terminal) {
		String url = url_prefix + ConnectionWrapper.CREATETERMIAL_URL;
		client.setUrl(url);
		
		LWTranslator translator = LWTranslator.getInstance();
		TerminalLW terminalLW = translator.toLW(terminal);
         
        return performPostRequest(client, terminalLW);
	}
	
	/**
	 * Get the number of consumers with a specific name
	 * 
	 * @param client: the HTTP client performing this request
	 * @param consumerName: the name to search for
	 * 
	 * @return
	 */
	public boolean getNumberConsumersFiltered(ConnectionWrapper client, String consumerName){
		String URL = url_prefix + ConnectionWrapper.GETNUMBERCONSUMERSFILTERED_URL;
		client.setUrl(URL);
		
		FilterLW filter = new FilterLW();
		filter.setAttribute("name");
		filter.setValue(consumerName);
		filter.setPartial(true);
		
		return performPostRequest(client, filter);
	}
	
	/**
	 * Read the consumers with a specific name 
	 * 
	 * @param client: the HTTP client performing this request
	 * @param numberOfConsumers: the number of consumers to get
	 * @param consumerName: the name to search for
	 * 
	 * @return
	 */
	public boolean readConsumersFiltered(ConnectionWrapper client, int numberOfConsumers, String consumerName) {
		String URL = url_prefix + ConnectionWrapper.READCONSUMERSFILTERED_URL;
		client.setUrl(URL);
		
		PageLW page = new PageLW();
		page.setFrom(0);
		page.setSize(numberOfConsumers);
		
		FilterLW filter = new FilterLW();
		filter.setAttribute("name");
		filter.setValue(consumerName);
		filter.setPartial(true);
		
		AppObjectMap holder = new AppObjectMap();
		holder.put("page", page);
		holder.put("filter", filter);
		
		return performPostRequest(client, holder);
	}
	
	public boolean readAllConsumers(ConnectionWrapper client, String consumerId) {
		String URL = url_prefix + ConnectionWrapper.READALLCONSUMER_URL;
		client.setUrl(URL);
		
		StringLW stringLW = new StringLW();
		stringLW.setValue(consumerId);
		
		return performPostRequest(client, stringLW);
	}
	
	/**
	 * Create an applet on the Issuer System
	 * 
	 * @param client: the HTTP client performing this request
	 * @param applet: the new applet to create
	 * 
	 * @return
	 */
	public boolean createApplet(ConnectionWrapper client, AppletLW applet) {
		String URL = url_prefix + ConnectionWrapper.CREATEAPPLET_URL;
		client.setUrl(URL);
		
		return performPostRequest(client, applet);
	}
	
	public boolean readApplet(ConnectionWrapper client, String appletSN) {
		String URL = url_prefix + ConnectionWrapper.READAPPLET_URL;
		client.setUrl(URL);
		
		StringLW stringLW = new StringLW(appletSN);
		
		return performPostRequest(client, stringLW);
	}
	
	/**
	 * Logout from the Issuer System
	 * 
	 * @param client: the HTTP client performing this request
	 * 
	 * @return
	 */
	public boolean logoutFromIssuer(ConnectionWrapper client) {
		String URL = url_prefix + ConnectionWrapper.LOGOUT_ISSUER_URL;
		client.setUrl(URL);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();  		

		headers.add(new BasicNameValuePair("Content-Type", JSON_UTF8));
		headers.add(new BasicNameValuePair("Accept", JSON_UTF8));

		client.setParams(params);
		client.setHeaders(headers);
		client.performHttpRequest(ConnectionWrapper.HTTP_POST, URL);

		Log.v("LoginIssuer","URL = " + client.getUrl());
		Log.v("LoginIssuer","ResponseCode = " + client.getResponseCode());
		Log.v("LoginIssuer","Response is: " + client.getResponse());
		
		if(client.getResponseCode() == 204){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Logout from the Acquirer
	 * 
	 * @param client: the HTTP client performing this request
	 * 
	 * @return
	 */
	public boolean logoutFromAcquirer(ConnectionWrapper client) {
		String URL = url_prefix + ConnectionWrapper.LOGOUT_ACQUIRER_URL;
		client.setUrl(URL);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();  		

		headers.add(new BasicNameValuePair("Content-Type", JSON_UTF8));
		headers.add(new BasicNameValuePair("Accept", JSON_UTF8));

		client.setParams(params);
		client.setHeaders(headers);
		client.performHttpRequest(ConnectionWrapper.HTTP_POST, URL);

		Log.v("LoginIssuer","URL = " + client.getUrl());
		Log.v("LoginIssuer","ResponseCode = " + client.getResponseCode());
		Log.v("LoginIssuer","Response is: " + client.getResponse());
		
		if(client.getResponseCode() == 204){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Logout from the Parking Operator system
	 * 
	 * @param client: the HTTP client performing this request
	 * 
	 * @return
	 */
	public boolean logoutFromParkingOperator(ConnectionWrapper client){
		String URL = url_prefix + ConnectionWrapper.LOGOUT_PARKING_URL;
		client.setUrl(URL);
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();  		

		headers.add(new BasicNameValuePair("Content-Type", JSON_UTF8));
		headers.add(new BasicNameValuePair("Accept", JSON_UTF8));

		client.setParams(params);
		client.setHeaders(headers);
		client.performHttpRequest(ConnectionWrapper.HTTP_POST, URL);

		Log.v("LoginIssuer","URL = " + client.getUrl());
		Log.v("LoginIssuer","ResponseCode = " + client.getResponseCode());
		Log.v("LoginIssuer","Response is: " + client.getResponse());
		
		if(client.getResponseCode() == 204){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Translate an n-Counter into a n-Counter to be requested
	 * 
	 * @param nCounter: the n-Counter to be translated
	 * 
	 * @return
	 */
	private RequestedNCounter parseIntoRequestedNCounter(TXnCounter nCounter) {
		RequestedNCounter result = new RequestedNCounter(
				nCounter.getnCMerchantUnit(), 
				nCounter.getnCLength(), 
				nCounter.getNarrative());
		
		return result;
	}
	
	/**
	 * Perform a GET request on the server
	 * 
	 * @param client: the HTTP client performing this request
	 * 
	 * @return
	 */
	private boolean performGetRequest(ConnectionWrapper client){
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		
		ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();  		
		headers.add(new BasicNameValuePair("Accept", JSON_UTF8));
		
		client.setParams(params);
		client.setHeaders(headers);
		
		client.performHttpRequest(ConnectionWrapper.HTTP_GET, client.getUrl());
		
		//-------------------------------------------//
		Log.v("Login","URL = " + client.getUrl());
		Log.v("Login","ResponseCode = " + client.getResponseCode());
		Log.v("Login","Response is: " + client.getResponse());
		
		if(client.getResponseCode() == 204 || client.getResponseCode() == 200){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Perform a POST request on the server
	 * 
	 * @param client: the HTTP client performing this request
	 * @param object: the object to post to the server
	 * 
	 * @return
	 */
	private boolean performPostRequest(ConnectionWrapper client, Object object){
		
		String json = GsonHelper.customGson.toJson(object);
		
		ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();  		
		headers.add(new BasicNameValuePair("Content-Type", JSON_UTF8));
		headers.add(new BasicNameValuePair("Accept", JSON_UTF8));

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("body", json));

		client.setParams(params);
		client.setHeaders(headers);

		client.performHttpRequest(ConnectionWrapper.HTTP_POST, client.getUrl());
		
		//-------------------------------------------//
		Log.v("Login","URL = " + client.getUrl());
		Log.v("Login","ResponseCode = " + client.getResponseCode());
		Log.v("Login","Response is: " + client.getResponse());
		
		if(client.getResponseCode() == 200 || client.getResponseCode() == 204){
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * Create transaction record on the Issuer System from an applet transaction store on the card
	 * 
	 * @param client: the HTTP client performing this request
	 * @param transactionStoreToUpload: the list of transactions to upload
	 * @param appletSN: the applet serial number
	 * 
	 * @return
	 */
	public boolean requestCreateTransactions(ConnectionWrapper client, 	List<DisplayTransaction> transactionStoreToUpload, String appletSN) {
		String URL = url_prefix + ConnectionWrapper.CREATE_TRANSACTIONS;
		client.setUrl(URL);
		
		LWTranslator translator = LWTranslator.getInstance();
		AppObjectList<uk.co.transaxiom.issuer.services.lw.TransactionLW> transactionsLW = new AppObjectList<uk.co.transaxiom.issuer.services.lw.TransactionLW>();
		
		for(DisplayTransaction transaction : transactionStoreToUpload){
			transactionsLW.add(translator.toTransactionLW(transaction));
		}
		
		StringLW stringLW = new StringLW(appletSN);
		
		AppObjectMap holder = new AppObjectMap();
		holder.put("string", stringLW);
		holder.put("transactions", transactionsLW);
		
		return performPostRequest(client, holder);
	}
	
	
	public boolean requestGetThemeCatalogIssuer(ConnectionWrapper client){
		String URL = url_prefix + ConnectionWrapper.GET_THEME_CATALOG_ISSUER;
		client.setUrl(URL);
		
		return performGetRequest(client);
	}
	
	public boolean requestGetThemeCatalogAcquirer(ConnectionWrapper client){
		String URL = url_prefix + ConnectionWrapper.GET_THEME_CATALOG_ACQUIRER;
		client.setUrl(URL);
		
		return performGetRequest(client);
	}
	
	public boolean requestGetThemeIssuer(ConnectionWrapper client, String themeName) {
		String URL = url_prefix + ConnectionWrapper.GET_THEME_ISSUER;
		client.setUrl(URL);
		
		StringLW stringLW = new StringLW(themeName);
		
		return performPostRequest(client, stringLW);
	}
	
	public boolean requestGetThemeAcquirer(ConnectionWrapper client, String themeName) {
		String URL = url_prefix + ConnectionWrapper.GET_THEME_ACQUIRER;
		client.setUrl(URL);
		
		StringLW stringLW = new StringLW(themeName);
		
		return performPostRequest(client, stringLW);
	}
	
	public boolean requestGetActiveMessagesAfter(ConnectionWrapper client, long minSeqNumber){
		String URL = url_prefix + ConnectionWrapper.GET_ACTIVE_MESSAGES_AFTER_URL;
		client.setUrl(URL);
		
		IndexLW indexLW = new IndexLW(minSeqNumber -1);
		return performPostRequest(client, indexLW);
	}
	
	public boolean requestSyncFinishedMessages(ConnectionWrapper client, TxTerminal terminal, List<AppletMessage> appletMessages){
		String URL = url_prefix + ConnectionWrapper.UPDATE_FINISHED_MESSAGES_URL;
		client.setUrl(URL);
		
		TerminalIdLW idLW = new TerminalIdLW();
		idLW.setType(terminal.getTerminalType());
		idLW.setTag(terminal.getTerminalTag());
		
		List<AppletMessageLW> appletMessagesLW = LWTranslator.getInstance().toLWs(appletMessages);
		AppObjectList<AppletMessageLW> listLW = new AppObjectList<AppletMessageLW>();
		for (AppletMessageLW appletMessageLW : appletMessagesLW) {
			listLW.add(appletMessageLW);
		}
		
		AppObjectMap holder = new AppObjectMap();
		holder.put("terminalid", idLW);
		holder.put("appletmessages", listLW);
		return performPostRequest(client, holder);
	}

	public boolean getUnblockCommands(ConnectionWrapper client, String cardId) {
		String URL = url_prefix + ConnectionWrapper.READUNBLOCKCOMMANDS_URL;
		client.setUrl(URL);
		
		StringLW stringLW = new StringLW();
		stringLW.setValue(cardId);
		
		return performPostRequest(client, stringLW);
	}
}
