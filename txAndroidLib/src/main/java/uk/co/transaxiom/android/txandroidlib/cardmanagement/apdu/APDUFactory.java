package uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu;

import android.util.Log;
import uk.co.transaxiom.android.txandroidlib.Utils;
import uk.co.transaxiom.terminal.common.utils.BinaryUtils;


public class APDUFactory {

	private static APDUFactory instance = new APDUFactory();
	
	private APDUFactory(){
	}

	public static APDUFactory getInstance(){
		return instance;
	}
	
	public CommandAPDU createCommand(byte CLA, byte INS, byte PARAM_1, byte PARAM_2){
		
		byte[] emptyArray = new byte[0];
		
		CommandAPDU command = new CommandAPDU(
				CLA, INS, PARAM_1, PARAM_2, 
				emptyArray, 
				emptyArray, 
				emptyArray);

		return command;
	}
	
	public CommandAPDU createCommand(byte CLA, byte INS, byte PARAM_1, byte PARAM_2, byte[] DATA){
		
		byte[] emptyArray = new byte[0];
		byte[] DATA_LENGTH = Utils.int2CleanByteArray(DATA.length);
		
		CommandAPDU apdu = new CommandAPDU(
				CLA, INS, PARAM_1, PARAM_2,
				DATA_LENGTH,
				DATA,
				emptyArray);
		
		Log.d("CreateCommandAPDU", "command = "+BinaryUtils.encode(apdu.getData()));
		return apdu;
	}
	
	public CommandAPDU createCommand(byte CLA, byte INS, byte PARAM_1, byte PARAM_2, int responseLength){
		
		byte[] emptyArray = new byte[0];
		byte[] MAX_RESP_LENGTH = Utils.int2CleanByteArray(responseLength);
		
		CommandAPDU apdu = new CommandAPDU(
				CLA, INS, PARAM_1, PARAM_2,
				emptyArray,
				emptyArray,
				MAX_RESP_LENGTH);
		
		return apdu;
	}
	
	public CommandAPDU createCommand(byte CLA, byte INS, byte PARAM_1, byte PARAM_2, byte[] DATA, int responseLength){
		
		byte[] DATA_LENGTH = Utils.int2CleanByteArray(DATA.length);
		byte[] MAX_RESP_LENGTH = Utils.int2CleanByteArray(responseLength);
		
		CommandAPDU apdu = new CommandAPDU(
				CLA, INS, PARAM_1, PARAM_2,
				DATA_LENGTH,
				DATA,
				MAX_RESP_LENGTH);
		
		return apdu;
	}
	
	public CommandAPDU parseAPDUIntoCommand(byte[] APDU){
		
		byte CLA = APDU[0];
		byte INS = APDU[1];
		byte PARAM_1 = APDU[2];
		byte PARAM_2 = APDU[3];
		
		int offset = 4;
		if(offset == APDU.length){
			return createCommand(CLA, INS, PARAM_1, PARAM_2);
		}
		else if(offset + 1 == APDU.length){
			int responseLength = APDU[offset];
			return createCommand(CLA, INS, PARAM_1, PARAM_2, responseLength);
		}
		else{
			int length = APDU[offset];
			offset++;
			byte[] DATA = new byte[length];
			System.arraycopy(APDU, offset, DATA, 0, length);
			offset += length;
			
			if(offset == APDU.length){
				return createCommand(CLA, INS, PARAM_1, PARAM_2, DATA);
			}
			else{
				int responseLength = APDU[offset];
				return createCommand(CLA, INS, PARAM_1, PARAM_2, DATA, responseLength);
			}
		}
	}
	
	public ResponseAPDU createResponse(byte[] DATA, String error){
		
		byte[] response = new byte[DATA.length + 2];
		System.arraycopy(DATA, 0, response, 0, DATA.length);
		
		if(error.equals(ErrorCode.COMMAND_SUCCESSFUL)){
			System.arraycopy(ErrorCode.bCOMMAND_SUCCESSFUL, 0, response, DATA.length, ErrorCode.bCOMMAND_SUCCESSFUL.length);
		}
		else if(error.equals(ErrorCode.CONDITIONS_NOT_SATISFIED)){
			System.arraycopy(ErrorCode.bCONDITIONS_NOT_SATISFIED, 0, response, DATA.length, ErrorCode.bCONDITIONS_NOT_SATISFIED.length);
		}
		else if(error.equals(ErrorCode.OS_ERROR)){
			System.arraycopy(ErrorCode.bOS_ERROR, 0, response, DATA.length, ErrorCode.bOS_ERROR.length);
		}
		else if(error.equals(ErrorCode.PERMISSION_DENIED)){
			System.arraycopy(ErrorCode.bPERMISSION_DENIED, 0, response, DATA.length, ErrorCode.bPERMISSION_DENIED.length);
		}
		else if(error.equals(ErrorCode.SECURITY_NOT_SATISFIED)){
			System.arraycopy(ErrorCode.bSECURITY_NOT_SATISFIED, 0, response, DATA.length, ErrorCode.bSECURITY_NOT_SATISFIED.length);
		}
		else if(error.equals(ErrorCode.WRONG_LENGTH)){
			System.arraycopy(ErrorCode.bWRONG_LENGTH, 0, response, DATA.length, ErrorCode.bWRONG_LENGTH.length);
		}
		
		return new ResponseAPDU(response);
	}
	
	public ResponseAPDU createResponse(String error){
		
		byte[] emptyArray = new byte[0];
		return createResponse(emptyArray, error);
	}
	
}
