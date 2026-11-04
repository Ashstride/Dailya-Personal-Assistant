package com.example.dailyassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;import java.text.ParseException;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DiaryManagerFacade {

    private static final String PREF_NAME = "DiaryPreferences";
    private static final String KEY_ENTRIES = "diary_entries";
    private static final String KEY_MOOD_PREFIX = "mood_";
    private static final String KEY_SAMPLE_DATA_INITIALIZED =
            "sample_data_initialized";
    private static final String KEY_REFLECTION_QUESTION_PREFIX =
            "reflection_question_";

    private static DiaryManagerFacade instance;
    private final SharedPreferences preferences;
    private final Context appContext;

    private static final String PREF_REFLECTION = "pref_reflection";

    private DiaryManagerFacade(Context context) {
        this.appContext = context.getApplicationContext();
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        initializeSampleDataIfNeeded();
    }


    public static synchronized DiaryManagerFacade getInstance(Context context) {
        if (instance == null) {
            instance = new DiaryManagerFacade(context);
        }
        return instance;
    }

    private String getTodayDate() {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
    }

    public String getDailyReflectionQuestion(Context context, String date) {
        String[] questions =
                context.getResources()
                        .getStringArray(R.array.daily_reflection_questions);

        if (questions.length == 0) {
            return "";
        }

        String keyForQuestion = KEY_REFLECTION_QUESTION_PREFIX + date;

        String savedQuestion = preferences.getString(keyForQuestion, null);

        if (!TextUtils.isEmpty(savedQuestion)) {
            return savedQuestion;
        }

        int selectedIndex;

        try {
            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("MMM dd, yyyy", Locale.US);

            Date parsedDate = dateFormat.parse(date);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);

            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
            int year = calendar.get(Calendar.YEAR);

            selectedIndex =
                    Math.abs(dayOfYear + (year * 12)) % questions.length;

        } catch (ParseException e) {
            selectedIndex = 0;
        }

        String selectedQuestion = questions[selectedIndex];

        preferences.edit()
                .putString(keyForQuestion, selectedQuestion)
                .apply();

        return selectedQuestion;
    }


    // ===============================
    // مدیریت Mood روزانه
    // ===============================

    public void saveTodayMood(String mood) {
        saveMoodForDate(getTodayDate(), mood);
    }

    public String getTodayMood() {
        return getMoodByDate(getTodayDate());
    }

    public void saveMoodForDate(String dateStr, String mood) {
        if (dateStr == null || mood == null) return;
        preferences.edit()
                .putString(KEY_MOOD_PREFIX + dateStr, mood)
                .apply();
    }

    public String getMoodByDate(String dateStr) {
        if (dateStr == null) {
            return null;
        }

        return preferences.getString(
                KEY_MOOD_PREFIX + dateStr,
                null
        );
    }

    // ===============================
    // مدیریت یادداشت‌ها (Diary Entries)
    // ===============================

    public synchronized List<DiaryEntry> getAllEntries() {
        List<DiaryEntry> entries = new ArrayList<>();
        String jsonString = preferences.getString(KEY_ENTRIES, "[]");

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);

                String savedMood = object.optString("mood");
                if (savedMood != null && savedMood.trim().isEmpty()) {
                    savedMood = null;
                }

                String savedReflectionQuestion =
                        object.optString("reflection_question");
                if (savedReflectionQuestion != null
                        && savedReflectionQuestion.trim().isEmpty()) {
                    savedReflectionQuestion = null;
                }

                DiaryEntry entry = new DiaryEntry(
                        object.optString("id"),
                        object.optString("title"),
                        object.optString("content"),
                        object.optString("date"),
                        savedMood,
                        object.optBoolean("favorite", false),
                        savedReflectionQuestion
                );

                entries.add(entry);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return entries;
    }

    public synchronized List<DiaryEntry> getRecentEntries() {
        List<DiaryEntry> list = getAllEntries();
        Collections.reverse(list);
        return list;
    }

    public synchronized List<DiaryEntry> getEntriesByDate(String dateStr) {
        List<DiaryEntry> filtered = new ArrayList<>();
        if (dateStr == null) return filtered;

        for (DiaryEntry entry : getAllEntries()) {
            if (entry != null && dateStr.equals(entry.getDate())) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public synchronized void saveEntry(
            String title,
            String content,
            String date,
            String mood,
            boolean favorite
    ) {
        saveEntry(title, content, date, mood, null, favorite);
    }

    public synchronized void saveEntry(
            String title,
            String content,
            String date,
            String mood,
            String reflectionQuestion,
            boolean favorite
    ) {
        DiaryEntry entry = new DiaryEntry(
                UUID.randomUUID().toString(),
                title,
                content,
                date,
                mood,
                favorite,
                reflectionQuestion
        );
        addEntry(entry);
    }

    public synchronized void addEntry(DiaryEntry entry) {
        if (entry == null) return;

        if (TextUtils.isEmpty(entry.getId())) {
            entry.setId(UUID.randomUUID().toString());
        }

        if (TextUtils.isEmpty(entry.getDate())) {
            entry.setDate(getTodayDate());
        }

        List<DiaryEntry> entries = getAllEntries();
        entries.add(entry);
        saveAllEntries(entries);
    }

    public synchronized void updateEntry(DiaryEntry updatedEntry) {
        if (updatedEntry == null || updatedEntry.getId() == null) return;

        List<DiaryEntry> entries = getAllEntries();
        for (int i = 0; i < entries.size(); i++) {
            if (updatedEntry.getId().equals(entries.get(i).getId())) {
                entries.set(i, updatedEntry);
                break;
            }
        }

        saveAllEntries(entries);
    }

    public synchronized void deleteEntry(String entryId) {
        if (entryId == null) return;

        List<DiaryEntry> entries = getAllEntries();
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (entryId.equals(entries.get(i).getId())) {
                entries.remove(i);
            }
        }

        saveAllEntries(entries);
    }

    public synchronized DiaryEntry getEntryById(String entryId) {
        if (entryId == null) return null;

        for (DiaryEntry entry : getAllEntries()) {
            if (entryId.equals(entry.getId())) {
                return entry;
            }
        }

        return null;
    }

    public synchronized List<DiaryEntry> searchEntries(String query) {
        List<DiaryEntry> results = new ArrayList<>();

        if (TextUtils.isEmpty(query)) {
            return getRecentEntries();
        }

        String lowerQuery = query.toLowerCase().trim();

        for (DiaryEntry entry : getAllEntries()) {
            if ((entry.getTitle() != null &&
                    entry.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (entry.getContent() != null &&
                            entry.getContent().toLowerCase().contains(lowerQuery))) {
                results.add(entry);
            }
        }

        Collections.reverse(results);
        return results;
    }

    private synchronized void initializeSampleDataIfNeeded() {
        boolean alreadyInitialized = preferences.getBoolean(
                KEY_SAMPLE_DATA_INITIALIZED,
                false
        );

        if (alreadyInitialized) {
            return;
        }

        List<DiaryEntry> existingEntries = getAllEntries();

        if (existingEntries.isEmpty()) {
            existingEntries.add(new DiaryEntry(
                    "sample_3",
                    "Busy Day",
                    "A lot happened today, but I handled it.",
                    "Jul 16, 2026",
                    "neutral",
                    false,
                    null
            ));

            existingEntries.add(new DiaryEntry(
                    "sample_2",
                    "Peaceful",
                    "I had a peaceful morning and a quiet evening.",
                    "Jul 17, 2026",
                    "good",
                    false,
                    null
            ));

            existingEntries.add(new DiaryEntry(
                    "sample_1",
                    "Great Day",
                    "Today was productive and calm.",
                    "Jul 18, 2026",
                    "happy",
                    false,
                    null
            ));

            existingEntries.add(new DiaryEntry(
                    "sample_4",
                    "A Calm Monday",
                    "I had a calm and productive day. I finished my tasks and had some time to relax.",
                    "Jul 20, 2026",
                    "good",
                    false,
                    null
            ));

            saveAllEntries(existingEntries);

            saveMoodForDate("Jul 20, 2026", "good");
        }

        preferences.edit()
                .putBoolean(KEY_SAMPLE_DATA_INITIALIZED, true)
                .apply();
    }

    private void saveAllEntries(List<DiaryEntry> entries) {
        JSONArray jsonArray = new JSONArray();

        for (DiaryEntry entry : entries) {
            try {
                JSONObject object = new JSONObject();
                object.put("id", entry.getId() == null ? "" : entry.getId());
                object.put("title", entry.getTitle() == null ? "" : entry.getTitle());
                object.put("content", entry.getContent() == null ? "" : entry.getContent());
                object.put("date", entry.getDate() == null ? "" : entry.getDate());
                object.put("mood", entry.getMood() == null ? "" : entry.getMood());
                object.put("favorite", entry.isFavorite());
                object.put(
                        "reflection_question",
                        entry.getReflectionQuestion() == null
                                ? ""
                                : entry.getReflectionQuestion()
                );
                jsonArray.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        preferences.edit()
                .putString(KEY_ENTRIES, jsonArray.toString())
                .apply();
    }

    public String getReflectionQuestionForDate(String date) {
        SharedPreferences prefs =
                appContext.getSharedPreferences(
                        PREF_REFLECTION,
                        Context.MODE_PRIVATE
                );

        String key = KEY_REFLECTION_QUESTION_PREFIX + date;
        return prefs.getString(key, null);
    }
    public void saveReflectionQuestionForDate(String date, String question) {
        SharedPreferences prefs =
                appContext.getSharedPreferences(PREF_REFLECTION, Context.MODE_PRIVATE);

        String key = KEY_REFLECTION_QUESTION_PREFIX + date;
        prefs.edit().putString(key, question).apply();
    }

}
