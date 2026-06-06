package com.iisysgroup.androidlite.all_history;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.iisysgroup.androidlite.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AllHistoryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_HISTORY = "histories";


    //    @BindView(R.id.history_tran_type_title)
//    TextView historyTranTypeTitle;
    @BindView(R.id.history_amount_textview)
    TextView historyAmountTextview;
    @BindView(R.id.history_date_textview)
    TextView historyDateTextview;
    @BindView(R.id.history_product_textview)
    TextView historyProductTextview;
    @BindView(R.id.history_recipient_textview)
    TextView historyRecipientTextview;
    @BindView(R.id.history_tran_reference_text)
    TextView historyTranReferenceText;


    @BindView(R.id.history_hamount_textview)
    TextView historyhamountTextview;

    @BindView(R.id.history_commission_textview)
    TextView historyCommissionTextview;


    @BindView(R.id.history_balance_textview)
    TextView historyBalanceTextview;

    @BindView(R.id.history_category_textview)
    TextView historyCategoryTextview;

    @BindView(R.id.history_description_textview)
    TextView historyDescreptionTextview;

    @BindView(R.id.history_orig_trans_ref_textview)
    TextView historyTransRefTextview;


    @BindView(R.id.history_orig_trans_amt_textview)
    TextView historyTransAmountTextview;

    @BindView(R.id.history_orig_trans_cat_textview)
    TextView historyTransCatTextview;

    @BindView(R.id.history_orig_trans_type_textview)
    TextView historyTransTypeTextview;

    @BindView(R.id.history_orig_trans_product_textview)
    TextView historyTransProductTextview;

    @BindView(R.id.history_orig_trans_desc_textview)
    TextView historyTransDescTextview;


//    @BindView(R.id.fab)
//    FloatingActionButton repeatTranFab;

    HistoryAdapter.History history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans_history);

        if (getIntent().hasExtra(EXTRA_HISTORY)) {
            history = (HistoryAdapter.History) getIntent().getSerializableExtra(EXTRA_HISTORY);
        } else {
            finish();
            return;
        }
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


//        if (history.isPurchase()) {
//            historyTranTypeTitle.setText("Purchase");
//        } else {
//            historyTranTypeTitle.setText(history.product);
//        }

        historyAmountTextview.setText('\u20A6' + " " + history.amount);
        Log.i("Details", "onCreate: " + history.amount);

        String[] date = history.date.split(" ");
        historyDateTextview.setText(date[0]);
        Log.i("Date", "processRecords_D: " + date[0]);

        historyProductTextview.setText(history.product);
        historyRecipientTextview.setText(history.beneficiary.trim().isEmpty() ? "None" : history.beneficiary);
        historyTranReferenceText.setText(history.transactionReference);
        historyCommissionTextview.setText('\u20A6' + " " + history.commisionEarned);
        Log.i("Details", "onCreate: " + history.commisionEarned);

        historyhamountTextview.setText('\u20A6' + " " + history.amount);

        historyBalanceTextview.setText('\u20A6' + " " + history.balanceAfter);
        Log.i("Details", "onCreate: " + history.balanceAfter);

        historyCategoryTextview.setText(history.category);

        historyDescreptionTextview.setText(history.transDescription);
        historyTransRefTextview.setText(history.originalTransRef);
        historyTransAmountTextview.setText('\u20A6' + " " + history.originalTransAmt);
        historyTransCatTextview.setText(history.originalTransCat);

        historyTransTypeTextview.setText(history.originalTransType);

        historyTransProductTextview.setText(history.originalTransProduct);

        historyTransDescTextview.setText(history.originalTransDesc);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
