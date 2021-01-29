package uk.co.transaxiom.android.txandroidlib.cardmanagement.applet.filesystem;

import uk.co.transaxiom.terminal.common.utils.BinaryUtils;


public class FileSystemAppletFCI {
	
	//4F 09 A0 00 00 05 44 0F 01 FF 80 85 08 10 00 00 00 00 00 00 00
	
	private byte FCI_BER_tag;//1 byte
	private byte AIDlength; //1 byte
	private byte[] appletID = new byte[9]; //9 bytes
	private byte appletDataBER; //1 byte
	private byte remainingLength; //1 byte
	private byte[] FS_Version = new byte[2]; //2 bytes
	private byte[] FS_ID = new byte[4]; //4 bytes
	private byte[] FS_State = new byte[2]; //2 bytes
	
	public FileSystemAppletFCI (byte[] data) {
		this.FCI_BER_tag = data[0];
		this.AIDlength = data[1];
		System.arraycopy(data, 2, appletID, 0, 9);
		this.appletDataBER = data[11];
		this.remainingLength = data[12];
		System.arraycopy(data, 13, FS_Version, 0, FS_Version.length);
		System.arraycopy(data, 15, FS_ID, 0, FS_ID.length);
		System.arraycopy(data, 19, FS_State, 0, FS_State.length);
	}
	
	public byte getFCI_BER_tag() {
		return FCI_BER_tag;
	}

	public short getLength() {
		return remainingLength;
	}

	public String getFS_Version() {
		return BinaryUtils.encode(FS_Version);
	}

	public String getFS_ID() {
		return BinaryUtils.encode(FS_ID);
	}

	public String getFS_State() {
		return BinaryUtils.encode(FS_State);
	}
	
	public String toString() {
		String tmp = "";
		tmp += "File System ID ";
		tmp += getFS_ID();
		tmp += " (v. ";
		tmp += getFS_Version();
		tmp += ")";
		return tmp;
	}

}
