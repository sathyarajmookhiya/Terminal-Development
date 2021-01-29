package uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu;

public class CommandAPDU extends APDU {
	
	private final int PREFIX_CMD_LENGTH = 4;
	
	private byte[] data;
	private byte CLA;
	private byte INS;
	private byte PARAM_1;
	private byte PARAM_2;
	private byte[] DATA_LENGTH;
	private byte[] TRANS_DATA;
	private byte[] MAX_RESP_LENGTH;
	
	public CommandAPDU(byte CLA, byte INS, byte PARAM_1, byte PARAM_2, byte[] DATA_LENGTH, byte[] DATA, byte[] MAX_RESP_LENGTH){
		int transactionLength = DATA.length;
		int offset = 0;
		
		data = new byte[PREFIX_CMD_LENGTH + DATA_LENGTH.length + transactionLength + MAX_RESP_LENGTH.length];
		data[offset] = CLA; 
		this.CLA = CLA;
		offset++;
		data[offset] = INS;
		this.INS = INS;
		offset++;
		data[offset] = PARAM_1;
		this.PARAM_1 = PARAM_1;
		offset++;
		data[offset] = PARAM_2;
		this.PARAM_2 = PARAM_2;
		offset++;

		this.DATA_LENGTH = new byte [DATA_LENGTH.length];
		this.TRANS_DATA = new byte [DATA.length];
		if(DATA_LENGTH.length > 0){
			System.arraycopy(DATA_LENGTH, 0, data, offset, DATA_LENGTH.length);
			System.arraycopy(DATA_LENGTH, 0, this.DATA_LENGTH, 0, DATA_LENGTH.length);
			offset += DATA_LENGTH.length;
			
			System.arraycopy(DATA, 0, data, offset, transactionLength);
			System.arraycopy(DATA, 0, this.TRANS_DATA, 0, transactionLength);
			offset += transactionLength;
		}
		
		this.MAX_RESP_LENGTH = new byte[MAX_RESP_LENGTH.length];
		if(MAX_RESP_LENGTH.length > 0){
			System.arraycopy(MAX_RESP_LENGTH, 0, data, offset, MAX_RESP_LENGTH.length);
			System.arraycopy(MAX_RESP_LENGTH, 0, this.MAX_RESP_LENGTH, 0, MAX_RESP_LENGTH.length);
		}
	}

	@Override
	public byte[] getData() {
		return data;
	}

	public int getPREFIX_CMD_LENGTH() {	return PREFIX_CMD_LENGTH;	}

	public byte getCLA() {	return CLA;	}

	public byte getINS() {	return INS;	}

	public byte getPARAM_1() {	return PARAM_1;	}

	public byte getPARAM_2() {	return PARAM_2;	}

	public byte[] getDATA_LENGTH() {	return DATA_LENGTH;	}

	public byte[] getTRANS_DATA() {	return TRANS_DATA;	}

	public byte[] getMAX_RESP_LENGTH() {	return MAX_RESP_LENGTH;	}


}
