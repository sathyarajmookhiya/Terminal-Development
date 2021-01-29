package uk.co.transaxiom.android.txandroidlib.card;


public class RecDataBase {

    protected byte recType;
    protected byte[] raw;

    public RecDataBase(byte pRecType, byte[] recStore) {
        recType = pRecType;
        raw = recStore;
    }
    
	public static RecDataBase buildRecData(byte pRecType, byte[] recStore, boolean bcdRecord) throws Exception {
        switch (pRecType) {
        case BaseTransaction.RT_EMPTY_PAYMENT:
        case BaseTransaction.RT_EMPTY_LOAD:
        case BaseTransaction.RT_INITIAL_PAYMENT:
        case BaseTransaction.RT_INITIAL_LOAD:
            return new RecDataBase(pRecType, recStore);
        case BaseTransaction.RT_NC_Payment:
            return new nCPaymentRec(pRecType, recStore, bcdRecord);
        case BaseTransaction.RT_STR_Payment:
            return new STRRecord(pRecType, recStore);
        case BaseTransaction.RT_NC_Load2:
        case BaseTransaction.RT_NC_Load:// Don't support nCLoad records at the moment
            return new RecDataBase(pRecType, recStore);
        case BaseTransaction.RT_STR_Load:
            return new STRRecord(pRecType, recStore);
        default:
            throw new Exception("Unknown Record Type");
        }
	}

}
