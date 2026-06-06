package com.iisysgroup.androidlite.all_history;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.iisysgroup.androidlite.R;

import java.io.Serializable;
import java.util.List;

public class HistoryAdapter extends  RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder_D> {

    List<Transaction> historyList;
    Context context;


    public HistoryAdapter(Context context, List<Transaction> historyList) {
        this.context = context;
        this.historyList = historyList;
        for (int i=0; i<this.historyList.size(); i++){
            Log.i("product history adapter", new Gson().toJson( historyList.get(i).getProduct()));
        }
    }



    public  History processRecords_D(int position) {

        History history = null;
        history = new History();

        Transaction actualTransaction =  historyList.get(position);

        Log.i("RetrofitTransactionddd", "Size...." + actualTransaction);
        history.transactionType =actualTransaction.getCategory() == ""? "" : actualTransaction.getCategory();
        history.product =actualTransaction.getProduct() == "" ? "" : actualTransaction.getProduct();
        Log.i("product history adapter", new Gson().toJson( history.product));

        history.type =actualTransaction.getType() == "" ? "" : actualTransaction.getType();
        history.transactionReference = actualTransaction.getReference()== null? "" : actualTransaction.getReference();
        history.amount =  actualTransaction.getAmount()== null ? " " :  " " +actualTransaction.getAmount();
        history.commisionEarned =actualTransaction.getCommissionAmount() == null ? "0.00" : actualTransaction.getCommissionAmount();
        history.balanceAfter = actualTransaction.getBalanceAfter() == null ? " " : actualTransaction.getBalanceAfter().toString();
        history.category = actualTransaction.getCategory().trim().isEmpty() ? " " : actualTransaction.getCategory();
        history.transDescription = actualTransaction.getDescription().trim().isEmpty() ? " " : actualTransaction.getDescription();
        history.beneficiary = actualTransaction.getBeneficiary() == null ? ""  : actualTransaction.getBeneficiary();
        history.originalTransRef =actualTransaction.getOriginalTransactionReference() == null ? " " : actualTransaction.getOriginalTransactionReference().toString();
        history.originalTransAmt = actualTransaction.getOriginalTransactionAmount() == null  ? " " : actualTransaction.getOriginalTransactionAmount();
        history.originalTransCat = actualTransaction.getOriginalTransactionCategory() == null ? " " : actualTransaction.getOriginalTransactionCategory().toString();
        history.originalTransType =actualTransaction.getOriginalTransactionType() == null ? " " : actualTransaction.getOriginalTransactionType().toString();
        history.originalTransProduct = actualTransaction.getOriginalTransactionProduct() == null? " " : actualTransaction.getOriginalTransactionProduct().toString();
        history.originalTransDesc =actualTransaction.getOriginalTransactionDescription() == null ? " " : actualTransaction.getOriginalTransactionDescription().toString();
//        history.subAgents =;
//        history.transactionType = actualTransaction.getCategory();transtype now category

        history.date = actualTransaction.getDate();


//        if (cols.length > 8) history.remark = cols[8];



        return history;
    }


    @Override
    public int getItemCount() {
        return historyList.size();
    }

