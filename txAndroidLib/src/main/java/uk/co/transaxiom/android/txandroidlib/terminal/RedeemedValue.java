package uk.co.transaxiom.android.txandroidlib.terminal;

import static uk.co.transaxiom.terminal.currency.CurrencyFormatter.getCurrencyFormatter;
import static uk.co.transaxiom.terminal.currency.CurrencyManager.getCurrencyManager;
import android.annotation.SuppressLint;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import uk.co.transaxiom.terminal.currency.TxCurrency;
import uk.co.transaxiom.terminal.ncounters.TXnCounter;

public class RedeemedValue {

	private String timestamp;
	private String amount;
	private String currencyCode;
	
	public RedeemedValue(){

	}
	
	@SuppressLint("SimpleDateFormat")
	public void parseIntoValue(List<TXnCounter> ncounters){
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		
		timestamp = format.format(now);
		
		long totalUnits = calculateTotalUnitsUsed(ncounters);
		BigDecimal bigDecimal = BigDecimal.valueOf(totalUnits);
		bigDecimal = bigDecimal.movePointLeft(2);
		
		NumberFormat formatNumber = NumberFormat.getInstance();
		formatNumber.setMinimumFractionDigits(2);
		formatNumber.setMaximumFractionDigits(2);
		
		amount = formatNumber.format(bigDecimal.doubleValue());
		currencyCode = ncounters.get(0).getMerchantCurrencyCode();
	}

	private long calculateTotalUnitsUsed(List<TXnCounter> ncounters) {
		long total = 0;
		
		for(TXnCounter ncounter : ncounters){
			int stepsUsed = ncounter.getnCStepsUsed();
			long stepValue = ncounter.getMerchantStepVal();
			
			total += stepsUsed * stepValue;
		}
		
		return total;
	}

	
	@Override
	public String toString(){
		String result = timestamp + " - "+ getCurrencySymbol() + amount ;
		return result;
	}
	
	public String getTimestamp() {	return timestamp;	}

	public String getAmount() {	return amount;	}

	public String getCurrencyCode() {	return currencyCode;	}
	
	public String getCurrencySymbol() {
		TxCurrency txCurrency = getCurrencyManager().getCurrency(currencyCode);
		return getCurrencyFormatter().getSymbol(txCurrency);
	}
	
	public void setTimestamp(String timestamp) {	this.timestamp = timestamp;	}

	public void setAmount(String amount) {	this.amount = amount;	}

	public void setCurrencyCode(String currency) {	this.currencyCode = currency;	}

	public void setAmount(double amnt) {
		NumberFormat formatNumber = NumberFormat.getInstance();
		formatNumber.setMinimumFractionDigits(2);
		formatNumber.setMaximumFractionDigits(2);
		
		amount = formatNumber.format(amnt);
	}



}
