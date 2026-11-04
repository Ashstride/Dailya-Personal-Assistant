package com.example.dailyassistant;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TaskManagerFragment extends Fragment {

    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private EditText etSearchTasks;
    private TextView filterAll, filterPending, filterDone, filterOverdue;
    private TaskManagerFacade taskFacade;
    private View layoutEmptyState;
    private View btnEmptyAddTask;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_manager, container, false);

        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        btnEmptyAddTask = view.findViewById(R.id.btnEmptyAddTask);

        btnEmptyAddTask.setOnClickListener(v -> showAddTaskDialog(null));


        taskFacade = TaskManagerFacade.getInstance(getContext());

        rvTasks = view.findViewById(R.id.rvTasksFragment);
        etSearchTasks = view.findViewById(R.id.etSearchTasks);

        filterAll = view.findViewById(R.id.filterAll);
        filterPending = view.findViewById(R.id.filterPending);
        filterDone = view.findViewById(R.id.filterDone);
        filterOverdue = view.findViewById(R.id.filterOverdue);

        View btnAddTask = view.findViewById(R.id.btnAddTask);
        if (btnAddTask != null) {
            btnAddTask.setOnClickListener(v -> showAddTaskDialog(null));
        }

        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));

        // دریافت لیست از طریق لایه Facade
        adapter = new TaskAdapter.Builder()
                .setTasks(taskFacade.getTasks())
                .setLimited(false)
                .build();
        rvTasks.setAdapter(adapter);

        // مدیریت تغییر وضعیت چک‌باکس تسک
        adapter.setOnTaskStatusChangeListener((task, isDone, position) -> {
            taskFacade.updateTaskStatus(task.getId(), isDone);
            adapter.updateData(taskFacade.getTasks());
        });

        // مدیریت کلیک‌های Long Press برای ویرایش و حذف
        adapter.setOnTaskActionListener(new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onDelete(Task task, int position) {
                Dialog dialog = new Dialog(requireContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                View dialogView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.dialog_delete_task, null, false);

                dialog.setContentView(dialogView);
                dialog.setCancelable(true);

                TextView btnCancel = dialogView.findViewById(R.id.btnCancelDelete);
                TextView btnConfirm = dialogView.findViewById(R.id.btnConfirmDelete);

                btnCancel.setOnClickListener(v -> dialog.dismiss());

                btnConfirm.setOnClickListener(v -> {
                    dialog.dismiss();
                    taskFacade.deleteTask(task.getId());
                    adapter.updateData(taskFacade.getTasks());
                    if (adapter.getItemCount() == 0) {
                        rvTasks.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        rvTasks.setVisibility(View.VISIBLE);
                        layoutEmptyState.setVisibility(View.GONE);
                    }
                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                });

                dialog.show();

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                    params.copyFrom(window.getAttributes());
                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.dimAmount = 0.35f;

                    window.setAttributes(params);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                }
            }



            @Override
            public void onEdit(Task task, int position) {
                // باز کردن دیالوگ در حالت ویرایش تسک
                showAddTaskDialog(task);
            }
        });

        setupSearch();
        setupFilters();

        return view;
    }

    // متد دیالوگ که هم برای اضافه کردن (پاس دادن null) و هم ویرایش (پاس دادن شیء تسک) استفاده می‌شود
    private void showAddTaskDialog(@Nullable Task task) {
        AddTaskBottomSheet bottomSheet;
        if (task != null) {
            bottomSheet = AddTaskBottomSheet.newInstance(task);
        } else {
            bottomSheet = new AddTaskBottomSheet();
        }

        bottomSheet.setOnTaskSavedListener((savedTask, isEditMode) -> {
            if (isEditMode) {
                // ۱. ویرایش تسک موجود در Facade
                taskFacade.updateTask(savedTask);
            } else {
                // ۲. اضافه کردن تسک جدید به Facade
                taskFacade.addTask(savedTask);
            }

            // ۳. آپدیت لیست در UI
            if (adapter != null) {
                adapter.updateData(taskFacade.getTasks());
                if (adapter.getItemCount() == 0) {
                    rvTasks.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    rvTasks.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                }

            }
        });
        bottomSheet.show(getChildFragmentManager(), "AddTaskBottomSheet");
    }

    private void setupFilters() {
        filterAll.setOnClickListener(v -> handleFilterClick(filterAll, "ALL"));
        filterPending.setOnClickListener(v -> handleFilterClick(filterPending, "PENDING"));
        filterDone.setOnClickListener(v -> handleFilterClick(filterDone, "DONE"));
        filterOverdue.setOnClickListener(v -> handleFilterClick(filterOverdue, "OVERDUE"));

        handleFilterClick(filterAll, "ALL");
    }

    private void handleFilterClick(TextView selectedView, String status) {
        filterAll.setSelected(false);
        filterPending.setSelected(false);
        filterDone.setSelected(false);
        filterOverdue.setSelected(false);

        selectedView.setSelected(true);

        if (adapter != null) {
            adapter.filterByStatus(status);

            // بررسی خالی بودن لیست
            if (adapter.getItemCount() == 0) {
                rvTasks.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                rvTasks.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);
            }
        }
    }


    private void setupSearch() {
        if (etSearchTasks == null) return;

        etSearchTasks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());

                    if (adapter.getItemCount() == 0) {
                        rvTasks.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        rvTasks.setVisibility(View.VISIBLE);
                        layoutEmptyState.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearchTasks.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                if (adapter != null) {
                    adapter.filter(etSearchTasks.getText().toString());
                }
                hideKeyboard();
                etSearchTasks.clearFocus();
                return true;
            }
            return false;
        });
    }

    private void hideKeyboard() {
        if (getActivity() != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }
}
