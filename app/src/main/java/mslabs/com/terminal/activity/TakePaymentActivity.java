package mslabs.com.terminal.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.davidmiguel.numberkeyboard.NumberKeyboard;
import com.davidmiguel.numberkeyboard.NumberKeyboardListener;

import java.text.NumberFormat;

import mslabs.com.terminal.R;


public class TakePaymentActivity extends AppCompatActivity implements NumberKeyboardListener {

    private static final double MAX_ALLOWED_AMOUNT = 9999.99;
    private static final int MAX_ALLOWED_DECIMALS = 2;

    private TextView amountEditText;
    private String amountText;
    private double amount;

    Toolbar toolbar;

    public TakePaymentActivity() {
        this.amountText = "";
        this.amount = 0.0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_payment);


        amountEditText = findViewById(R.id.amount);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.wizard_icon));
        getSupportActionBar().setTitle("    "+getString(R.string.app_name));
        NumberKeyboard numberKeyboard = findViewById(R.id.numberKeyboard);
        numberKeyboard.setListener(this);
        amountEditText = findViewById(R.id.amount);

    }

    @Override
    public void onNumberClicked(int number) {
        if (amountText.isEmpty() && number == 0) {
            return;
        }
        updateAmount(amountText + number);
    }

    @Override
    public void onLeftAuxButtonClicked() {
        // Comma button
        if (!hasComma(amountText)) {
            amountText = amountText.isEmpty() ? "0," : amountText + ",";
            showAmount(amountText);
        }
    }

    @Override
    public void onRightAuxButtonClicked() {
        // Delete button
        if (amountText.isEmpty()) {
            return;
        }
        String newAmountText;
        if (amountText.length() <= 1) {
            newAmountText = "";
        } else {
            newAmountText = amountText.substring(0, amountText.length() - 1);
            if (newAmountText.charAt(newAmountText.length() - 1) == ',') {
                newAmountText = newAmountText.substring(0, newAmountText.length() - 1);
            }
            if ("0".equals(newAmountText)) {
                newAmountText = "";
            }
        }
        updateAmount(newAmountText);
    }

    /**
     * Update new entered amount if it is valid.
     */
    private void updateAmount(String newAmountText) {
        double newAmount = newAmountText.isEmpty() ? 0.0 : Double.parseDouble(newAmountText.replaceAll(",", "."));
        if (newAmount >= 0.0 && newAmount <= MAX_ALLOWED_AMOUNT
                && getNumDecimals(newAmountText) <= MAX_ALLOWED_DECIMALS) {
            amountText = newAmountText;
            amount = newAmount;
            showAmount(amountText);
        }
    }

    /**
     * Add . every thousand.
     */
    private String addThousandSeparator(String amount) {
        String integer;
        String decimal;
        if (amount.indexOf(',') >= 0) {
            integer = amount.substring(0, amount.indexOf(','));
            decimal = amount.substring(amount.indexOf(','), amount.length());
        } else {
            integer = amount;
            decimal = "";
        }
        if (integer.length() > 3) {
            StringBuilder tmp = new StringBuilder(integer);
            for (int i = integer.length() - 3; i > 0; i = i - 3) {
                tmp.insert(i, ".");
            }
            integer = tmp.toString();
        }
        return integer + decimal;
    }

    /**
     * Shows amount in UI.
     */
    private void showAmount(String amount) {
        amountEditText.setText("€" + (amount.isEmpty() ? "0" : addThousandSeparator(amount)));
    }

    /**
     * Checks whether the string has a comma.
     */
    private boolean hasComma(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ',') {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate the number of decimals of the string.
     */
    private int getNumDecimals(String num) {
        if (!hasComma(num)) {
            return 0;
        }
        return num.substring(num.indexOf(',') + 1, num.length()).length();
    }
}