package com.techvipul.personalfinancemanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TransactionListActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvTransactions;
    private LinearLayout emptyState;
    private TransactionAdapter adapter;
    private SharedPreferences prefs;
    private List<Transaction> allTransactions;
    private List<Transaction> filteredTransactions;
    private SearchView searchView;
    private Spinner spSort;
    private ImageButton btnFilter;
    private static final String PREFS_NAME = "FinancePrefs";
    private static final String PREF_CURRENCY = "currency";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        rvTransactions = findViewById(R.id.rv_transactions);
        emptyState = findViewById(R.id.empty_state);
        searchView = findViewById(R.id.search_view);
        spSort = findViewById(R.id.sp_sort);
        btnFilter = findViewById(R.id.btn_filter);

        allTransactions = dbHelper.getAllTransactions();
        filteredTransactions = new ArrayList<>(allTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(filteredTransactions);
        rvTransactions.setAdapter(adapter);

        // Setup SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTransactions(newText);
                return true;
            }
        });

        // Setup Sort Spinner
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(sortAdapter);
        spSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                sortTransactions(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Setup Filter Button
        btnFilter.setOnClickListener(v -> showFilterDialog());

        updateEmptyState();
    }

    private void filterTransactions(String query) {
        filteredTransactions.clear();
        if (query.isEmpty()) {
            filteredTransactions.addAll(allTransactions);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (Transaction transaction : allTransactions) {
                if (transaction.getNote() != null && transaction.getNote().toLowerCase(Locale.getDefault()).contains(lowerQuery) ||
                        transaction.getCategory().toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                    filteredTransactions.add(transaction);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void sortTransactions(int sortOption) {
        Collections.sort(filteredTransactions, (t1, t2) -> {
            switch (sortOption) {
                case 0: // Date: Newest First
                    return t2.getDate().compareTo(t1.getDate());
                case 1: // Date: Oldest First
                    return t1.getDate().compareTo(t2.getDate());
                case 2: // Amount: High to Low
                    return Double.compare(t2.getAmount(), t1.getAmount());
                case 3: // Amount: Low to High
                    return Double.compare(t1.getAmount(), t2.getAmount());
                default:
                    return 0;
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        Spinner spFilterType = dialogView.findViewById(R.id.sp_filter_type);
        Spinner spFilterCategory = dialogView.findViewById(R.id.sp_filter_category);
        EditText etStartDate = dialogView.findViewById(R.id.et_start_date);
        EditText etEndDate = dialogView.findViewById(R.id.et_end_date);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnApply = dialogView.findViewById(R.id.btn_apply);

        // Setup Spinners
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterCategory.setAdapter(categoryAdapter);

        // Setup Date Pickers
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            String selectedType = spFilterType.getSelectedItem().toString();
            String selectedCategory = spFilterCategory.getSelectedItem().toString();
            String startDate = etStartDate.getText().toString();
            String endDate = etEndDate.getText().toString();

            filteredTransactions.clear();
            for (Transaction transaction : allTransactions) {
                boolean matchesType = selectedType.equals("All Types") || transaction.getType().equals(selectedType);
                boolean matchesCategory = selectedCategory.equals("All Categories") || transaction.getCategory().equals(selectedCategory);
                boolean matchesDate = true;
                if (!startDate.isEmpty() && !endDate.isEmpty()) {
                    try {
                        long transDate = sdf.parse(transaction.getDate()).getTime();
                        long start = sdf.parse(startDate).getTime();
                        long end = sdf.parse(endDate).getTime();
                        matchesDate = transDate >= start && transDate <= end;
                    } catch (ParseException e) {
                        matchesDate = false;
                    }
                }
                if (matchesType && matchesCategory && matchesDate) {
                    filteredTransactions.add(transaction);
                }
            }
            adapter.notifyDataSetChanged();
            updateEmptyState();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    editText.setText(sdf.format(selectedDate.getTime()));
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void updateEmptyState() {
        if (filteredTransactions.isEmpty()) {
            rvTransactions.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvTransactions.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private final List<Transaction> transactions;

        public TransactionAdapter(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Transaction transaction = transactions.get(position);

            // Get currency from SharedPreferences (default to INR)
            String currency = prefs.getString(PREF_CURRENCY, "INR");
            String currencySymbol = currency.equals("INR") ? "₹" : currency.equals("USD") ? "$" : currency.equals("EUR") ? "€" : "£";

            // Bind data to TextViews
            holder.tvAmount.setText(String.format("%s%.2f", currencySymbol, transaction.getAmount()));
            holder.tvType.setText(String.format("Type: %s", transaction.getType()));
            holder.tvCategory.setText(String.format("Category: %s", transaction.getCategory()));
            holder.tvDate.setText(String.format("Date: %s", transaction.getDate()));
            holder.tvNote.setText(String.format("Note: %s", transaction.getNote() != null ? transaction.getNote() : ""));

            // Setup delete button
            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(TransactionListActivity.this)
                        .setTitle("Delete Transaction")
                        .setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dbHelper.deleteTransaction(transaction.getId());
                            transactions.remove(position);
                            allTransactions.remove(transaction);
                            notifyItemRemoved(position);
                            updateEmptyState();
                            Toast.makeText(TransactionListActivity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAmount, tvType, tvCategory, tvDate, tvNote;
            ImageButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvAmount = itemView.findViewById(R.id.tv_amount);
                tvType = itemView.findViewById(R.id.tv_type);
                tvCategory = itemView.findViewById(R.id.tv_category);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvNote = itemView.findViewById(R.id.tv_note);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }
}