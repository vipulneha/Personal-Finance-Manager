package com.techvipul.personalfinancemanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private EditText etAmount, etNote, etDate;
    private Spinner spType, spCategory;
    private Button btnDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbHelper = new DatabaseHelper(this);
        etAmount = findViewById(R.id.et_amount);
        spType = findViewById(R.id.sp_type);
        spCategory = findViewById(R.id.sp_category);
        etNote = findViewById(R.id.et_note);
        etDate = findViewById(R.id.et_date);
        btnDate = findViewById(R.id.btn_date);
        Button btnSave = findViewById(R.id.btn_save);

        // Setup Spinners
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.transaction_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.transaction_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // Setup Date Picker
        btnDate.setOnClickListener(v -> showDatePickerDialog());

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    etDate.setText(sdf.format(selectedDate.getTime()));
                },
                year, month, day);

        // Restrict past dates
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString();
        String type = spType.getSelectedItem().toString();
        String category = spCategory.getSelectedItem().toString();
        String note = etNote.getText().toString();
        String date = etDate.getText().toString();

        if (amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill in amount and date", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setType(type);
            transaction.setCategory(category);
            transaction.setNote(note);
            transaction.setDate(date);

            dbHelper.addTransaction(transaction);
            Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
        }
    }
}