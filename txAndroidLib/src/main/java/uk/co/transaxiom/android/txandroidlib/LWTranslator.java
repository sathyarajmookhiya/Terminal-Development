package uk.co.transaxiom.android.txandroidlib;

import static uk.co.transaxiom.terminal.currency.CurrencyManager.getCurrencyManager;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.transaxiom.acquirer.services.lw.AppletMessageLW;
import uk.co.transaxiom.acquirer.services.lw.MerchantLW;
import uk.co.transaxiom.acquirer.services.lw.NCounterBatchLW;
import uk.co.transaxiom.acquirer.services.lw.NCounterLW;
import uk.co.transaxiom.acquirer.services.lw.ProfileLW;
import uk.co.transaxiom.acquirer.services.lw.PropertyLW;
import uk.co.transaxiom.acquirer.services.lw.TerminalIdLW;
import uk.co.transaxiom.acquirer.services.lw.TerminalLW;
import uk.co.transaxiom.acquirer.services.lw.TransactionLW;
import uk.co.transaxiom.acquirer.services.rest.lw.AppObjectList;
import uk.co.transaxiom.android.txandroidlib.card.DisplayTransaction;
import uk.co.transaxiom.android.txandroidlib.terminal.AndroidTerminalWrapper;
import uk.co.transaxiom.android.txandroidlib.terminal.AppletMessage;
import uk.co.transaxiom.android.txandroidlib.terminal.RequestedNCounter;
import uk.co.transaxiom.issuer.services.lw.CardLW;
import uk.co.transaxiom.issuer.services.lw.ConsumerLW;
import uk.co.transaxiom.issuer.services.lw.CumulativeTypeLW;
import uk.co.transaxiom.issuer.services.lw.FrontierLW;
import uk.co.transaxiom.issuer.services.lw.LoadStateLW;
import uk.co.transaxiom.issuer.services.lw.OrderLW;
import uk.co.transaxiom.issuer.services.lw.UnblockCommandLW;
import uk.co.transaxiom.services.lw.AddressLW;
import uk.co.transaxiom.services.lw.ApduLW;
import uk.co.transaxiom.services.lw.FciLW;
import uk.co.transaxiom.services.lw.IdLW;
import uk.co.transaxiom.services.lw.MoneyLW;
import uk.co.transaxiom.services.lw.StringLW;
import uk.co.transaxiom.services.lw.ThemeCatalogueLW;
import uk.co.transaxiom.services.lw.ThemeLW;
import uk.co.transaxiom.services.lw.TimestampLW;
import uk.co.transaxiom.services.lw.TransferDetailsLW;
import uk.co.transaxiom.terminal.TxTerminal;
import uk.co.transaxiom.terminal.ncounters.NCountersBatch;
import uk.co.transaxiom.terminal.ncounters.TXnCounter;
import uk.co.transaxiom.terminal.payment.entity.TxTransaction;
import uk.co.transaxiom.util.lw.BinaryUtils;
import android.util.Log;

public class LWTranslator {
	
	private static LWTranslator instance = new LWTranslator();
	public static final String JSON_DATA = "data";
	public static final String JSON_VALUE = "value";
	
	public static final String PROPERTY_NCOUNTER_GROUPCOUNT = "nCounter.groupCount";
	public static final String PROPERTY_NCOUNTER_FLOATCOUNT = "nCounter.floatCount.";
	public static final String PROPERTY_NCOUNTER_NARRATIVE = "nCounter.narrative.";
	public static final String PROPERTY_NCOUNTER_CURRENCY = "nCounter.currency.";
	public static final String PROPERTY_NCOUNTER_FLOAT = "nCounter.float.";
	
	private LWTranslator() {
	}

	public static LWTranslator getInstance() {
		
		return instance;
	}
	
