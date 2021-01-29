package uk.co.transaxiom.android.txandroidlib.cardmanagement.applet.filesystem;

import java.io.IOException;

import android.nfc.tech.IsoDep;
import android.util.Log;
import uk.co.transaxiom.android.txandroidlib.Utils;
import uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu.APDUFactory;
import uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu.CommandAPDU;
import uk.co.transaxiom.android.txandroidlib.cardmanagement.apdu.ResponseAPDU;
import uk.co.transaxiom.terminal.common.utils.BinaryUtils;

public class FSApplet {

	private IsoDep card;
	
	public static final String appletName = "File System";
	
	public static final int MAX_APDU_SIZE = 128;
	public static final int MAX_DATA_SENT_SIZE = MAX_APDU_SIZE - 9;
	public static final int READ_CMD_SIZE = 9;
	public static final int MAX_RESP_SIZE = 126;
	
	public static final byte IMAGE_FILE_ID = (byte) 0x00;
	public static final int IMAGE_FILE_SIZE = 6000;
	public static final byte ADATA_FILE_ID = (byte) 0x01;
	public static final int ADATA_FILE_SIZE = 2000;
	
	public static final byte[] FS_AID = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x05, 0x44, 0x0F, 0x01, (byte) 0xFF, (byte) 0x80};
	
	public static final byte[] SQUID_FS_ID = new byte[] {0x11, 0x22, 0x33, 0x44};
	public static final byte[] VACCINE_FS_ID = new byte[] {0x12, 0x34, 0x56, 0x78};
	
	public static final byte[] FS_CMD_OPEN_APPLET = new byte[] {0x00, (byte) 0xA4, 0x04, 0x00};
	
	public static final byte[] FS_CMD_FORMAT_FS = new byte[] {(byte) 0xD0, 0x70, 0x00, 0x00};

	public static final byte[] FS_CMD_READ_FILE = new byte[] {(byte) 0xD0, 0x71, 0x00, 0x00};
	
	public static final byte[] FS_CMD_WRITE_FILE = new byte[] {(byte) 0xD0, 0x72, 0x00, 0x00};
	
	public static final byte[] FS_ERASE_FS_CMD = new byte[] {(byte) 0xD0, 0x73, 0x00, 0x00};
	
	private FSApplet(){
	}
	
	public FSApplet(IsoDep card){
		this.card = card;
	}

	/**
	 * Open the File System Applet
	 * 
	 * @return ResponseAPDU FSfci
	 * @throws IOException
	 */
	public ResponseAPDU openApplet() throws IOException{
		APDUFactory factory = APDUFactory.getInstance();
		CommandAPDU cmd = factory.createCommand(
				FS_CMD_OPEN_APPLET[0], 
				FS_CMD_OPEN_APPLET[1],
				FS_CMD_OPEN_APPLET[2],
				FS_CMD_OPEN_APPLET[3],
				FS_AID);
		
		byte[] tmp = card.transceive(cmd.getData());
		ResponseAPDU response = new ResponseAPDU(tmp);
		return response;
	}
	
	/**
	 * Format the applet and instantiate 2 files
	 * 
	 * @param fsId: the id of the File System applet on the card
	 * @param legnthFile1: the full length of File 1
	 * @param lengthFile2: the full length of File 2
	 * 
	 * @return true if format operation succeeded
	 * @throws IOException
	 */
	public boolean formatApplet(byte[] fsId, int legnthFile) throws IOException{
		byte nbOfFile = (byte) 1;

		byte[] bLengthFile = Utils.int2ByteArray(legnthFile);
		
		byte[] DATA = new byte[fsId.length + 1 + 2];
		System.arraycopy(fsId, 0, DATA, 0, fsId.length);
		DATA[4] = nbOfFile;
		DATA[5] = bLengthFile[2];
		DATA[6] = bLengthFile[3];
		
		APDUFactory factory = APDUFactory.getInstance();
		CommandAPDU cmd = factory.createCommand(
				FS_CMD_FORMAT_FS[0], 
				FS_CMD_FORMAT_FS[1], 
				FS_CMD_FORMAT_FS[2], 
				FS_CMD_FORMAT_FS[3], 
				DATA);
		
		byte[] tmp = card.transceive(cmd.getData());
		ResponseAPDU response = new ResponseAPDU(tmp);
		
		return response.getErrorCode().isOk();
	}
	
	/**
	 * Format the applet and instantiate 2 files
	 * 
	 * @param fsId: the id of the File System applet on the card
	 * @param legnthFile1: the full length of File 1
	 * @param lengthFile2: the full length of File 2
	 * 
	 * @return true if format operation succeeded
	 * @throws IOException
	 */
	public boolean formatApplet(byte[] fsId, int legnthFile1, int lengthFile2) throws IOException{
		byte nbOfFile = (byte) 2;

		byte[] bLengthFile1 = Utils.int2ByteArray(legnthFile1);
		byte[] bLengthFile2 = Utils.int2ByteArray(lengthFile2);
		
		byte[] DATA = new byte[fsId.length + 1 + 2 + 2];
		System.arraycopy(fsId, 0, DATA, 0, fsId.length);
		DATA[4] = nbOfFile;
		DATA[5] = bLengthFile1[2];
		DATA[6] = bLengthFile1[3];
		DATA[7] = bLengthFile2[2];
		DATA[8] = bLengthFile2[3];
		
		APDUFactory factory = APDUFactory.getInstance();
		CommandAPDU cmd = factory.createCommand(
				FS_CMD_FORMAT_FS[0], 
				FS_CMD_FORMAT_FS[1], 
				FS_CMD_FORMAT_FS[2], 
				FS_CMD_FORMAT_FS[3], 
				DATA);
		
		byte[] tmp = card.transceive(cmd.getData());
		ResponseAPDU response = new ResponseAPDU(tmp);
		
		return response.getErrorCode().isOk();
	}
	
	/**
	 * Write file, from offset 0 to length of the data + 2 bytes describing the length of the data to be written.
	 * 
	 * @param fileId: id of the file (0 or 1)
	 * @param fileData: the full data to write into the file
	 * 
	 * @return ResponseAPDU sent back from the card, describing if write was successful or not
	 * @throws IOException
	 */
	public ResponseAPDU writeFile(byte fileId, byte[] fileData) throws IOException{
		int nbBytes = fileData.length;
		byte[] fileSize = Utils.int2ByteArray(nbBytes);
		byte[] finalData = new byte[nbBytes + 2];
		
		System.arraycopy(fileSize, 2, finalData, 0, 2);
		System.arraycopy(fileData, 0, finalData, 2, nbBytes);
		Log.d("WRITEFILEONCARD", "file length = "+finalData.length);
		
		int cmdSent = 0;
		int offset = 0;
		int nbOfCmds = finalData.length / MAX_DATA_SENT_SIZE;
		
		while(cmdSent <= nbOfCmds){
			offset = cmdSent * MAX_DATA_SENT_SIZE;
			Log.d("AsyncClass", "Offset="+offset);
			
			if(offset + MAX_DATA_SENT_SIZE < finalData.length){
				
				byte[] tmpData = new byte[MAX_DATA_SENT_SIZE];
				System.arraycopy(finalData, offset, tmpData, 0, MAX_DATA_SENT_SIZE);
				
				ResponseAPDU response = writeFile(fileId, offset, tmpData);
				
				if(response.getErrorCode().isOk()){
					cmdSent ++;
				}else{
					return response;
				}
			}
			else{
				
				int bytesLeft = finalData.length - offset;
				byte[] tmpData = new byte[bytesLeft];
				System.arraycopy(finalData, offset, tmpData, 0, bytesLeft);
				
				ResponseAPDU response = writeFile(fileId, offset, tmpData);
				
				return response;
			}
		}
		return null;
	}
	
	/**
	 * Read the whole file from offset 0 to the length of the data written
	 * 
	 * @param fileId: id of the file to be read
	 * 
	 * @return byte[] containing the full data 
	 * @throws IOException
	 */
	public byte[] readFileData(byte fileId) throws IOException{
		try{
			int fileSize = readFileSize(fileId) + 2;
			byte[] fileData = new byte[fileSize];
			
			int index = 0;
			int offset = 0;
			int nbCmd = fileSize / FSApplet.MAX_RESP_SIZE;

			while (index <= nbCmd) {
				offset = index * MAX_RESP_SIZE;
				Log.d("AsyncClass", "Offset=" + offset);

				if (offset + 126 < fileSize) {
					int lengthToRead = FSApplet.MAX_RESP_SIZE;
					
					ResponseAPDU response = readFile(fileId, offset, lengthToRead);
					if(response != null && response.getErrorCode().isOk()){
						index++;
						System.arraycopy(response.getResponseData(), 0, fileData, offset, response.getResponseData().length);
					}else {
						return fileData;
					}
					
				} else {
					int bytesLeft = fileSize - offset;
					
					ResponseAPDU response = readFile(fileId, offset, bytesLeft);
					if(response != null && response.getErrorCode().isOk()){
						index++;
						System.arraycopy(response.getResponseData(), 0, fileData, offset, response.getResponseData().length);
					}else {
						return fileData;
					}
				}
			}
			return fileData;
		}catch (Exception e){
			return null;
		}
	}
	
	/**
	 * Read the length of the data contained in the file. 
	 * This method is private and only used by readFileData(byte fileId);
	 * 
	 * @param fileId: id of the file
	 * 
	 * @return int the length of data to read
	 * @throws IOException
	 */
	private int readFileSize(byte fileId) throws IOException{
		ResponseAPDU response = readFile(fileId, 0, 2);
		int fileSize = Utils.byteA2UnsignedInt(response.getResponseData());
		
		return fileSize;
	}
	
	/**
	 * Read specified length of data, starting from the specified offset.
	 *  
	 * @param fileId: if of the file to read
	 * @param offset: position to start reading from
	 * @param length: length of data to read
	 * 
	 * @return ResponseAPDU containing the content of the file
	 * @throws IOException
	 */
	public ResponseAPDU readFile(byte fileId, int offset, int length) throws IOException{
		byte[] bOffset = Utils.int2ByteArray(offset);
		byte[] DATA = new byte[4];
		DATA[0] = fileId;
		DATA[1] = bOffset[2];
		DATA[2] = bOffset[3];
		DATA[3] = (byte) length;
		
		APDUFactory factory = APDUFactory.getInstance();
		CommandAPDU cmd = factory.createCommand(
				FS_CMD_READ_FILE[0], 
				FS_CMD_READ_FILE[1],
				FS_CMD_READ_FILE[2],
				FS_CMD_READ_FILE[3],
				DATA);
		
		byte[] tmp = card.transceive(cmd.getData());
		ResponseAPDU response = new ResponseAPDU(tmp);
		Log.d("ReadResponseFromCard", "content="+BinaryUtils.encode(response.getData()));
		return response;
	}
	
	/**
	 * Write data from specified offset.
	 * 
	 * @param fileID: id of the file to write
	 * @param offset: position to start writing from
	 * @param fileData: the data to write
	 * 
	 * @return ResponseAPDU describing if writing was successful or not
	 * @throws IOException
	 */
	public ResponseAPDU writeFile(byte fileID, int offset, byte[] fileData) throws IOException{
		byte[] bOffset = Utils.int2ByteArray(offset);
		byte[] dataPrefix = new byte[3];
		dataPrefix[0] = fileID;
		dataPrefix[1] = bOffset[2];
		dataPrefix[2] = bOffset[3];
		
		byte[] DATA = new byte[dataPrefix.length + fileData.length + 1];
		
		System.arraycopy(dataPrefix, 0, DATA, 0, dataPrefix.length);
		DATA[dataPrefix.length] = (byte) fileData.length;
		System.arraycopy(fileData, 0, DATA, dataPrefix.length + 1, fileData.length);
		
		APDUFactory factory = APDUFactory.getInstance();
		CommandAPDU cmd = factory.createCommand(
				FS_CMD_WRITE_FILE[0], 
				FS_CMD_WRITE_FILE[1], 
				FS_CMD_WRITE_FILE[2], 
				FS_CMD_WRITE_FILE[3], 
				DATA);
		
		byte[] tempResp = card.transceive(cmd.getData());
		ResponseAPDU resp = new ResponseAPDU(tempResp);

		return resp;
	}
	
}
