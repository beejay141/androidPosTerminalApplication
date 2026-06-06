package com.iisysgroup.androidlite;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.iisysgroup.androidlite.models.QrItemScanModel;
import com.iisysgroup.androidlite.payments_menu.BasePaymentActivity;
import com.iisysgroup.androidlite.payments_menu.PaymentsActivity;
import com.iisysgroup.androidlite.payments_menu.TransactionProcessActivity;
import com.iisysgroup.poslib.host.Host;
import com.iisysgroup.poslib.utils.AccountType;

import java.util.ArrayList;
import java.util.Locale;

public class QRScannerActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener, View.OnClickListener{
    private QRCodeReaderView qrCodeReaderView;
    private MyAdapter adapter;
    private Button mSubmit, mPurchase;

    private TextView total;
    private RadioGroup radioGroup;

    private LinearLayout mQrCodeMode;
    private ConstraintLayout mAccountSelect;

    private ItemTouchHelper itemTouchHelper;

    private static int numberOfTimesScanned = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        radioGroup = findViewById(R.id.radio_payments_account_group);
        mQrCodeMode = findViewById(R.id.qrCodeView);
        mAccountSelect = findViewById(R.id.account_select);
        mSubmit = findViewById(R.id.button_qr_checkout);
        mPurchase = findViewById(R.id.submit_button);
        mPurchase.setOnClickListener(this);
        mSubmit.setOnClickListener(this);

        qrCodeReaderView = findViewById(R.id.qr_code);
        total = findViewById(R.id.qr_total_amount);

        RecyclerView mRecyclerView = findViewById(R.id.recyclerview_qr_items);
        adapter = new MyAdapter();

        total.setText(String.format(Locale.UK, "%d", adapter.getTotalPrice()));
        mRecyclerView.setAdapter(adapter);



        qrCodeReaderView.setBackCamera();


        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.removeElement(viewHolder.getAdapterPosition());
                total.setText(String.format(Locale.UK, "%d", adapter.getTotalPrice()));
            }
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void beep(){
        MediaPlayer m = new MediaPlayer();
        try {
            if (m.isPlaying()) {
                m.stop();
                m.release();
                m = new MediaPlayer();
            }

            AssetFileDescriptor descriptor = getAssets().openFd("beep.mp3");
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.prepare();
            m.setVolume(0.3f, 0.3f);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReaderView.setQRDecodingEnabled(true);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.setQRDecodingEnabled(false);
        qrCodeReaderView.setOnQRCodeReadListener(null);
        qrCodeReaderView.stopCamera();
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        //simulate getting values from real scan
        if (numberOfTimesScanned == 0){
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            numberOfTimesScanned++;
            QrItemScanModel model = new QrItemScanModel("Ankara", 200);
            adapter.setData(model);
            beep();
            total.setText(String.format(Locale.UK, "%d", adapter.getTotalPrice()));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_qr_checkout:
                mQrCodeMode.setVisibility(View.GONE);
                mAccountSelect.setVisibility(View.VISIBLE);
                break;
            case R.id.submit_button:
                int selectedId = radioGroup.getCheckedRadioButtonId();
                AccountType radio_selected = null;
                switch (selectedId)
                {
                    case R.id.radio_savings: radio_selected = AccountType.SAVINGS;
                        break;
                    case R.id.radio_current: radio_selected = AccountType.CURRENT;
                        break;
                    case R.id.radio_default: radio_selected = AccountType.DEFAULT_UNSPECIFIED;
                        break;
                    case R.id.radio_credit: radio_selected = AccountType.CREDIT;
                        break;
                }
                if (radio_selected == null){
                    Toast.makeText(QRScannerActivity.this, "Select an account Type", Toast.LENGTH_LONG).show();
                    return;
                }

                long amount = Long.parseLong(total.getText().toString());

                Intent intent = new Intent(QRScannerActivity.this, TransactionProcessActivity.class);
                intent.putExtra(BasePaymentActivity.TRANSACTION_ACCOUNT_TYPE, radio_selected);
                intent.putExtra(BasePaymentActivity.TRANSACTION_TYPE, Host.TransactionType.PURCHASE);
                intent.putExtra(BasePaymentActivity.TRANSACTION_AMOUNT, amount);
                intent.putExtra(BasePaymentActivity.TRANSACTION_ADDITIONAL_AMOUNT, 0);
                startActivity(intent);
                finish();
                break;
        }

    }

    public class MyAdapter extends RecyclerView.Adapter<QRViewHolder> {
        ArrayList<QrItemScanModel> models = new ArrayList<>();
        public MyAdapter(){
            models = new ArrayList<>();
        }
        @NonNull
        @Override
        public QRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.qr_recyclerview_itemlist, parent, false);
            return new QRViewHolder(view);
        }

        public long getTotalPrice(){
            if (models.isEmpty())
                return 0;

            long total = 0;

            for (QrItemScanModel itemScanModel : models){
                total += itemScanModel.getItemPrice();
            }
            return total;
        }

        public void removeElement(int position){
            models.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull QRViewHolder holder, int position) {
            QrItemScanModel itemScanModel = models.get(position);
            holder.itemName.setText(itemScanModel.getItemName());
            holder.itemPrice.setText(String.format(Locale.UK,"%d", itemScanModel.getItemPrice() ));
        }

        @Override
        public int getItemCount() {
            return models == null ? 0 : models.size();
        }

        public void setData(QrItemScanModel itemModel){
            models.add(itemModel);
            notifyDataSetChanged();
        }
    }

    class QRViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView itemPrice;
        public QRViewHolder(View itemView) {
            super(itemView);
            itemName =  itemView.findViewById(R.id.qrItemName);
            itemPrice = itemView.findViewById(R.id.qrItemPrice);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (mAccountSelect.getVisibility() == View.GONE && mQrCodeMode.getVisibility() == View.VISIBLE){
            Intent intent = new Intent(QRScannerActivity.this, PaymentsActivity.class);
            startActivity(intent);
            finish();
        }
        else if (mAccountSelect.getVisibility() == View.VISIBLE){
            mAccountSelect.setVisibility(View.GONE);
            mQrCodeMode.setVisibility(View.VISIBLE);
        }
    }
}
