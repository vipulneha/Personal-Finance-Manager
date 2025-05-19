package com.techvipul.personalfinancemanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "FinancePrefs";
    private static final String PREF_CURRENCY = "currency";
    private static final String PREF_LANGUAGE = "language";
    private static final String PREF_NOTIFICATIONS = "notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Dark Theme Toggle
        Switch switchTheme = findViewById(R.id.switch_theme);
        switchTheme.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Currency Selection
        Spinner spCurrency = findViewById(R.id.sp_currency);
        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCurrency.setAdapter(currencyAdapter);
        String savedCurrency = prefs.getString(PREF_CURRENCY, "USD");
        spCurrency.setSelection(currencyAdapter.getPosition(savedCurrency));
        spCurrency.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                prefs.edit().putString(PREF_CURRENCY, parent.getItemAtPosition(position).toString()).apply();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Language Selection
        Spinner spLanguage = findViewById(R.id.sp_language);
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLanguage.setAdapter(languageAdapter);
        String savedLanguage = prefs.getString(PREF_LANGUAGE, "English");
        spLanguage.setSelection(languageAdapter.getPosition(savedLanguage));
        spLanguage.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedLanguage = parent.getItemAtPosition(position).toString();
                prefs.edit().putString(PREF_LANGUAGE, selectedLanguage).apply();
                // Note: Actual language change requires locale update and activity recreation
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Notification Toggle
        Switch switchNotifications = findViewById(R.id.switch_notifications);
        switchNotifications.setChecked(prefs.getBoolean(PREF_NOTIFICATIONS, false));
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(PREF_NOTIFICATIONS, isChecked).apply();
            Toast.makeText(SettingsActivity.this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Clear Data
        Button btnClearData = findViewById(R.id.btn_clear_data);
        btnClearData.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Data")
                    .setMessage("Are you sure you want to delete all transactions?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbHelper.clearAllData();
                        Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Clear Cache
        Button btnClearCache = findViewById(R.id.btn_clear_cache);
        btnClearCache.setOnClickListener(v -> {
            try {
                File cacheDir = getCacheDir();
                deleteDir(cacheDir);
                Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to clear cache", Toast.LENGTH_SHORT).show();
            }
        });

        // Backup Data
        Button btnBackupData = findViewById(R.id.btn_backup_data);
        btnBackupData.setOnClickListener(v -> {
            try {
                backupData();
                Toast.makeText(this, "Data backed up to Downloads", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Restore Data
        Button btnRestoreData = findViewById(R.id.btn_restore_data);
        btnRestoreData.setOnClickListener(v -> {
            try {
                restoreData();
                Toast.makeText(this, "Data restored", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Restore failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDir(child);
                }
            }
        }
        dir.delete();
    }

    private void backupData() throws Exception {
        List<Transaction> transactions = dbHelper.getAllTransactions();
        JSONArray jsonArray = new JSONArray();
        for (Transaction t : transactions) {
            JSONObject obj = new JSONObject();
            obj.put("id", t.getId());
            obj.put("amount", t.getAmount());
            obj.put("type", t.getType());
            obj.put("category", t.getCategory());
            obj.put("note", t.getNote());
            obj.put("date", t.getDate());
            jsonArray.put(obj);
        }
        String jsonStr = jsonArray.toString();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "finance_backup.json");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(jsonStr.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void restoreData() throws Exception {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "finance_backup.json");
        if (!file.exists()) {
            throw new Exception("Backup file not found");
        }
        StringBuilder jsonStr = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            int content;
            while ((content = fis.read()) != -1) {
                jsonStr.append((char) content);
            }
        }
        JSONArray jsonArray = new JSONArray(jsonStr.toString());
        dbHelper.clearAllData();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Transaction t = new Transaction();
            t.setId(obj.getInt("id"));
            t.setAmount(obj.getDouble("amount"));
            t.setType(obj.getString("type"));
            t.setCategory(obj.getString("category"));
            t.setNote(obj.getString("note"));
            t.setDate(obj.getString("date"));
            dbHelper.addTransaction(t);
        }
    }
}