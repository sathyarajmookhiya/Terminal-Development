package uk.co.transaxiom.android.txandroidlib;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

import android.app.Activity;

public class ConnectionWrapper {

	Activity activity;
	String url;
	String response;
	int responseCode;
	HTTPClient client;
	
	ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
	ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
	
	CookieStore cookieStore = new BasicCookieStore();	

	public static final String UPDATE_FINISHED_MESSAGES_URL = "/AcquirerWeb/rest/appletMessage/updateAllFinishedAppletMessages";
	public static final String GET_ACTIVE_MESSAGES_AFTER_URL = "/AcquirerWeb/rest/appletMessage/readAllActiveAppletMessagesAfter";
	public static final String CREATE_LOAD_COMMAND_URL = "/IssuerWeb/rest/load/createLoadCommand";
	public static final String START_LOAD_CONVERSATION_URL = "/IssuerWeb/rest/load/startLoadConversation";
	public static final String GET_THEME_CATALOG_ISSUER = "/IssuerWeb/rest/general/getThemeCatalogue";
	public static final String GET_THEME_CATALOG_ACQUIRER = "/AcquirerWeb/rest/general/getThemeCatalogue";
	public static final String GET_THEME_ISSUER = "/IssuerWeb/rest/general/getTheme";
	public static final String GET_THEME_ACQUIRER = "/AcquirerWeb/rest/general/getTheme";
	public static final String READMERCHANT_URL = "/AcquirerWeb/rest/merchant/readMerchant";
	public static final String GETNCOUNTERS_URL = "/AcquirerWeb/rest/nCount/getNCounterBatches";
	public static final String CREATETERMIAL_URL = "/AcquirerWeb/rest/merchant/createTerminal";
	public static final String GETNUMBERTERMINALSFILTERED_URL = "/AcquirerWeb/rest/merchant/getNumberOfTerminalsFiltered";
	public static final String UPLOADTRANSACTIONS_URL = "/AcquirerWeb/rest/merchant/createTransactions";
	public static final String READTERMINALSFILTERED_URL = "/AcquirerWeb/rest/merchant/readTerminalsFiltered";
	public static final String READ_PROFILE_URL = "/AcquirerWeb/rest/merchant/readProfile";
	public static final String READ_PROFILEIFCHANGED_URL = "/AcquirerWeb/rest/merchant/readProfileIfChanged";
	public static final String UPDATETERMINAL_URL = "/AcquirerWeb/rest/merchant/updateTerminal";
	public static final String REDEEMNC_URL = "/AcquirerWeb/rest/nCount/redeemNCounters";
	public static final String REGISTER_MERCHANT_URL = "/AcquirerWeb/rest/merchant/register";
	public static final String PREPARE_PAYMENT_URL = "/ParkingOperatorWeb/rest/terminal/preparePayment";
	public static final String CONSUME_URL = "/ParkingOperatorWeb/rest/terminal/consume";
	public static final String AUTHENTICATE_URL = "/IssuerWeb/rest/security/authenticate";
	public static final String REGSITER_CONSUMER_URL = "/IssuerWeb/rest/consumer/registerConsumer";
	public static final String GET_TRANSACTION_FRONTIER = "/IssuerWeb/rest/consumer/getTransactionFrontier";
	public static final String CREATE_TRANSACTIONS = "/IssuerWeb/rest/consumer/createTransactions";
	public static final String GETNUMBERCONSUMERSFILTERED_URL = "/IssuerWeb/rest/consumer/getNumberOfAllConsumersFiltered";
	public static final String READCONSUMERSFILTERED_URL = "/IssuerWeb/rest/consumer/readAllConsumersFiltered";
	public static final String READALLCONSUMER_URL = "/IssuerWeb/rest/consumer/readAllConsumer";
	public static final String READCARDS_FILTERED_URL = "/IssuerWeb/rest/card/readAllCardsFiltered";
	public static final String GETNUMBERCARDSFILTERED_URL = "/IssuerWeb/rest/card/getNumberOfAllCardsFiltered";
	public static final String UPDATECARD_URL	= "/IssuerWeb/rest/card/updateCard";
	public static final String CREATEAPPLET_URL = "/IssuerWeb/rest/applet/createAllApplet";
	public static final String READUNBLOCKCOMMANDS_URL = "/IssuerWeb/rest/applet/getUnblockCommands";	
	public static final String READAPPLET_URL = "/IssuerWeb/rest/applet/readAllApplet";
	public static final String GETAUTHENTICATETEMPLATE_URL = "/IssuerWeb/rest/security/getAuthenticationTemplate";
	public static final String GET_OPENPURSE_FORLOAD_URL = "/IssuerWeb/rest/load/getOpenPurseForLoadCommand";
	public static final String READ_LOAD_RESULT = "/IssuerWeb/rest/load/readLoad";
	public static final String AUTHORIZE_URL = "/IssuerWeb/rest/load/authorize";
	public static final String GET_OPEN_APPLET_URL = "/ParkingOperatorWeb/rest/terminal/getOpenAppletCommand";
	public static final String SET_LOADRESULT_URL = "/IssuerWeb/rest/load/setLoadResult";
	public static final String LOGIN_ISSUER_URL = "/IssuerWeb/rest/authentication/login";
	public static final String LOGIN_ASTYPE_ISSUER_URL = "/IssuerWeb/rest/authentication/loginAsType";
	public static final String LOGIN_ACQUIRER_URL = "/AcquirerWeb/rest/authentication/login";
	public static final String LOGOUT_ISSUER_URL = "/IssuerWeb/rest/authentication/logout";
	public static final String LOGOUT_ACQUIRER_URL = "/AcquirerWeb/rest/authentication/logout";
	public static final String LOGIN_PARKING_URL = "/ParkingOperatorWeb/rest/authentication/login";
	public static final String LOGOUT_PARKING_URL = "/ParkingOperatorWeb/rest/authentication/logout";
	
	
	public static final String HTTP_GET = "GET";
	public static final String HTTP_POST = "POST";
	
