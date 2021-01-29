package uk.co.transaxiom.android.txandroidlib.terminal;

public enum Operations {

		/***** Card Operations *****/
		WIPE_AND_LOAD_KEYS, 
		OPEN_CARD, READ_ALL_PRODUCTS,
		MAIN_ACTIVITY_START,
		ADVANCED_ACTIVITY_START,
		PERFORM_PAYMENT, PREPARE_NCOUNTERS, READ_LAST_LOAD,
		READ_TRANSACTION_STORE, READ_PAYER_FCI,
		LOAD_VALUE, READ_BALANCE, READ_RECORDS,
		OPEN_NCOUNT_CMD2, OPEN_ITSO_CMD2, 
		OPEN_ITSO_JAVA, OPEN_ITSO_DESFIRE, UNBLOCK_CARD,
		
		/****** Server Operations *****/
		REGISTER_MERCHANT, CREATE_TERMINAL, 
		GET_NCOUNTERS, REDEEM_NCOUNTERS,
		REDEEM_AND_DELETE, PREPARE_PAYMENT, 
		GET_OPEN_APPLET_COMMAND, UPDATE_TERMINAL,
		READ_MERCHANT, PREPARE_LOAD, PERFORM_LOAD,
		GET_TERMINALS_FILTERED, GET_NUMBER_TERMINALS_FILTERED,
		UPLOAD_TRANSACTIONS, LOGIN_AS_ADMIN, REGISTER_CONSUMER,
		ACTIVATE_CARD, READ_CONSUMERS_FILTERED, DELETE_TERMINAL,
		PERFORM_TOPUP, READ_PROFILE, READ_PROFILE_IFCHANGED, 
		GET_THEME_CATALOGUE, GET_THEME, LOGOUT, GET_LOAD_RESULT,
		GET_MESSAGES_AFTER, UPDATE_FINISHED_MESSAGES, GET_CARD_OWNER,
		GET_UNBLOCK_COMMAND
	
}
