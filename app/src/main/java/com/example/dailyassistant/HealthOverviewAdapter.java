package com.example.dailyassistant;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HealthOverviewAdapter
        extends RecyclerView.Adapter<HealthOverviewAdapter.ViewHolder> {

    private final Context context;
    private List<HealthOverviewItem> items;

    public HealthOverviewAdapter(
            Context context,
            List<HealthOverviewItem> items
    ) {
        this.context = context;
        this.items = items;
    }

    public void updateData(
            List<HealthOverviewItem> newItems
    ) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_health_overview,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        HealthOverviewItem item = items.get(position);

        holder.txtHealthTitle.setText(item.getTitle());
        holder.txtHealthValue.setText(item.getValue());
        holder.txtHealthUnit.setText(item.getUnit());
        holder.progressHealth.setProgress(item.getProgress());

        if (item.getType() == HealthOverviewItem.TYPE_WATER) {
            holder.imgSmallIcon.setImageResource(R.drawable.ic_water_drop);
            holder.imgBigMascot.setImageResource(R.drawable.ic_water_glass);
            holder.progressHealth.setProgressDrawable(
                    getDrawableCompat(R.drawable.progress_water)
            );
            holder.layoutIconContainer.setBackgroundResource(R.drawable.bg_water_icon_circle);
        } else if (item.getType() == HealthOverviewItem.TYPE_SLEEP) {
            holder.imgSmallIcon.setImageResource(R.drawable.ic_moon);
            holder.imgBigMascot.setImageResource(R.drawable.ic_sleep_mascot);
            holder.progressHealth.setProgressDrawable(
                    getDrawableCompat(R.drawable.progress_sleep)
            );
            holder.layoutIconContainer.setBackgroundResource(R.drawable.bg_sleep_icon_circle);
        } else {
            holder.layoutIconContainer.setBackgroundResource(R.drawable.bg_circle_icon);
        }
    }

    private Drawable getDrawableCompat(int drawableResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(drawableResId);
        }
        return ContextCompat.getDrawable(context, drawableResId);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgSmallIcon;
        ImageView imgBigMascot;
        TextView txtHealthTitle;
        TextView txtHealthValue;
        TextView txtHealthUnit;
        ProgressBar progressHealth;
        View layoutIconContainer;

        public ViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            layoutIconContainer = itemView.findViewById(R.id.layoutIconContainer);
            imgSmallIcon = itemView.findViewById(R.id.imgSmallIcon);
            imgBigMascot = itemView.findViewById(R.id.imgBigMascot);
            txtHealthTitle = itemView.findViewById(R.id.txtHealthTitle);
            txtHealthValue = itemView.findViewById(R.id.txtHealthValue);
            txtHealthUnit = itemView.findViewById(R.id.txtHealthUnit);
            progressHealth = itemView.findViewById(R.id.progressHealth);
        }
    }
}
