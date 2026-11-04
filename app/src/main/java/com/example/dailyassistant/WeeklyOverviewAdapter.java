package com.example.dailyassistant;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeeklyOverviewAdapter
        extends RecyclerView.Adapter<WeeklyOverviewAdapter.ViewHolder> {

    public static final String TYPE_WATER = "water";
    public static final String TYPE_SLEEP = "sleep";

    private final Context context;
    private final List<WeeklyHealthItem> items;
    private final String todayDate;
    private final String chartType;
    private final float maxValue;

    private boolean shouldAnimate = true;

    public WeeklyOverviewAdapter(Context context,
                                 List<WeeklyHealthItem> items,
                                 String chartType,
                                 int maxValue) {
        this.context = context;
        this.items = new ArrayList<>(items);
        this.chartType = chartType;
        this.maxValue = Math.max(maxValue, 1);
        this.todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_weekly_overview,
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
        WeeklyHealthItem item = items.get(position);

        boolean isToday = item.getDate() != null
                && item.getDate().equals(todayDate);

        boolean isFuture = isFutureDate(item.getDate());

        float numericValue = extractNumericValue(item.getValue());
        boolean hasData = numericValue > 0f;

        holder.txtDay.setText(item.getDay());

        if (isFuture && !hasData) {
            holder.txtValue.setText("--");
        } else {
            holder.txtValue.setText(item.getValue());
        }

        bindBarStyle(holder, isToday, isFuture, hasData);
        bindTextStyle(holder, isToday, isFuture, hasData);
        bindBarHeight(holder, numericValue, position, isFuture, hasData);
    }

    private void bindBarHeight(ViewHolder holder,
                               float value,
                               int position,
                               boolean isFuture,
                               boolean hasData) {

        cancelRunningAnimation(holder);

        int minHeightPx = dpToPx(0);
        int maxHeightPx = dpToPx(82);

        if (isFuture || !hasData) {
            setBarHeight(holder.viewBar, minHeightPx);
            holder.viewBar.setAlpha(0f);
            return;
        }

        holder.viewBar.setAlpha(1f);

        float safeValue = Math.max(value, 0f);
        float ratio = Math.min(safeValue / maxValue, 1f);
        int targetHeightPx = Math.round(maxHeightPx * ratio);
        targetHeightPx = Math.max(targetHeightPx, dpToPx(10));

        if (!shouldAnimate) {
            setBarHeight(holder.viewBar, targetHeightPx);
            return;
        }

        setBarHeight(holder.viewBar, minHeightPx);

        ValueAnimator animator = ValueAnimator.ofInt(
                minHeightPx,
                targetHeightPx
        );

        animator.setDuration(520);
        animator.setStartDelay(position * 45L);
        animator.setInterpolator(new OvershootInterpolator(0.85f));

        animator.addUpdateListener(animation -> {
            int animatedHeight = (int) animation.getAnimatedValue();
            setBarHeight(holder.viewBar, animatedHeight);
        });

        holder.barAnimator = animator;
        animator.start();
    }

    private void bindBarStyle(ViewHolder holder,
                              boolean isToday,
                              boolean isFuture,
                              boolean hasData) {

        if (TYPE_SLEEP.equals(chartType)) {
            holder.viewBar.setBackgroundResource(
                    R.drawable.bg_weekly_bar_fill_sleep
            );
        } else {
            holder.viewBar.setBackgroundResource(
                    R.drawable.bg_weekly_bar_fill_water
            );
        }

        if (isFuture || !hasData) {
            holder.viewBar.setAlpha(0f);
        } else {
            holder.viewBar.setAlpha(isToday ? 1f : 0.78f);
        }
    }

    private void bindTextStyle(ViewHolder holder,
                               boolean isToday,
                               boolean isFuture,
                               boolean hasData) {

        if (isFuture) {
            holder.txtDay.setTextColor(Color.parseColor("#CBD5E1"));
            holder.txtValue.setTextColor(Color.parseColor("#CBD5E1"));
            return;
        }

        if (!hasData) {
            holder.txtDay.setTextColor(Color.parseColor("#98A2B3"));
            holder.txtValue.setTextColor(Color.parseColor("#CBD5E1"));
            return;
        }

        if (isToday) {
            holder.txtDay.setTextColor(Color.parseColor(
                    TYPE_SLEEP.equals(chartType)
                            ? "#7C67F8"
                            : "#2F8FFF"
            ));

            holder.txtValue.setTextColor(Color.parseColor(
                    TYPE_SLEEP.equals(chartType)
                            ? "#7C67F8"
                            : "#2F8FFF"
            ));
        } else {
            holder.txtDay.setTextColor(Color.parseColor("#98A2B3"));
            holder.txtValue.setTextColor(Color.parseColor("#374151"));
        }
    }

    private boolean isFutureDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }

        return date.compareTo(todayDate) > 0;
    }

    private void setBarHeight(View bar, int heightPx) {
        ViewGroup.LayoutParams layoutParams = bar.getLayoutParams();
        layoutParams.height = heightPx;
        bar.setLayoutParams(layoutParams);
    }

    private void cancelRunningAnimation(ViewHolder holder) {
        if (holder.barAnimator != null) {
            holder.barAnimator.cancel();
            holder.barAnimator = null;
        }

        holder.viewBar.animate().cancel();
    }

    private float extractNumericValue(String valueText) {
        if (valueText == null || valueText.trim().isEmpty()) {
            return 0f;
        }

        String cleanValue = valueText.trim();

        if (cleanValue.equals("--")) {
            return 0f;
        }

        if (TYPE_SLEEP.equals(chartType)) {
            return extractSleepHours(cleanValue);
        }

        if (TYPE_WATER.equals(chartType)) {
            return extractWaterAmount(cleanValue);
        }

        return 0f;
    }

    private float extractWaterAmount(String valueText) {
        try {
            if (valueText.contains("/")) {
                String waterPart = valueText.substring(
                        0,
                        valueText.indexOf("/")
                ).trim();

                return Float.parseFloat(waterPart);
            }

            return Float.parseFloat(valueText.trim());

        } catch (Exception e) {
            return 0f;
        }
    }

    private float extractSleepHours(String valueText) {
        try {
            float hours = 0f;
            float minutes = 0f;

            if (valueText.contains("h")) {
                String hoursPart = valueText.substring(
                        0,
                        valueText.indexOf("h")
                ).trim();

                if (!hoursPart.isEmpty()) {
                    hours = Float.parseFloat(hoursPart);
                }
            }

            if (valueText.contains("h") && valueText.contains("m")) {
                String minutesPart = valueText.substring(
                        valueText.indexOf("h") + 1,
                        valueText.indexOf("m")
                ).trim();

                if (!minutesPart.isEmpty()) {
                    minutes = Float.parseFloat(minutesPart);
                }
            }

            return hours + (minutes / 60f);

        } catch (Exception e) {
            return 0f;
        }
    }

    private int dpToPx(int dp) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;

        return Math.round(dp * density);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateData(List<WeeklyHealthItem> newItems) {
        shouldAnimate = true;

        items.clear();

        if (newItems != null) {
            items.addAll(newItems);
        }

        notifyDataSetChanged();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        cancelRunningAnimation(holder);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView txtDay;
        final TextView txtValue;
        final View viewBar;
        final FrameLayout barContainer;

        ValueAnimator barAnimator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDay = itemView.findViewById(R.id.txtDay);
            txtValue = itemView.findViewById(R.id.txtValue);
            viewBar = itemView.findViewById(R.id.viewBar);
            barContainer = itemView.findViewById(R.id.barContainer);
        }
    }
}
