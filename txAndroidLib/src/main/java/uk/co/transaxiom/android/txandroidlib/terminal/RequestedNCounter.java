package uk.co.transaxiom.android.txandroidlib.terminal;

public class RequestedNCounter {

	private byte[] merchantUnit;
	private short length;
	private byte[] narrative;
		
	public RequestedNCounter(byte[] merchantUnit, short length, byte[] narrative) {
		super();
		this.merchantUnit = merchantUnit;
		this.length = length;
		this.narrative = narrative;
	}
	
	public byte[] getMerchantUnit() {
		return merchantUnit;
	}
	public void setMerchantUnit(byte[] merchantUnit) {
		this.merchantUnit = merchantUnit;
	}
	public short getLength() {
		return length;
	}
	public void setLength(short length) {
		this.length = length;
	}
	public byte[] getNarrative() {
		return narrative;
	}
	public void setNarrative(byte[] narrative) {
		this.narrative = narrative;
	}
	
	
}
