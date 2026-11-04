package com.example.dailyassistant;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class PermissionActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DailyaPrefs";
    private static final String KEY_PERMISSION_ONBOARDING_SHOWN =
            "permission_onboarding_shown";

    public static final String EXTRA_IS_REQUIRED =
            "com.example.dailyassistant.extra.IS_REQUIRED";

    private static final int COLOR_BUTTON_DEFAULT = 0xFF4E846B;
    private static final int COLOR_BUTTON_DONE = 0xFFF4C542;

    private static final long PERMISSION_REFRESH_DELAY_MS = 300L;

    private LinearLayout rowNotification;
    private LinearLayout rowExactAlarm;
    private LinearLayout rowBatteryOpt;

    private ImageView iconNotification;
    private ImageView iconExactAlarm;
    private ImageView iconBatteryOpt;

    private ImageView chevronNotification;
    private ImageView chevronExactAlarm;
    private ImageView chevronBatteryOpt;

    private AppCompatButton btnMaybeLater;

    private ActivityResultLauncher<String> notificationPermissionLauncher;

    private boolean allPermissionsGrantedLastState = false;
    private boolean notificationPermissionRequestedOnce = false;
    private boolean isRequired = false;
    private boolean hasExplicitMode = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean waitingForBatteryResult = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);

        Intent callerIntent = getIntent();

        hasExplicitMode =
                callerIntent != null
                        && callerIntent.hasExtra(EXTRA_IS_REQUIRED);

        if (hasExplicitMode) {

            isRequired =
                    callerIntent.getBooleanExtra(
                            EXTRA_IS_REQUIRED,
                            false
                    );

        } else {

            SharedPreferences prefs =
                    getSharedPreferences(
                            PREFS_NAME,
                            MODE_PRIVATE
                    );

            boolean onboardingShown =
                    prefs.getBoolean(
                            KEY_PERMISSION_ONBOARDING_SHOWN,
                            false
                    );

            if (onboardingShown) {
                goToDashboardDirectly();
                return;
            }

            isRequired = false;
        }

        setContentView(R.layout.activity_permission);

        initViews();
        initLaunchers();
        setupClicks();
        setupBackHandling();

        refreshPermissionsState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshPermissionsState();

        if (waitingForBatteryResult) {
            handler.postDelayed(() -> {
                refreshPermissionsState();
                waitingForBatteryResult = false;
            }, 350);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void initViews() {

        rowNotification = findViewById(R.id.rowNotification);
        rowExactAlarm = findViewById(R.id.rowExactAlarm);
        rowBatteryOpt = findViewById(R.id.rowBatteryOpt);

        iconNotification = findViewById(R.id.iconNotification);
        iconExactAlarm = findViewById(R.id.iconExactAlarm);
        iconBatteryOpt = findViewById(R.id.iconBatteryOpt);

        chevronNotification = findViewById(R.id.chevronNotification);
        chevronExactAlarm = findViewById(R.id.chevronExactAlarm);
        chevronBatteryOpt = findViewById(R.id.chevronBatteryOpt);

        btnMaybeLater = findViewById(R.id.btnMaybeLater);
    }

    private void initLaunchers() {

        notificationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {

                            refreshPermissionsState();

                            if (!isGranted
                                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                                boolean showRationale =
                                        shouldShowRequestPermissionRationale(
                                                Manifest.permission.POST_NOTIFICATIONS
                                        );

                                if (!showRationale) {
                                    openAppNotificationSettings();
                                }
                            }
                        }
                );
    }

    private void setupClicks() {

        rowNotification.setOnClickListener(v -> requestNotificationPermission());
        rowExactAlarm.setOnClickListener(v -> openExactAlarmSettings());
        rowBatteryOpt.setOnClickListener(v -> openBatteryOptimizationSettings());

        btnMaybeLater.setOnClickListener(v -> {

            boolean allGranted = areAllReminderPermissionsGranted(this);

            if (isRequired) {

                if (allGranted) {
                    goToDashboardDirectly();
                }

            } else {

                if (allGranted) {

                    if (!hasExplicitMode) {
                        markPermissionOnboardingShown();
                    }

                    goToDashboardDirectly();

                } else {
                    goToDashboardDirectly();
                }
            }
        });

    }

    private void setupBackHandling() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
        );
    }

    private void refreshPermissionsState() {

        boolean notificationGranted = isNotificationGranted();
        boolean exactAlarmGranted = hasExactAlarmPermission();
        boolean batteryGranted = isBatteryExempt();

        setRowState(
                rowNotification,
                iconNotification,
                chevronNotification,
                notificationGranted
        );

        setRowState(
                rowExactAlarm,
                iconExactAlarm,
                chevronExactAlarm,
                exactAlarmGranted
        );

        setRowState(
                rowBatteryOpt,
                iconBatteryOpt,
                chevronBatteryOpt,
                batteryGranted
        );

        boolean allGranted =
                notificationGranted
                        && exactAlarmGranted
                        && batteryGranted;

        updateContinueButtonState(allGranted);
    }

    private void setRowState(
            View row,
            ImageView icon,
            ImageView chevron,
            boolean granted
    ) {

        row.setAlpha(granted ? 1f : 0.95f);

        icon.setImageResource(
                granted ? R.drawable.ic_unlock : R.drawable.ic_lock
        );

        chevron.setVisibility(
                granted ? View.GONE : View.VISIBLE
        );
    }

    private void requestNotificationPermission() {

        if (isNotificationGranted()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (!notificationPermissionRequestedOnce) {

                notificationPermissionRequestedOnce = true;

                notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                );

                return;
            }

            boolean shouldShowRationale =
                    shouldShowRequestPermissionRationale(
                            Manifest.permission.POST_NOTIFICATIONS
                    );

            if (shouldShowRationale) {
                notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                );
            } else {
                openAppNotificationSettings();
            }

            return;
        }

        openAppNotificationSettings();
    }

    private void openExactAlarmSettings() {

        if (hasExactAlarmPermission()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            Intent intent =
                    new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);

            intent.setData(
                    Uri.parse("package:" + getPackageName())
            );

            try {
                startActivity(intent);
            } catch (Exception e) {
                openAppDetailsSettings();
            }
        }
    }

    private void openBatteryOptimizationSettings() {

        if (isBatteryExempt()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            try {

                Intent intent =
                        new Intent(
                                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        );

                intent.setData(
                        Uri.parse("package:" + getPackageName())
                );

                waitingForBatteryResult = true;

                startActivity(intent);
                return;

            } catch (Exception ignored) {
            }

            try {

                Intent intent =
                        new Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        );

                intent.addCategory(Intent.CATEGORY_DEFAULT);

                intent.setData(
                        Uri.parse("package:" + getPackageName())
                );

                startActivity(intent);
                return;

            } catch (Exception ignored) {
            }

            openAppDetailsSettings();
        }
    }

    private boolean isNotificationGranted() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }

        return NotificationManagerCompat.from(this).areNotificationsEnabled();
    }

    private boolean hasExactAlarmPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            AlarmManager alarmManager =
                    (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }

        return true;
    }

    private boolean isBatteryExempt() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            PowerManager powerManager =
                    (PowerManager) getSystemService(POWER_SERVICE);

            return powerManager != null
                    && powerManager.isIgnoringBatteryOptimizations(
                    getPackageName()
            );
        }

        return true;
    }

    public static boolean areAllReminderPermissionsGranted(Context context) {

        boolean notificationGranted;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            notificationGranted =
                    ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED;

        } else {

            notificationGranted =
                    NotificationManagerCompat.from(context).areNotificationsEnabled();
        }

        boolean exactAlarmGranted = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            exactAlarmGranted =
                    alarmManager != null && alarmManager.canScheduleExactAlarms();
        }

        boolean batteryGranted = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            PowerManager powerManager =
                    (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            batteryGranted =
                    powerManager != null
                            && powerManager.isIgnoringBatteryOptimizations(
                            context.getPackageName()
                    );
        }

        return notificationGranted && exactAlarmGranted && batteryGranted;
    }

    private void updateContinueButtonState(boolean allGranted) {

        if (isRequired && !allGranted) {

            btnMaybeLater.setVisibility(View.GONE);

        } else {

            btnMaybeLater.setVisibility(View.VISIBLE);

            btnMaybeLater.setText(allGranted ? "Done" : "Maybe Later");

            btnMaybeLater.setTextColor(
                    ContextCompat.getColor(this, android.R.color.white)
            );
        }

        if (allGranted && !allPermissionsGrantedLastState) {

            animateButtonToDone();

        } else if (!allGranted && allPermissionsGrantedLastState) {

            btnMaybeLater.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            COLOR_BUTTON_DEFAULT
                    )
            );

            btnMaybeLater.setScaleX(1f);
            btnMaybeLater.setScaleY(1f);
        }

        allPermissionsGrantedLastState = allGranted;
    }

    private void animateButtonToDone() {

        int currentColor = COLOR_BUTTON_DEFAULT;

        if (btnMaybeLater.getBackground() instanceof ColorDrawable) {
            currentColor = ((ColorDrawable) btnMaybeLater.getBackground()).getColor();
        }

        ValueAnimator colorAnimator =
                ValueAnimator.ofObject(
                        new ArgbEvaluator(),
                        currentColor,
                        COLOR_BUTTON_DONE
                );

        colorAnimator.setDuration(300L);
        colorAnimator.setInterpolator(new DecelerateInterpolator());
        colorAnimator.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            btnMaybeLater.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(color)
            );
        });

        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(
                btnMaybeLater,
                View.SCALE_X,
                1f,
                1.04f
        );

        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(
                btnMaybeLater,
                View.SCALE_Y,
                1f,
                1.04f
        );

        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(
                btnMaybeLater,
                View.SCALE_X,
                1.04f,
                1f
        );

        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(
                btnMaybeLater,
                View.SCALE_Y,
                1.04f,
                1f
        );

        TimeInterpolator interpolator = new DecelerateInterpolator();

        scaleXUp.setInterpolator(interpolator);
        scaleYUp.setInterpolator(interpolator);
        scaleXDown.setInterpolator(interpolator);
        scaleYDown.setInterpolator(interpolator);

        scaleXUp.setDuration(150L);
        scaleYUp.setDuration(150L);
        scaleXDown.setDuration(150L);
        scaleYDown.setDuration(150L);

        AnimatorSet pulseUp = new AnimatorSet();
        pulseUp.playTogether(scaleXUp, scaleYUp);

        AnimatorSet pulseDown = new AnimatorSet();
        pulseDown.playTogether(scaleXDown, scaleYDown);

        AnimatorSet fullSet = new AnimatorSet();
        fullSet.playSequentially(pulseUp, pulseDown);
        fullSet.start();

        colorAnimator.start();
    }

    private void openAppNotificationSettings() {

        try {

            Intent intent =
                    new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);

            intent.putExtra(
                    Settings.EXTRA_APP_PACKAGE,
                    getPackageName()
            );

            startActivity(intent);

        } catch (Exception e) {
            openAppDetailsSettings();
        }
    }

    private void openAppDetailsSettings() {

        Intent intent =
                new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

        intent.setData(
                Uri.fromParts("package", getPackageName(), null)
        );

        startActivity(intent);
    }

    private void markPermissionOnboardingShown() {

        SharedPreferences prefs =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        prefs.edit()
                .putBoolean(
                        KEY_PERMISSION_ONBOARDING_SHOWN,
                        true
                )
                .apply();
    }

    private void goToDashboardDirectly() {
        Intent intent = new Intent(PermissionActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

}
