package uk.co.transaxiom.android.txandroidlib.card;

import static uk.co.transaxiom.terminal.currency.CurrencyFormatter.getCurrencyFormatter;
import static uk.co.transaxiom.terminal.currency.CurrencyManager.getCurrencyManager;
import uk.co.transaxiom.terminal.currency.TxCurrency;
import uk.co.transaxiom.terminal.payment.entity.Amount;


public class DisplayTransaction {
	
	BaseTransaction baseTrans;
	String amount;
	long balanceInPennies;
	long payFrontier;
	long loadFrontier;
	
	
	public DisplayTransaction (BaseTransaction baseTransaction, String cleanAmount){
		this.baseTrans = baseTransaction;
		this.amount = cleanAmount;
	}

	public String getTransactionType(){
		return this.baseTrans.getRecordType(this.baseTrans.getRecType());
	}
	
	public String getNarrative(){
		return this.baseTrans.getNarrative();
	}
	
	public String getCurrencyCode(){
		return this.baseTrans.getCurrency();
	}
	
	public String getCurrencySymbol(){
		TxCurrency currency = getCurrencyManager().getCurrency(baseTrans.getCurrency());
		return getCurrencyFormatter().getSymbol(currency);
	}
	
	public String getSequenceNumber(){
		return this.baseTrans.getTransactionNumber();
	}
	
	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public long getAmountAsPennies(){
		Amount pennies = new Amount();
		pennies.setCurrency(baseTrans.getCurrency());
		pennies.setStringAmount(amount);
		
		return pennies.getValue();
	}
	
	public String getCumulativeAmount(){
		return baseTrans.getAmount();
	}

	public long getPayFrontier() {
		return payFrontier;
	}

	public void setPayFrontier(long payFrontier) {
		this.payFrontier = payFrontier;
	}

	public long getLoadFrontier() {
		return loadFrontier;
	}

	public void setLoadFrontier(long loadFrontier) {
		this.loadFrontier = loadFrontier;
	}

	public long getBalanceInPennies() {
		return balanceInPennies;
	}

	public void setBalanceInPennies(long balanceInPennies) {
		this.balanceInPennies = balanceInPennies;
	}

	public String toString(){
		String result = getSequenceNumber() + " | "
				+ amount + " | "
				+ getCumulativeAmount() + " | "
				+ payFrontier + "/" + loadFrontier + " | "
				+ balanceInPennies;
		
		return result;
	}

	public long getCumulAmountAsPennies() {
		Amount pennies = new Amount();
		pennies.setCurrency(baseTrans.getCurrency());
		pennies.setStringAmount(baseTrans.getAmount());
		
		return pennies.getValue();
	}
}

