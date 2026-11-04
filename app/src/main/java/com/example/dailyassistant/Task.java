package com.example.dailyassistant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task implements Cloneable {
    private String id;
    private String title;
    private String dueDate;
    private int priority;
    private boolean isDone;
    private String time;
    private boolean isReminderActive;
    private int colorIndex = -1;


    private Task(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.dueDate = builder.dueDate;
        this.priority = builder.priority;
        this.time = builder.time;
        this.isDone = builder.isDone;
        this.isReminderActive = builder.isReminderActive;
        this.colorIndex = builder.colorIndex;
    }

    @Override
    public Task clone() {
        try {
            Task cloned = (Task) super.clone();
            cloned.colorIndex = this.colorIndex;   // ← مهم‌ترین خط
            return cloned;
        } catch (CloneNotSupportedException e) {
            return new Task.Builder()
                    .setId(this.id)
                    .setTitle(this.title)
                    .setDueDate(this.dueDate)
                    .setPriority(this.priority)
                    .setTime(this.time)
                    .setDone(this.isDone)
                    .setReminderActive(this.isReminderActive)
                    .build();
        }
    }


    public boolean hasDisplayableMeta() {
        return !safeTrim(dueDate).isEmpty() || !safeTrim(time).isEmpty();
    }

    public boolean shouldShowReminderIcon() {
        return isReminderActive && !safeTrim(time).isEmpty();
    }

    public boolean isOverdue() {
        if (isDone || safeTrim(dueDate).isEmpty() || safeTrim(time).isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat dateTimeParser = new SimpleDateFormat("d/M/yyyy h:mm a", Locale.US);
            dateTimeParser.setLenient(false);
            Date taskDateTime = dateTimeParser.parse(dueDate + " " + time);
            return taskDateTime != null && taskDateTime.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public static class Builder {
        private String id;
        private String title;
        private String dueDate;
        private int priority;
        private String time;
        private boolean isDone = false;
        private boolean isReminderActive = false;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDueDate(String dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder setTime(String time) {
            this.time = time;
            return this;
        }

        public Builder setReminderTime(String time) {
            this.time = time;
            return this;
        }

        public Builder setReminderActive(boolean active) {
            this.isReminderActive = active;
            return this;
        }

        private int colorIndex = -1;

        public Builder setDone(boolean done) {
            this.isDone = done;
            return this;
        }

        public Builder setColorIndex(int colorIndex) {
            this.colorIndex = colorIndex;
            return this;
        }

        public Task build() {
            if (id == null) id = "";
            if (title == null) title = "";
            if (dueDate == null) dueDate = "";
            if (time == null) time = "";
            return new Task(this);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? "" : id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate == null ? "" : dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTime() {
        return time == null ? "" : time;
    }

    public void setTime(String time) {
        this.time = time == null ? "" : time;
    }

    public boolean isReminderActive() {
        return isReminderActive;
    }

    public void setReminderActive(boolean reminderActive) {
        isReminderActive = reminderActive;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int index) {
        this.colorIndex = index;
    }

}
