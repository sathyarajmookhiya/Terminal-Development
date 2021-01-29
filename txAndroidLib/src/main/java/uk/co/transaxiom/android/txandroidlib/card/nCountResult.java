package uk.co.transaxiom.android.txandroidlib.card;

public class nCountResult {
    private byte[] counterValue;
	private byte steps;
	
    public byte[] getCounterValue() {
		return counterValue;
	}


	public byte getSteps() {
		return steps;
	}



    public nCountResult(int counterValueSize) {
        counterValue = new byte[counterValueSize];
    }


    public int deserialise(byte[] raw, int ptr) {
        System.arraycopy(raw, ptr, counterValue, 0, counterValue.length);
        ptr += counterValue.length;
        steps = raw[ptr++];
        return ptr;
    }
}
