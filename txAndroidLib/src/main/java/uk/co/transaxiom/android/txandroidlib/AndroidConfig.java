package uk.co.transaxiom.android.txandroidlib;

public class AndroidConfig {

	private static final String majorNumber = "2";
	private static final String minorNumber = "9";
	private static String versionNumber;
	
	public static String getVersionNumber(){
		versionNumber = majorNumber + "." + minorNumber;
		return versionNumber;
	}
	
	
}
