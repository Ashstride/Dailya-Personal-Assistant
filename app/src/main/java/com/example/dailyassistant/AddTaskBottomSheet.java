package com.example.dailyassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private String selectedDate = "";
    private String selectedTime = "";

    // اگر null نباشد، یعنی BottomSheet در حالت ویرایش است
    private Task taskToEdit;

    private SwitchCompat swReminder;
    private MaterialButton btnSave;
    private View layoutReminderContainer;

    // جلوگیری از اجرای ناخواسته Listener هنگام setChecked
    private boolean suppressReminderListener = false;

    private ActivityResultLauncher<Intent> permissionLauncher;


    public interface OnTaskSavedListener {
        void onTaskSaved(Task task, boolean isEditMode);
    }

    private OnTaskSavedListener listener;

    public void setOnTaskSavedListener(OnTaskSavedListener listener) {
        this.listener = listener;
    }

    /**
     * ساخت BottomSheet در حالت ویرایش
     */
    public static AddTaskBottomSheet newInstance(Task task) {
        AddTaskBottomSheet fragment = new AddTaskBottomSheet();
        fragment.taskToEdit = task;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * ثبت ActivityResultLauncher برای بررسی دسترسی‌های نوتیفیکیشن و آلارم
         */
        permissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                suppressReminderListener = true;
                                if (swReminder != null) {
                                    swReminder.setChecked(true);
                                }
                                suppressReminderListener = false;
                            } else {
                                suppressReminderListener = true;
                                if (swReminder != null) {
                                    swReminder.setChecked(false);
                                }
                                suppressReminderListener = false;
                            }
                        }
                );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(
                R.layout.bottom_sheet_add_task,
                container,
                false
        );


        TextView tvSheetTitle = v.findViewById(R.id.tvSheetTitle);
        EditText etTitle = v.findViewById(R.id.etTaskTitle);
        TextView tvDate = v.findViewById(R.id.tvSelectedDate);
        TextView tvTime = v.findViewById(R.id.tvSelectedTime);
        swReminder = v.findViewById(R.id.switchReminder);
        btnSave = v.findViewById(R.id.btnSaveTask);
        layoutReminderContainer = (View) swReminder.getParent();

        View btnSetDate = v.findViewById(R.id.btnSetDate);
        View btnSetTime = v.findViewById(R.id.btnSetTime);

        // در حالت ساخت تسک جدید، ابتدا تاریخ و ساعت خالی هستند
        selectedDate = "";
        selectedTime = "";
        tvDate.setText("Select Date");
        tvTime.setText("Select Time");

        /*
         * تنظیم اطلاعات Task در حالت ویرایش
         */
        if (taskToEdit != null) {
            if (tvSheetTitle != null) {
                tvSheetTitle.setText("Edit Task");
            }

            if (taskToEdit.getTitle() != null) {
                etTitle.setText(taskToEdit.getTitle());
            }

            String oldDate = taskToEdit.getDueDate();
            if (oldDate != null && !oldDate.trim().isEmpty()) {
                selectedDate = oldDate.trim();
                tvDate.setText(selectedDate);
            }

            String oldTime = taskToEdit.getTime();
            if (oldTime != null && !oldTime.trim().isEmpty()) {
                selectedTime = oldTime.trim();
                tvTime.setText(selectedTime);
            }

            suppressReminderListener = true;
            swReminder.setChecked(taskToEdit.isReminderActive());
            suppressReminderListener = false;

            btnSave.setText("Update Task");
        } else {
            suppressReminderListener = true;
            swReminder.setChecked(false);
            suppressReminderListener = false;
        }

        // بررسی وضعیت اولیه فعال/غیرفعال بودن دکمه ذخیره و سوییچ یادآور
        updateUiState();

        /*
         * =====================================================
         * Reminder Permission Gate
         * =====================================================
         */
        swReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressReminderListener) {
                return;
            }

            // خاموش کردن Reminder نیازی به Permission ندارد
            if (!isChecked) {
                return;
            }

            if (PermissionActivity.areAllReminderPermissionsGranted(requireContext())) {
                return;
            }

            // باز کردن صفحه Permission
            Intent intent = new Intent(requireContext(), PermissionActivity.class);
            intent.putExtra(PermissionActivity.EXTRA_IS_REQUIRED, true);
            permissionLauncher.launch(intent);
        });

        /*
         * =====================================================
         * انتخاب تاریخ
         * =====================================================
         */
        btnSetDate.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    R.style.DailyPickerDialogTheme,
                    (datePicker, year, month, dayOfMonth) -> {
                        selectedDate = String.format(
                                Locale.US,
                                "%02d/%02d/%04d",
                                dayOfMonth,
                                month + 1,
                                year
                        );
                        tvDate.setText(selectedDate);
                        updateUiState();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            dialog.setOnShowListener(d -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_picker_dialog);
                }
            });

            dialog.show();
        });

        /*
         * =====================================================
         * انتخاب ساعت
         * =====================================================
         */
        btnSetTime.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();

            TimePickerDialog dialog = new TimePickerDialog(
                    requireContext(),
                    R.style.DailyPickerDialogTheme,
                    (timePicker, hourOfDay, minute) -> {
                        Calendar temp = Calendar.getInstance();
                        temp.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        temp.set(Calendar.MINUTE, minute);
                        temp.set(Calendar.SECOND, 0);

                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                        selectedTime = timeFormat.format(temp.getTime());
                        tvTime.setText(selectedTime);
                        updateUiState();
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            );

            dialog.setOnShowListener(d -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_picker_dialog);
                }
            });

            dialog.show();
        });

        /*
         * =====================================================
         * ذخیره Task
         * =====================================================
         */
        btnSave.setOnClickListener(view -> {
            String title = etTitle.getText() != null
                    ? etTitle.getText().toString().trim()
                    : "";

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(selectedDate) || TextUtils.isEmpty(selectedTime)) {
                Toast.makeText(requireContext(), "Please set date and time first", Toast.LENGTH_SHORT).show();
                return;
            }

            Task.Builder builder = new Task.Builder();

            if (taskToEdit != null) {
                builder.setId(taskToEdit.getId())
                        .setDone(taskToEdit.isDone())
                        .setPriority(taskToEdit.getPriority());
            } else {
                builder.setId(String.valueOf(System.currentTimeMillis()))
                        .setDone(false)
                        .setPriority(1);
            }

            builder.setTitle(title)
                    .setDueDate(selectedDate);

            if (swReminder.isChecked() && !TextUtils.isEmpty(selectedTime)) {
                builder.setReminderActive(true)
                        .setTime(selectedTime);
            } else {
                builder.setReminderActive(false)
                        .setTime(selectedTime);
            }

            Task savedTask = builder.build();


            if (taskToEdit != null) {
                ReminderScheduler.cancelReminder(requireContext(), taskToEdit);
            }

            if (savedTask.isReminderActive()
                    && savedTask.getTime() != null
                    && !savedTask.getTime().trim().isEmpty()) {
                ReminderScheduler.scheduleReminder(requireContext(), savedTask);
            }

            if (listener != null) {
                listener.onTaskSaved(savedTask, taskToEdit != null);
            }

            dismiss();
        });

        return v;
    }

    /**
     * بررسی لحظه‌ای پر بودن تاریخ و ساعت
     * و فعال/غیرفعال کردن دکمه ذخیره و سوییچ ریمایندر
     */
    private void updateUiState() {
        boolean hasDate = !TextUtils.isEmpty(selectedDate);
        boolean hasTime = !TextUtils.isEmpty(selectedTime);
        boolean isReady = hasDate && hasTime;

        // فعال/غیرفعال کردن سوییچ ریمایندر
        if (swReminder != null) {
            swReminder.setEnabled(isReady);
            if (!isReady) {
                suppressReminderListener = true;
                swReminder.setChecked(false);
                suppressReminderListener = false;
            }
        }

        // تنظیم شفافیت ظاهری برای بخش ریمایندر
        if (layoutReminderContainer != null) {
            layoutReminderContainer.setAlpha(isReady ? 1.0f : 0.5f);
        }

        // فعال/غیرفعال کردن دکمه ذخیره و استایل رنگی آن
        if (btnSave != null) {
            btnSave.setEnabled(isReady);
            if (isReady) {
                btnSave.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6C63FF")));
                btnSave.setTextColor(Color.WHITE);
            } else {
                btnSave.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
                btnSave.setTextColor(Color.parseColor("#9E9E9E"));
            }
        }
    }
}
