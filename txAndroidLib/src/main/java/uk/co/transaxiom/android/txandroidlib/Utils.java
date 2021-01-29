package uk.co.transaxiom.android.txandroidlib;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.util.Log;

public class Utils
{

	private static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1',
			(byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
			(byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
			(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' };

	
	public static short byte2BCD(byte tmp){
		byte [] tmp2 = new byte[2];
		tmp2[0] = (byte) 0x00;
		tmp2[1] = (byte) tmp;
		return (short) byteA2BCD(tmp2);
	}
	
	  /**
     * Converts a 4 byte array of unsigned bytes to a long
     * @param bytes an array of 4 unsigned bytes
     * @return a long representing the unsigned int
     */
    public static final long byteA2UnsignedLong(byte[] bytes)
    {
        long l = 0;
        l |= bytes[0] & 0xFF;
        l <<= 8;
        l |= bytes[1] & 0xFF;
        l <<= 8;
        l |= bytes[2] & 0xFF;
        l <<= 8;
        l |= bytes[3] & 0xFF;
        return l;
    }

    /**
     * Converts a two byte array to an integer
     * @param bytes a byte array of length 2
     * @return an int representing the unsigned short
     */
    public static int byteA2UnsignedInt(byte[] bytes)
    {
        int i = 0;
        i |= bytes[0] & 0xFF;
        i <<= 8;
        i |= bytes[1] & 0xFF;
        return i;
    }

	public static byte[] int2ByteArray(int val)
	{
		   return new byte[] {
		            (byte)(val >>> 24),
		            (byte)(val >>> 16),
		            (byte)(val >>> 8),
		            (byte)val};
	}
	public static byte[] int2CleanByteArray(int val){
		if(val == 0){
			return new byte[0];
		}
		int offset = 0;
		int nbOfBytes = 4;
		byte[] original = int2ByteArray(val);
		byte[] cleanBytes = new byte[nbOfBytes];
		
		System.arraycopy(original, offset, cleanBytes, 0, nbOfBytes);
		
		while(original[offset] == (byte)0x00 && nbOfBytes > 0){
			nbOfBytes --;
			offset ++;
			cleanBytes = new byte[nbOfBytes];
			System.arraycopy(original, offset, cleanBytes, 0, nbOfBytes);
		}
		return cleanBytes;
	}
    /**
     * Convert signed int to unsigned long
     * @param input
     * @return
     */
    public static long int2UnsignedLong(int input)
    {
        return input & 0xFFFFFFFFL;
    }

    /**
     * Convert signed short to unsigned int
     * @param input
     * @return
     */
    public static int short2UnsignedInt(short input) {
        return input & 0xFFFF;
    }

	/**
	 * Convert byte array to ASCII string
	 * @param bytes
	 * @return
	 */
	public static String byteA2ASCIIString(byte[] bytes)
	{
		String result = "";
		try {
			result = new String(bytes, 0, bytes.length, "ASCII");
		} catch (UnsupportedEncodingException e) {
			Log.v("UTILS", "byteA2ASCIIString: " + e.getMessage());
		}
		return result;
	}
	
	public static int BCDtoInt(byte[] bcd){
		return Integer.valueOf(BCDtoString(bcd));
	}
	
	public static String BCDtoString(byte[] bcd) {

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < bcd.length; i++) {
			sb.append(BCDtoString(bcd[i]));
		}

		return sb.toString();
	}
	
	public static String BCDtoString(byte bcd) {
		StringBuffer sb = new StringBuffer();
		
		byte high = (byte) (bcd & 0xf0);
		high >>>= (byte) 4;	
		high = (byte) (high & 0x0f);
		byte low = (byte) (bcd & 0x0f);
		
		sb.append(high);
		sb.append(low);
		
		return sb.toString();
	}
	

    /**
     * Convert signed byte to unsigned short
     * @param input
     * @return
     */
    public static short byte2UnsignedShort(byte input) {
        return (short)(input & 0xFF);
    }
	
	/**
	 * Convert array of bytes to int
	 * 
	 * @param bytes
	 * @return
	 */
	public static int byteA2Int(byte[] bytes)
	{
		int value = 0;
		value = ByteBuffer.wrap(bytes).getShort();
		return value;
	}

    /**
     * Convert signed byte to unsigned int
     * @param input
     * @return
     */
    public static int byte2UnsignedInt(byte input) {
        return (int)(input & 0x000000FF);
    }
    
	/**
	 * Convert array of bytes to BCD
	 * 
	 * @param bytes
	 * @return
	 */
	public static int byteA2BCD(byte[] bytes)
	{
		return int2BCD(byteA2Int(bytes));
	}

	/**
	 * Convert int to BCD
	 * 
	 * @param num
	 * @return
	 */
	public static int int2BCD(int num)
	{
		int result = 0;
		int temp;
		int rot = 28;
		int mask = 0xF0000000;
		int mult = 10000000;

		while (rot >= 0)
		{
			temp = (num & mask) >> rot;

			if (temp > 9)
				throw new NumberFormatException();
			else
				result += temp * mult;

			mult /= 10;
			mask = mask >>> 4;
			rot -= 4;
		} // end-while
		return result;
	}

	/**
	 * Convert raw bytes to String
	 * 
	 * @param raw
	 * @param len
	 * @return
	 */
	public static String getHexString(byte[] raw, int len)
	{
		byte[] hex = new byte[2 * len];
		int index = 0;
		int pos = 0;

		for (byte b : raw)
		{
			if (pos >= len)
				break;

			pos++;
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}

		return new String(hex);
	}

	public static String getHexString(byte[] raw)
	{
		return getHexString(raw, raw.length);
	}
	
	public static String getHexVal(byte raw)
	{
		byte[] tmp = new byte[1];
		tmp[0] = raw;
		return getHexString(tmp, 1);
	}

	/**
	 * 
	 * @param data
	 *            : raw data to scan for pattern
	 * @param patten
	 *            : byte array to find
	 * @return List of indices if found, empty list otherwise
	 */
	public static List<Integer> findAll(byte[] data, byte[] pattern)
	{
		List<Integer> result = new ArrayList<Integer>();
		ArrayList<byte[]> splitSourceData = new ArrayList<byte[]>();
		for (int i = 0; i < data.length - 1; i++)
		{
			byte[] tmp = new byte[pattern.length];
			System.arraycopy(data, i, tmp, 0, pattern.length);
			// System.out.println(getHexString(tmp, tmp.length));
			splitSourceData.add(tmp);
			i = i + pattern.length - 1;
		}
		// System.out.println(sourceData.size());
		for (int j = 0; j < splitSourceData.size(); j++)
		{
			// System.out.println("TRYING...");
			// System.out.println(getHexString(sourceData.get(j),
			// sourceData.get(j).length));
			// System.out.println(getHexString(pattern, pattern.length));
			if (Arrays.equals(splitSourceData.get(j), pattern))
			{
				// System.out.println(j);
				result.add(j);
			}
		}
		return result;

	}

	public static byte[] hex2Bytes(String str)
	{
		str = str.replaceAll("\\s", ""); // Strips away spaces
		byte[] bytes = new byte[str.length() / 2];
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2),
					16);
		}
		return bytes;
	}
	
	public static byte chars2Byte(char c1, char c2)
	{
		String str = ""+c1+c2;
		return (byte) Integer.parseInt(str,16);
	}
	
	public static String bytes2hex(byte b)
	{
		return bytes2hex(new byte[]{b});
	}

	public static String bytes2hex(byte[] b)
	{
		// String Buffer can be used instead
		String hs = "";
		String stmp = "";

		for (int n = 0; n < b.length; n++)
		{
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
			hs = hs.toUpperCase(Locale.ENGLISH);
			if (n < b.length - 1)
				hs = hs + "-";
		}
		return hs;
	}
	
	public static byte[] decodeTo3Bytes(int loadAmount) throws Exception
	{
		return decodeTo3Bytes(""+loadAmount);
	}

	public static byte[] decodeTo3Bytes(String value) throws NumberFormatException
	{
		if (value.length() > 6) throw new NumberFormatException("value is too large");
		while (value.length() < 6) value = "0" + value;
		char[] chars = value.toCharArray();
		byte[] bytes = new byte[3];
		
		bytes[0] = chars2Byte(chars[0],chars[1]);
		bytes[1] = chars2Byte(chars[2],chars[3]);
		bytes[2] = chars2Byte(chars[4],chars[5]);
		
		System.out.println(Utils.bytes2hex(bytes));
		
		return bytes;
	}
	
	public static byte[] initByte(int size, byte init)
	{
		byte[] output = new byte[size];
		for(int i = 0; i < output.length; i++) output[i] = init;
		return output;
	}
	
	public static short toShort(byte[] barr, int offset)
	{
		ByteBuffer bb = ByteBuffer.wrap(barr); // Wrapper around underlying byte[].
		ShortBuffer sb = bb.asShortBuffer(); // Wrapper around ByteBuffer.
		// Now traverse ShortBuffer to obtain each short.
		short s1;
		int i = 0;
		do
		{
			s1 = bb.getShort();
		}
		while(++i < offset);
		return s1;
	}

	public static int[] sortIntArrayDescending(int[] units)
	{
		Arrays.sort(units);
		int[] newUnits = new int[units.length];
		for (int i = 0; i < units.length; i++)
		{
			newUnits[units.length-(i+1)] = units[i];
		}
		return newUnits;
	}
	
	public static short byteAtoShort(byte[] barr, int offset)
	{
		ByteBuffer bb = ByteBuffer.wrap(barr); // Wrapper around underlying byte[].
		//ShortBuffer sb = bb.asShortBuffer(); // Wrapper around ByteBuffer.
		// Now traverse ShortBuffer to obtain each short.
		short s1;
		int i = 0;
		do
		{
			s1 = bb.getShort();
		}
		while(++i < offset);
		return s1;
	}
	
	public static byte[] DecToBCDArray(long num) {
		int digits = 0;
 
		long temp = num;
		while (temp != 0) {
			digits++;
			temp /= 10;
		}
 
		int byteLen = digits % 2 == 0 ? digits / 2 : (digits + 1) / 2;
		boolean isOdd = digits % 2 != 0;
 
		byte bcd[] = new byte[byteLen];
 
		for (int i = 0; i < digits; i++) {
			byte tmp = (byte) (num % 10);
 
			if (i == digits - 1 && isOdd)
				bcd[i / 2] = tmp;
			else if (i % 2 == 0)
				bcd[i / 2] = tmp;
			else {
				byte foo = (byte) (tmp << 4);
				bcd[i / 2] |= foo;
			}
 
			num /= 10;
		}
 
		for (int i = 0; i < byteLen / 2; i++) {
			byte tmp = bcd[i];
			bcd[i] = bcd[byteLen - i - 1];
			bcd[byteLen - i - 1] = tmp;
		}
 
		return bcd;
	}

	public static byte[] int2BCDArray(long num) {
		int digits = 0;
 
		long temp = num;
		while (temp != 0) {
			digits++;
			temp /= 10;
		}
 
		int byteLen = digits % 2 == 0 ? digits / 2 : (digits + 1) / 2;
		boolean isOdd = digits % 2 != 0;
 
		byte bcd[] = new byte[byteLen];
 
		for (int i = 0; i < digits; i++) {
			byte tmp = (byte) (num % 10);
 
			if (i == digits - 1 && isOdd)
				bcd[i / 2] = tmp;
			else if (i % 2 == 0)
				bcd[i / 2] = tmp;
			else {
				byte foo = (byte) (tmp << 4);
				bcd[i / 2] |= foo;
			}
 
			num /= 10;
		}
 
		for (int i = 0; i < byteLen / 2; i++) {
			byte tmp = bcd[i];
			bcd[i] = bcd[byteLen - i - 1];
			bcd[byteLen - i - 1] = tmp;
		}
 
		return bcd;
	}
	
	public static byte[] intToBCD3BytesArray(int amount) {
		byte[] tmp = DecToBCDArray(amount);
		byte[] res = new byte[3];
		if (tmp.length == 2)
		{
			res[0] = 0x00; //padding
			System.arraycopy(tmp, 0, res, 1, 2);
			return res;
		}
		else if (tmp.length == 1)
		{
			res[0] = 0x00; //padding
			res[1] = 0x00; //padding
			System.arraycopy(tmp, 0, res, 2, 1);
			return res;
		}
		else return tmp;	
	}
	
	public static byte[] shortTo2BytesArray(short value){
		
		byte[] result = new byte[2];
		result[0] = (byte)((value >> 8) & 0xFF);
		result[1] = (byte)(value & 0xFF);
		
		return result;
	}
	
	public static byte[] concatenateByteArrays (byte[] a, byte[] b){
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
	
}
