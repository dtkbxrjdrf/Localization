package com.example.localization;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // Default quantity is 1.
    private int mInputQuantity = 1;

    // Get the number format for the user-selected locale.
    private NumberFormat mNumberFormat = NumberFormat.getInstance();

    // Add a TAG for reporting an exception with the entered quantity.
    private static final String TAG = MainActivity.class.getSimpleName();

    // Fixed price in U.S. dollars and cents: ten cents.
    private double mPrice = 0.10;

    // Approximate exchange rates for France (FR) and Israel (IW).
    private double mFrExchangeRate = 0.93; // 0.93 euros = $1.
    private double mIwExchangeRate = 3.61; // 3.61 new shekels = $1.

    // Get the currency format for the user-selected locale.
    private NumberFormat mCurrencyFormat = NumberFormat.getCurrencyInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHelp();
            }
        });

        // Get the current date.
        final Date myDate = new Date();
        // Add 5 days in milliseconds to create the expiration date.
        final long expirationDate = myDate.getTime() + TimeUnit.DAYS.toMillis(5);
        // Set the expiration date as the date to display.
        myDate.setTime(expirationDate);

        // Format the date for the locale.
        String myFormattedDate = DateFormat.getDateInstance().format(myDate);
        // Display the formatted date.
        TextView expirationDateView = (TextView) findViewById(R.id.date);
        expirationDateView.setText(myFormattedDate);

        // Set up the price and currency format.
        String myFormattedPrice;
        // Get the country code for the user-selected locale.
        String deviceLocale = Locale.getDefault().getCountry();
        // If country code is France or Israel, calculate price
        // with exchange rate and change to the country's currency format.
        if (deviceLocale.equals("FR") || deviceLocale.equals("IL")) {
            if (deviceLocale.equals("FR")) {
                // Calculate mPrice in euros.
                mPrice *= mFrExchangeRate;
            } else {
                // Calculate mPrice in new shekels.
                mPrice *= mIwExchangeRate;
            }
            // Use the user-chosen locale's currency format, which
            // is either France or Israel.
            myFormattedPrice = mCurrencyFormat.format(mPrice);
        } else {
            // mPrice is the same (based on U.S. dollar).
            // Use the currency format for the U.S.
            mCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            myFormattedPrice = mCurrencyFormat.format(mPrice);
        }

        // Show the price string.
        TextView localePrice = (TextView) findViewById(R.id.price);
        localePrice.setText(myFormattedPrice);

        // Get the EditText view for the entered quantity.
        final EditText enteredQuantity = (EditText) findViewById(R.id.quantity);

        // Add an OnEditorActionListener to the EditText view.
        enteredQuantity.setOnEditorActionListener
                (new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            // Close the keyboard.
                            InputMethodManager imm = (InputMethodManager)
                                    v.getContext().getSystemService
                                            (Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                            // Parse string in view v to a number.
                            try {
                                // Use the number format for the locale.
                                mInputQuantity = mNumberFormat.parse(v.getText()
                                        .toString()).intValue();
                                v.setError(null);
                            } catch (ParseException e) {
                                Log.e(TAG,Log.getStackTraceString(e));
                                v.setError(getText(R.string.enter_number));
                                return false;
                            }

                            // Convert to string using locale's number format.
                            String myFormattedQuantity =
                                    mNumberFormat.format(mInputQuantity);
                            // Show the locale-formatted quantity.
                            v.setText(myFormattedQuantity);

                            EditText editQuantity = findViewById(R.id.quantity);
                            String quantityString = editQuantity.getText().toString();
                            int quantity = Integer.parseInt(quantityString);

                            TextView localePrice = findViewById(R.id.price);
                            String priceString = localePrice.getText().toString().replaceAll("[^\\d.,]", "");
                            double price;
                            Locale currentLocale = Locale.getDefault();
                            if (currentLocale.equals(Locale.FRANCE) || currentLocale.equals(Locale.GERMANY)) {
                                price = Double.parseDouble(priceString.replace(',', '.'));
                            } else if (currentLocale.equals(new Locale("he", "IL"))) {
                                price = Double.parseDouble(priceString);
                            } else {
                                price = Double.parseDouble(priceString.replace(',', '.'));
                            }

                            double amount = price * quantity;

                            String formatAmount;
                            if (currentLocale.equals(Locale.FRANCE) || currentLocale.equals(Locale.GERMANY)) {
                                formatAmount = NumberFormat.getCurrencyInstance(currentLocale).format(amount);
                            } else if (currentLocale.equals(new Locale("he", "IL"))) {
                                formatAmount = "₪" + new DecimalFormat("#,##0.00").format(amount);
                            } else {
                                formatAmount = NumberFormat.getCurrencyInstance(Locale.US).format(amount);
                            }

                            TextView totalAmount = findViewById(R.id.total);
                            totalAmount.setText(formatAmount);

                            return true;
                        }
                        return false;
                    }
                });
    }

    /**
     * Shows the Help screen.
     */
    private void showHelp() {
        // Create the intent.
        Intent helpIntent = new Intent(this, Help.class);
        // Start the HelpActivity.
        startActivity(helpIntent);
    }

    /**
     * Clears the quantity when resuming the app after language is changed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        ((EditText) findViewById(R.id.quantity)).getText().clear();
    }

    /**
     * Creates the options menu and returns true.
     *
     * @param menu       Options menu
     * @return boolean   True after creating options menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles options menu item clicks.
     *
     * @param item      Menu item
     * @return boolean  True if menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle options menu item clicks here.
        switch (item.getItemId()) {
            case R.id.action_help:
                showHelp();
                return true;
            case R.id.action_language:
                Intent languageIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivity(languageIntent);
                return true;
            default:
                // Do nothing
        }
        return super.onOptionsItemSelected(item);
    }
}