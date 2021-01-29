package uk.co.transaxiom.android.txandroidlib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class HTTPClient extends DefaultHttpClient {

	private String URL;
	private ArrayList<NameValuePair> params;
	private ArrayList<NameValuePair> headers;

	private String response;
	private int responseCode;
	private String message;

	public enum RequestMethod {
		GET, POST
	}

	public HTTPClient(String url) {
		this.URL = url;
		params = new ArrayList<NameValuePair>();
		headers = new ArrayList<NameValuePair>();
		
		//SSL stuff
//		ALLOW_ALL_HOSTNAME_VERIFIER
//		BROWSER_COMPATIBLE_HOSTNAME_VERIFIER
//		STRICT_HOSTNAME_VERIFIER // fails because host name doesn't match certificate's common name

		
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		SchemeRegistry registry = new SchemeRegistry();
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 443));
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		
		
		Log.d("HTTPClient", "HTTP client created for URL: " + URL);
	}
	  
	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	
	public ArrayList<NameValuePair> getParameters() {
		return params;
	}

	public void setParams(ArrayList<NameValuePair> params) {
		this.params = params;
	}

	public ArrayList<NameValuePair> getHeaders() {
		return headers;
	}

	public void setHeaders(ArrayList<NameValuePair> headers) {
		this.headers = headers;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Boolean Execute(RequestMethod method) throws Exception {
		Log.v("HTTPClientBody","Executing method...");

		switch (method) {
		
		case GET: {
			Log.v("HTTPClientBody","Method is GET");
			Log.d("HTTPClient", "Sending HTTP GET request: " + URL);
			// add parameters
			String combinedParams = "";
			if (!params.isEmpty()) {
				combinedParams += "?";
				for (NameValuePair p : params) {
					String paramString = p.getName() + "="
							+ URLEncoder.encode(p.getValue(), "UTF-8");
					if (combinedParams.length() > 1) {
						combinedParams += "&" + paramString;
					} else {
						combinedParams += paramString;
					}
				}
			}

			Log.d("HTTPClient", "Sending HTTP GET request: " + URL
					+ combinedParams);
			HttpGet request = new HttpGet(URL + combinedParams);

			// add headers
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
			}

			executeRequest(request, URL);
			break;
		}
		case POST: {
			Log.v("HTTPClientBody","Method is POST");
			Log.d("HTTPClient", "Sending HTTP POST request: " + URL);
			HttpPost request = new HttpPost(URL);

			// add headers
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
			}

			if (!params.isEmpty()) {
			
				if(params.get(0).getName().equals("body")){	
					Log.v("HTTPClientBody","body is supposed to be:"+params.get(0).getValue());
					request.setEntity(new StringEntity((params.get(0).getValue()), HTTP.UTF_8));
				}else {
					
					request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				}
			}
				
				if(request.getEntity() != null){
					String tmp = convertStreamToString(request.getEntity().getContent());
					Log.e("HTTPClientBody","Entity:"+tmp);
				}
				else{
					Log.e("HTTPClientBody","Entity is empty!");
				}
						
			
			executeRequest(request, URL);
			break;
		}
		}
		return true;
	}

	private void executeRequest(HttpUriRequest request, String url)
			throws ClientProtocolException, IOException {
		
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");

		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");

		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");

		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
		
		
		HttpResponse httpResponse;
		
		Log.d("HTTPCLIENT", "CookieStore BEFORE request: "+this.getCookieStore().toString());
		
		httpResponse = this.execute(request);
		
		Log.d("HTTPCLIENT", "CookieStore AFTER request: "+this.getCookieStore().toString());
			
		
		responseCode = httpResponse.getStatusLine().getStatusCode();
		message = httpResponse.getStatusLine().getReasonPhrase();

		HttpEntity entity = httpResponse.getEntity();

		if (entity != null) {

			InputStream instream = entity.getContent();
			response = convertStreamToString(instream);

			// Closing the input stream will trigger connection release
			instream.close();
		} else{
			Log.v("HTTPClient", "Response entity is empty!");
			response = "null";
		}
		// client.getConnectionManager().shutdown(); //was in the catch
	}

	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}