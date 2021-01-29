package uk.co.transaxiom.android.txandroidlib.cardmanagement.applet.filesystem;

import uk.co.transaxiom.android.txandroidlib.Utils;

public class AdminData {
	
	public static final int STUDENT_ID_SIZE = 4;
	public static final int CARD_NUMBER_SIZE = 4;
	 
	private byte[] data;
	
	private String studentName;
	private String schoolName;
	private String className;
	private byte[] studentId;
	private byte[] cardNumber;
	
	public AdminData (String studentName, String schoolName, String className, byte[] studentId, byte[] cardNumber){
		this.studentId = new byte[studentId.length];
		this.cardNumber = new byte[cardNumber.length];
		
		this.studentName = studentName;
		this.schoolName = schoolName;
		this.className = className;
		System.arraycopy(studentId, 0, this.studentId, 0, studentId.length);
		System.arraycopy(cardNumber, 0, this.cardNumber, 0, cardNumber.length);
		
		serialise();
	}

	
	public AdminData() {
	}

	
	public byte[] getData() {	return data;	}	

	public void setData(byte[] data) {	this.data = data;	}

	public String getStudentName() {	return studentName;	}

	public void setStudentName(String studentName) {	this.studentName = studentName;	}

	public String getSchoolName() {	return schoolName;	}

	public void setSchoolName(String schoolName) {	this.schoolName = schoolName;	}

	public String getClassName() {	return className;	}

	public void setClassName(String className) {	this.className = className;	}

	public byte[] getStudentId() {	return studentId;	}

	public void setStudentId(byte[] studentId) {	this.studentId = studentId;	}

	public byte[] getCardNumber() {	return cardNumber;	}

	public void setCardNumber(byte[] cardNumber) {	this.cardNumber = cardNumber;	}

	
	private void serialise() {
		int totalLength = 0;
		
		byte[] bStudentName = studentName.getBytes();
		byte[] studentNameLength = Utils.int2ByteArray(bStudentName.length);
		totalLength += bStudentName.length + 2;
		
		byte[] bSchool = schoolName.getBytes();
		byte[] schoolLength = Utils.int2ByteArray(bSchool.length);
		totalLength += bSchool.length + 2;
		
		byte[] bClass = className.getBytes();
		byte[] classLength = Utils.int2ByteArray(bClass.length);
		totalLength += bClass.length + 2;
		
		byte[] studentIdLength = Utils.int2ByteArray(studentId.length);
		totalLength += studentId.length + 2;
		
		byte[] cardNumberLength = Utils.int2ByteArray(cardNumber.length);
		totalLength += cardNumber.length + 2;
		
		int offset = 0;
		data = new byte[totalLength];
		System.arraycopy(studentNameLength, 2, data, offset, 2);
		offset += 2;
		System.arraycopy(bStudentName, 0, data, offset, bStudentName.length);
		offset += bStudentName.length;
		
		System.arraycopy(schoolLength, 2, data, offset, 2);
		offset += 2;
		System.arraycopy(bSchool, 0, data, offset, bSchool.length);
		offset += bSchool.length;
		
		System.arraycopy(classLength, 2, data, offset, 2);
		offset += 2;
		System.arraycopy(bClass, 0, data, offset, bClass.length);
		offset += bClass.length;
		
		System.arraycopy(studentIdLength, 2, data, offset, 2);
		offset += 2;
		System.arraycopy(studentId, 0, data, offset, studentId.length);
		offset += studentId.length;
		
		System.arraycopy(cardNumberLength, 2, data, offset, 2);
		offset += 2;
		System.arraycopy(cardNumber, 0, data, offset, cardNumber.length);
	}
	


}
