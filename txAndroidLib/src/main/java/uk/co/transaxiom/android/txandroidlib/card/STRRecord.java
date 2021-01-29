package uk.co.transaxiom.android.txandroidlib.card;

import uk.co.transaxiom.android.txandroidlib.Utils;

public class STRRecord extends RecDataBase{

    //private byte IDParams;                              // Anybody know what this is?
	private byte[] specificData = new byte[4];
    private byte[] currentValueGroup = new byte[48];
    private byte[] newValueGroup = new byte[48];

    public STRRecord(byte pRecType, byte[] recStore) {
    	super(pRecType, recStore);
        // raw[] holds the record specific data as shown in tables 5.6 - 5.9 in D12-007.3
    	 //IDParams = recStore[0];
    	 System.arraycopy(recStore, 0, specificData, 0, specificData.length);
//        int temp = recStore[1] & 0xFF;
//        currentValueGroup = new byte[temp];
    	 System.arraycopy(recStore, specificData.length, currentValueGroup, 0, currentValueGroup.length);
    	 //newValueGroup = new byte[recStore[2 + currentValueGroup.length]];
    	 System.arraycopy(recStore, specificData.length + currentValueGroup.length, newValueGroup, 0, newValueGroup.length);
    }

    public String ToString() {
    	String result = "";
//    	result += "IDParams=";
//    	result += IDParams;
    	result += ", currentVG\r\n";
    	result += Utils.getHexString(currentValueGroup, currentValueGroup.length);
    	result += "\r\nnewVG\r\n";
    	result += Utils.getHexString(newValueGroup, newValueGroup.length);
    	result += "\r\n";
        return result;
    }

}