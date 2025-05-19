package com.techvipul.personalfinancemanager;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        dbHelper = new DatabaseHelper(this);
        pieChart = findViewById(R.id.pie_chart);

        setupPieChart();
    }

    private void setupPieChart() {
        double totalIncome = dbHelper.getTotalIncome();
        double totalExpense = dbHelper.getTotalExpense();

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) totalIncome, "Income"));
        entries.add(new PieEntry((float) totalExpense, "Expense"));

        PieDataSet dataSet = new PieDataSet(entries, "Summary");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}