	public List<NCounterBatchLW> toNCounterBatchsLW(JSONArray json){
		List<NCounterBatchLW> result = new ArrayList<NCounterBatchLW>();
		
		try{
			for(int i=0; i<json.length(); i++){
				JSONObject object = json.getJSONObject(i);
				NCounterBatchLW batchLW = toNCounterBatchLW(object);
				
				if(batchLW != null)
					result.add(batchLW);
			
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public FrontierLW toFrontierLW(String response) throws JSONException{
		FrontierLW frontierLW = new FrontierLW();
		
		JSONObject json = new JSONObject(response);
		
		frontierLW.setLoadCumulativePennies(json.getLong("loadCumulativePennies"));
		frontierLW.setPaymentCumulativePennies(json.getLong("paymentCumulativePennies"));
		
		return frontierLW;
	}
	
	public uk.co.transaxiom.issuer.services.lw.TransactionLW toTransactionLW(DisplayTransaction transaction){
		uk.co.transaxiom.issuer.services.lw.TransactionLW transactionLW = new uk.co.transaxiom.issuer.services.lw.TransactionLW();
		
		int fractionDigits = getCurrencyManager().getCurrency(transaction.getCurrencyCode()).getFractionDigits();
		
		BigDecimal penniesBd = new BigDecimal(transaction.getAmount()).movePointRight(fractionDigits);
		BigDecimal cumulPenniesBd = new BigDecimal(transaction.getCumulativeAmount()).movePointRight(fractionDigits);
		
		transactionLW.setPennies(penniesBd.longValue());
		transactionLW.setCumulativePennies(cumulPenniesBd.longValue());
		transactionLW.setBalancePennies(transaction.getBalanceInPennies());
		
		OrderLW frontier = new OrderLW();
		frontier.setPaymentOrder(transaction.getPayFrontier());
		frontier.setLoadOrder(transaction.getLoadFrontier());
		transactionLW.setOrder(frontier);
		
		if(transaction.getTransactionType().equals("Payment")){
			try {
				transactionLW.setType(CumulativeTypeLW.PAYMENT);
				byte[] byteNarrative = transaction.getNarrative().getBytes("UTF-8");
				transactionLW.setNarrative(BinaryUtils.encode(byteNarrative));
			} catch (UnsupportedEncodingException e) {
			} 
		}else{
			transactionLW.setType(CumulativeTypeLW.LOAD);
			transactionLW.setNarrative("");
		}
		return transactionLW;
	}
	
	
	public List<uk.co.transaxiom.issuer.services.lw.TransactionLW> toTransactionLWs(List<DisplayTransaction> transactions){
		List<uk.co.transaxiom.issuer.services.lw.TransactionLW> transactionLWs = new ArrayList<uk.co.transaxiom.issuer.services.lw.TransactionLW> ();
		
		for(int i=transactions.size()-1; i>=0; i--){
			
			transactionLWs.add(toTransactionLW(transactions.get(i)));
		}
		
		return transactionLWs;
	}
	
	public LoadStateLW toLW(String response){
		response = response.replaceAll("\"", "").trim();
		
		return LoadStateLW.valueOf(response);
	}
	
	public ThemeLW toThemeLW(String response){
		ThemeLW theme = new ThemeLW();
		
		try {
			JSONObject json = new JSONObject(response);
			theme.setDarkColour(json.getString("darkColour"));
			theme.setHighlightColour(json.getString("highlightColour"));
			theme.setIconURL(json.getString("iconURL"));
			theme.setMainColour(json.getString("mainColour"));
			theme.setPageColour(json.getString("pageColour"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return theme;
	}
	
	public TxTheme fromLW(ThemeLW themeLW){
		TxTheme theme = new TxTheme();
		
		theme.setColorMain("#FF"+themeLW.getMainColour());
		theme.setColorDark("#FF"+themeLW.getDarkColour());
		theme.setColorHighlight("#FF"+themeLW.getHighlightColour());
		theme.setPageColor("#FF"+themeLW.getPageColour());
		theme.setBackgroundColor("#FF"+themeLW.getPageColour());
		
		if(themeLW.getPageColour().equalsIgnoreCase("FFFFFF")){
			theme.setBackgroundColor("#25"+themeLW.getMainColour());
		}
		
		theme.setSlogan("");
		theme.setLogoLocation(themeLW.getIconURL());
		
		return theme;
	}
	
	
	public ThemeCatalogueLW toThemeCatalogueLW(String response){
		ThemeCatalogueLW themeCatalogue = new ThemeCatalogueLW();
		List<String> names = new ArrayList<String>();
		
		try{
			JSONObject obj = new JSONObject(response);
			JSONArray json = obj.getJSONArray("themeNames");
			for(int i=0; i<json.length(); i++){
				names.add(json.getString(i));
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		themeCatalogue.setThemeNames(names);
		return themeCatalogue;
	}
	
	public NCounterBatchLW toNCounterBatchLW(JSONObject json){
		NCounterBatchLW result = new NCounterBatchLW();
		
		try {
			result.setBatchId(json.getString("batchId").replace("-", ""));
			
			JSONArray jsonNCounters = json.getJSONArray("counters");
			List<NCounterLW> ncountersLW = new ArrayList<NCounterLW>();
			
			for(int i=0 ; i<jsonNCounters.length(); i++){
				NCounterLW ncounterLW = toNCounterLW(jsonNCounters.getJSONObject(i));
				
				if(ncounterLW != null)
					ncountersLW.add(ncounterLW);
			}
			result.setCounters(ncountersLW);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return result;
	}
	
	public PropertyLW toPropertyLW(JSONObject json) throws JSONException{
		PropertyLW propertyLW = new PropertyLW();
		
		propertyLW.setName(json.getString("name"));
		propertyLW.setType(json.getString("type"));
		propertyLW.setValue(json.getString("value"));
		
		return propertyLW;
	}
	
	public ProfileLW toProfileLW(JSONObject json) throws JSONException{
		ProfileLW profileLW = new ProfileLW();
		
		profileLW.setName(json.getString("name"));
		profileLW.setProfileId(json.getString("profileId"));

		Map<String, PropertyLW> properties = new HashMap<String, PropertyLW>();
		
		PropertyLW groupCount = new PropertyLW();
		JSONObject propertyJson = json.getJSONObject("properties");
		
		groupCount = toPropertyLW(propertyJson.getJSONObject(PROPERTY_NCOUNTER_GROUPCOUNT));
		properties.put(PROPERTY_NCOUNTER_GROUPCOUNT, groupCount);
		
		long nbGroups = groupCount.asLong();
		
		for(int i=1; i<= nbGroups; i++){
			
			PropertyLW propertyCurrency = toPropertyLW(propertyJson.getJSONObject(PROPERTY_NCOUNTER_CURRENCY+i));
			PropertyLW propertyNarrative = toPropertyLW(propertyJson.getJSONObject(PROPERTY_NCOUNTER_NARRATIVE+i));
			PropertyLW propertyFloatCount = toPropertyLW(propertyJson.getJSONObject(PROPERTY_NCOUNTER_FLOATCOUNT+i));

			properties.put(PROPERTY_NCOUNTER_CURRENCY+i, propertyCurrency);
			properties.put(PROPERTY_NCOUNTER_NARRATIVE+i, propertyNarrative);
			properties.put(PROPERTY_NCOUNTER_FLOATCOUNT+i, propertyFloatCount);
			
			long floatCount = propertyFloatCount.asLong();
			
			for(int j=1; j<=floatCount; j++){
				
				PropertyLW ncounterProperty = toPropertyLW(propertyJson.getJSONObject(PROPERTY_NCOUNTER_FLOAT+i+"."+j));
				properties.put(PROPERTY_NCOUNTER_FLOAT+i+"."+j, ncounterProperty);	
			}
		}
		profileLW.setProperties(properties);
		
		return profileLW;
	}
	
	public List<RequestedNCounter> fromLW(ProfileLW profileLW) throws UnsupportedEncodingException {

		List<RequestedNCounter> ncounters = new ArrayList<RequestedNCounter>();
		Map<String, PropertyLW> properties = profileLW.getProperties();

		long nbGroup = properties.get(PROPERTY_NCOUNTER_GROUPCOUNT).asLong();
		for(int i=1; i<=nbGroup; i++){
			
			String currency = properties.get(PROPERTY_NCOUNTER_CURRENCY + i).asString();
			String tmp = properties.get(PROPERTY_NCOUNTER_NARRATIVE + i).asString();
			byte[] bNarrative = BinaryUtils.decode(tmp);
			String narrative = new String(bNarrative, "UTF-8");

			long floatCount = properties.get(PROPERTY_NCOUNTER_FLOATCOUNT + i).asLong();
			for(int j=1; j<=floatCount; j++){
				
				List<Long> floatValues = properties.get(PROPERTY_NCOUNTER_FLOAT + i + "." + j).asListLong();
				long nbOfNcounters = floatValues.get(0);
				for(int k=1; k<=nbOfNcounters; k++){
					
					AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
						RequestedNCounter nCounterBcd = wrapper.createRequestedNCounterBCD(currency, 
								floatValues.get(1).intValue(), 
								floatValues.get(2).shortValue(), 
								narrative);
						ncounters.add(nCounterBcd);
						
						RequestedNCounter nCounterLong = wrapper.createRequestedNCounterLong(currency, 
								floatValues.get(1).intValue(), 
								floatValues.get(2).shortValue(), 
								narrative);
						ncounters.add(nCounterLong);
					}
				}
			}
		
		return ncounters;
	}
	
	public NCountersBatch fromLW(NCounterBatchLW batchLW){
		NCountersBatch ncounters = new NCountersBatch();
		
		List<NCounterLW> ncountersLW = batchLW.getCounters();
		for(NCounterLW ncounterLW : ncountersLW){
			TXnCounter ncounter = fromLW(batchLW.getBatchId(), ncounterLW);
			ncounters.add(ncounter);
		}
		
		return ncounters;
	}
	
	
	public AppObjectList<NCounterLW> toLW(List<TXnCounter> nCounters){
		AppObjectList<NCounterLW> result = new AppObjectList<NCounterLW>();
		
		for(TXnCounter nCounter : nCounters){
			result.add(toLW(nCounter));
		}
		
		return result;
		
	}
	
	public TXnCounter fromLW(String batchId, NCounterLW ncLW){
		TXnCounter result = new TXnCounter();
		
		result.setnCXn(BinaryUtils.decode(ncLW.getXn()));
		result.setBatchId(BinaryUtils.decode(batchId));
		result.setnCIssuerKeyID(BinaryUtils.decode(ncLW.getIssuerKeyId()));
		result.setnCIssuerUnit(BinaryUtils.decode(ncLW.getIssuerUnit()));
		result.setCreationDate(ncLW.getCreationDate());
		result.setValidity(ncLW.getValiditySpan());
		result.setnCLength(ncLW.getLength());
		result.setnCLoc((ncLW.getLength()).byteValue());
		result.setnCMerchantUnit(BinaryUtils.decode(ncLW.getMerchantUnit()));
		result.setnCSeed(BinaryUtils.decode(ncLW.getSeed()));
		result.setnCValue(BinaryUtils.decode(ncLW.getXn()));
		result.setnCRedeemedSteps(Short.valueOf("0"));
		result.setnCStepsUsed(Short.valueOf("0"));
		try {
			result.setNarrative(ncLW.getNarrative().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
	
	public MerchantLW toMerchantLW(String response) throws JSONException{
		MerchantLW merchantLW = new MerchantLW();
		
		JSONObject json = new JSONObject(response);

		JSONObject jsonId = json.getJSONObject("merchantId");
		IdLW idLW = new IdLW();
		idLW.setValue(jsonId.getString("value"));
		
		JSONObject jsonAddress = json.getJSONObject("address");
		AddressLW addressLW = toAddressLW(jsonAddress.toString());
		
		JSONObject jsonTransfer = json.getJSONObject("transferDetails");
		TransferDetailsLW transferDetailsLW = toStringLW(TransferDetailsLW.class, jsonTransfer.toString());
		
		merchantLW.setMerchantId(idLW);
		merchantLW.setAddress(addressLW);
		merchantLW.setTransferDetails(transferDetailsLW);
		merchantLW.setEmail(json.getString("email"));
		merchantLW.setName(json.getString("name"));
		merchantLW.setNumberOfActiveTerminals(json.getLong("numberOfActiveTerminals"));
		
		return merchantLW;
	}
	
	public <T> T toStringLW(Class <T> clazz, String json) throws JSONException{
		
		T result = null;
		try {
			result = (T) clazz.newInstance();
		} catch (Exception e) {
		}
		
		JSONObject stringJson = new JSONObject(json);
		((StringLW)result).setValue(stringJson.getString("value"));
		
		return result;
	}
	
	public AddressLW toAddressLW(String json) throws JSONException{
		AddressLW addressLW = new AddressLW();
		
		JSONObject addressJson = new JSONObject(json);
		
		addressLW.setLocality(addressJson.getString("locality"));
		addressLW.setNameOrNumber(addressJson.getString("nameOrNumber"));
		addressLW.setPostCode(addressJson.getString("postCode"));
		addressLW.setPostTown(addressJson.getString("postTown"));
		addressLW.setStreetName(addressJson.getString("streetName"));
		
		return addressLW;
	}
	
	public TerminalLW toLW(TxTerminal txTerminal){
		TerminalLW result = new TerminalLW();
		TerminalIdLW idLW = new TerminalIdLW();
		Log.d("LWTRANSLATOR", "type="+txTerminal.getTerminalType());
		idLW.setType(txTerminal.getTerminalType());
		idLW.setTag(txTerminal.getTerminalTag());
		
		result.setTerminalId(idLW);
		result.setDescription(txTerminal.getTerminalDescription());
		result.setLocation(txTerminal.getTerminalLocation());
		result.setDeviceId(txTerminal.getTerminalDeviceId());
		result.setActive(txTerminal.isActive());
		result.setReplacement(txTerminal.isReplacement());
		Log.i("LWTranslator", "active is "+txTerminal.isActive());
		return result;
	}
	
	public AppletMessageLW toLW(AppletMessage message){
		AppletMessageLW appletMessageLW = new AppletMessageLW();
		
//		appletMessageLW.setAppletSerialNumber(message.getAppletSerialNumber());
		appletMessageLW.setIndex(message.getSeqNumber());
//		appletMessageLW.setCommandAPDU(message.getCommandApdu());
		appletMessageLW.setResponseAPDU(message.getResponseApdu());
		
		return appletMessageLW;
	}
	
	public List<AppletMessageLW> toLWs(List<AppletMessage> messages){
		List<AppletMessageLW> appletMessages = new ArrayList<AppletMessageLW>();
		
		for (AppletMessage message : messages) {
			appletMessages.add(toLW(message));
		}
		
		return appletMessages;
	}
	
	
	public AppletMessage fromLW(AppletMessageLW appletMessageLW){
		AppletMessage message = new AppletMessage();
		
		message.setAppletSerialNumber(appletMessageLW.getAppletSerialNumber());
		message.setSeqNumber(appletMessageLW.getIndex());
		message.setCommandApdu(appletMessageLW.getCommandAPDU());
		
		return message;
	}

	public AppletMessageLW toAppletMessageLW(String json){
		AppletMessageLW appletMessageLW = new AppletMessageLW();
		
		try{
			JSONObject jsonObject = new JSONObject(json);
			appletMessageLW.setAppletSerialNumber(jsonObject.getString("appletSerialNumber"));
			appletMessageLW.setCommandAPDU(jsonObject.getString("commandAPDU"));
			appletMessageLW.setIndex(jsonObject.getLong("index"));
		} catch(JSONException e){
			e.printStackTrace();
		}
		
		return appletMessageLW;
	}
	
	public List<AppletMessageLW> toAppletMessageLWs(String json){
		List<AppletMessageLW> messagesLW = new ArrayList<AppletMessageLW>();
		
		try {
			JSONObject jsonObject = new JSONObject(json);
			JSONArray jsonArray = jsonObject.getJSONArray("item");
			for(int i=0; i < jsonArray.length(); i++){
				messagesLW.add(toAppletMessageLW(jsonArray.getString(i)));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return messagesLW;
	}
	
	public List<AppletMessage> fromLWs(List<AppletMessageLW> appletMessagesLW){
		List<AppletMessage> messages = new ArrayList<AppletMessage>();
		
		for (AppletMessageLW appletMessageLW : appletMessagesLW) {
			messages.add(fromLW(appletMessageLW));
		}
		
		return messages;
	}
	
	public TxTerminal fromLW(TerminalLW terminalLW){
		TxTerminal result = new TxTerminal();
		
		result.setTerminalType(terminalLW.getTerminalId().getType());
		result.setTerminalTag(terminalLW.getTerminalId().getTag());
		result.setTerminalLocation(terminalLW.getLocation());
		result.setTerminalDescription(terminalLW.getDescription());
		result.setTerminalDeviceId(terminalLW.getDeviceId());
		result.setReplacement(true);
		result.setActive(terminalLW.getActive());
		
		return result;
	}
	
	public NCounterLW toLW (TXnCounter txNC){
		NCounterLW result = new NCounterLW();
		result.setXn(BinaryUtils.encode(txNC.getnCXn()));
		result.setValue(BinaryUtils.encode(txNC.getnCValue()));
		result.setSeed(BinaryUtils.encode(txNC.getnCSeed()));
		
		return result;
		
	}
	
	public NCounterLW toNCounterLW (JSONObject ncounterJSON){
		NCounterLW result = new NCounterLW();
		
		try {
			result.setXn(ncounterJSON.getString("xn"));
			result.setIssuerKeyId(ncounterJSON.getString("issuerKeyId"));
			result.setIssuerUnit(ncounterJSON.getString("issuerUnit"));
			result.setSeed(ncounterJSON.getString("seed"));
			result.setCreationDate(((Integer)(ncounterJSON.get("creationDate"))).shortValue());
			result.setValiditySpan(((Integer) ncounterJSON.get("validitySpan")).shortValue());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return result;
	}
	
	public NCounterLW toLW (RequestedNCounter ncounter){
		NCounterLW result = new NCounterLW();
		byte[] narrativeT = ncounter.getNarrative();
		
		Log.d("LWTranslator", "requested ncounter to ncLW: "+ncounter.getLength()+" -- "+Utils.getHexString(ncounter.getMerchantUnit()));
		
		
		result.setMerchantUnit(BinaryUtils.encode(ncounter.getMerchantUnit()));
		result.setLength(ncounter.getLength());
		result.setNarrative(BinaryUtils.encode(narrativeT));
        
		return result;
	}
	
	public FciLW toLW (byte[] fci){
		FciLW result = new FciLW();
		
		result.setData(BinaryUtils.encode(fci));
		return result;
	}

	public List<TerminalLW> toTerminalsLW(JSONArray json) {

		try{
			
			List<TerminalLW> terminals = new ArrayList<TerminalLW>();
			for(int i=0; i<json.length(); i++){
				JSONObject object = json.getJSONObject(i);
				TerminalLW terminal = toTerminalLW(object);
				
				if(terminal != null)
					terminals.add(terminal);
			
			}
			
			return terminals;
		} catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<CardLW> toCardsLW(JSONArray array){
		
		try{
			List<CardLW> cards = new ArrayList<CardLW>();
			for(int i=0; i<array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				CardLW card = toCardLW(object);
				
				if(card != null){
					cards.add(card);
				}
			}
			
			return cards;
		}catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}
	
	
	public TransactionLW toLW(TxTransaction transaction){
		
		TransactionLW result = new TransactionLW();
		
		result.setSequenceNumber(Long.valueOf(transaction.getReference()));
		
		TerminalIdLW idLW = new TerminalIdLW();
		idLW.setTag(transaction.getTerminalTag());
		idLW.setType(transaction.getTerminalType());
		result.setTerminalId(idLW);

		TimestampLW stampLW = new TimestampLW(transaction.getTimestamp());
		result.setStamp(stampLW);
		
		BigDecimal bd = new BigDecimal(transaction.getAmount());
		bd = bd.movePointRight(2);
		MoneyLW amount = new MoneyLW();
		amount.setCurrencyCode(transaction.getCurrencyCode());
		amount.setPennies(bd.longValue());
		result.setAmount(amount);

		result.setComments(transaction.getNewBalance()+transaction.getCurrencySymbol()+" | "+transaction.getAppletSerialNumber());
		
		return result;
		
	}
	
	public CardLW toCardLW(JSONObject json){
		try{
			CardLW card = new CardLW();
			
			card.setCardId(json.getString("cardId"));
			card.setConsumerId(json.getString("consumerId"));
			card.setDescription(json.getString("description"));
			card.setSerialNumber(json.getString("serialNumber"));
			
			return card;
		}catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public TerminalLW toTerminalLW(JSONObject json) {
		
		try{
			TerminalLW terminal = new TerminalLW();
			
			JSONObject jsonStamp = json.getJSONObject("stamp");
			TimestampLW stampLW = new TimestampLW(jsonStamp.getLong(JSON_VALUE));
			
			JSONObject jsonIdLw = json.getJSONObject("terminalId");
			TerminalIdLW idLW = new TerminalIdLW();
			idLW.setTag(jsonIdLw.getString("tag"));
			idLW.setType(jsonIdLw.getString("type"));
			
			terminal.setStamp(stampLW);
			terminal.setTerminalId(idLW);
			terminal.setDescription(json.getString("description"));
			terminal.setLocation(json.getString("location"));
			terminal.setDeviceId(json.getString("deviceId"));
			terminal.setActive(json.getBoolean("active"));
			
			return terminal;
		}catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}

	public ConsumerLW toConsumerLW(JSONObject json){
		try{
			ConsumerLW consumer = new ConsumerLW();
			
			AddressLW address = new AddressLW();
			JSONObject jsonAddress = json.getJSONObject("address");
			address.setNameOrNumber(jsonAddress.getString("nameOrNumber"));
			address.setStreetName(jsonAddress.getString("streetName"));
			address.setPostCode(jsonAddress.getString("postCode"));
			address.setLocality(jsonAddress.getString("locality"));
			address.setPostTown(jsonAddress.getString("postTown"));
			
			consumer.setAddress(address);
			
			consumer.setConsumerId(json.getString("consumerId"));
			consumer.setEmail(json.getString("email"));
			consumer.setName(json.getString("name"));
			
			JSONObject jsonTransfer = json.getJSONObject("transferDetails");
			TransferDetailsLW details = new TransferDetailsLW();
			details.setValue(jsonTransfer.getString("value"));
			
			consumer.setTransferDetails(details);
			
			return consumer;
		}catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public List<ConsumerLW> toConsumersLW(JSONArray array) {
		try{
			List<ConsumerLW> consumers = new ArrayList<ConsumerLW>();
			for(int i=0; i<array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				ConsumerLW card = toConsumerLW(object);
				
				if(card != null){
					consumers.add(card);
				}
			}
			
			return consumers;
		}catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}

	public UnblockCommandLW toUnblockCommandLW(JSONObject json){
		try{
			UnblockCommandLW command = new UnblockCommandLW();
			
			ApduLW apdu = new ApduLW();
			JSONObject jsonApdu = json.getJSONObject("unblockCommand");
			apdu.setData(jsonApdu.getString("data"));
			
			command.setUnblockCommand(apdu);
			command.setAppletSerialNumber(json.getString("appletSerialNumber"));
			
			return command;
		}catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public List<UnblockCommandLW> toUnblockCommandLWs(JSONArray array) {
		try{
			List<UnblockCommandLW> commands = new ArrayList<UnblockCommandLW>();
			for(int i=0; i<array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				UnblockCommandLW command = toUnblockCommandLW(object);
				
				if(command != null){
					commands.add(command);
				}
			}
			
			return commands;
		}catch(JSONException e){
			e.printStackTrace();
			return null;
		}
	}

}
