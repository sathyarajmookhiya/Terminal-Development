package uk.co.transaxiom.android.txandroidlib.design;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class BoldTextView extends TextView{

	public BoldTextView(Context context) {
		super(context);
		init();
	}
	
	public BoldTextView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	public BoldTextView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	}
	
	private void init(){
		Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/titilliumBold.otf");
		this.setTypeface(bold);
	}

}
