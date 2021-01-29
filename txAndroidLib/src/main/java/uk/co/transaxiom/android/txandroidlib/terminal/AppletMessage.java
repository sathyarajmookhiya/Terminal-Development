package uk.co.transaxiom.android.txandroidlib.terminal;

import uk.co.transaxiom.terminal.common.utils.BinaryUtils;

public class AppletMessage {
	
	private String appletSerialNumber;
	private long seqNumber = 1;
	private String commandApdu;
	private String responseApdu;
	
	public String getAppletSerialNumber() {
		return appletSerialNumber;
	}
	
	public void setAppletSerialNumber(String appletSerialNumber) {
		this.appletSerialNumber = appletSerialNumber;
	}

	public long getSeqNumber() {
		return seqNumber;
	}

	public void setSeqNumber(long seqNumber) {
		this.seqNumber = seqNumber;
	}

	public String getCommandApdu() {
		return commandApdu;
	}

	public void setCommandApdu(String commandApdu) {
		this.commandApdu = commandApdu;
	}

	public String getResponseApdu() {
		return responseApdu;
	}

	public void setResponseApdu(String responseApdu) {
		this.responseApdu = responseApdu;
	}
	
	public byte[] getCommandApduBytes(){
		return BinaryUtils.decode(commandApdu);
	}
	
	@Override
	public String toString(){
		String message = "asn: "+appletSerialNumber
				+" - seqNubmer: "+seqNumber
				+" - command: "+commandApdu
				+" - response: "+responseApdu;
		
		return message;
	}
	
}
