package uk.co.transaxiom.android.txandroidlib.card;

import java.util.Arrays;

import uk.co.transaxiom.android.txandroidlib.Currencies;
import uk.co.transaxiom.android.txandroidlib.Utils;
import uk.co.transaxiom.terminal.common.utils.BinaryUtils;
import uk.co.transaxiom.terminal.common.utils.Constants;
import android.util.Log;

public class BaseTransaction {

	public static final byte RT_EMPTY_PAYMENT = (byte) 0x80;
	public static final byte RT_EMPTY_LOAD = (byte) 0x90;
	public static final byte RT_INITIAL_PAYMENT = 0x00;
	public static final byte RT_INITIAL_LOAD = 0x10;
	public static final byte RT_NC_Payment = 0x01;
	public static final byte RT_NC_Payment2 = 0x02;                          // MOD002 (Unsupported)
	public static final byte RT_STR_Payment = 0x0F;                          // was 0x02.    MOD002
	public static final byte RT_NC_Load = 0x11;
	public static final byte RT_NC_Load2 = 0x13;
	public static final byte RT_STR_Load = 0x1F;
	
	private boolean bcdRecord;
    private byte seqNum;
    private byte recType;
    private byte seqRefNum;
    private byte[] amount; //3 bytes for currency + 3 or 8 bytes for amount
    public byte getRecType() {
		return recType;
	}

	private byte[] recStore;
    private byte[] keyID = new byte[Constants.SIZE_KEYID];       // KeyID no stored in both store types. MOD002
    
    public BaseTransaction(byte[] buffer, int pOffset, boolean bcdAmount) throws Exception {
        bcdRecord = bcdAmount;
    	
    	seqNum = buffer[pOffset++];
        recType = (byte) buffer[pOffset++];
        seqRefNum = buffer[pOffset++];
        if(bcdAmount){
        	amount = new byte[Constants.SIZE_AMOUNT_BCD];
        }else{
        	amount = new byte[Constants.SIZE_AMOUNT_64BITS];
        }
        System.arraycopy(buffer, pOffset, amount, 0, amount.length);
        pOffset += amount.length;                           // Point past the Amount
        System.arraycopy(buffer, pOffset, keyID, 0, keyID.length);    // MOD002
        pOffset += keyID.length;

        switch (recType) {
	        case RT_EMPTY_LOAD:
	        case RT_EMPTY_PAYMENT:
	        case RT_INITIAL_LOAD:
	        case RT_INITIAL_PAYMENT:
	            recStore = new byte[0];
	            break;
	        case RT_STR_Load:
	        case RT_STR_Payment:
	        case RT_NC_Payment:
	            recStore = new byte[buffer.length - pOffset];
	            break;
	        case RT_NC_Load:
	        case RT_NC_Load2:
	            recStore = new byte[0];                     // For now
	            break;
	        default:
	            throw new Exception("Invalid Record Type Serialised");
        }

        System.arraycopy(buffer, pOffset, recStore, 0, recStore.length);
        RecDataBase.buildRecData(recType, recStore, bcdAmount);
    }
    
    @Override
    public String toString() {
    	String result = "";
    	result += "recType: " + Utils.getHexVal(recType) + " (" + getRecordType(recType) + ")";
    	result += " - seqNum: " + Utils.getHexVal(seqNum);
    	result += " - seqRefNum: " + Utils.getHexVal(seqRefNum);
    	result += " - amount: " + Utils.getHexString(amount, amount.length);
        return result;
    }
    
    public String getTransactionNumber(){
    	int seqNum = Utils.byte2UnsignedInt(this.seqNum);
    	int seqRefNum = Utils.byte2UnsignedInt(this.seqRefNum);
    	String result =seqNum + "/" + seqRefNum;
    	return result;
    }
    
    public String toNiceString()
    {
    	String result = "";
    	result += getRecordType(recType);
    	result += " - (" + Utils.getHexVal(seqNum) + "/" + Utils.getHexVal(seqRefNum) + ")";
    	result += " - " + getTransactionAmount();
    	if (recType == RT_STR_Load)
    		return result;
    	else
    	{
    		result += " - " + getNarrative();
        	return result;
    	}
    }
    public String getTransactionAmount()
    {
    	String result = "";
    	result += getCurrency() + " " + getAmount();
    	return result;
    }
    
    String getNarrative()
    {
    	String result = "";
    	nCPaymentRec tmp = new nCPaymentRec(this.recType, this.recStore, bcdRecord);
    	result += tmp.getNarrative();
    	if(result == null){
    		result = "";
    	}
    	return result;
    }
    
    String getCurrency()
    {
    	byte[] tmp = new byte[3];
    	System.arraycopy(amount, 0, tmp, 0, 3);
		if(!Arrays.equals(tmp, Currencies.TEST))
			try{
				String res = new String(tmp, "US-ASCII");
				return res;
			} catch (Exception e)
			{
				Log.v("BaseTransaction", "getCurrency failed: " + e.getMessage());
				return "N/A";
			}
		else
			return "XYZ";
    }
    
    public String getAmount() {
    	long total = 0;
    	
    	byte[] tmp = new byte[amount.length-3];
    	System.arraycopy(amount, 3, tmp, 0, tmp.length);
    	if(tmp.length == 3){
    		total = BinaryUtils.BCDtoInt(tmp);
    	}else{
    		total = BinaryUtils.getBytesAsLong(tmp);
    	}

    	String res = String.valueOf(total/100f);
    	return res;
    }
    
    public String getRecordType(byte type) {
        switch (type) {
	        case RT_EMPTY_LOAD:
	        	return "ERT_EMPTY_LOAD";
	        case RT_EMPTY_PAYMENT:
	        	return "RT_EMPTY_PAYMENT";
	        case RT_INITIAL_LOAD:
	        	return "RT_INITIAL_LOAD";
	        case RT_INITIAL_PAYMENT:
	        	return "RT_INITIAL_PAYMENT";
	        case RT_STR_Load:
	        case RT_NC_Load2:
	        	return "Load";
	        case RT_STR_Payment:
	        	return "RT_STR_Payment";
	        case RT_NC_Payment:
	        	return "Payment";
	        case RT_NC_Load:
	        	return "RT_NC_Load";
	        default:
	        	return "UNKNOWN";
	    }
    }
}