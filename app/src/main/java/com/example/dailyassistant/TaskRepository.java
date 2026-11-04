package com.example.dailyassistant;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson; // اگر نداری، باید اضافه کنی یا از همون روش JSONArray دایری استفاده کنی
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private static final String PREF_NAME = "TaskPreferences";
    private static final String KEY_TASKS = "task_list";
    private final SharedPreferences prefs;
    private final Gson gson; // برای تبدیل آبجکت به رشته و برعکس

    public TaskRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // متد جدید: خواندن از حافظه
    public List<Task> getTasks() {
        String json = prefs.getString(KEY_TASKS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<ArrayList<Task>>(){}.getType();
        return gson.fromJson(json, type);
    }

    // متد جدید: ذخیره در حافظه
    private void saveTasks(List<Task> tasks) {
        String json = gson.toJson(tasks);
        prefs.edit().putString(KEY_TASKS, json).apply();
    }

    public void saveTasksFromFacade(List<Task> tasks) {
        saveTasks(tasks);
    }

    public void addTask(Task newTask) {
        List<Task> tasks = getTasks();
        tasks.add(newTask);
        saveTasks(tasks);
    }

    public void updateTask(Task updatedTask) {
        List<Task> tasks = getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                break;
            }
        }
        saveTasks(tasks);
    }

    public void updateTaskStatus(String taskId, boolean isDone) {
        List<Task> tasks = getTasks();
        for (Task task : tasks) {
            if (task.getId().equals(taskId)) {
                task.setDone(isDone);
                break;
            }
        }
        saveTasks(tasks);
    }

    public void deleteTask(String taskId) {
        List<Task> tasks = getTasks();
        tasks.removeIf(task -> task.getId().equals(taskId));
        saveTasks(tasks);
    }
}
