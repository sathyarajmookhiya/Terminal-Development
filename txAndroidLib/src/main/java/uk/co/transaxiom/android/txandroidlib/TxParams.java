package uk.co.transaxiom.android.txandroidlib;

import java.util.HashMap;
import java.util.Map;

public class TxParams {
	
	private static TxParams instance = new TxParams();
	
	private final Map<String, Object> params = new HashMap<String, Object>();
	
	private TxParams (){
	}
	
	public static TxParams getTxParams(){
		
		return instance;
	}
	
	public void setParam(String name, Object param){
		
		params.put(name, param);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getParam(String name){
		
		return (T) (params.get(name));
	}
}
