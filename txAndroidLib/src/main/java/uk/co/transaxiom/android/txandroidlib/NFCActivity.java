package uk.co.transaxiom.android.txandroidlib;

import uk.transaxiom.android.txandroidlib.R;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class NFCActivity extends BaseActivity {

	public static final int SELECT_APP_REQUEST = 1;
	
	private boolean shared = false;
	
	public IsoDep currentCard;
	
	public IsoDep getCurrentCard() {	return currentCard;	}

	public void setCurrentCard(IsoDep currentCard) {	this.currentCard = currentCard;	}

	public boolean isShared() {	return shared;	}
	
	public void setShared(boolean shared) {	this.shared = shared;	}


	public void playSuccessSound(){
		setVolumeControlStream(AudioManager.STREAM_RING);
        
        MediaPlayer player = MediaPlayer.create(this, R.raw.success);
        player.start();
	}
	
	public void playFailSound(){
		setVolumeControlStream(AudioManager.STREAM_RING);
        
        MediaPlayer player = MediaPlayer.create(this, R.raw.fail);
        player.start();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (NfcAdapter.getDefaultAdapter(this) != null) {
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					new Intent(this, getClass())
							.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this,
					pendingIntent, null, null);
		}
	}
		
	@Override
	protected void onPause() {
		super.onPause();
		if (NfcAdapter.getDefaultAdapter(this) != null)
			NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
	}
	
	protected void shareFile(String filePath, String cardType) {
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/rtf");
		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+filePath));
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, cardType+" Card content");
		startActivityForResult(Intent.createChooser(shareIntent, "Send to:"), SELECT_APP_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int reqCode, int resCode, Intent data){
		switch(reqCode) {
		case SELECT_APP_REQUEST:
			Toast.makeText(this, "File shared succesfully!", Toast.LENGTH_SHORT).show();
			shared = true;
			break;
		default:
			Toast.makeText(this, "Something went wrong while sharing the file...", Toast.LENGTH_SHORT).show();
			break;
		}
		super.onActivityResult(reqCode, resCode, data);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   
		return true;
	}

}
