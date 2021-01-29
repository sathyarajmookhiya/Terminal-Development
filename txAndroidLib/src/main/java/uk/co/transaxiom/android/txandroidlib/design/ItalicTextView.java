package uk.co.transaxiom.android.txandroidlib.design;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class ItalicTextView extends TextView {

	public ItalicTextView(Context context) {
		super(context);
		init();
	}

	public ItalicTextView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	public ItalicTextView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	}
	
	private void init(){
		Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/titilliumItalic.otf");
		this.setTypeface(bold);
	}
}
