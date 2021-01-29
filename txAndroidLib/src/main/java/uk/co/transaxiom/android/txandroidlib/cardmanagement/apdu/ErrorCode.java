package uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu;

import java.util.Arrays;

public class ErrorCode {

	public static final String UNKOWN_ERROR = "Unknown error";
	
	public static final String COMMAND_SUCCESSFUL = "Command successfully executed"; //90 00
	public static final byte[] bCOMMAND_SUCCESSFUL = new byte[]{(byte) 0x90, 0x00};
	
	public static final String CONDITIONS_NOT_SATISFIED = "Conditions of use not satisfied"; //69 85
	public static final byte[] bCONDITIONS_NOT_SATISFIED = new byte[]{0x69, (byte) 0x85};
	
	public static final String PERMISSION_DENIED = "Permission Denied"; //69 F0
	public static final byte[] bPERMISSION_DENIED = new byte[]{0x69, (byte) 0xF0};
	
	public static final String SECURITY_NOT_SATISFIED = "Security condition not satisfied"; //69 82
	public static final byte[] bSECURITY_NOT_SATISFIED = new byte[]{0x69, (byte) 0x82};
	
	public static final String WRONG_LENGTH = "Wrong length"; //67 00
	public static final byte[] bWRONG_LENGTH = new byte[]{(byte) 0x67, 0x00};
	
	public static final String OS_ERROR = "Operating System error"; //6F 00
	public static final byte[] bOS_ERROR = new byte[]{0x6F, 0x00};

	private byte[] FULL_ERROR = new byte[2];
	private byte ERROR_CODE1;
	private byte ERROR_CODE2;
	private String errorMsg;
	private boolean ok = false;
	
	
	public ErrorCode(byte [] fullError){
		
		System.arraycopy(fullError, 0, FULL_ERROR, 0, FULL_ERROR.length);
		ERROR_CODE1 = FULL_ERROR[0];
		ERROR_CODE2 = FULL_ERROR[1];
		
		errorMsg = readErrorMessage();
	}
	
	
	private String readErrorMessage() {
		
		ok = false;
		
		if(Arrays.equals(FULL_ERROR, bCOMMAND_SUCCESSFUL)){
			this.ok = true;
			return COMMAND_SUCCESSFUL;
		}
		else if(Arrays.equals(FULL_ERROR, bCONDITIONS_NOT_SATISFIED)){
			return CONDITIONS_NOT_SATISFIED;
		}
		else if(Arrays.equals(FULL_ERROR, bPERMISSION_DENIED)){
			return PERMISSION_DENIED;
		}
		else if(Arrays.equals(FULL_ERROR, bSECURITY_NOT_SATISFIED)){
			return SECURITY_NOT_SATISFIED;
		}
		else if(Arrays.equals(FULL_ERROR, bWRONG_LENGTH)){
			return WRONG_LENGTH;
		}
		else if(Arrays.equals(FULL_ERROR, bOS_ERROR)){
			return OS_ERROR;
		}
		return UNKOWN_ERROR; 
	}


	public byte[] getFULL_ERROR() {	return FULL_ERROR;	}
	
	public byte getERROR_CODE1() {	return ERROR_CODE1;	}
	
	public byte getERROR_CODE2() {	return ERROR_CODE2;	}
	
	public String getErrorMsg() {	return errorMsg;	}

	public boolean isOk() {	return ok;	}
	
	
}
