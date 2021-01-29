package uk.co.transaxiom.android.txandroidlib.cardmanagement.applet.filesystem;

import uk.co.transaxiom.android.txandroidlib.Utils;

public class RawDataTranslator {

	private static RawDataTranslator instance = new RawDataTranslator();

	private RawDataTranslator(){
	}
	
	public static RawDataTranslator getInstance(){
		return instance;
	}
	
	public byte[] dataFromFile(byte[] fileData, int offset){
		int dataLength = Utils.byteA2UnsignedInt(new byte[]{fileData[offset], fileData[offset + 1]});
		
		byte[] data = new byte[dataLength];
		System.arraycopy(fileData, offset + 2, data, 0, dataLength);
		return data;
	}
	
	public AdminData toAdminData (byte[] rawFile){
		int offset = 0;
		byte[] fullData = dataFromFile(rawFile, offset);
		
		byte[] bStudentName = dataFromFile(fullData, offset);
		offset += bStudentName.length + 2;
		
		byte[] bSchool = dataFromFile(fullData, offset);
		offset += bSchool.length + 2;
		
		byte[] bClass = dataFromFile(fullData, offset);
		offset += bClass.length + 2;
		
		byte[] studentId = dataFromFile(fullData, offset);
		offset += studentId.length + 2;
		
		byte[] cardNumber = dataFromFile(fullData, offset);
		
		return toAdminData(bStudentName, bSchool, bClass, studentId, cardNumber);
	}
	
	private AdminData toAdminData(byte[] bStudentName, byte[] bSchool, byte[] bClass, byte[] studentId, byte[] cardNumber){
		AdminData adminData = new AdminData();
		
		String studentName = new String(bStudentName);
		String schoolName = new String(bSchool);
		String className = new String(bClass);
		
		adminData.setStudentName(studentName);
		adminData.setSchoolName(schoolName);
		adminData.setClassName(className);
		adminData.setCardNumber(cardNumber);
		adminData.setStudentId(studentId);
		
		return adminData;
	}
	
}
