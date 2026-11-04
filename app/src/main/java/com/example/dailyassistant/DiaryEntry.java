package com.example.dailyassistant;

public class DiaryEntry {
    private String id;
    private String title;
    private String content;
    private String date;
    private String mood;
    private boolean favorite;
    private String reflectionQuestion; // فیلد جدید

    public DiaryEntry(String id, String title, String content, String date, String mood, boolean favorite, String reflectionQuestion) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.mood = mood;
        this.favorite = favorite;
        this.reflectionQuestion = reflectionQuestion; // مقداردهی فیلد جدید
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    // متدهای جدید برای فیلد سؤال
    public String getReflectionQuestion() { return reflectionQuestion; }
    public void setReflectionQuestion(String reflectionQuestion) { this.reflectionQuestion = reflectionQuestion; }
}
