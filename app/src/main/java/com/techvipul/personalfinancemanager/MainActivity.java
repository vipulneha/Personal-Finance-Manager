package com.techvipul.personalfinancemanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.cardview.widget.CardView;


public class MainActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private TextView tvIncome, tvExpense, tvBalance;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "FinancePrefs";
    private static final String PREF_CURRENCY = "currency";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Bind UI elements
        tvIncome = findViewById(R.id.tv_income);
        tvExpense = findViewById(R.id.tv_expense);
        tvBalance = findViewById(R.id.tv_balance);
        CardView cardAddTransaction = findViewById(R.id.card_add_transaction);
        CardView cardViewReport = findViewById(R.id.card_view_report);
        CardView cardSettings = findViewById(R.id.card_settings);
        CardView cardViewTransactions = findViewById(R.id.card_view_transactions);

        // Update summary
        updateSummary();

        // Set up navigation
        cardAddTransaction.setOnClickListener(v -> startActivity(new Intent(this, AddTransactionActivity.class)));
        cardViewReport.setOnClickListener(v -> startActivity(new Intent(this, ReportActivity.class)));
        cardSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        cardViewTransactions.setOnClickListener(v -> startActivity(new Intent(this, TransactionListActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSummary(); // Refresh data when returning to activity
    }

    private void updateSummary() {
        double totalIncome = dbHelper.getTotalIncome();
        double totalExpense = dbHelper.getTotalExpense();
        double totalBalance = totalIncome - totalExpense;

        // Get currency from SharedPreferences (default to INR)
        String currency = prefs.getString(PREF_CURRENCY, "INR");
        String currencySymbol = currency.equals("INR") ? "₹" : currency.equals("USD") ? "$" : currency.equals("EUR") ? "€" : "£";

        tvIncome.setText(String.format("Income: %s%.2f", currencySymbol, totalIncome));
        tvExpense.setText(String.format("Expense: %s%.2f", currencySymbol, totalExpense));
        tvBalance.setText(String.format("%s%.2f", currencySymbol, totalBalance));
    }
}