    @Override
    public HistoryViewHolder_D onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.new_history_item_r_d, parent, false);

        return new HistoryViewHolder_D(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder_D holder, int position) {

        History transaction=  processRecords_D(position);
        holder.bind(context, transaction);
    }

    public static class History implements Serializable {

        public String transactionReference, amount,commisionEarned, balanceAfter,category,product,transDescription,beneficiary,originalTransRef;
        public List<SubAgent>subAgents;
        public String wallet_Id,originalTransAmt,originalTransCat,originalTransType,originalTransProduct,originalTransDesc,date,transactionType,type, remark;


        public boolean isPurchase() {
            String tranType = transactionType.toLowerCase().trim();
            return !(tranType.contains("fund") || tranType.contains("transfer") || tranType.contains("promo"));
        }

        public boolean isRecharge() {
            String productCode = product.toLowerCase().trim();

            return (productCode.contains("etisalat") ||
                    productCode.contains("mtn") || productCode.contains("glo") || productCode.contains("airtel"));
        }
    }

    class HistoryViewHolder_D extends RecyclerView.ViewHolder {
        LinearLayout layout_background;
        View repayView, contentView;
        TextView balanceBefore;
        ImageView productLogo;
        TextView tranTypeTitleView, amountView, referenceView,
                beneficiaryView, productView, dateView, history_amount;

        public HistoryViewHolder_D(View itemView) {
            super(itemView);
            contentView = itemView;
            layout_background = (LinearLayout)itemView.findViewById(R.id.details_layout);
//        repayView = itemView.findViewById(R.id.reload_view);
            balanceBefore = itemView.findViewById(R.id.balanceafter_txt_view);
            tranTypeTitleView = itemView.findViewById(R.id.history_tran_type_title);
            amountView = itemView.findViewById(R.id.history_amount_textview);
            history_amount = itemView.findViewById(R.id.history_amount);
            productLogo = itemView.findViewById(R.id.productLogo);
            beneficiaryView = itemView.findViewById(R.id.history_beneficiary_textview);
            //productView = itemView.findViewById(R.id.history_product_textview);
            dateView = itemView.findViewById(R.id.history_date_textview);
            referenceView = itemView.findViewById(R.id.history_reference_textview);
        }
        private void colorView(LinearLayout layout, TextView textView, String type){

        }
        public void bind(final Context context, final HistoryAdapter.History history) {

            tranTypeTitleView.setText(history.product.toUpperCase());
            beneficiaryView.setText(history.beneficiary);
            amountView.setText('\u20A6' + "" + history.amount);
            amountView.setTextColor(context.getResources().getColor(R.color.grey));
            history_amount.setTextColor(context.getResources().getColor(R.color.grey));
            Log.d("amount trans", "bind: "+ history.amount);
            String [] date = history.date.split(" ");
            if (date!=null){
                dateView.setText(date[0]);
            }
            history_amount.setTextColor(ContextCompat.getColor(context, R.color.red));
            referenceView.setText(history.transactionReference);
            String [] services = {"mtn", "glo", "airtel", "gotv", "dstv", "starttimes", "9mobile","smile", "ikeja", "eko", "lcc", "waec", "movies","ibadan","phed","enugu","abuja"};
            int [] servicesLogo = {R.drawable.mtn_logo, R.drawable.glo_logo, R.drawable.airtel_logo,
                    R.drawable.gotv_logo, R.drawable.dstv_logo, R.drawable.startimes_logo,
                    R.drawable.ninemobile_logo, R.drawable.smile_logo, R.drawable.ikeja_electric_logo,
                    R.drawable.eko_electric_logo, R.drawable.lcc, R.drawable.waec_logo, R.drawable.ic_genesis,R.drawable.ibedc,
                    R.drawable.port_harcout_electric_logo,R.drawable.enugu_electric_logo,R.drawable.abuja_electric_logo};        //productView.setText(history.product);

            productLogo.setImageResource(R.drawable.ic_account_balance_black_24dp);
            for (int k = 0; k < services.length; k++){
                if (history.product.toLowerCase().contains(services[k])){
                    Log.d("printlogo", "bind: "+ "logo"+ history.product);
                    productLogo.setImageResource(servicesLogo[k]);
                }
            }

            history_amount.setText(history.amount);
            balanceBefore.setText('\u20A6' + " " + history.balanceAfter);
            if (history.type.contains("Debit")) {
                amountView.setTextColor(context.getResources().getColor(R.color.red));
            }
            else{
                amountView.setTextColor(context.getResources().getColor(R.color.colorGreen));
            }
//        productView.setText(history.transactionType);

//        if (history.isPurchase()) {
//            //Changed from INVISIBLE to GONE
//            repayView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    Intent intent = new Intent(context, RepeatTransactionActivity.class);
//                    intent.putExtra(RepeatTransactionActivity.EXTRA_HISTORY, history);
//                    context.startActivity(intent);
//                }
//            });
//        } else {
//            repayView.setVisibility(View.INVISIBLE);
//        }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, AllHistoryDetailActivity.class);
                    intent.putExtra(AllHistoryDetailActivity.EXTRA_HISTORY, history);
                    context.startActivity(intent);
                }
            });

        }
    }

}
