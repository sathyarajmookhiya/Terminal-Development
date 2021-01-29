package uk.co.transaxiom.android.txandroidlib.terminal;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.transaxiom.android.txandroidlib.Utils;
import uk.co.transaxiom.terminal.ncounters.NCountersBatch;
import uk.co.transaxiom.terminal.ncounters.TXnCounter;
import uk.co.transaxiom.terminal.payment.entity.TxTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBTXTerminalLib {

	private DatabaseHelper DBHelper;
	private final Context context;
	private SQLiteDatabase db;
	
	// singleton
    private static DBTXTerminalLib instance = null;
    
	private static final String DB_NAME = "TXTerminalLibDB";
	private static final int DB_VERSION = 1;
	
	
        byte[] demoKey1 = new byte[] {0x00, 0x00, 0x00, 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44};
        byte[] demoKey2 = new byte[] {0x00, 0x00, 0x00, 0x02, (byte) 0xAA, (byte) 0x55, (byte) 0xCC, (byte) 0x66};
        byte[] demoKey3 = new byte[] {0x00, 0x00, 0x00, 0x04, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78};
        byte[] demoKey4 = new byte[] {0x00, 0x00, 0x00, 0x06, (byte) 0x9A, (byte) 0xAB, (byte) 0xBC, (byte) 0xCD};

        byte[] liveKey1 = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x01, (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF, (byte) 0x00};
        byte[] liveKey2 = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x02, (byte) 0xAA, (byte) 0x55, (byte) 0xCC, (byte) 0x66, (byte) 0xEE, (byte) 0x77, (byte) 0x00, (byte) 0x88, (byte) 0x22, (byte) 0x99, (byte) 0x44, (byte) 0xAA, (byte) 0x66, (byte) 0xBB, (byte) 0x88, (byte) 0xCC};
        byte[] liveKey3 = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x03, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0, (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF};
        byte[] liveKey4 = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x04, (byte) 0x9A, (byte) 0xAB, (byte) 0xBC, (byte) 0xCD, (byte) 0xDE, (byte) 0xEF, (byte) 0xF0, (byte) 0x01, (byte) 0x12, (byte) 0x23, (byte) 0x34, (byte) 0x45, (byte) 0x56, (byte) 0x67, (byte) 0x78, (byte) 0x89};
        
	
	//Table of n-Counters
	private static final String DB_NCOUNTERS_TABLE = "ncounters";
	
	public static final String NCOUNTER_ID = "_id";
	public static final String NCOUNTER_XN = "Xn";
	public static final String NCOUNTER_BATCH_ID = "batchId";
	public static final String NCOUNTER_ISSUER_KEY_ID = "issuerKeyId";
	public static final String NCOUNTER_ISSUER_KEY = "issuerKey";
	public static final String NCOUNTER_SEED = "seed";
	public static final String NCOUNTER_ISSUER_UNITS = "issuerUnits";
	public static final String NCOUNTER_ISSUER_CURRENCY = "issuerCurrency"; 		//only required for sorting purpose
	public static final String NCOUNTER_ISSUER_STEPVALUE = "issuerStepValue";		//only required for sorting purpose
	public static final String NCOUNTER_MERCHANT_UNITS = "merchantUnits";
	public static final String NCOUNTER_MERCHANT_CURRENCY = "merchantCurrency";		//only required for sorting purpose
	public static final String NCOUNTER_MERCHANT_STEPVALUE = "merchantStepValue";	//only required for sorting purpose
	public static final String NCOUNTER_LENGTH = "length";
	public static final String NCOUNTER_STEPS_USED = "stepsUsed";
	public static final String NCOUNTER_CURRENT_VALUE = "currentValue";
	public static final String NCOUNTER_STEPS_REDEEMED = "stepsRedeemed";
	public static final String NCOUNTER_NARRATIVE = "narrative";
	public static final String NCOUNTER_CREATION_DATE = "creationDate";
	public static final String NCOUNTER_VALIDITY_SPAN = "validitySpan";
	
	private static final String DB_CREATE_NCOUNTERS_TABLE = 
			"create table ncounters "
			+" (_id integer primary key autoincrement, "
			+ "Xn blob, " 
			+ "batchId blob, "
			+ "issuerKeyId blob, "
			+ "issuerKey blob, "
			+ "seed blob, "
			+ "issuerUnits blob, "
			+ "issuerCurrency text, "
			+ "issuerStepValue integer, "
			+ "merchantUnits blob, "
			+ "merchantCurrency text, "
			+ "merchantStepValue integer, "
			+ "length integer, "
			+ "stepsUsed integer, "
			+ "currentValue blob, "
			+ "stepsRedeemed integer, "
			+ "narrative blob, "
			+ "creationDate short, "
			+ "validitySpan short);";
	
	//Table of transactions
	private static final String DB_TRANSACTIONS_TABLE = "transactions";

	public static final String TRANSACTION_ID = "_id";
	public static final String TRANSACTION_REFERENCE = "transacationReference";
	public static final String TRANSACTION_TIMESTAMP = "timestamp";
	public static final String TRANSACTION_AMOUNT = "amount";
	public static final String TRANSACTION_CURRENCY = "currency";
	public static final String TRANSACTION_APPLET_SN = "appletSerialNumber";
	public static final String TRANSACTION_TERMINAL_TAG = "terminalTag";
	public static final String TRANSACTION_TERMINAL_TYPE = "terminalType";
	public static final String TRANSACTION_TERMINAL_LOCATION = "terminalLocation";
	public static final String TRANSACTION_NEW_BALANCE = "newBalance";
	public static final String TRANSACTION_UPLOADED = "uploaded";


	private static final String DB_CREATE_TRANSACTIONS_TABLE = 
			"create table transactions "
					+" (_id integer primary key autoincrement, "
					+ TRANSACTION_REFERENCE + " long not null, "
					+ TRANSACTION_TIMESTAMP + " long not null, "
					+ TRANSACTION_AMOUNT + " string not null, "
					+ TRANSACTION_CURRENCY + " string not null, "
					+ TRANSACTION_APPLET_SN + " string not null, "
					+ TRANSACTION_TERMINAL_TAG + " string not null, "
					+ TRANSACTION_TERMINAL_TYPE + " string not null, "
					+ TRANSACTION_TERMINAL_LOCATION + " string not null, "
					+ TRANSACTION_NEW_BALANCE + " string not null, "
					+ TRANSACTION_UPLOADED + " int not null);";
		
		
	//Table of redemptions
	private static final String DB_REDEMPTIONS_TABLE = "redemptions";

	public static final String REDEMPTION_ID = "_id";
	public static final String REDEMPTION_TIMESTAMP = "timestamp";
	public static final String REDEMPTION_CURRENT_VALUE = "currentValue";
	public static final String REDEMPTION_TERMINAL_NAME = "terminalName";
	public static final String REDEMPTION_NCOUNTERID = "ncounterID";
	public static final String REDEMPTION_KEY_ID = "keyID";
	public static final String REDEMPTION_CURRENCY = "currency";


	private static final String DB_CREATE_REDEMPTIONS_TABLE = 
			"create table redemptions "
					+" (_id integer primary key autoincrement, "
					+ "timestamp string not null, "
					+ "currentValue string not null, "
					+ "terminalName string not null, "
					+ "ncounterID string not null, "
					+ "keyID string not null, "
					+ "currency string not null);";

	
	//Table of redeemed values
	private static final String DB_REDEEMEDVALUES_TABLE = "redeemedValues";
	
	public static final String REDEEMEDVALUES_ID = "_id";
	public static final String REDEEMEDVALUES_TIMESTAMP = "timestamp";
	public static final String REDEEMEDVALUES_AMOUNT = "amount";
	public static final String REDEEMEDVALUES_CURRENCY = "currency";
	
	private static final String DB_CREATE_REDEEMEDVALUES_TABLE = 
			"create table "+DB_REDEEMEDVALUES_TABLE
					+" (_id integer primary key autoincrement, "
					+ REDEEMEDVALUES_TIMESTAMP + " string not null, "
					+ REDEEMEDVALUES_AMOUNT + " string not null, "
					+ REDEEMEDVALUES_CURRENCY + " string not null);";
	
	
	// Table of async applet messages
	private static final String DB_APPLET_MESSAGES_TABLE = "appletMessages";
	
	public static final String APPLET_MESSAGE_ID = "_id";
	public static final String APPLET_MESSAGE_ASN = "appletSerialNumber";
	public static final String APPLET_MESSAGE_SEQ = "sequenceNumber";
	public static final String APPLET_MESSAGE_COMMAND = "commandApdu";
	public static final String APPLET_MESSAGE_RESPONSE = "responseApdu";
	
	private static final String DB_CREATE_APPLET_MESSAGES_TABLE = 
			"create table "+DB_APPLET_MESSAGES_TABLE
			+"(" + APPLET_MESSAGE_ID + " integer primary key autoincrement, "
			+ APPLET_MESSAGE_ASN + " string not null, "
			+ APPLET_MESSAGE_SEQ + " long not null, "
			+ APPLET_MESSAGE_COMMAND + " string not null, "
			+ APPLET_MESSAGE_RESPONSE + " string);";
	
	
	
	//Table of n-Counters keys
		private static final String DB_KEYS_TABLE = "ncKeys";
		
		public static final String NCKEY_ID = "_id";
		public static final String NCKEY_VALUE = "key_value";
		
		
		private static final String DB_CREATE_KEYS_TABLE = 
				"create table ncKeys "
				+" (_id integer primary key autoincrement, "
				+ "key_value blob);";
	
	
	

	public DBTXTerminalLib(Context context){
		this.context = context;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper
	{	

		DatabaseHelper(Context context)
		{
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			try {
				db.execSQL(DB_CREATE_NCOUNTERS_TABLE);
				db.execSQL(DB_CREATE_TRANSACTIONS_TABLE);
				db.execSQL(DB_CREATE_REDEMPTIONS_TABLE);
				db.execSQL(DB_CREATE_KEYS_TABLE);
				db.execSQL(DB_CREATE_REDEEMEDVALUES_TABLE);
				db.execSQL(DB_CREATE_APPLET_MESSAGES_TABLE);
			} catch (SQLException e) {
				e.printStackTrace();
				Log.d("DBTEST","DatabaseHelper->onCreate() -> Exception!");
				Log.v("DBTEST", e.getMessage());
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			db.execSQL("DROP TABLE IF EXISTS ncounters");
			db.execSQL("DROP TABLE IF EXISTS transactions");
			db.execSQL("DROP TABLE IF EXISTS narratives");
			db.execSQL("DROP TABLE IF EXISTS redemptions");
			db.execSQL("DROP TABLE IF EXISTS ncKeys");
			db.execSQL("DROP TABLE IF EXISTS "+DB_APPLET_MESSAGES_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "+DB_REDEEMEDVALUES_TABLE);
			onCreate(db);
		}

	}

	/**
	 * Opens the Terminal database for access.
	 * @param None. 
	 * @return The DBTerminalApp handle.
	 */
	public DBTXTerminalLib open() throws SQLException
	{
		try {
			//Toast.makeText(context, "DB opened", Toast.LENGTH_SHORT).show();
			db = DBHelper.getWritableDatabase();
		} catch (SQLException e) {
			Toast.makeText(context, "DB opened - read-only", Toast.LENGTH_SHORT).show();
			db = DBHelper.getReadableDatabase();
		}
		return this;
	}

	/**
	 * Closes the Terminal database. 
	 * @param None. 
	 * @return void
	 */
	public void close()
	{
		DBHelper.close();
		//Toast.makeText(context, "DB closed", Toast.LENGTH_SHORT).show();
	}

	public void createAllTables()
	{
		Log.d("DBTEST","Creating all tables permanently");
		try {
			db.execSQL(DB_CREATE_NCOUNTERS_TABLE);
			db.execSQL(DB_CREATE_TRANSACTIONS_TABLE);
			db.execSQL(DB_CREATE_REDEMPTIONS_TABLE);
			db.execSQL(DB_CREATE_KEYS_TABLE);
			db.execSQL(DB_CREATE_APPLET_MESSAGES_TABLE);
			db.execSQL(DB_CREATE_REDEEMEDVALUES_TABLE);
			insertKey();
			Log.d("DBTEST", "ALL TABLES created");
		} catch (SQLException e) {
			e.printStackTrace();
			Log.d("DBTEST", e.getMessage());
		}
	}

	public void wipeAppletMessages(){
		db.execSQL("DROP TABLE IF EXISTS "+DB_APPLET_MESSAGES_TABLE);
		try {
			db.execSQL(DB_CREATE_APPLET_MESSAGES_TABLE);
			Log.d("DBTEST", DB_APPLET_MESSAGES_TABLE + " deleted");
		} catch (SQLException e) {
			e.printStackTrace();
			Log.d("DBTEST", e.getMessage());
		}
	}
	
	public void wipeRedeemedValues(){
		db.execSQL("DROP TABLE IF EXISTS "+DB_REDEEMEDVALUES_TABLE);
		try {
			db.execSQL(DB_CREATE_REDEEMEDVALUES_TABLE);
			Log.d("DBTEST", "DB_CREATE_REDEEMEDVALUES_TABLE deleted");
		} catch (SQLException e) {
			e.printStackTrace();
			Log.d("DBTEST", e.getMessage());
		}
	}
	

	/**
	 * Cleans the ncounters table. 
	 * @param void 
	 * @return void
	 */
	public void wipeNCounters()
	{
		db.execSQL("DROP TABLE IF EXISTS ncounters");
		try {
			db.execSQL(DB_CREATE_NCOUNTERS_TABLE);
			Log.d("DBTEST", "DB_CREATE_NCOUNTERS_TABLE deleted");
		} catch (SQLException e) {
			e.printStackTrace();
			Log.d("DBTEST", e.getMessage());
		}
	}

	/**
	 * Cleans the transactions table. 
	 * @param void 
	 * @return void
	 */
	public void wipeTransactions()
	{
		db.execSQL("DROP TABLE IF EXISTS transactions");
		try {
			db.execSQL(DB_CREATE_TRANSACTIONS_TABLE);
			Log.d("DBTEST", "DB_CREATE_TRANSACTIONS_TABLE deleted");
		} catch (SQLException e) {
			e.printStackTrace();
			Log.d("DBTEST", e.getMessage());
		}
	}

	/**
	 * Cleans the redemptions table. 
	 * @param void 
	 * @return void
	 */
	public void wipeRedemptions()
	{
		db.execSQL("DROP TABLE IF EXISTS redemptions");
		try {
			db.execSQL(DB_CREATE_REDEMPTIONS_TABLE);
			Log.d("DBTEST", "DB_CREATE_REDEMPTIONS_TABLE deleted");
		} catch (SQLException e) {
			e.printStackTrace();
			Log.d("DBTEST", e.getMessage());
		}
	}
	
	public void wipeAllTables(){
		db.execSQL("DROP TABLE IF EXISTS "+DB_NCOUNTERS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+DB_REDEEMEDVALUES_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+DB_REDEMPTIONS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS "+DB_TRANSACTIONS_TABLE);
		
		try{
			db.execSQL(DB_CREATE_NCOUNTERS_TABLE);
			db.execSQL(DB_CREATE_REDEEMEDVALUES_TABLE);
			db.execSQL(DB_CREATE_REDEMPTIONS_TABLE);
			db.execSQL(DB_CREATE_TRANSACTIONS_TABLE);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	
	private void insertKey ()
	{
		Log.v("DBTEST", "insertKey()");
		ContentValues ncKey1 = new ContentValues();
		ncKey1.put(NCKEY_VALUE, demoKey1); 
		ContentValues ncKey2 = new ContentValues();
		ncKey2.put(NCKEY_VALUE, demoKey2); 
		ContentValues ncKey3 = new ContentValues();
		ncKey3.put(NCKEY_VALUE, demoKey3); 
		ContentValues ncKey4 = new ContentValues();
		ncKey4.put(NCKEY_VALUE, demoKey4); 
		ContentValues ncKey5 = new ContentValues();
		ncKey5.put(NCKEY_VALUE, liveKey1); 
		ContentValues ncKey6 = new ContentValues();
		ncKey6.put(NCKEY_VALUE, liveKey2); 
		ContentValues ncKey7 = new ContentValues();
		ncKey7.put(NCKEY_VALUE, liveKey3); 
		ContentValues ncKey8 = new ContentValues();
		ncKey8.put(NCKEY_VALUE, liveKey4); 

		db.insert(DB_KEYS_TABLE, null, ncKey1);
		db.insert(DB_KEYS_TABLE, null, ncKey2);
		db.insert(DB_KEYS_TABLE, null, ncKey3);
		db.insert(DB_KEYS_TABLE, null, ncKey4);
		db.insert(DB_KEYS_TABLE, null, ncKey5);
		db.insert(DB_KEYS_TABLE, null, ncKey6);
		db.insert(DB_KEYS_TABLE, null, ncKey7);
		db.insert(DB_KEYS_TABLE, null, ncKey8);
	}
	

	/**
	 * Insert an n-Counter in the database
	 * @param counter a TXnCounter
	 * @return
	 */
	public long insertNCounter(TXnCounter counter) {
		ContentValues ncounter = new ContentValues();
		ncounter.put(NCOUNTER_XN, counter.getnCXn());
		ncounter.put(NCOUNTER_BATCH_ID, counter.getBatchId());
		ncounter.put(NCOUNTER_ISSUER_KEY_ID, counter.getnCIssuerKeyID());
		ncounter.put(NCOUNTER_ISSUER_KEY, counter.getnCIssuerKey());
		ncounter.put(NCOUNTER_SEED, counter.getnCSeed());
		ncounter.put(NCOUNTER_ISSUER_UNITS, counter.getnCIssuerUnit());
		ncounter.put(NCOUNTER_ISSUER_STEPVALUE, counter.getIssuerStepVal());
		ncounter.put(NCOUNTER_ISSUER_CURRENCY, counter.getIssuerCurrencyCode());
		ncounter.put(NCOUNTER_MERCHANT_UNITS, counter.getnCMerchantUnit());
		ncounter.put(NCOUNTER_MERCHANT_STEPVALUE, counter.getMerchantStepVal());
		ncounter.put(NCOUNTER_MERCHANT_CURRENCY, counter.getMerchantCurrencyCode());
		ncounter.put(NCOUNTER_LENGTH, counter.getnCLength());
		ncounter.put(NCOUNTER_STEPS_USED, counter.getnCStepsUsed());
		if (counter.getnCValue() != null)
			ncounter.put(NCOUNTER_CURRENT_VALUE, counter.getnCValue());
		else
			ncounter.put(NCOUNTER_CURRENT_VALUE, "");
		ncounter.put(NCOUNTER_STEPS_REDEEMED, counter.getnCRedeemedSteps());
		if (counter.getNarrative() != null)
			ncounter.put(NCOUNTER_NARRATIVE, counter.getNarrative());
		else
			ncounter.put(NCOUNTER_NARRATIVE, "");
		ncounter.put(NCOUNTER_CREATION_DATE, counter.getCreationDate());		
		ncounter.put(NCOUNTER_VALIDITY_SPAN, counter.getValiditySpan());
		return db.insert(DB_NCOUNTERS_TABLE, null, ncounter);
	}

	public long insertTransaction(TxTransaction transaction) {
		
		ContentValues values = new ContentValues();
		values.put(TRANSACTION_TIMESTAMP, transaction.getTimestamp());
		values.put(TRANSACTION_AMOUNT, transaction.getAmount());
		values.put(TRANSACTION_CURRENCY, transaction.getCurrencyCode());
		values.put(TRANSACTION_APPLET_SN, transaction.getAppletSerialNumber());
		values.put(TRANSACTION_TERMINAL_TAG, transaction.getTerminalTag());
		values.put(TRANSACTION_TERMINAL_LOCATION, transaction.getTerminalLocation());
		values.put(TRANSACTION_NEW_BALANCE, transaction.getNewBalance());
		values.put(TRANSACTION_TERMINAL_TYPE, transaction.getTerminalType());
		values.put(TRANSACTION_REFERENCE, transaction.getReference());
		if(transaction.isUploaded()){
			values.put(TRANSACTION_UPLOADED, 1);
		}else{
			values.put(TRANSACTION_UPLOADED, 0);
		}
		
		Log.d("DBTEST", "insertTransaction(" + transaction.getAmount() + ")");
		return db.insert(DB_TRANSACTIONS_TABLE, null, values);
	}
	

	public long insertRedeemedValue(RedeemedValue value){
		ContentValues redeemedValue = new ContentValues();
		String amnt = value.getAmount();
		redeemedValue.put(REDEEMEDVALUES_TIMESTAMP, value.getTimestamp());
		redeemedValue.put(REDEEMEDVALUES_AMOUNT, amnt);
		redeemedValue.put(REDEEMEDVALUES_CURRENCY, value.getCurrencyCode());
		
		Log.d("DBTEST", "insertRedeemedValue(" + value.getAmount() + ")");
		return db.insert(DB_REDEEMEDVALUES_TABLE, null, redeemedValue);
	}

	public long insertRedemptions (String timestamp, String currentValue, String terminalName, String ncounterID, String keyID, String currency) {
		ContentValues redemption = new ContentValues();
		redemption.put(REDEMPTION_TIMESTAMP, timestamp);
		redemption.put(REDEMPTION_CURRENT_VALUE, currentValue);
		redemption.put(REDEMPTION_TERMINAL_NAME, terminalName);
		redemption.put(REDEMPTION_NCOUNTERID, ncounterID);
		redemption.put(REDEMPTION_KEY_ID, keyID);
		redemption.put(REDEMPTION_CURRENCY, currency);


		Log.d("DBTEST", "NCounterRedeemed(" + currentValue + ")");
		return db.insert(DB_REDEMPTIONS_TABLE, null, redemption);
	}

	
	public void insertAppletMessages(List<AppletMessage> messages){
		for (AppletMessage appletMessage : messages) {
			insertAppletMessage(appletMessage);
		}
	}
	
	public long insertAppletMessage (AppletMessage message){
		ContentValues newMessage = new ContentValues();
		
		newMessage.put(APPLET_MESSAGE_ASN, message.getAppletSerialNumber().replace("-", ""));
		newMessage.put(APPLET_MESSAGE_SEQ, message.getSeqNumber());
		newMessage.put(APPLET_MESSAGE_COMMAND, message.getCommandApdu());
		
		Log.d("DBTest", "AppletMessage(" + newMessage +")");
		return db.insert(DB_APPLET_MESSAGES_TABLE, null, newMessage);
	}
	
	public List<AppletMessage> getMessagesFor(String appletSerialNumber){
		List<AppletMessage> messages = new ArrayList<AppletMessage>();
		
		Cursor cursor = retrieveAllMessagesForASN(appletSerialNumber);
		if(cursor.moveToFirst()){
			do{
				AppletMessage message = new AppletMessage();
				
				message.setAppletSerialNumber(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_ASN)));
				message.setSeqNumber(cursor.getLong(cursor.getColumnIndex(APPLET_MESSAGE_SEQ)));
				message.setCommandApdu(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_COMMAND)));
				
				messages.add(message);
			}while(cursor.moveToNext());
		}
		
		return messages;
	}
	
	private Cursor retrieveAllMessagesForASN(String appletSerialNumber){
		
		return db.query(DB_APPLET_MESSAGES_TABLE, new String[]{
				APPLET_MESSAGE_ID, APPLET_MESSAGE_ASN, APPLET_MESSAGE_SEQ,APPLET_MESSAGE_COMMAND},
				APPLET_MESSAGE_ASN + " = '" + appletSerialNumber + "'"
				+ " AND " + APPLET_MESSAGE_RESPONSE + " IS NULL", null, null, null,
				APPLET_MESSAGE_SEQ + " ASC");
	}
	
	public List<AppletMessage> getAllMessagesDB(){
		List<AppletMessage> messages = new ArrayList<AppletMessage>();
		
		Cursor cursor = retrieveAllMessages();
		if(cursor.moveToFirst()){
			do{
				AppletMessage message = new AppletMessage();
				
				message.setAppletSerialNumber(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_ASN)));
				message.setSeqNumber(cursor.getLong(cursor.getColumnIndex(APPLET_MESSAGE_SEQ)));
				message.setCommandApdu(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_COMMAND)));
				
				if(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_RESPONSE)) != null){
					message.setResponseApdu(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_RESPONSE)));
				}
				
				messages.add(message);
			}while(cursor.moveToNext());
		}
		
		return messages;
	}
	
	private Cursor retrieveAllMessages(){
		return db.query(DB_APPLET_MESSAGES_TABLE, new String[]{
				APPLET_MESSAGE_ID, APPLET_MESSAGE_ASN, APPLET_MESSAGE_SEQ,APPLET_MESSAGE_COMMAND, APPLET_MESSAGE_RESPONSE},
				null, null, null, null,
				APPLET_MESSAGE_SEQ + " ASC");
	}
	
	public List<AppletMessage> getDeliveredMessages(){
		List<AppletMessage> messages = new ArrayList<AppletMessage>();
		
		Cursor cursor = retrieveAllDeliveredMessages();
		if(cursor.moveToFirst()){
			do{
				AppletMessage message = new AppletMessage();
				
				message.setAppletSerialNumber(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_ASN)));
				message.setSeqNumber(cursor.getLong(cursor.getColumnIndex(APPLET_MESSAGE_SEQ)));
				message.setCommandApdu(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_COMMAND)));
				message.setResponseApdu(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_RESPONSE)));
				
				messages.add(message);
			}while(cursor.moveToNext());
		}
		
		return messages;
	}
	
	public AppletMessage getOldestMessage(){
		AppletMessage message = new AppletMessage();
		
		Cursor cursor = retrieveNotDeliveredMessages();
		if(cursor.moveToFirst()){
			message.setAppletSerialNumber(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_ASN)));
			message.setSeqNumber(cursor.getLong(cursor.getColumnIndex(APPLET_MESSAGE_SEQ)));
			message.setCommandApdu(cursor.getString(cursor.getColumnIndex(APPLET_MESSAGE_COMMAND)));
		}
		
		return message;
	}
	
	private Cursor retrieveNotDeliveredMessages(){
		return db.query(DB_APPLET_MESSAGES_TABLE, new String[]{
				APPLET_MESSAGE_ID, APPLET_MESSAGE_ASN, APPLET_MESSAGE_SEQ, APPLET_MESSAGE_COMMAND, APPLET_MESSAGE_RESPONSE}, 
				APPLET_MESSAGE_RESPONSE + " IS NULL", 
				null, null, null, APPLET_MESSAGE_SEQ + " ASC");
	}
	
	private Cursor retrieveAllDeliveredMessages(){
		
		return db.query(DB_APPLET_MESSAGES_TABLE, new String[]{
				APPLET_MESSAGE_ID, APPLET_MESSAGE_ASN, APPLET_MESSAGE_SEQ, APPLET_MESSAGE_COMMAND, APPLET_MESSAGE_RESPONSE}, 
				APPLET_MESSAGE_RESPONSE + " IS NOT NULL", 
				null, null, null, APPLET_MESSAGE_SEQ + " ASC");
	}

	private Cursor getAllNcounters() {
//		Orders by merchant units:
//			1- currency 
//			2- step value
//			3- steps used
		if(db == null){
			Log.d("AndroidTerminalWrapper", "is null? YESSSS");
		}else{
			Log.d("AndroidTerminalWrapper", "is null? NOOOOO");
		}	
		
		return db.query(DB_NCOUNTERS_TABLE, new String[] {
				NCOUNTER_ID, NCOUNTER_BATCH_ID, NCOUNTER_ISSUER_KEY_ID, NCOUNTER_ISSUER_KEY, NCOUNTER_SEED, 
				NCOUNTER_ISSUER_UNITS, NCOUNTER_ISSUER_STEPVALUE, NCOUNTER_ISSUER_CURRENCY,
				NCOUNTER_MERCHANT_UNITS, NCOUNTER_MERCHANT_STEPVALUE, NCOUNTER_MERCHANT_CURRENCY, 
				NCOUNTER_LENGTH, NCOUNTER_STEPS_USED, NCOUNTER_CURRENT_VALUE, NCOUNTER_STEPS_REDEEMED, NCOUNTER_NARRATIVE, 
				NCOUNTER_CREATION_DATE, NCOUNTER_VALIDITY_SPAN, NCOUNTER_XN}, null, null, null, null, 
				NCOUNTER_MERCHANT_CURRENCY +" DESC, " + NCOUNTER_MERCHANT_STEPVALUE +" DESC, " + NCOUNTER_STEPS_USED +" DESC");
	}
	
	public List<RedeemedValue> getAllRedeemedValues(){
		List<RedeemedValue> list = new ArrayList<RedeemedValue>();
		
		Cursor c = getRedeemedValues();
		if(c.moveToFirst()){
			do{
				RedeemedValue value = new RedeemedValue();
				double amnt = c.getDouble(c.getColumnIndex(REDEEMEDVALUES_AMOUNT));
				
				value.setTimestamp(c.getString(c.getColumnIndex(REDEEMEDVALUES_TIMESTAMP)));
				value.setAmount(amnt);
				value.setCurrencyCode(c.getString(c.getColumnIndex(REDEEMEDVALUES_CURRENCY)));
				
				list.add(value);
			}while(c.moveToNext());
		}
		return list;
	}
	
	private Cursor getRedeemedValues(){
		
		return db.query(DB_REDEEMEDVALUES_TABLE, new String[] {
				REDEEMEDVALUES_ID, REDEEMEDVALUES_TIMESTAMP, REDEEMEDVALUES_AMOUNT, REDEEMEDVALUES_CURRENCY,
		}, null, null, null, null, REDEEMEDVALUES_ID + " DESC");
	}
	
	/**
	 * Get all n-Counters
	 * @return RTnCounts array list of TXnCounters
	 */
	public TXnCounter getSinglenCounters(int _id)
	{
		Log.v("DBTXTerminalLib", "getSinglenCounters()");
		TXnCounter result = new TXnCounter();
		
		Cursor c = getSpecificNCounter_ID(_id);
			
		if(c.moveToFirst()){
			
				
				try{
							
					result = new TXnCounter(c.getInt(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_ID)), 
							new byte[4],
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_ISSUER_KEY_ID)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_ISSUER_KEY)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_SEED)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_ISSUER_UNITS)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_MERCHANT_UNITS)),
							c.getShort(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_LENGTH)),
							c.getShort(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_STEPS_USED)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_CURRENT_VALUE)),
							c.getShort(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_STEPS_REDEEMED)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_NARRATIVE)),
							c.getShort(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_CREATION_DATE)),
							c.getShort(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_VALIDITY_SPAN)));
					byte[] xn = c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_XN));
					
					result.setnCXn(xn);
	
					//Log.v(MainActivity.NCTB, "NCOUNTER_CURRENT_VALUE = " + c.getString(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_CURRENT_VALUE)));
					if (result.getnCValue().length == 1) //must be a 0 in that case
					{
						//Log.v(MainActivity.NCTB, "NCOUNTER_CURRENT_VALUE = NULL");
						result.setnCValue(null); //force to null
					}
					else 
					{
						//Log.v(MainActivity.NCTB, "NCOUNTER_CURRENT_VALUE = " + c.getString(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_CURRENT_VALUE)));
					}
					Log.v("DBTXTerminalLib", "n-Counter found in DB:");
					Log.v("DBTXTerminalLib", result.toFriendlyString());
	
				} catch (Exception e)
				{
					Log.v("DBTXTerminalLib", "Exception caught in getAllnCounters: " + e.getMessage());
				}
				
			
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Get all n-Counters
	 * @return RTnCounts array list of TXnCounters
	 */
	public NCountersBatch getAllnCounters()
	{
		Log.v("DBTXTerminalLib", "getAllnCounters()");
		NCountersBatch result = new NCountersBatch();
		
		Cursor c = getAllNcounters();
		
		if(c.moveToFirst()){
			do{
				
				try{
							
					TXnCounter tmpnCounter = new TXnCounter(c.getInt(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_ID)), 
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_BATCH_ID)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_ISSUER_KEY_ID)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_ISSUER_KEY)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_SEED)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_ISSUER_UNITS)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_MERCHANT_UNITS)),
							c.getShort(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_LENGTH)),
							c.getShort(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_STEPS_USED)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_CURRENT_VALUE)),
							c.getShort(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_STEPS_REDEEMED)),
							c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_NARRATIVE)),
							c.getShort(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_CREATION_DATE)),
							c.getShort(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_VALIDITY_SPAN)));
					
					byte[] xn = c.getBlob(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_XN));
					
					tmpnCounter.setnCXn(xn);
	
					//Log.v(MainActivity.NCTB, "NCOUNTER_CURRENT_VALUE = " + c.getString(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_CURRENT_VALUE)));
					if (tmpnCounter.getnCValue().length == 1) //must be a 0 in that case
					{
						//Log.v(MainActivity.NCTB, "NCOUNTER_CURRENT_VALUE = NULL");
						tmpnCounter.setnCValue(null); //force to null
					}
					else 
					{
						//Log.v(MainActivity.NCTB, "NCOUNTER_CURRENT_VALUE = " + c.getString(c.getColumnIndex(DBTXTerminalLib.NCOUNTER_CURRENT_VALUE)));
					}
					Log.v("DBTXTerminalLib", "n-Counter found in DB:");
					Log.v("DBTXTerminalLib", tmpnCounter.toFriendlyString());
	
					result.add(tmpnCounter);
				} catch (Exception e)
				{
					Log.v("DBTXTerminalLib", "Exception caught in getAllnCounters: " + e.getMessage());
				}
				
			}while(c.moveToNext());
		}
		
		return result;
	}
	
	/**
	 * Get all n-Counters from a given batch ID
	 * @param batchID
	 * @return RTnCounts RTnCounts array list of TXnCounters
	 */
	public NCountersBatch getAllnCounters(byte[] batchID)
	{
		Log.v("DBTXTerminalLib", "getAllnCounters(batchID == " + Utils.getHexString(batchID) + ")");
		NCountersBatch result = new NCountersBatch();
		NCountersBatch tmp = getAllnCounters();
		for (TXnCounter counter : tmp)
			if (Arrays.equals(batchID, counter.getBatchId()))
				result.add(counter);
		return result;
	}
	
	/**
	 * Get all n-Counters from a given batch ID and a given currency
	 * @param batchID
	 * @return RTnCounts RTnCounts array list of TXnCounters
	 */
	public NCountersBatch getAllnCounters(byte[] batchID, String currency){
		Log.v("DBTXTerminalLib", "getAllnCounters(batchID & currency == " + Utils.getHexString(batchID) +" & " +currency +  ")");
		NCountersBatch result = new NCountersBatch();
		NCountersBatch tmp = getAllnCounters();
		for (TXnCounter counter : tmp)
			if (Arrays.equals(batchID, counter.getBatchId()) && counter.getMerchantCurrencyCode().equals(currency))
				result.add(counter);
		return result;
	}
	
	public NCountersBatch getAllnCounters(String currency){
		
		Log.v("DBTXTerminalLib", "getAllnCounters(currency == " + currency +  ")");
		NCountersBatch result = new NCountersBatch();
		NCountersBatch tmp = getAllnCounters();
		
		for (TXnCounter counter : tmp)
			if (counter.getMerchantCurrencyCode().equals(currency))
				result.add(counter);
		return result;
	}
	
	/**
	 * Get all n-Counters from a given batch ID, a given currency and a narrative
	 * @param batchID
	 * @return RTnCounts RTnCounts array list of TXnCounters
	 */
	public NCountersBatch getAllnCounters(byte[] batchID, String currency, byte[] narrative){
		Log.v("DBTXTerminalLib", "getAllnCounters(batchID & currency & narrative == "
				+ Utils.getHexString(batchID) +" & " +currency +  "& "+  Utils.getHexString(narrative) +")");
		NCountersBatch result = new NCountersBatch();
		NCountersBatch tmp = getAllnCounters();
		for (TXnCounter counter : tmp)
			if (Arrays.equals(batchID, counter.getnCIssuerKeyID()) && 
					counter.getMerchantCurrencyCode().equals(currency) &&
					Arrays.equals(narrative, counter.getNarrative()))
				result.add(counter);
		return result;
	}
	
	
	public List<TxTransaction> getNotUploadedTransactions(){
		List<TxTransaction> transactions = new ArrayList<TxTransaction>();
		Cursor c = getNotUploadedTrans();
		
		if(c.moveToFirst()){
			do{
				TxTransaction transaction = cursorToTxTransaction(c);
				transactions.add(transaction);
			}while(c.moveToNext());
		}
		return transactions;
	}
	
	private Cursor getNotUploadedTrans() {
		return db.query(DB_TRANSACTIONS_TABLE, new String[] {
				TRANSACTION_ID, TRANSACTION_REFERENCE, TRANSACTION_TIMESTAMP, TRANSACTION_AMOUNT, TRANSACTION_CURRENCY, 
				TRANSACTION_APPLET_SN, TRANSACTION_TERMINAL_LOCATION, TRANSACTION_TERMINAL_TAG, TRANSACTION_TERMINAL_TYPE,
				TRANSACTION_NEW_BALANCE, TRANSACTION_UPLOADED}
		, TRANSACTION_UPLOADED + " = 0" , null, null, null, TRANSACTION_ID +" DESC" );
	}

	public List<TxTransaction> getAllTxTransactions(){
		List<TxTransaction> transactions = new ArrayList<TxTransaction>();
		Cursor c = getAllTransactions();
		
		if(c.moveToFirst()){
			do{
				TxTransaction transaction = cursorToTxTransaction(c);
				transactions.add(transaction);
			}while(c.moveToNext());
		}
		return transactions;
	}
	
	
	private TxTransaction cursorToTxTransaction(Cursor c) {
		TxTransaction transaction = new TxTransaction();
		
		transaction.setId(c.getInt(c.getColumnIndex(TRANSACTION_ID)));
		
		String amount = c.getString(c.getColumnIndex(TRANSACTION_AMOUNT));
		String newBalance = c.getString(c.getColumnIndex(TRANSACTION_NEW_BALANCE));
		
		transaction.setAmount(amount);
		transaction.setAppletSerialNumber(c.getString(c.getColumnIndex(TRANSACTION_APPLET_SN)));
		transaction.setCurrencyCode(c.getString(c.getColumnIndex(TRANSACTION_CURRENCY)));
		transaction.setReference(c.getInt(c.getColumnIndex(TRANSACTION_REFERENCE)));
		transaction.setNewBalance(newBalance);
		int synced = c.getInt(c.getColumnIndex(TRANSACTION_UPLOADED));
		if(synced == 1){
			transaction.setUploaded(true);
		} else{
			transaction.setUploaded(false);
		}
		transaction.setTerminalLocation(c.getString(c.getColumnIndex(TRANSACTION_TERMINAL_LOCATION)));
		transaction.setTerminalTag(c.getString(c.getColumnIndex(TRANSACTION_TERMINAL_TAG)));
		transaction.setTerminalType(c.getString(c.getColumnIndex(TRANSACTION_TERMINAL_TYPE)));
		transaction.setTimestamp(c.getLong(c.getColumnIndex(TRANSACTION_TIMESTAMP)));
		
		return transaction;
	}

	private Cursor getAllTransactions(){
		return db.query(DB_TRANSACTIONS_TABLE, new String[] {
				TRANSACTION_ID, TRANSACTION_REFERENCE, TRANSACTION_TIMESTAMP, TRANSACTION_AMOUNT, TRANSACTION_CURRENCY, 
				TRANSACTION_APPLET_SN, TRANSACTION_TERMINAL_LOCATION, TRANSACTION_TERMINAL_TAG, TRANSACTION_TERMINAL_TYPE,
				TRANSACTION_NEW_BALANCE, TRANSACTION_UPLOADED}
		, null, null, null, null, TRANSACTION_ID +" DESC" );
	}

	public Cursor getAllRedemptions(){
		return db.query(DB_REDEMPTIONS_TABLE, new String[] {
				REDEMPTION_ID ,REDEMPTION_TIMESTAMP, REDEMPTION_CURRENT_VALUE, REDEMPTION_TERMINAL_NAME,
				REDEMPTION_NCOUNTERID, REDEMPTION_KEY_ID, REDEMPTION_CURRENCY}
		, null, null, null, null, REDEMPTION_ID +" DESC" );
	}

	
	public Cursor getSpecificNCounter_ID(int ncid){
		return db.query(DB_NCOUNTERS_TABLE, new String[] {
				NCOUNTER_ID ,NCOUNTER_ISSUER_KEY_ID, NCOUNTER_ISSUER_KEY, NCOUNTER_SEED, NCOUNTER_ISSUER_UNITS, NCOUNTER_MERCHANT_UNITS, 
				NCOUNTER_LENGTH, NCOUNTER_STEPS_USED, NCOUNTER_CURRENT_VALUE, NCOUNTER_STEPS_REDEEMED, 
				NCOUNTER_NARRATIVE, NCOUNTER_CREATION_DATE, NCOUNTER_VALIDITY_SPAN, NCOUNTER_XN}, NCOUNTER_ID + " = " + ncid, null, null, null, null);
	}
	
	public int editNcounter(String ncid, String currentValue, String stepsUsed){
		ContentValues args = new ContentValues();
		args.put(NCOUNTER_CURRENT_VALUE, currentValue);
		args.put(NCOUNTER_STEPS_USED, stepsUsed);
		return db.update(DB_NCOUNTERS_TABLE, args, NCOUNTER_ID + "=" + ncid, null);
	}

	public int updateNcounter(TXnCounter counter){
		ContentValues updateNC = new ContentValues();
		updateNC.put(NCOUNTER_ISSUER_KEY_ID, counter.getnCIssuerKeyID());
		updateNC.put(NCOUNTER_ISSUER_KEY, counter.getnCIssuerKey());
		updateNC.put(NCOUNTER_SEED, counter.getnCSeed());
		updateNC.put(NCOUNTER_ISSUER_UNITS, counter.getnCIssuerUnit());
		updateNC.put(NCOUNTER_ISSUER_STEPVALUE, counter.getIssuerStepVal());
		updateNC.put(NCOUNTER_ISSUER_CURRENCY, counter.getIssuerCurrencyCode());
		updateNC.put(NCOUNTER_MERCHANT_UNITS, counter.getnCMerchantUnit());
		updateNC.put(NCOUNTER_MERCHANT_STEPVALUE, counter.getMerchantStepVal());
		updateNC.put(NCOUNTER_MERCHANT_CURRENCY, counter.getMerchantCurrencyCode());
		updateNC.put(NCOUNTER_LENGTH, counter.getnCLength());
		updateNC.put(NCOUNTER_STEPS_USED, counter.getnCStepsUsed());
		if (counter.getnCValue() != null)
			updateNC.put(NCOUNTER_CURRENT_VALUE, counter.getnCValue());
		else
			updateNC.put(NCOUNTER_CURRENT_VALUE, "");
		updateNC.put(NCOUNTER_STEPS_REDEEMED, counter.getnCRedeemedSteps());
		updateNC.put(NCOUNTER_NARRATIVE, counter.getNarrative());
		updateNC.put(NCOUNTER_CREATION_DATE, counter.getCreationDate());
		updateNC.put(NCOUNTER_VALIDITY_SPAN, counter.getValiditySpan());
		
		return db.update(DB_NCOUNTERS_TABLE, updateNC, NCOUNTER_ID + "=" + counter.getnCId(), null);
	}
	
	public void updateTransactionsToUploaded(List<TxTransaction> transactions){
		
		for(TxTransaction transaction : transactions){
			updateTransaction(transaction);
		}
	}
	
	public void updateAppletMessages(List<AppletMessage> messages){
		for (AppletMessage appletMessage : messages) {
			updateMessage(appletMessage);
		}
	}
	
	private long updateMessage(AppletMessage message){
		ContentValues appletMessage = new ContentValues();
		
		appletMessage.put(APPLET_MESSAGE_RESPONSE, message.getResponseApdu());
		long res = db.update(DB_APPLET_MESSAGES_TABLE, appletMessage, APPLET_MESSAGE_SEQ + "=" + message.getSeqNumber(), null);
		
		Log.d("DBTest", "Updated applet message seqValue = "+message.getSeqNumber());
		return res;
	}
	
	private int updateTransaction(TxTransaction transaction){
		
		ContentValues updatedTransaction = new ContentValues();
		updatedTransaction.put(TRANSACTION_UPLOADED, 1);
		
		int res = db.update(DB_TRANSACTIONS_TABLE, updatedTransaction, TRANSACTION_ID + "=" + transaction.getId(), null);
		
		Log.d("UpdateTransaction", "Updated transaction: "+res);
		return res;
	}
	
	public void deleteAllMessages(){
		
		db.delete(DB_APPLET_MESSAGES_TABLE, null, null);
	}
	
	public void deleteAllDeliveredMessages(){
		db.delete(DB_APPLET_MESSAGES_TABLE, APPLET_MESSAGE_RESPONSE + " IS NOT NULL", null);
	}
	
	public void deleteNCounter (String _id){
		db.delete(DB_NCOUNTERS_TABLE, NCOUNTER_ID+ "='" + _id +"'", null);
		Log.d("DBTEST", "deleteNCounter(" + _id + ")");
	}


}
