package com.iisysgroup.androidlite.vas.activity.Genesis.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iisysgroup.androidlite.R;
import com.iisysgroup.androidlite.vas.activity.Genesis.GenesisMovieListActivity;
import com.itex.richard.payviceconnect.model.Genesis;

import java.util.List;

public class MoviesHouseAdapters extends RecyclerView.Adapter<MoviesHouseAdapters.MyViewHolder> {
    List<Genesis.CimaHouse> mData;
    Context context;
    LayoutInflater layoutInflater;

    public  MoviesHouseAdapters(List<Genesis.CimaHouse> cimaHouses, Context context){
        this.context = context;
        this.mData = cimaHouses;
        layoutInflater = LayoutInflater.from(context); //For inflating the cardview
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.cinema_house_view, parent, false);
        MoviesHouseAdapters.MyViewHolder holder = new MoviesHouseAdapters.MyViewHolder(view);
        return holder;
}

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Genesis.CimaHouse cinemaHouse = mData.get(position);
        holder.setData(cinemaHouse);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            cardView = itemView.findViewById(R.id.movie);
        }

        public void setData(final Genesis.CimaHouse cinemaHouse) {
            title.setText(cinemaHouse.getName());
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMovieListActivity(cinemaHouse);

                }
            });
        }
        private void openMovieListActivity(Genesis.CimaHouse cimaHouse){
            Intent intent = new Intent(context, GenesisMovieListActivity.class);
            intent.putExtra("cima", cimaHouse.getCinema_id());
            context.startActivity(intent);
        }

    }
}
