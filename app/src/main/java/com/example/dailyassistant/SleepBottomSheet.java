package com.example.dailyassistant;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Locale;

public class SleepBottomSheet extends BottomSheetDialogFragment {

    private TextView tvSleepTime;
    private TextView tvWakeTime;
    private TextView tvSleepDuration;
    private Spinner spSleepQuality;
    private Button btnSaveSleep;

    private String sleepTime = "";
    private String wakeTime = "";
    private float sleepHours = 0f;

    private OnSleepSavedListener listener;

    public interface OnSleepSavedListener {
        void onSaved();
    }

    public void setOnSleepSavedListener(
            OnSleepSavedListener listener
    ) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.bottom_sheet_sleep,
                container,
                false
        );

        initViews(view);
        setupSpinner();
        setupTimePicker();
        setupSaveButton();

        return view;
    }

    private void initViews(View view) {
        tvSleepTime =
                view.findViewById(R.id.tvSleepTime);

        tvWakeTime =
                view.findViewById(R.id.tvWakeTime);

        tvSleepDuration =
                view.findViewById(R.id.tvSleepDuration);

        spSleepQuality =
                view.findViewById(R.id.spSleepQuality);

        btnSaveSleep =
                view.findViewById(R.id.btnSaveSleep);
    }

    private void setupSpinner() {
        String[] qualities = {
                "Excellent",
                "Good",
                "Normal",
                "Poor"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        qualities
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spSleepQuality.setAdapter(adapter);
    }

    private void setupTimePicker() {
        tvSleepTime.setOnClickListener(
                view -> openTimePicker(true)
        );

        tvWakeTime.setOnClickListener(
                view -> openTimePicker(false)
        );
    }

    private void openTimePicker(boolean isSleepTime) {
        Calendar calendar =
                Calendar.getInstance();

        int currentHour =
                calendar.get(Calendar.HOUR_OF_DAY);

        int currentMinute =
                calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog =
                new TimePickerDialog(
                        requireContext(),
                        R.style.DailyPickerDialogTheme,
                        (timePicker, selectedHour, selectedMinute) -> {
                            String selectedTime =
                                    String.format(
                                            Locale.US,
                                            "%02d:%02d",
                                            selectedHour,
                                            selectedMinute
                                    );

                            if (isSleepTime) {
                                sleepTime = selectedTime;
                                tvSleepTime.setText(selectedTime);
                            } else {
                                wakeTime = selectedTime;
                                tvWakeTime.setText(selectedTime);
                            }

                            calculateSleep();
                        },
                        currentHour,
                        currentMinute,
                        true
                );

        dialog.setOnShowListener(d -> {
            Button positiveButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            if (positiveButton != null) {
                positiveButton.setTextColor(Color.BLACK);
            }

            Button negativeButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                    );

            if (negativeButton != null) {
                negativeButton.setTextColor(Color.BLACK);
            }

            if (dialog.getWindow() != null) {
                dialog.getWindow()
                        .setBackgroundDrawableResource(
                                R.drawable.bg_picker_dialog
                        );
            }
        });

        dialog.show();
    }

    private void calculateSleep() {
        if (sleepTime.isEmpty() || wakeTime.isEmpty()) {
            sleepHours = 0f;

            tvSleepDuration.setText(
                    "Sleep Duration: --"
            );

            return;
        }

        try {
            String[] sleepParts =
                    sleepTime.split(":");

            String[] wakeParts =
                    wakeTime.split(":");

            int sleepMinutes =
                    Integer.parseInt(sleepParts[0]) * 60
                            + Integer.parseInt(sleepParts[1]);

            int wakeMinutes =
                    Integer.parseInt(wakeParts[0]) * 60
                            + Integer.parseInt(wakeParts[1]);

            if (wakeMinutes <= sleepMinutes) {
                wakeMinutes += 24 * 60;
            }

            int totalMinutes =
                    wakeMinutes - sleepMinutes;

            int hours =
                    totalMinutes / 60;

            int minutes =
                    totalMinutes % 60;

            sleepHours =
                    totalMinutes / 60f;

            tvSleepDuration.setText(
                    "Sleep Duration: "
                            + hours
                            + "h "
                            + minutes
                            + "m"
            );

        } catch (Exception e) {
            sleepHours = 0f;

            tvSleepDuration.setText(
                    "Sleep Duration: --"
            );
        }
    }

    private void setupSaveButton() {
        btnSaveSleep.setOnClickListener(view -> {
            if (sleepTime.isEmpty() || wakeTime.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Please select sleep and wake time",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (sleepHours <= 0f) {
                Toast.makeText(
                        requireContext(),
                        "Invalid sleep duration",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String quality =
                    spSleepQuality.getSelectedItem()
                            .toString();

            HealthManagerFacade manager =
                    HealthManagerFacade.getInstance(
                            requireContext()
                    );

            manager.updateSleep(
                    sleepHours,
                    sleepTime,
                    wakeTime,
                    quality
            );

            if (listener != null) {
                listener.onSaved();
            }

            Toast.makeText(
                    requireContext(),
                    "Sleep data saved",
                    Toast.LENGTH_SHORT
            ).show();

            dismiss();
        });
    }
}
