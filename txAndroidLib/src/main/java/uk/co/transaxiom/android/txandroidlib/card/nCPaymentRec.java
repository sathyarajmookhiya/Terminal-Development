package uk.co.transaxiom.android.txandroidlib.card;

import java.io.UnsupportedEncodingException;

import uk.co.transaxiom.terminal.common.utils.Constants;

import android.util.Log;

public class nCPaymentRec extends RecDataBase{
	
    private byte[] pinlessBalance;
    private byte[] merchantUUID = new byte[Constants.SIZE_MERCHANT_UUID];
    private nCountResult[] nCounts;
    private byte[] narrative;
    private byte valueLen;
    
    public nCPaymentRec(byte pRecType, byte[] recStore, boolean bcdRecord) {
    	super(pRecType, recStore);
        // raw[] holds the record specific data as shown in tables 5.6 - 5.9 in D12-007.3
    	if(bcdRecord){
    		pinlessBalance = new byte[Constants.SIZE_PINLESS_PAYMENT_BCD];
    	}else{
    		pinlessBalance = new byte[Constants.SIZE_PINLESS_PAYMENT_64BITS];
    	}
    	System.arraycopy(recStore, 0, pinlessBalance, 0, pinlessBalance.length);
        int ptr = pinlessBalance.length;
        System.arraycopy(recStore, ptr, merchantUUID, 0, merchantUUID.length);
        ptr += merchantUUID.length;
        // The byte after the MerchantUUID gives the number of nCounts.
        nCounts = new nCountResult[recStore[ptr++]];
        for (int i = 0; (i < nCounts.length); i++) {
            // The first byte of each nCount Record is the length of the Counter Value 
            valueLen = recStore[ptr++];
            nCounts[i] = new nCountResult(valueLen);
            ptr = nCounts[i].deserialise(recStore, ptr);
        }
        narrative = new byte[recStore[ptr++]];
        System.arraycopy(recStore, ptr, narrative, 0, narrative.length);
    }
    
    public String getNarrative()
    {
    	String result = "";
    	try {
			result += new String(narrative, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.v("nCPaymentRec", "getNarrative in nCPaymentRec  failed: " + e.getMessage());
		}
    	return result;
    }
}

