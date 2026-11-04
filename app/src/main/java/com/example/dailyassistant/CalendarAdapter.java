package com.example.dailyassistant;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(CalendarDayItem day);
    }

    private final List<CalendarDayItem> days;
    private final OnDayClickListener listener;

    private int selectedPosition = RecyclerView.NO_POSITION;
    private String selectedDate = "";

    public CalendarAdapter(List<CalendarDayItem> days, OnDayClickListener listener) {
        this.days = days != null ? days : new ArrayList<>();
        this.listener = listener;
    }

    public void update(List<CalendarDayItem> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    public void setSelected(String date) {
        selectedDate = date != null ? date.trim() : "";

        int oldPosition = selectedPosition;
        selectedPosition = findPositionByDate(selectedDate);

        if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition);
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }
    }

    private int findPositionByDate(String date) {
        if (date == null || date.isEmpty()) {
            return RecyclerView.NO_POSITION;
        }

        for (int i = 0; i < days.size(); i++) {
            CalendarDayItem item = days.get(i);
            if (item == null) continue;

            String fullDate = item.getFullDate();
            String dateValue = item.getDate();

            if (date.equals(fullDate) || date.equals(dateValue)) {
                return i;
            }
        }

        return RecyclerView.NO_POSITION;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDayItem day = days.get(position);

        holder.tvDayName.setText(day.getDayName());
        holder.tvDayNumber.setText(day.getDayNumber());

        holder.itemView.setAlpha(day.isInCurrentMonth() ? 1f : 0.35f);

        boolean isSelected = position == selectedPosition
                || (day.getFullDate() != null && day.getFullDate().equals(selectedDate))
                || (day.getDate() != null && day.getDate().equals(selectedDate));

        if (isSelected) {
            holder.tvDayNumber.setBackgroundResource(R.drawable.bg_day_selected);
            holder.tvDayNumber.setTextColor(Color.WHITE);
            holder.tvDayName.setTextColor(Color.parseColor("#8A7BFF"));
        } else {
            holder.tvDayNumber.setBackgroundResource(android.R.color.transparent);
            holder.tvDayNumber.setTextColor(Color.parseColor("#1E1E1E"));
            holder.tvDayName.setTextColor(Color.parseColor("#9E9E9E"));
        }

        if (day.isHasDiary()) {
            holder.dotIndicator.setVisibility(View.VISIBLE);
            // فقط رنگ تغییر می‌کند، شکل دایره حفظ می‌شود
            ViewCompat.setBackgroundTintList(
                    holder.dotIndicator,
                    ColorStateList.valueOf(
                            Color.parseColor(isSelected ? "#8A7BFF" : "#9E9E9E")
                    )
            );
        } else {
            holder.dotIndicator.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            int clickedPosition = holder.getBindingAdapterPosition();
            if (clickedPosition == RecyclerView.NO_POSITION) {
                return;
            }

            int previousPosition = selectedPosition;
            selectedPosition = clickedPosition;
            selectedDate = days.get(clickedPosition).getFullDate();

            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(clickedPosition);

            if (listener != null) {
                listener.onDayClick(days.get(clickedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        final TextView tvDayName;
        final TextView tvDayNumber;
        final View dotIndicator;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            dotIndicator = itemView.findViewById(R.id.dotIndicator);
        }
    }
}
