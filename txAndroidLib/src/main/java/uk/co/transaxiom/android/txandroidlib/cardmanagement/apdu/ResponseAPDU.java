package uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu;

public class ResponseAPDU extends APDU {
	
	private byte[] responseData;
	private ErrorCode errorCode;

	public ResponseAPDU(byte[] response){
		
		deserialise(response);
	}

	private void deserialise(byte[] response) {
		
		byte[] tmpErr = new byte[2];
		tmpErr[0] = response[response.length - 2];
		tmpErr[1] = response[response.length - 1];
		
		this.errorCode = new ErrorCode(tmpErr);
		
		if(response.length > 2){
			responseData = new byte[response.length - 2];
			System.arraycopy(response, 0, responseData, 0, responseData.length);
		}
		else{
			responseData = new byte[0];
		}
	}
	
	@Override
	public byte[] getData() {
		
		return serialise();
	}
	
	private byte[] serialise() {
		
		byte[] result = new byte[responseData.length + 2];
		
		System.arraycopy(responseData, 0, result, 0, responseData.length);
		result[result.length - 2] = errorCode.getERROR_CODE1();
		result[result.length - 1] = errorCode.getERROR_CODE2();
		return result;
	}
	
	public byte[] getResponseData() {	return responseData;	}

	public ErrorCode getErrorCode() {	return errorCode;	}
}
