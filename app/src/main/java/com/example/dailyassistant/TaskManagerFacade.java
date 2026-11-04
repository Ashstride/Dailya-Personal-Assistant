package com.example.dailyassistant;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskManagerFacade {

    private static TaskManagerFacade instance;
    private final TaskRepository taskRepository;

    // Prototype Pattern
    private final Task prototypeMorningWalk;
    private final Task prototypeStudyEnglish;
    private final Task prototypeReadBook;
    private final Task prototypeBakeCake;
    private final Task prototypeGymSession;
    private final Task prototypeSubmitReport;
    private final Task prototypePlanTomorrow;
    private int colorCounter = 0;

    private TaskManagerFacade(Context context) {
        this.taskRepository = new TaskRepository(context);

        prototypeMorningWalk = new Task.Builder()
                .setId("PROTO_WALK")
                .setTitle("Morning Walk")
                .setPriority(1)
                .setReminderTime("8:00 AM")
                .setReminderActive(true)
                .setDone(false)
                .build();

        prototypeStudyEnglish = new Task.Builder()
                .setId("PROTO_ENGLISH")
                .setTitle("Study English")
                .setPriority(1)
                .setReminderTime("10:00 AM")
                .setReminderActive(true)
                .setDone(false)
                .build();

        prototypeReadBook = new Task.Builder()
                .setId("PROTO_BOOK")
                .setTitle("Read a Book")
                .setPriority(1)
                .setReminderTime(null)
                .setReminderActive(false)
                .setDone(false)
                .build();

        prototypeBakeCake = new Task.Builder()
                .setId("PROTO_CAKE")
                .setTitle("Bake a Cake")
                .setPriority(1)
                .setReminderTime("6:00 PM")
                .setReminderActive(true)
                .setDone(false)
                .build();

        prototypeGymSession = new Task.Builder()
                .setId("PROTO_GYM")
                .setTitle("Gym Session")
                .setPriority(1)
                .setReminderTime("7:30 PM")
                .setReminderActive(true)
                .setDone(false)
                .build();

        prototypeSubmitReport = new Task.Builder()
                .setId("PROTO_REPORT")
                .setTitle("Submit Report")
                .setPriority(2)
                .setReminderTime("9:00 AM")
                .setReminderActive(true)
                .setDone(false)
                .build();

        prototypePlanTomorrow = new Task.Builder()
                .setId("PROTO_PLAN")
                .setTitle("Plan Tomorrow")
                .setPriority(1)
                .setReminderTime("8:30 PM")
                .setReminderActive(true)
                .setDone(false)
                .build();

        seedInitialTasks();
    }

    public static synchronized TaskManagerFacade getInstance(Context context) {
        if (instance == null) {
            instance = new TaskManagerFacade(context);
        }
        return instance;
    }

    private void seedInitialTasks() {
        if (!taskRepository.getTasks().isEmpty()) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.US);

        String today = dateFormat.format(calendar.getTime());

        Calendar yesterdayCalendar = (Calendar) calendar.clone();
        yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = dateFormat.format(yesterdayCalendar.getTime());

        Calendar tomorrowCalendar = (Calendar) calendar.clone();
        tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = dateFormat.format(tomorrowCalendar.getTime());

        Task t1 = prototypeMorningWalk.clone();
        t1.setId("1");
        t1.setDueDate(today);

        Task t2 = prototypeStudyEnglish.clone();
        t2.setId("2");
        t2.setDueDate(today);

        Task t3 = prototypeReadBook.clone();
        t3.setId("3");
        t3.setDueDate(today);

        Task t4 = prototypeBakeCake.clone();
        t4.setId("4");
        t4.setDueDate(today);

        Task t5 = prototypeGymSession.clone();
        t5.setId("5");
        t5.setDueDate(today);

        Task t6 = prototypeSubmitReport.clone();
        t6.setId("6");
        t6.setDueDate(yesterday);

        Task t7 = prototypePlanTomorrow.clone();
        t7.setId("7");
        t7.setDueDate(tomorrow);

        taskRepository.addTask(t1);
        taskRepository.addTask(t2);
        taskRepository.addTask(t3);
        taskRepository.addTask(t4);
        taskRepository.addTask(t5);
        taskRepository.addTask(t6);
        taskRepository.addTask(t7);
    }

    public List<Task> getTasks() {

        List<Task> originalList = taskRepository.getTasks();

        boolean needsSave = false;

        for (int i = 0; i < originalList.size(); i++) {

            Task task = originalList.get(i);

            if (task.getColorIndex() < 0) {

                task.setColorIndex(i % 5);
                needsSave = true;
            }
        }


        if (needsSave) {
            taskRepository.saveTasksFromFacade(originalList);
        }


        List<Task> reversedList = new ArrayList<>();

        for (int i = originalList.size() - 1; i >= 0; i--) {

            reversedList.add(originalList.get(i).clone());
        }


        return reversedList;
    }

    public void addTask(Task newTask) {
        if (newTask != null) {

            if (newTask.getColorIndex() < 0) {
                newTask.setColorIndex(getNextColorIndex());
            }

            taskRepository.addTask(newTask);
        }
    }

    public void updateTask(Task updatedTask) {
        if (updatedTask != null) {
            taskRepository.updateTask(updatedTask);
        }
    }

    public void updateTaskStatus(String taskId, boolean isDone) {
        taskRepository.updateTaskStatus(taskId, isDone);
    }

    public void deleteTask(String taskId) {
        taskRepository.deleteTask(taskId);
    }

    public int getNextColorIndex() {
        return taskRepository.getTasks().size() % 5;
    }

}
