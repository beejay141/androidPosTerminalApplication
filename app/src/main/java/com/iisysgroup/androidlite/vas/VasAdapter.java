package com.iisysgroup.androidlite.vas;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iisysgroup.androidlite.R;

import java.util.ArrayList;

/**
 * Created by Agbede on 3/20/2018.
 */

public class VasAdapter extends RecyclerView.Adapter<VasAdapter.VasViewHolder> {
   public interface VasClickListener {
        void onVasItemClick(ArrayList<VasItems> vasItemsArrayList, int position);
    }

    VasClickListener listener;

    ArrayList<VasItems> vasItemsArrayList;
    Context context;
    public VasAdapter(ArrayList<VasItems> vasItemsArrayList, Context context, VasClickListener listener){
        this.vasItemsArrayList = vasItemsArrayList;
        this.context = context;
        this.listener = listener;
    }
    @NonNull
    @Override
    public VasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.individual_vas_item, parent, false);
        return new VasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VasViewHolder holder, int position) {
        holder.vasTitle.setText(vasItemsArrayList.get(position).title);

        Drawable drawable = context.getDrawable(context.getResources().getIdentifier(vasItemsArrayList.get(position).getImageDrawable(),"drawable","com.iisysgroup.androidlite"));

        Glide.with(context).load(drawable).into(holder.vasImage);
    }


    @Override
    public int getItemCount() {
        return vasItemsArrayList.size();
    }

    class VasViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView vasTitle;
        ImageView vasImage;

        public VasViewHolder(View itemView) {
            super(itemView);
            vasTitle = itemView.findViewById(R.id.vasTitle);
            vasImage = itemView.findViewById(R.id.vasImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onVasItemClick(vasItemsArrayList, getAdapterPosition());
        }
    }
}
