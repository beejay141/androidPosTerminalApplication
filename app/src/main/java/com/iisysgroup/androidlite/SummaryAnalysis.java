package com.iisysgroup.androidlite;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.iisysgroup.androidlite.transaction_viewpager_fragments.TransactionSummary;
import com.iisysgroup.poslib.host.Host;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SummaryAnalysis extends AppCompatActivity implements OnChartValueSelectedListener {
    PieChart pieChart;
    BarChart barChart;

    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_analysis);

        Intent intent = getIntent();
        if (intent.getSerializableExtra(Intent.EXTRA_COMPONENT_NAME) != null){
            HashMap<Host.TransactionType, TransactionSummary.TransactionsStore> map = (HashMap<Host.TransactionType, TransactionSummary.TransactionsStore>)intent.getSerializableExtra(Intent.EXTRA_COMPONENT_NAME);

            Log.d("Values", map.toString());
            if (map == null){
                Toast.makeText(this, "Results is null", Toast.LENGTH_SHORT).show();
            }
            else
            {
                setUpTransactionsData(map);
            }
    } else {
            Toast.makeText(this, "Entry point incorrect", Toast.LENGTH_LONG).show();
        }
    }


    private void setUpTransactionsData(HashMap<Host.TransactionType, TransactionSummary.TransactionsStore> map) {
        TransactionSummary.TransactionsStore cashAdvance = map.get(Host.TransactionType.PURCHASE_WITH_CASH_ADVANCE);
        TransactionSummary.TransactionsStore cashBack = map.get(Host.TransactionType.PURCHASE_WITH_CASH_BACK);
        TransactionSummary.TransactionsStore purchase = map.get(Host.TransactionType.PURCHASE);
        TransactionSummary.TransactionsStore fundsTransfer = map.get(Host.TransactionType.FUND_TRANSFER);

        setUpPieChart(cashAdvance, cashBack, purchase, fundsTransfer);
        setUpBarChart(cashAdvance, cashBack, purchase, fundsTransfer);
    }

    private void setUpPieChart(TransactionSummary.TransactionsStore cashAdvance, TransactionSummary.TransactionsStore cashBack, TransactionSummary.TransactionsStore purchase, TransactionSummary.TransactionsStore fundsTransfer) {
        pieChart = findViewById(R.id.pieChart);

        pieChart.setUsePercentValues(true);

        // entry label styling
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);


        // add a selection listener
        pieChart.setOnChartValueSelectedListener(this);
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(purchase.getAmount(), "Purchase" ));
        entries.add(new PieEntry(cashAdvance.getAmount(), "Cash Advance"));
        entries.add(new PieEntry(cashBack.getAmount(), "Cash Back"));
        entries.add(new PieEntry(fundsTransfer.getAmount(), "Funds Transfer"));

        PieDataSet set = new PieDataSet(entries, "Transactions Amount summary");

        set.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(set);

        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void setUpBarChart(TransactionSummary.TransactionsStore cashAdvance, TransactionSummary.TransactionsStore cashBack, TransactionSummary.TransactionsStore purchase, TransactionSummary.TransactionsStore fundsTransfer) {
        barChart = findViewById(R.id.barChart);
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

            }

            @Override
            public void onNothingSelected() {

            }
        });

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1f, purchase.getAmount(), "Purchase"));
        entries.add(new BarEntry(2f, cashAdvance.getAmount(), "Cash Advance"));
        entries.add(new BarEntry(3f, cashBack.getAmount(), "Cash back"));
        entries.add(new BarEntry(4f, fundsTransfer.getAmount(), "Funds Transfer"));

        BarDataSet set = new BarDataSet(entries, "Transactions Amount Summary");
        set.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData data = new BarData(set);

        barChart.setData(data);
        barChart.invalidate();
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
