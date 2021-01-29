package uk.co.transaxiom.android.txandroidlib.terminal;

import uk.co.transaxiom.android.txandroidlib.Utils;


public class LoadAppletFCI {
//	4F           	
//	07           Length of nCount Mint ID (which is essentially the Applet's AID)
//	A0 00 00 05 44 0F 01         The Mint ID
//	85           Ber Tag for Applet specific data (I think)
//	10           Length of following data
//	1015       Applet code version 1.015 (ie MOD015)
//	0001       Suite ID.  This is a sort of version code for all the messages that this Applet handles.  In the future, a redesign of the commands would see this value bumped by one.
//	0023       Payer_Initial_V1 Message ID
//	FF 00 00 01          This is the current KeyID this Applet is working to (Issuer Key ID)
//	00 00 01                The first part of the Balance (Currency code 00 00 01)
//	00 13 13                1,313 test currencies stored in the Purse.

	
	//4F			BER tag for FCI
	//09			Length of Mint ID
	//A0 00 00 05 44 0F 01 FF 02	Mint ID (applet ID)
	//85 			Ber Tag for Applet specific data
	//1A			Length of following data
	//10 24			Applet code version
	//00 01			Suite ID
	//41 DB BF 91	Session nonce
	//07 83 EA F0 D7 60 4B A1 42 30 B1 1E D3 A7 45 A6	TransactionID
	//47 42 50		Currency code
	//00 09 50		Current balance
	
	
	
	
	private byte FCI_BER_tag;//1 byte
	private byte mintIDlength; //1 byte
	private byte[] mintID; //7 bytes - applet AID
	private byte appletDataBER; //1 byte
	private byte remainingLength; //1 byte
	private byte[] appletVersion; //2 bytes
	private byte[] suiteID; //2 bytes
	private byte[] sessionNonce; //4 bytes
	private byte[] transactionID; //16 bytes
	private byte[] currencyCode; //3 bytes
	private byte[] balance; //3 bytes BCD
	
	public LoadAppletFCI(byte[] data)
	{
		if(data != null)
			parseFCIData(data);
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
		
		appletVersion = new byte[2];
		System.arraycopy(data, index, appletVersion, 0, 2);
		index += 2;
		
		suiteID = new byte[2];
		System.arraycopy(data, index, suiteID, 0, 2);
		index += 2;
		
		sessionNonce = new byte[4];
		System.arraycopy(data, index, sessionNonce, 0, 4);
		index += 4;
		
		transactionID = new byte[16];
		System.arraycopy(data, index, transactionID, 0, 16);
		index += 16;		
		
		currencyCode = new byte[3];
		System.arraycopy(data, index, currencyCode, 0, 3);
		index += 3;				
		
		balance = new byte[3];
		System.arraycopy(data, index, balance, 0, 3);	
	}
	
	public String getStringAppletVersion() {
		String result = "";
		result += Utils.getHexString(appletVersion, 2);
		result = result.substring(0, 1) + "." + result.substring(1, result.length());
		return result;
	}
	
    public int getIntBalance() {
    	
    	byte[] tmp = new byte[2];
    	tmp[0] = 0;
    	tmp[1] = balance[0];
    	int total = Utils.byteA2BCD(tmp) * 100;
    	System.arraycopy(balance, 1, tmp, 0, 2);
    	total += Utils.byteA2BCD(tmp);
    	return total;
    }
    
    public String getCurrency() {
		
    	try {
			String res = new String(currencyCode, "US-ASCII");
			return res;
		} catch (Exception e) {
			return "N/A";
		}
    }

	public byte getFCI_BER_tag() {
		return FCI_BER_tag;
	}

	public byte getMintIDlength() {
		return mintIDlength;
	}

	public byte[] getMintID() {
		return mintID;
	}

	public byte getAppletDataBER() {
		return appletDataBER;
	}

	public byte getRemainingLength() {
		return remainingLength;
	}

	public byte[] getAppletVersion() {
		return appletVersion;
	}

	public byte[] getSuiteID() {
		return suiteID;
	}

	public byte[] getsessionNonce() {
		return sessionNonce;
	}

	public byte[] gettransactionID() {
		return transactionID;
	}

	public byte[] getCurrencyCode() {
		return currencyCode;
	}

	public byte[] getBalance() {
		return balance;
	}
	
	public String toString() {
//		private byte FCI_BER_tag;//1 byte
//		private byte mintIDlength; //1 byte
//		private byte[] mintID;
//		private byte appletDataBER; //1 byte
//		private byte remainingLength; //1 byte
//		private byte[] appletVersion; //2 bytes
//		private byte[] suiteID; //2 bytes
//		private byte[] sessionNonce; //4 bytes
//		private byte[] transactionID; //16 bytes
//		private byte[] currencyCode; //3 bytes
//		private byte[] balance; //3 bytes BCD
		String temp = "";
		temp += "FCI_BER_tag " + Utils.getHexVal(FCI_BER_tag) + "\n";
		temp += "mintIDlength " + Utils.getHexVal(mintIDlength) + "\n"; 
		temp += "mintID " + Utils.getHexString(mintID) + "\n";
		temp += "appletDataBER " + Utils.getHexVal(appletDataBER) + "\n";
		temp += "remainingLength " + Utils.getHexVal(remainingLength) + "\n"; 
		temp += "appletVersion " + Utils.getHexString(appletVersion) + "\n";
		temp += "suiteID " + Utils.getHexString(suiteID) + "\n";
		temp += "sessionNonce " + Utils.getHexString(sessionNonce) + "\n";
		temp += "transactionID " + Utils.getHexString(transactionID) + "\n";
		temp += "currencyCode " + Utils.getHexString(currencyCode) + "\n";
		temp += "balance " + Utils.getHexString(balance) + "\n";
		return temp;
	}
}
