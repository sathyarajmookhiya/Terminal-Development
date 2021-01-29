package uk.co.transaxiom.android.txandroidlib.design;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class LightTextView extends TextView{

	public LightTextView(Context context) {
		super(context);
		init();
	}
	
	public LightTextView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	public LightTextView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	}
	
	private void init(){
		Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/titilliumLight.otf");
		this.setTypeface(bold);
	}
}