	private boolean internetConnection = false;
	
	public ConnectionWrapper(Activity activity) {
		this.activity = activity;
		this.client = new HTTPClient("");
	}


	public CookieStore getCookieStore() {
		return cookieStore;
	}


	public void setCookieStore(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}


	public void setClient(HTTPClient client) {
		this.client = client;
	}


	public HTTPClient getClient() {
		return client;
	}


	public Activity getActivity() {
		return activity;
	}


	public void setParams(ArrayList<NameValuePair> params) {
		this.params = params;
	}


	public void setHeaders(ArrayList<NameValuePair> headers) {
		this.headers = headers;
	}

	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getResponse() {
		return response;
	}


	public void setResponse(String response) {
		this.response = response;
	}

	
	public int getResponseCode() {
		return responseCode;
	}


	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}


	public void launchConnection(){
		try {
			new HandleRequest(this.activity).execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean performHttpRequest(String op, String url){
		client.setURL(url);
		
		client.setHeaders(headers);
		client.setParams(params);
		if(op.equals(HTTP_POST))
			client.setCookieStore(cookieStore);
		setInternetConnection(false);
		boolean result = false;
		try {
			if(op.equals(HTTP_POST))
			{
				result = client.Execute(HTTPClient.RequestMethod.POST);
				cookieStore = client.getCookieStore();
			}
			else if (op.equals(HTTP_GET))
			{
				result = client.Execute(HTTPClient.RequestMethod.GET);
			}
			setInternetConnection(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		response = client.getResponse();
		responseCode = client.getResponseCode();
		return result;
	}
	

	public boolean isInternetConnectionOn() {
		return internetConnection;
	}


	public void setInternetConnection(boolean internetConnection) {
		this.internetConnection = internetConnection;
	}
	
	
	
	
}
