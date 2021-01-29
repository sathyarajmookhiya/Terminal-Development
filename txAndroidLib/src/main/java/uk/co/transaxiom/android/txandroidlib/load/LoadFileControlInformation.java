package uk.co.transaxiom.android.txandroidlib.load;

import java.util.Arrays;

import uk.co.transaxiom.android.txandroidlib.Utils;
import uk.co.transaxiom.terminal.common.AppletConstants;
import uk.co.transaxiom.terminal.common.utils.BinaryUtils;
import uk.co.transaxiom.terminal.common.utils.Currencies;

/**
 *
 * @author Mohanad-TransaXiom
 */
public class LoadFileControlInformation {


    public static final String appletName = "nCount Applet";
    public static final int nonceLength = 4;
    public static final int transactionIdLength = 16;
    
    private boolean bcdBalance = false;
    private byte FCI_BER_tag;//1 byte
    private byte mintIDlength; //1 byte
    private byte[] mintID;
    private byte[] batchId;
    private byte appletDataBER; //1 byte
    private byte remainingLength; //1 byte
    private byte[] appletVersion; //2 bytes
    private byte[] suiteID; //2 bytes
    private byte[] currencyCode; //3 bytes
    private byte[] balance; //3 bytes BCD
    private byte[] nonce; // 4 bytes
    private byte[] transactionId; // 16 bytes


    public LoadFileControlInformation(byte[] data) {
        if (data != null) {
            parseFCIData(data);
        }
    }

    private void parseFCIData(byte[] data) {
        int index = 0;
        FCI_BER_tag = data[index++];
        mintIDlength = data[index++];
        mintID = new byte[mintIDlength];
        System.arraycopy(data, index, mintID, 0, mintIDlength);
        index += mintIDlength;
        appletDataBER = data[index++];
        remainingLength = data[index++];

        byte[] remainingBytes = new byte[remainingLength];
        System.arraycopy(data, index, remainingBytes, 0, remainingLength);
        parseStream(remainingBytes);
    }

    private void parseStream(byte[] data) {
        int offset = 0;

        while(remainingLength > offset){

            byte[] id = new byte[]{data[offset], data[offset+1]};
            offset += 2;

            if(Arrays.equals(id, AppletConstants.APPLET_VERSION_ID)){
                this.appletVersion = new byte[AppletConstants.APPLET_VERSION_LENGTH];
                System.arraycopy(data, offset, this.appletVersion, 0, AppletConstants.APPLET_VERSION_LENGTH);
                offset += AppletConstants.APPLET_VERSION_LENGTH;
            }
            else if(Arrays.equals(id, AppletConstants.SUITEID_ID)){
                this.suiteID = new byte[AppletConstants.SUITEID_LENGTH];
                System.arraycopy(data, offset, this.suiteID, 0, AppletConstants.SUITEID_LENGTH);
                offset += AppletConstants.SUITEID_LENGTH;
            }
            else if(Arrays.equals(id, AppletConstants.BCD_BALANCE_ID)){
                bcdBalance = true;
            	this.currencyCode = new byte[]{data[offset], data[offset+1], data[offset+2]};
                this.balance = new byte[]{data[offset+3], data[offset+4], data[offset+5]};
                offset += AppletConstants.BCD_BALANCE_LENGTH;
            }
            else if(Arrays.equals(id, AppletConstants.LONG_BALANCE_ID)){
                bcdBalance = false;
                this.currencyCode = new byte[]{data[offset], data[offset+1], data[offset+2]};
                offset += 3;
                this.balance = new byte[AppletConstants.LONG_BALANCE_LENGTH-3];
                System.arraycopy(data, offset, balance, 0, balance.length);
                offset += AppletConstants.LONG_BALANCE_LENGTH;
            }
            else if(Arrays.equals(id, AppletConstants.CHALLENGE_RESPONSE_ID)){
            	this.nonce = new byte[nonceLength];
            	System.arraycopy(data, offset, this.nonce, 0, nonceLength);
            	offset += nonceLength;
            	
            	this.transactionId = new byte[transactionIdLength];
            	System.arraycopy(data, offset, this.transactionId, 0, transactionIdLength);
            	offset += transactionIdLength;
            }
            
            else{
                offset ++;
            }
        }
    }

    public boolean isBalanceInBCD(){
        return bcdBalance;
    }
    
    public long getLongBalance() {
    	if(bcdBalance){
            return BinaryUtils.BCDtoInt(balance);
        }
        else{
            return BinaryUtils.getBytesAsLong(balance);
        }
    }

    public String getCurrency() {
        if (!Arrays.equals(currencyCode, Currencies.TEST)) {
            try {
                String res = new String(currencyCode, "US-ASCII");
                return res;
            } catch (Exception e) {
                return "N/A";
            }
        } else {
            return "XYZ";
        }
    }

    public byte getFCI_BER_tag() {
        return FCI_BER_tag;
    }

    public void setFCI_BER_tag(byte FCI_BER_tag) {
        this.FCI_BER_tag = FCI_BER_tag;
    }

    public byte getMintIDlength() {
        return mintIDlength;
    }

    public void setMintIDlength(byte mintIDlength) {
        this.mintIDlength = mintIDlength;
    }

    public byte[] getMintID() {
        return mintID;
    }

    public void setMintID(byte[] mintID) {
        this.mintID = mintID;
    }

    public byte[] getBatchId() {
        return batchId;
    }

    public void setBatchId(byte[] batchId) {
        this.batchId = batchId;
    }

    public byte getAppletDataBER() {
        return appletDataBER;
    }

    public void setAppletDataBER(byte appletDataBER) {
        this.appletDataBER = appletDataBER;
    }

    public byte getRemainingLength() {
        return remainingLength;
    }

    public void setRemainingLength(byte remainingLength) {
        this.remainingLength = remainingLength;
    }

    public byte[] getAppletVersion() {
        return appletVersion;
    }

    public void setAppletVersion(byte[] appletVersion) {
        this.appletVersion = appletVersion;
    }

    public byte[] getSuiteID() {
        return suiteID;
    }

    public void setSuiteID(byte[] suiteID) {
        this.suiteID = suiteID;
    }
    
    public byte[] getNonce() {
		return nonce;
	}

	public void setNonce(byte[] nonce) {
		this.nonce = nonce;
	}

	public byte[] getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(byte[] transactionId) {
		this.transactionId = transactionId;
	}

	public byte[] getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(byte[] currencyCode) {
        this.currencyCode = currencyCode;
    }

    public byte[] getBalance() {
        return balance;
    }

    public void setBalance(byte[] balance) {
        this.balance = balance;
    }
    
	public String toString() {

		String temp = "";
		
		temp += "FCI_BER_tag " + Utils.getHexVal(FCI_BER_tag) + "\n";
		temp += "mintIDlength " + Utils.getHexVal(mintIDlength) + "\n"; 
		temp += "mintID " + Utils.getHexString(mintID) + "\n";
		temp += "appletDataBER " + Utils.getHexVal(appletDataBER) + "\n";
		temp += "remainingLength " + Utils.getHexVal(remainingLength) + "\n"; 
		temp += "appletVersion " + Utils.getHexString(appletVersion) + "\n";
		temp += "suiteID " + Utils.getHexString(suiteID) + "\n";
		temp += "sessionNonce " + Utils.getHexString(nonce) + "\n";
		temp += "transactionID " + Utils.getHexString(transactionId) + "\n";
		temp += "currencyCode " + Utils.getHexString(currencyCode) + "\n";
		temp += "balance " + Utils.getHexString(balance) + "\n";
		
		return temp;
	}

}

