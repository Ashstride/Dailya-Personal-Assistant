package com.example.dailyassistant;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvTasks;
    private RecyclerView rvHealthOverview;
    private HealthOverviewAdapter healthAdapter;

    private TextView tvDiaryReflectionQuestion;

    // عناصر کارت وضعیت تسک‌ها
    private View layoutEmptyState;
    private TextView tvEmptyStateTitle;
    private TextView tvEmptyStateDescription;
    private ImageView imgEmptyStateIcon;
    private MaterialButton btnEmptyStateAction;

    private ImageButton moodHappy, moodGood, moodNeutral, moodSad, moodMiserable;
    private TaskAdapter adapter;
    private TaskManagerFacade taskFacade;
    private DiaryManagerFacade diaryFacade;
    private String selectedMood = "neutral";

    private static final long TASK_COMPLETE_REFRESH_DELAY_MS = 700L;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // وضعیت کنونی کارت خالی (آیا تسک‌ها کامل شده‌اند یا هیچ تسکی نبوده)
    private boolean isAllTasksCompletedState = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Facades
        taskFacade = TaskManagerFacade.getInstance(getContext());
        diaryFacade = DiaryManagerFacade.getInstance(requireContext());

        // ناوبری به تب تسک‌ها
        TextView btnViewAllTasks = view.findViewById(R.id.btnViewAllTasks);
        if (btnViewAllTasks != null) {
            btnViewAllTasks.setOnClickListener(v -> {
                if (getActivity() instanceof DashboardActivity) {
                    ((DashboardActivity) getActivity()).openTasksTab();
                }
            });
        }

        // ناوبری به تب سلامت
        TextView btnViewAllHealth = view.findViewById(R.id.btnViewAllHealth);
        if (btnViewAllHealth != null) {
            btnViewAllHealth.setOnClickListener(v -> {
                if (getActivity() instanceof DashboardActivity) {
                    ((DashboardActivity) getActivity()).openHealthTab();
                }
            });
        }

        // تنظیمات Mood
        moodHappy = view.findViewById(R.id.mood_happy);
        moodGood = view.findViewById(R.id.mood_good);
        moodNeutral = view.findViewById(R.id.mood_neutral);
        moodSad = view.findViewById(R.id.mood_sad);
        moodMiserable = view.findViewById(R.id.mood_miserable);

        moodHappy.setOnClickListener(v -> selectMood("happy"));
        moodGood.setOnClickListener(v -> selectMood("good"));
        moodNeutral.setOnClickListener(v -> selectMood("neutral"));
        moodSad.setOnClickListener(v -> selectMood("sad"));
        moodMiserable.setOnClickListener(v -> selectMood("miserable"));

        resetAllMoods();
        loadTodayMood();

        // Tasks UI Binding
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        imgEmptyStateIcon = view.findViewById(R.id.imgEmptyStateIcon);
        tvEmptyStateTitle = view.findViewById(R.id.tvEmptyStateTitle);
        tvEmptyStateDescription = view.findViewById(R.id.tvEmptyStateDescription);
        btnEmptyStateAction = view.findViewById(R.id.btnEmptyStateAction);
        rvTasks = view.findViewById(R.id.rvTasks);

        if (btnEmptyStateAction != null) {
            btnEmptyStateAction.setOnClickListener(v -> handleEmptyStateAction());
        }

        setupTasksRecycler();

        // Health UI
        rvHealthOverview = view.findViewById(R.id.rvHealthOverview);
        if (rvHealthOverview != null) {
            setupHealthOverviewRecycler();
        }

        // Diary Shortcut
        View cardDiaryShortcut = view.findViewById(R.id.cardDiaryShortcut);
        tvDiaryReflectionQuestion = view.findViewById(R.id.tvDiaryReflectionQuestion);

        setupDiaryShortcut();

        if (cardDiaryShortcut != null) {
            cardDiaryShortcut.setOnClickListener(v -> {
                if (getActivity() instanceof DashboardActivity) {
                    ((DashboardActivity) getActivity()).openDiaryTab();
                }
            });
        }

        return view;
    }

    private void handleEmptyStateAction() {
        if (isAllTasksCompletedState) {
            // اگر همه تسک‌ها انجام شده‌اند، به تب تسک‌ها هدایت شود
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).openTasksTab();
            }
        } else {
            // اگر هیچ تسکی نیست، باتم‌شیت ساخت تسک باز شود
            openTasksAndShowAddSheet();
        }
    }

    private void setupDiaryShortcut() {
        if (tvDiaryReflectionQuestion == null || diaryFacade == null) {
            return;
        }

        String today = new SimpleDateFormat("MMM dd, yyyy", Locale.US)
                .format(new Date());


        String question = diaryFacade.getReflectionQuestionForDate(today);

        if (question == null) {

            question = diaryFacade.getDailyReflectionQuestion(requireContext(), today);


            diaryFacade.saveReflectionQuestionForDate(today, question);
        }

        tvDiaryReflectionQuestion.setText(question);
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshTasks();
        refreshHealthOverview();
        loadTodayMood();
        setupDiaryShortcut();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void setupTasksRecycler() {
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setHasFixedSize(false);
        rvTasks.setNestedScrollingEnabled(false);
        rvTasks.setClipChildren(true);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(200);
        animator.setRemoveDuration(200);
        animator.setMoveDuration(200);
        animator.setChangeDuration(0);
        animator.setSupportsChangeAnimations(false);
        rvTasks.setItemAnimator(animator);

        List<Task> allTasks = taskFacade.getTasks();
        List<Task> pendingTasks = getPendingTasks(allTasks);

        adapter = new TaskAdapter.Builder()
                .setTasks(pendingTasks)
                .setLimited(true)
                .build();

        adapter.setOnTaskStatusChangeListener((task, isDone, position) -> {
            if (isDone) {
                taskFacade.updateTaskStatus(task.getId(), true);
                mainHandler.postDelayed(this::refreshTasks, TASK_COMPLETE_REFRESH_DELAY_MS);
            }
        });

        rvTasks.setAdapter(adapter);
        updateEmptyState(allTasks, pendingTasks);
    }

    private void setupHealthOverviewRecycler() {
        rvHealthOverview.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvHealthOverview.setHasFixedSize(false);
        rvHealthOverview.setNestedScrollingEnabled(false);

        List<HealthOverviewItem> healthItems = buildHealthOverviewItems();
        healthAdapter = new HealthOverviewAdapter(requireContext(), healthItems);
        rvHealthOverview.setAdapter(healthAdapter);
    }

    private void refreshHealthOverview() {
        if (healthAdapter != null) {
            List<HealthOverviewItem> newItems = buildHealthOverviewItems();
            healthAdapter.updateData(newItems);
        }
    }

    private List<HealthOverviewItem> buildHealthOverviewItems() {
        HealthManagerFacade healthManager = HealthManagerFacade.getInstance(requireContext());

        int water = healthManager.getTodayWater();
        float sleep = healthManager.getSleepHours(healthManager.getTodayDate());

        String sleepValue;
        if (sleep <= 0) {
            sleepValue = "--";
        } else {
            int hours = (int) sleep;
            int minutes = Math.round((sleep - hours) * 60);
            sleepValue = hours + "h " + minutes + "m";
        }

        int waterProgress = (water * 100) / 8;
        int sleepProgress = (int) ((sleep / 8f) * 100);
        if (sleepProgress > 100) {
            sleepProgress = 100;
        }

        List<HealthOverviewItem> list = new ArrayList<>();
        list.add(new HealthOverviewItem(
                HealthOverviewItem.TYPE_WATER,
                "Water Intake",
                water + " / 8",
                "Glasses",
                waterProgress
        ));

        list.add(new HealthOverviewItem(
                HealthOverviewItem.TYPE_SLEEP,
                "Sleep Tracker",
                sleepValue,
                "Hours",
                sleepProgress
        ));

        return list;
    }

    private void refreshTasks() {
        if (adapter != null) {
            List<Task> allTasks = taskFacade.getTasks();
            List<Task> updatedTasks = getPendingTasks(allTasks);

            adapter.updateData(updatedTasks);
            updateEmptyState(allTasks, updatedTasks);
        }
    }

    private void updateEmptyState(List<Task> allTasks, List<Task> pendingTasks) {
        boolean hasPending = pendingTasks != null && !pendingTasks.isEmpty();
        List<Task> todayTasks = getTodayTasks(allTasks);
        boolean hasTodayTasks = todayTasks != null && !todayTasks.isEmpty();

        if (hasPending) {
            rvTasks.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) {
                layoutEmptyState.setVisibility(View.GONE);
            }
        } else {
            rvTasks.setVisibility(View.GONE);
            if (layoutEmptyState != null) {
                layoutEmptyState.setVisibility(View.VISIBLE);
            }

            if (hasTodayTasks) {
                // سناریو ۲: تمام تسک‌های امروز با موفقیت تیک خورده و تکمیل شده‌اند
                isAllTasksCompletedState = true;
                setEmptyStateCompletedMode();
            } else {
                // سناریو ۱: از اول هیچ تسکی برای امروز تعریف نشده
                isAllTasksCompletedState = false;
                setEmptyStateNoTasksMode();
            }
        }
    }

    /**
     * تنظیم حالت بنفش و دکمه Add Task با آیکون مثبت
     */
    private void setEmptyStateNoTasksMode() {
        if (imgEmptyStateIcon != null) {
            imgEmptyStateIcon.clearColorFilter(); // حذف فیلتر رنگی
            imgEmptyStateIcon.setImageResource(R.drawable.tasks_today); // عکس بدون تسک
        }
        if (tvEmptyStateTitle != null) {
            tvEmptyStateTitle.setText("No tasks for today");
        }
        if (tvEmptyStateDescription != null) {
            tvEmptyStateDescription.setText("Add a task to stay organized and get started.");
        }
        if (btnEmptyStateAction != null) {
            btnEmptyStateAction.setText("Add Task");
            btnEmptyStateAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#7C5FC2")));
            btnEmptyStateAction.setIconResource(R.drawable.ic_plus);
            btnEmptyStateAction.setIconGravity(com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START);
        }
    }

    private void setEmptyStateCompletedMode() {
        if (imgEmptyStateIcon != null) {
            imgEmptyStateIcon.clearColorFilter(); // حذف فیلتر رنگی
            imgEmptyStateIcon.setImageResource(R.drawable.tasks_tomorrow); // عکس تسک‌های انجام‌شده
        }
        if (tvEmptyStateTitle != null) {
            tvEmptyStateTitle.setText("You're all caught up!");
        }
        if (tvEmptyStateDescription != null) {
            tvEmptyStateDescription.setText("Nice work! All tasks are completed for today.");
        }
        if (btnEmptyStateAction != null) {
            btnEmptyStateAction.setText("Plan Tomorrow");
            btnEmptyStateAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2EB872")));
            btnEmptyStateAction.setIconResource(R.drawable.ic_arrow_forward_white);
            btnEmptyStateAction.setIconGravity(com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_END);
        }
    }


    private List<Task> getTodayTasks(List<Task> allTasks) {
        List<Task> todayList = new ArrayList<>();
        if (allTasks != null) {
            for (Task task : allTasks) {
                if (isToday(task)) {
                    todayList.add(task);
                }
            }
        }
        return todayList;
    }

    private List<Task> getPendingTasks(List<Task> allTasks) {
        List<Task> pendingList = new ArrayList<>();
        if (allTasks != null) {
            for (Task task : allTasks) {
                if (!task.isDone() && isToday(task)) {
                    pendingList.add(task);
                }
            }
        }
        return pendingList;
    }

    private boolean isToday(Task task) {
        if (task == null || task.getDueDate() == null || task.getDueDate().trim().isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.US);
            sdf.setLenient(false);

            Date taskDate = sdf.parse(task.getDueDate().trim());
            if (taskDate == null) return false;

            String taskDay = sdf.format(taskDate);
            String todayDay = sdf.format(new Date());

            return taskDay.equals(todayDay);
        } catch (ParseException e) {
            return false;
        }
    }

    private void resetAllMoods() {
        moodHappy.setImageResource(R.drawable.ic_mood_happy);
        moodGood.setImageResource(R.drawable.ic_mood_good);
        moodNeutral.setImageResource(R.drawable.ic_mood_neutral);
        moodSad.setImageResource(R.drawable.ic_mood_sad);
        moodMiserable.setImageResource(R.drawable.ic_mood_miserable);

        moodHappy.setAlpha(0.45f);
        moodGood.setAlpha(0.45f);
        moodNeutral.setAlpha(0.45f);
        moodSad.setAlpha(0.45f);
        moodMiserable.setAlpha(0.45f);
    }

    private void applyMoodSelection(String mood) {
        resetAllMoods();
        selectedMood = mood;

        if (mood == null) return;

        switch (mood) {
            case "happy":
                moodHappy.setImageResource(R.drawable.ic_mood_happy_act);
                moodHappy.setAlpha(1f);
                break;
            case "good":
                moodGood.setImageResource(R.drawable.ic_mood_good_act);
                moodGood.setAlpha(1f);
                break;
            case "sad":
                moodSad.setImageResource(R.drawable.ic_mood_sad_act);
                moodSad.setAlpha(1f);
                break;
            case "miserable":
                moodMiserable.setImageResource(R.drawable.ic_mood_miserable_act);
                moodMiserable.setAlpha(1f);
                break;
            case "neutral":
                moodNeutral.setImageResource(R.drawable.ic_mood_neutral_act);
                moodNeutral.setAlpha(1f);
                break;
        }
    }

    private void selectMood(String mood) {
        applyMoodSelection(mood);
        diaryFacade.saveTodayMood(mood);
    }

    private void loadTodayMood() {
        if (diaryFacade == null) {
            diaryFacade = DiaryManagerFacade.getInstance(requireContext());
        }
        String todayMood = diaryFacade.getTodayMood();
        applyMoodSelection(todayMood);
    }

    private void openTasksAndShowAddSheet() {

        AddTaskBottomSheet bottomSheet = new AddTaskBottomSheet();

        bottomSheet.setOnTaskSavedListener((task, isEditMode) -> {

            if (isEditMode) {
                taskFacade.updateTask(task);
            } else {
                taskFacade.addTask(task);
            }

            requireActivity().runOnUiThread(() -> {
                refreshTasks();

                // بعد از ذخیره، حالا می‌تونی بروی تب تسک‌ها
                if (getActivity() instanceof DashboardActivity) {
                    ((DashboardActivity) getActivity()).openTasksTab();
                }
            });
        });

        bottomSheet.show(requireActivity().getSupportFragmentManager(), "AddTaskBottomSheet");
    }



}
