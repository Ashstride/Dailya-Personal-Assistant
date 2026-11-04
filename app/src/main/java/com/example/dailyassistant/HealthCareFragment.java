package com.example.dailyassistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HealthCareFragment extends Fragment {

    private RecyclerView rvWeeklyWater;
    private RecyclerView rvWeeklySleep;

    private WeeklyOverviewAdapter waterAdapter;
    private WeeklyOverviewAdapter sleepAdapter;

    private TextView txtWeekRange;

    private TextView txtWeeklyWaterBadge;
    private TextView txtWaterWeeklySummary;

    private TextView txtWeeklySleepBadge;
    private TextView txtSleepSummary;


    private View btnPrevWeek;
    private View btnNextWeek;

    private HealthManagerFacade healthManager;
    private Calendar weekStartDate;

    private static final int WATER_GOAL = 8;

    // ===============================
    // 💧 Daily Water Views
    // ===============================
    private TextView txtWaterSummary;
    private TextView txtWaterPercent;
    private TextView txtWaterDateRange;
    private Button btnAddGlass;
    private ImageView[] waterGlassViews;

    // ===============================
    // 🌙 Daily Sleep Views
    // ===============================
    private TextView txtSleepDuration;
    private TextView txtSleepTime;
    private TextView txtSleepStartValue;
    private TextView txtWakeTimeValue;
    private Button btnUpdateSleep;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_health_care,
                container,
                false
        );

        healthManager = HealthManagerFacade.getInstance(requireContext());

        weekStartDate = Calendar.getInstance();

        weekStartDate.set(Calendar.DAY_OF_WEEK, weekStartDate.getFirstDayOfWeek());


        initViews(view);
        setupRecyclerViews();
        setupNavigation();
        setupWaterActions();
        setupSleepActions();
        refreshUI();

        return view;
    }

    private void initViews(View view) {

        // ===============================
        // Weekly Overview
        // ===============================
        rvWeeklyWater = view.findViewById(R.id.rvWeeklyWater);
        rvWeeklySleep = view.findViewById(R.id.rvWeeklySleep);

        txtWeekRange = view.findViewById(R.id.txtWeekRange);

        txtWeeklyWaterBadge =
                view.findViewById(R.id.txtWeeklyWaterBadge);

        txtWaterWeeklySummary =
                view.findViewById(R.id.txtWaterWeeklySummary);

        txtWeeklySleepBadge =
                view.findViewById(R.id.txtWeeklySleepBadge);

        txtSleepSummary =
                view.findViewById(R.id.txtSleepSummary);



        btnPrevWeek = view.findViewById(R.id.btnPrevWeek);
        btnNextWeek = view.findViewById(R.id.btnNextWeek);

        // ===============================
        // Daily Water Card
        // ===============================
        txtWaterSummary = view.findViewById(R.id.txtWaterSummary);
        txtWaterPercent = view.findViewById(R.id.txtWaterPercent);
        txtWaterDateRange = view.findViewById(R.id.txtWaterDateRange);
        btnAddGlass = view.findViewById(R.id.btnAddGlass);

        waterGlassViews = new ImageView[]{
                view.findViewById(R.id.imgGlass1),
                view.findViewById(R.id.imgGlass2),
                view.findViewById(R.id.imgGlass3),
                view.findViewById(R.id.imgGlass4),
                view.findViewById(R.id.imgGlass5),
                view.findViewById(R.id.imgGlass6),
                view.findViewById(R.id.imgGlass7),
                view.findViewById(R.id.imgGlass8)
        };

        // ===============================
        // Daily Sleep Card
        // ===============================
        txtSleepDuration = view.findViewById(R.id.txtSleepDuration);
        txtSleepTime = view.findViewById(R.id.txtSleepTime);

        txtSleepStartValue = view.findViewById(
                R.id.txtSleepStartValue
        );

        txtWakeTimeValue = view.findViewById(
                R.id.txtWakeTimeValue
        );

        btnUpdateSleep = view.findViewById(
                R.id.btnUpdateSleep
        );
    }

    // ===============================
    // 📊 Weekly RecyclerViews
    // ===============================
    private void setupRecyclerViews() {

        rvWeeklyWater.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        RecyclerView.HORIZONTAL,
                        false
                )
        );

        rvWeeklySleep.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        RecyclerView.HORIZONTAL,
                        false
                )
        );

        rvWeeklyWater.setHasFixedSize(true);
        rvWeeklySleep.setHasFixedSize(true);

        rvWeeklyWater.setOverScrollMode(View.OVER_SCROLL_NEVER);
        rvWeeklySleep.setOverScrollMode(View.OVER_SCROLL_NEVER);

        waterAdapter = new WeeklyOverviewAdapter(
                requireContext(),
                healthManager.getWeeklyWater(weekStartDate),
                WeeklyOverviewAdapter.TYPE_WATER,
                WATER_GOAL
        );

        sleepAdapter = new WeeklyOverviewAdapter(
                requireContext(),
                healthManager.getWeeklySleep(weekStartDate),
                WeeklyOverviewAdapter.TYPE_SLEEP,
                8
        );

        rvWeeklyWater.setAdapter(waterAdapter);
        rvWeeklySleep.setAdapter(sleepAdapter);
    }


    // ===============================
    // 💧 Water Logic
    // ===============================
    private void setupWaterActions() {
        btnAddGlass.setOnClickListener(v -> addOneGlass());
    }

    private void addOneGlass() {

        int currentWater = healthManager.getTodayWater();

        if (currentWater >= WATER_GOAL) {
            refreshDailyWaterCard();
            return;
        }

        int updatedWater = currentWater + 1;

        healthManager.updateTodayWater(updatedWater);

        refreshUI();
    }

    private void refreshDailyWaterCard() {

        int todayWater = healthManager.getTodayWater();

        float liters = todayWater * 0.25f;

        int percent = Math.min(
                100,
                (todayWater * 100) / WATER_GOAL
        );

        txtWaterSummary.setText(
                String.format(
                        Locale.US,
                        "%.2f L",
                        liters
                )
        );

        txtWaterPercent.setText(
                percent + "%"
        );

        txtWaterDateRange.setText("Today");


        for (int i = 0; i < waterGlassViews.length; i++) {

            ImageView glassView = waterGlassViews[i];

            if (i < todayWater) {

                glassView.setAlpha(1f);

                glassView.animate()
                        .alpha(1f)
                        .scaleX(1.08f)
                        .scaleY(1.08f)
                        .setDuration(160)
                        .withEndAction(() ->
                                glassView.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(120)
                                        .start()
                        )
                        .start();

            } else {

                glassView.setAlpha(0.25f);
                glassView.setScaleX(1f);
                glassView.setScaleY(1f);
            }
        }

        if (todayWater >= WATER_GOAL) {
            btnAddGlass.setText(
                    "🎉 Goal complete! 8 glasses today"
            );
        } else {
            btnAddGlass.setText("+ Add Glass");
        }
    }

    // ===============================
    // 🌙 Sleep Logic
    // ===============================
    private void setupSleepActions() {

        btnUpdateSleep.setOnClickListener(v -> {

            SleepBottomSheet bottomSheet =
                    new SleepBottomSheet();

            bottomSheet.setOnSleepSavedListener(() -> {
                refreshDailySleepCard();
                refreshWeeklyRecycler();
            });

            bottomSheet.show(
                    getParentFragmentManager(),
                    "SleepBottomSheet"
            );
        });
    }

    private void refreshDailySleepCard() {

        String todayDate = healthManager.getTodayDate();

        float sleepHours =
                healthManager.getSleepHours(todayDate);

        String sleepStart =
                healthManager.getSleepStart(todayDate);

        String wakeTime =
                healthManager.getWakeTime(todayDate);

        /*
         * اگر ساعت خوابی ثبت نشده باشد،
         * کارت در حالت خالی نمایش داده می‌شود.
         */
        if (sleepHours <= 0f) {

            txtSleepDuration.setText("0h");
            txtSleepTime.setText("Not rated");

            txtSleepStartValue.setText("--:--");
            txtWakeTimeValue.setText("--:--");

            return;
        }

        int hours = (int) sleepHours;

        int minutes = Math.round(
                (sleepHours - hours) * 60
        );

        if (minutes == 60) {
            hours++;
            minutes = 0;
        }

        String durationText;

        if (minutes == 0) {
            durationText = hours + "h";
        } else {
            durationText = hours + "h " + minutes + "m";
        }


        String sleepQuality =
                healthManager.getSleepQuality(todayDate);

        sleepQuality = formatSleepQualityText(sleepQuality);

        txtSleepDuration.setText(durationText);

// نمایش کیفیت خواب مثل: Good Sleep
        txtSleepTime.setText(sleepQuality);


        // نمایش زمان شروع خواب و بیدارشدن در باکس‌های پایین
        txtSleepStartValue.setText(
                isValidSleepTime(sleepStart)
                        ? sleepStart
                        : "--:--"
        );

        txtWakeTimeValue.setText(
                isValidSleepTime(wakeTime)
                        ? wakeTime
                        : "--:--"
        );
    }

    private boolean isValidSleepTime(String time) {

        return time != null
                && !time.trim().isEmpty()
                && !time.equals("--:--");
    }
    private String formatSleepQualityText(String quality) {

        if (quality == null || quality.trim().isEmpty()) {
            return "Normal Sleep";
        }

        quality = quality.trim();

        if (quality.toLowerCase(Locale.US).contains("sleep")) {
            return quality;
        }

        return quality + " Sleep";
    }


    // ===============================
    // 📊 Weekly Refresh
    // ===============================
    private void refreshWeeklyRecycler() {

        if (waterAdapter != null) {
            waterAdapter.updateData(
                    healthManager.getWeeklyWater(weekStartDate)
            );
        }

        if (sleepAdapter != null) {
            sleepAdapter.updateData(
                    healthManager.getWeeklySleep(weekStartDate)
            );
        }

        refreshWeeklyStats();
    }
    private void refreshWeeklyStats() {

        int waterPercent =
                healthManager.getWeeklyWaterProgressPercent(
                        weekStartDate
                );

        int sleepPercent =
                healthManager.getWeeklySleepProgressPercent(
                        weekStartDate
                );

        String avgWaterText =
                healthManager.getWeeklyAverageWaterText(
                        weekStartDate
                );

        String avgSleepText =
                healthManager.getWeeklyAverageSleepNumber(
                        weekStartDate
                );

        if (txtWeeklyWaterBadge != null) {
            txtWeeklyWaterBadge.setText(
                    waterPercent + "%"
            );
        }

        if (txtWaterWeeklySummary != null) {
            txtWaterWeeklySummary.setText(
                    avgWaterText
            );
        }

        if (txtWeeklySleepBadge != null) {
            txtWeeklySleepBadge.setText(
                    sleepPercent + "%"
            );
        }

        if (txtSleepSummary != null) {
            txtSleepSummary.setText(
                    avgSleepText
            );
        }
    }




    // ===============================
    // ⬅️➡️ Week Navigation
    // ===============================
    private void setupNavigation() {

        btnPrevWeek.setOnClickListener(v -> {
            weekStartDate.add(
                    Calendar.DAY_OF_YEAR,
                    -7
            );

            refreshUI();
        });

        btnNextWeek.setOnClickListener(v -> {
            weekStartDate.add(
                    Calendar.DAY_OF_YEAR,
                    7
            );

            refreshUI();
        });
    }

    // ===============================
    // 🔄 UI Refresh
    // ===============================
    private void refreshUI() {

        SimpleDateFormat sdf =
                new SimpleDateFormat(
                        "MMM d",
                        Locale.US
                );

        String start =
                sdf.format(weekStartDate.getTime());

        Calendar endCal =
                (Calendar) weekStartDate.clone();

        endCal.add(
                Calendar.DAY_OF_YEAR,
                6
        );

        String end =
                sdf.format(endCal.getTime());

        txtWeekRange.setText(
                start + " - " + end
        );

        refreshDailyWaterCard();
        refreshDailySleepCard();
        refreshWeeklyRecycler();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (healthManager != null) {
            refreshUI();
        }
    }
}
