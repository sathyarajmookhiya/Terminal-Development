package uk.co.transaxiom.android.txandroidlib.cardmanagement.applet.filesystem;

import uk.co.transaxiom.android.txandroidlib.Utils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Image {
	
	private byte[] imageSize = new byte[2];
	private byte[] data;
	private Bitmap bitmap;
	
	public Image (byte[] data){
		int size = data.length;
		byte[] tmp = Utils.int2ByteArray(size);
		System.arraycopy(tmp, 2, imageSize, 0, 2);
		this.data = data;
		this.bitmap = BitmapFactory.decodeByteArray(data, 0, data.length); 
	}

	public byte[] getData() {	return data;	}

	public void setData(byte[] data) {	this.data = data;	}

	public Bitmap getBitmap() {	return bitmap;	}

	public void setBitmap(Bitmap bitmap) {	this.bitmap = bitmap;	}
	
	

}
