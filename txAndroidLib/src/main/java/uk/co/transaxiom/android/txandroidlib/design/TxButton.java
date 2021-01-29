package uk.co.transaxiom.android.txandroidlib.design;

import uk.transaxiom.android.txandroidlib.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class TxButton extends Button {

	public TxButton(Context context) {
		super(context);
		init();
	}
	
	public TxButton(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	public TxButton(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	}
	
	private void init(){
		Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/titilliumBold.otf");
		this.setTypeface(bold);
		
		this.setBackgroundResource(R.drawable.tx_button_background);
		this.setPadding(0, 7, 0, 15);
		
		try{
            XmlResourceParser parser = getResources().getXml(R.drawable.tx_button_text_color);
            ColorStateList colors = ColorStateList.createFromXml(getResources(), parser);
            this.setTextColor(colors);
        }catch (Exception e){
            e.printStackTrace();
        }
	}

}
