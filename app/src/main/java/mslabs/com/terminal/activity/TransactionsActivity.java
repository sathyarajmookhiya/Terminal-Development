package mslabs.com.terminal.activity;

import java.util.ArrayList;
import java.util.List;

import mslabs.com.terminal.R;
import mslabs.com.terminal.Utils.AsyncClass;
import mslabs.com.terminal.Utils.TransactionAdapter;
import uk.co.transaxiom.android.txandroidlib.AndroidWrapper;
import uk.co.transaxiom.android.txandroidlib.BaseActivity;
import uk.co.transaxiom.android.txandroidlib.TxTheme;
import uk.co.transaxiom.android.txandroidlib.terminal.AndroidTerminalWrapper;
import uk.co.transaxiom.android.txandroidlib.terminal.Operations;

import uk.co.transaxiom.terminal.TxTerminal;
import uk.co.transaxiom.terminal.payment.entity.TxTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TransactionsActivity extends BaseActivity {

	private List<TxTransaction> transactions = new ArrayList<TxTransaction>();
	private int lastExpandedPosition = -1;

	Toolbar toolbar;

	public List<TxTransaction> getTransactions() {
		return transactions;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transactions);

		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.wizard_icon));
		getSupportActionBar().setTitle("    " + getString(R.string.app_name));


		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		
		SharedPreferences settings = getSharedPreferences(MainActivity.TERM_DATA, 0);
		TxTerminal terminal = wrapper.getTerminalDetailsFromSharedPreference(settings);

		List<TxTransaction> allTransactions = wrapper.getAllTransactions(this);
	
		TextView terminalTypeTxtVw = (TextView)	findViewById(R.id.terminalType_TxtVw);
		terminalTypeTxtVw.setText(terminal.getTerminalType());
		
		if(allTransactions.isEmpty()){
			terminalTypeTxtVw.setText("No transactions");
			Toast.makeText(this, "No transactions found on the terminal...", Toast.LENGTH_SHORT).show();
		}else{
			displayTransactions(allTransactions);
		}
		
		/*theme = AndroidWrapper.getInstance().getThemeFromSharedPreferences(this);
		if(!theme.getName().equals("N/A")){*/
			updateLayout();
		//}
	}

	
	public void updateLayout() {
		super.updateLayout();
		/*
		View rootView = findViewById(R.id.rootLayout);
		rootView.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));
		
		Bitmap bmp = AndroidWrapper.getInstance().readImageFromFile(theme.getLogoLocation());
		ImageView schemeLogo_ImgVw = (ImageView) findViewById(R.id.schemeLogo_ImgVw);
		schemeLogo_ImgVw.setImageBitmap(bmp);
		
		TextView schemeName_TxtVw = (TextView) findViewById(R.id.schemeNameTxtVw);
		schemeName_TxtVw.setTextColor(Color.parseColor(theme.getColorMain()));
		schemeName_TxtVw.setText(theme.getName());
		
		TextView terminalTypeTxtVw = (TextView)	findViewById(R.id.terminalType_TxtVw);
		terminalTypeTxtVw.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));
		terminalTypeTxtVw.setTextColor(Color.parseColor(theme.getColorMain()));
		*/
		TextView terminalTypeTxtVw = (TextView)	findViewById(R.id.terminalType_TxtVw);

		Button uploadBtn = (Button) findViewById(R.id.upload_Btn);
		/*uploadBtn.setBackgroundColor(Color.parseColor(theme.getColorMain()));
		uploadBtn.setTextColor(Color.parseColor(theme.getPageColor()));
		uploadBtn.setOnTouchListener(new MyTouchListener());*/
		uploadBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				uploadTransactions(v);
			}
		});
	}

/*	public void buttonClicked(View view){
		uploadTransactions(view);
	}*/
	
	private void displayTransactions(List<TxTransaction> allTransactions) {
		TransactionAdapter adapter = new TransactionAdapter(this, allTransactions, this);
		
		final ExpandableListView transactionsLstVw = (ExpandableListView) findViewById(R.id.transactions_ExpLstVw);
		transactionsLstVw.setAdapter(adapter);
		transactionsLstVw.setOnGroupExpandListener(new OnGroupExpandListener() {

		    @Override
		    public void onGroupExpand(int groupPosition) {
		            if (lastExpandedPosition != -1
		                    && groupPosition != lastExpandedPosition) {
		            	transactionsLstVw.collapseGroup(lastExpandedPosition);
		            }
		            lastExpandedPosition = groupPosition;
		    }
		});
	}

	
	public void uploadTransactions(View view){
		AndroidTerminalWrapper wrapper = AndroidTerminalWrapper.getInstance();
		List<TxTransaction> transactionsToUpload = wrapper.getAllTransactionsToUpload(this);
		
		if(transactionsToUpload.isEmpty()){
			Toast.makeText(this, "You have no transactions to upload!", Toast.LENGTH_SHORT).show();
		}else{
			transactions = transactionsToUpload;
			new AsyncClass(this, Operations.UPLOAD_TRANSACTIONS).execute(Operations.UPLOAD_TRANSACTIONS);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.transactions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.about:
	        	displayAbout();
	            return true;
			case android.R.id.home:
				onBackPressed();
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

}
