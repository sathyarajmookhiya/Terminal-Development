package uk.co.transaxiom.android.txandroidlib;

import uk.transaxiom.android.txandroidlib.R;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity {

	protected TxTheme theme = new TxTheme();
	
	/*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);
    }
	*/
	public void showError(String message){
		Toast.makeText(this, "Error: "+message, Toast.LENGTH_SHORT).show();
	}
	
	public void displayAbout(){
		AndroidWrapper wrapper = AndroidWrapper.getInstance();
		wrapper.displayVersions(this);
	}
	
	public void refreshScreen(){
		Intent intent = new Intent(this, this.getClass());
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		startActivity(intent);
	}
	
	public void revertToInstalledLayout() {
		AndroidWrapper wrapper = AndroidWrapper.getInstance();
		wrapper.eraseThemeFromSharedPreferences(this);
		refreshScreen();
	}
	
	public void updateLayout() {
/*		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme.getColorMain())));
		AndroidWrapper.getInstance().setActionBarImage(this, theme.getLogoLocation(), theme);*/
	}
	
	public void buttonClicked(View view) {
		
	}
	
	public class MyTouchListener implements OnTouchListener{
		
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				/*view.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));
				((Button) view).setTextColor(Color.parseColor(theme.getColorMain()));*/
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){
				/*view.setBackgroundColor(Color.parseColor(theme.getColorMain()));
				((Button) view).setTextColor(Color.parseColor(theme.getPageColor()));
				buttonClicked(view);*/
			}
			return true;
		}
	}

	
}
