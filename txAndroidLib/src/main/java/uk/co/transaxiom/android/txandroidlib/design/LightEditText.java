package uk.co.transaxiom.android.txandroidlib.design;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class LightEditText extends EditText {

	public LightEditText(Context context) {
		super(context);
		init();
	}

	public LightEditText(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	public LightEditText(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	}
	
	private void init(){
		Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/titilliumLight.otf");
		this.setTypeface(bold);
	}
	
}
