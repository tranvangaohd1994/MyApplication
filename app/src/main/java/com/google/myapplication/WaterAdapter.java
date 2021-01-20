package com.google.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WaterAdapter extends RecyclerView.Adapter<WaterAdapter.ViewHolder> {

    private ArrayList<WaterCard> arrWaterCard;
    private int type;

    public WaterAdapter(ArrayList<WaterCard> data) {
        this.arrWaterCard = data;
        this.type = type;
    }

    @NonNull
    @Override
    public WaterAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_water_row, parent, false);
        return new WaterAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaterAdapter.ViewHolder holder, int position) {
        holder.tvWaterTimeCard.setText(arrWaterCard.get(position).getWaterTime());
        int temperature = arrWaterCard.get(position).getWaterTemperature();
        holder.tvTemperature.setText(temperature + "\u00B0C");
        if (temperature < 30) {
            holder.imgTemperature.setImageResource(R.drawable.cool_temperature_icon);
        } else {
            holder.imgTemperature.setImageResource(R.drawable.hot_temperature_icon);
        }
    }

    @Override
    public int getItemCount() {
        return arrWaterCard.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvWaterTimeCard;
        private ImageView imgWaterCard;
        private TextView tvTemperature;
        private ImageView imgTemperature;

        public ViewHolder(View itemView) {
            super(itemView);
            tvWaterTimeCard = itemView.findViewById(R.id.tv_water_time);
            imgWaterCard = itemView.findViewById(R.id.img_water_item);
            tvTemperature = itemView.findViewById(R.id.tv_temperature);
            imgTemperature = itemView.findViewById(R.id.img_temperature);
        }
    }
}
