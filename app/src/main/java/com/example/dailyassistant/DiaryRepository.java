package com.example.dailyassistant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DiaryRepository {
    private final ArrayList<DiaryEntry> entries;
    private final ArrayList<MoodRecord> moods;

    public DiaryRepository() {
        entries = new ArrayList<>();
        moods = new ArrayList<>();

        // Monday, Jul 20
        entries.add(new DiaryEntry(
                "4",
                "A Calm Monday",
                "I had a calm and productive day. I finished my tasks and had some time to relax.",
                "Jul 20, 2026",
                "good",
                false,
                ""
        ));

        entries.add(new DiaryEntry(
                "1",
                "Great Day",
                "Today was productive and calm.",
                "Jul 18, 2026",
                "happy",
                false,
                ""
        ));

        entries.add(new DiaryEntry(
                "2",
                "Peaceful",
                "I had a peaceful morning and a quiet evening.",
                "Jul 17, 2026",
                "good",
                false,
                ""
        ));

        entries.add(new DiaryEntry(
                "3",
                "Busy Day",
                "A lot happened today, but I handled it.",
                "Jul 16, 2026",
                "neutral",
                false,
                ""
        ));

        // Mood مربوط به دوشنبه برای تقویم هفتگی
        saveMoodForDate("Jul 20, 2026", "good");
    }

    public List<DiaryEntry> getAllEntries() {
        return new ArrayList<>(entries);
    }

    public void addEntry(DiaryEntry entry) {
        entries.add(0, entry);
    }

    public void updateEntry(DiaryEntry updatedEntry) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(updatedEntry.getId())) {
                entries.set(i, updatedEntry);
                return;
            }
        }
    }

    public void deleteEntry(String id) {
        Iterator<DiaryEntry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(id)) {
                iterator.remove();
                return;
            }
        }
    }

    public DiaryEntry findById(String id) {
        for (DiaryEntry entry : entries) {
            if (entry.getId().equals(id)) return entry;
        }
        return null;
    }

    public List<DiaryEntry> getEntriesByDate(String date) {
        ArrayList<DiaryEntry> result = new ArrayList<>();
        for (DiaryEntry entry : entries) {
            if (entry.getDate().equals(date)) {
                result.add(entry);
            }
        }
        return result;
    }

    public List<DiaryEntry> search(String query) {
        ArrayList<DiaryEntry> result = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return getAllEntries();
        }

        String lowerQuery = query.trim().toLowerCase(java.util.Locale.ROOT);

        for (DiaryEntry entry : entries) {

            String title = entry.getTitle() == null
                    ? ""
                    : entry.getTitle().toLowerCase(java.util.Locale.ROOT);

            String content = entry.getContent() == null
                    ? ""
                    : entry.getContent().toLowerCase(java.util.Locale.ROOT);

            String date = entry.getDate() == null
                    ? ""
                    : entry.getDate().toLowerCase(java.util.Locale.ROOT);

            String mood = entry.getMood() == null
                    ? ""
                    : entry.getMood().toLowerCase(java.util.Locale.ROOT);

            String reflectionQuestion = entry.getReflectionQuestion() == null
                    ? ""
                    : entry.getReflectionQuestion().toLowerCase(java.util.Locale.ROOT);

            if (title.contains(lowerQuery)
                    || content.contains(lowerQuery)
                    || date.contains(lowerQuery)
                    || mood.contains(lowerQuery)
                    || reflectionQuestion.contains(lowerQuery)) {

                result.add(entry);
            }
        }

        return result;
    }

    public void saveMoodForDate(String date, String mood) {
        for (MoodRecord record : moods) {
            if (record.date.equals(date)) {
                record.mood = mood;
                return;
            }
        }
        moods.add(new MoodRecord(date, mood));
    }

    public String getMoodByDate(String date) {
        for (MoodRecord record : moods) {
            if (record.date.equals(date)) {
                return record.mood;
            }
        }
        return null;
    }

    public void saveTodayMood(String mood) {
        saveMoodForDate(getTodayDate(), mood);
    }

    public String getTodayMood() {
        return getMoodByDate(getTodayDate());
    }

    private String getTodayDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US);
        return sdf.format(new java.util.Date());
    }

    private static class MoodRecord {
        String date;
        String mood;

        MoodRecord(String date, String mood) {
            this.date = date;
            this.mood = mood;
        }
    }
}
