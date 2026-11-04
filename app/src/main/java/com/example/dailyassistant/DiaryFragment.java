package com.example.dailyassistant;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import android.graphics.Color;


public class DiaryFragment extends Fragment {

    private DiaryManagerFacade facade;
    private DiaryAdapter diaryAdapter;
    private CalendarAdapter weekAdapter;

    private RecyclerView rvWeekDays;
    private RecyclerView rvRecentEntries;

    private EditText etDiaryContent;
    private EditText etDiaryTitle;
    private EditText etDiarySearch;

    private TextView btnWriteEntry;
    private TextView tvMonthYear;
    private TextView tvEntriesTitle;
    private TextView tvDiaryEmptySearch;
    private TextView tvReflectionQuestionLabel;

    private View btnOpenCalendar;
    private View layoutDiarySearch;
    private View cardWeeklyCalendar;
    private View cardDiaryReflection;
    private View btnExpandDiary;

    private ImageButton btnDiarySearch;
    private ImageButton btnCloseDiarySearch;

    private String selectedMood = null;

    /**
     * تاریخ مخصوص Calendar selection
     * فرمت: yyyy-MM-dd
     */
    private String selectedCalendarDate;

    /**
     * تاریخ مخصوص ذخیره/بازیابی Diary
     * فرمت: MMM dd, yyyy
     */
    private String selectedDiaryDate;

    private boolean isSearchMode = false;
    private boolean receiverRegistered = false;

    private static final String PREF_REFLECTION = "pref_reflection";
    private static final String KEY_LAST_DATE = "last_date";
    private static final String KEY_LAST_INDEX = "last_index";
    private static final String KEY_REFLECTION_QUESTION_PREFIX = "reflection_question_";

    private static final String ACTION_DIARY_SYNC =
            "com.example.dailyassistant.action.DIARY_SYNC";

    private static final SimpleDateFormat DB_DATE_FORMAT =
            new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    private static final SimpleDateFormat CALENDAR_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private ActivityResultLauncher<Intent> editorLauncher;
    private TextView tabMyDiary, tabMemories;


    private final BroadcastReceiver diarySyncReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (!ACTION_DIARY_SYNC.equals(intent.getAction())) {
                        return;
                    }

                    String title = intent.getStringExtra("UPDATED_TITLE");
                    String content = intent.getStringExtra("UPDATED_CONTENT");
                    String mood = intent.getStringExtra("UPDATED_MOOD");
                    String question = intent.getStringExtra("UPDATED_QUESTION");

                    if (title != null && etDiaryTitle != null) {
                        etDiaryTitle.setText(title);
                    }

                    if (content != null && etDiaryContent != null) {
                        etDiaryContent.setText(content);
                    }

                    if (getView() != null) {
                        selectedMood = mood;
                        updateMoodSelection(mood);
                    }

                    if (question != null && tvReflectionQuestionLabel != null) {
                        tvReflectionQuestionLabel.setText(question);
                    }
                }
            };

    public DiaryFragment() {
    }
    private View layoutMemoryCapsule;


    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_diary,
                container,
                false
        );
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        editorLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) {
                        return;
                    }

                    Intent data = result.getData();

                    if (data == null) {
                        return;
                    }

                    String title = data.getStringExtra("UPDATED_TITLE");
                    String content = data.getStringExtra("UPDATED_CONTENT");
                    String mood = data.getStringExtra("UPDATED_MOOD");
                    String question = data.getStringExtra("UPDATED_QUESTION");

                    String safeTitle = title == null ? "" : title;
                    String safeContent = content == null ? "" : content;

                    boolean emptyResult =
                            safeTitle.trim().isEmpty()
                                    && safeContent.trim().isEmpty();

                    if (emptyResult) {
                        return;
                    }

                    if (etDiaryTitle != null) {
                        etDiaryTitle.setText(safeTitle);
                    }

                    if (etDiaryContent != null) {
                        etDiaryContent.setText(safeContent);
                    }

                    selectedMood = mood;
                    updateMoodSelection(mood);

                    if (question != null && tvReflectionQuestionLabel != null) {
                        tvReflectionQuestionLabel.setText(question);
                    }

                    boolean silentSave =
                            data.getBooleanExtra(
                                    "SILENT_SAVE",
                                    false
                            );

                    saveEntry(!silentSave);

                    loadDiaryForSelectedDate();
                    refreshRecentEntries();
                    setupWeekCalendar();
                }
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        facade = DiaryManagerFacade.getInstance(requireContext());

        rvWeekDays = view.findViewById(R.id.rvWeekDays);
        rvRecentEntries = view.findViewById(R.id.rvRecentEntries);

        etDiaryTitle = view.findViewById(R.id.etDiaryTitle);
        etDiaryContent = view.findViewById(R.id.etDiaryContent);
        etDiarySearch = view.findViewById(R.id.etDiarySearch);

        btnWriteEntry = view.findViewById(R.id.btnWriteEntry);
        btnOpenCalendar = view.findViewById(R.id.btnOpenCalendar);
        btnExpandDiary = view.findViewById(R.id.btnExpandDiary);

        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        tvEntriesTitle = view.findViewById(R.id.tvEntriesTitle);
        tvDiaryEmptySearch = view.findViewById(R.id.tvDiaryEmptySearch);
        tvReflectionQuestionLabel =
                view.findViewById(R.id.tvReflectionQuestionLabel);

        layoutDiarySearch = view.findViewById(R.id.layoutDiarySearch);
        cardWeeklyCalendar = view.findViewById(R.id.cardWeeklyCalendar);
        cardDiaryReflection = view.findViewById(R.id.cardDiaryReflection);

        layoutMemoryCapsule = view.findViewById(R.id.layoutMemoryCapsule);




        setSelectedDateToToday();

        setupWeekCalendar();
        setupRecentEntries();
        setupMoodBar(view);
        setupDailyReflection();

        btnWriteEntry.setOnClickListener(v -> saveEntry());
        btnOpenCalendar.setOnClickListener(v -> openCalendar());

        tabMyDiary = view.findViewById(R.id.tabMyDiary);
        tabMemories = view.findViewById(R.id.tabMemories);

        tabMyDiary.setOnClickListener(v -> switchToMyDiary());
        tabMemories.setOnClickListener(v -> switchToMemories());
        switchToMyDiary();


        if (btnExpandDiary != null) {
            btnExpandDiary.setOnClickListener(v -> openFullScreenEditor());
        }

        etDiarySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                searchDiaryEntries(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        loadDiaryForSelectedDate();
    }

    private void setSelectedDateToToday() {
        Date today = new Date();
        selectedDiaryDate = DB_DATE_FORMAT.format(today);
        selectedCalendarDate = CALENDAR_DATE_FORMAT.format(today);
    }

    private void openFullScreenEditor() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        Intent intent = new Intent(
                requireContext(),
                DiaryFullEditorActivity.class
        );

        intent.putExtra(
                "DIARY_TITLE",
                etDiaryTitle == null
                        ? ""
                        : etDiaryTitle.getText().toString()
        );

        intent.putExtra(
                "DIARY_CONTENT",
                etDiaryContent == null
                        ? ""
                        : etDiaryContent.getText().toString()
        );

        intent.putExtra(
                "DIARY_MOOD",
                selectedMood
        );

        intent.putExtra(
                "DAILY_QUESTION",
                tvReflectionQuestionLabel == null
                        ? ""
                        : tvReflectionQuestionLabel
                        .getText()
                        .toString()
        );

        editorLauncher.launch(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (receiverRegistered || getContext() == null) {
            return;
        }

        IntentFilter filter =
                new IntentFilter(ACTION_DIARY_SYNC);

        ContextCompat.registerReceiver(
                requireContext(),
                diarySyncReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        receiverRegistered = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        Context context = getContext();

        if (context != null && receiverRegistered) {
            try {
                context.unregisterReceiver(diarySyncReceiver);
            } catch (IllegalArgumentException ignored) {
            }

            receiverRegistered = false;
        }
    }

    private void setupDailyReflection() {
        if (tvReflectionQuestionLabel == null || facade == null) {
            return;
        }

        SharedPreferences prefs =
                requireContext().getSharedPreferences(
                        PREF_REFLECTION,
                        Context.MODE_PRIVATE
                );

        String storedQuestion =
                prefs.getString(
                        KEY_REFLECTION_QUESTION_PREFIX + selectedDiaryDate,
                        null
                );

        if (!TextUtils.isEmpty(storedQuestion)) {
            tvReflectionQuestionLabel.setText(storedQuestion);
            return;
        }

        String[] questions =
                getResources().getStringArray(
                        R.array.daily_reflection_questions
                );

        if (questions.length == 0) {
            tvReflectionQuestionLabel.setText("");
            return;
        }

        String today = DB_DATE_FORMAT.format(new Date());

        int selectedIndex;

        if (selectedDiaryDate.equals(today)) {
            String savedDate = prefs.getString(KEY_LAST_DATE, "");
            int savedIndex = prefs.getInt(KEY_LAST_INDEX, -1);

            if (today.equals(savedDate)
                    && savedIndex >= 0
                    && savedIndex < questions.length) {
                selectedIndex = savedIndex;
            } else {
                Random random = new Random();
                if (questions.length > 1) {
                    do {
                        selectedIndex = random.nextInt(questions.length);
                    } while (selectedIndex == savedIndex);
                } else {
                    selectedIndex = 0;
                }

                prefs.edit()
                        .putString(KEY_LAST_DATE, today)
                        .putInt(KEY_LAST_INDEX, selectedIndex)
                        .apply();
            }
        } else {
            try {
                Date date = DB_DATE_FORMAT.parse(selectedDiaryDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
                int year = cal.get(Calendar.YEAR);

                selectedIndex = (dayOfYear + (year * 12)) % questions.length;

            } catch (ParseException e) {
                e.printStackTrace();
                selectedIndex = 0;
            }
        }

        String selectedQuestion = questions[selectedIndex];

        tvReflectionQuestionLabel.setText(selectedQuestion);
        facade.saveReflectionQuestionForDate(selectedDiaryDate, selectedQuestion);


        prefs.edit()
                .putString(
                        KEY_REFLECTION_QUESTION_PREFIX + selectedDiaryDate,
                        selectedQuestion
                )
                .apply();
    }

    private void setupWeekCalendar() {
        Calendar today = Calendar.getInstance();

        List<CalendarDayItem> days = new ArrayList<>();

        int todayIndex = 3;

        SimpleDateFormat dayNameFormat =
                new SimpleDateFormat("EEE", Locale.US);

        SimpleDateFormat dayNumberFormat =
                new SimpleDateFormat("d", Locale.US);

        for (int i = 0; i < 7; i++) {
            Calendar currentDay = (Calendar) today.clone();

            currentDay.add(Calendar.DAY_OF_MONTH, i - todayIndex);

            String dayName =
                    dayNameFormat.format(currentDay.getTime());

            String dayNumber =
                    dayNumberFormat.format(currentDay.getTime());

            String fullDate =
                    CALENDAR_DATE_FORMAT.format(currentDay.getTime());

            boolean isToday = i == todayIndex;

            boolean inCurrentMonth =
                    currentDay.get(Calendar.MONTH)
                            == today.get(Calendar.MONTH)
                            && currentDay.get(Calendar.YEAR)
                            == today.get(Calendar.YEAR);

            String databaseDate = convertDateFormat(fullDate);

            List<DiaryEntry> entries =
                    facade.getEntriesByDate(databaseDate);

            boolean hasDiary =
                    entries != null && !entries.isEmpty();

            days.add(
                    new CalendarDayItem(
                            dayName,
                            dayNumber,
                            fullDate,
                            inCurrentMonth,
                            isToday,
                            hasDiary
                    )
            );
        }

        weekAdapter = new CalendarAdapter(
                days,
                day -> {
                    selectedCalendarDate = day.getFullDate();
                    selectedDiaryDate = convertDateFormat(day.getFullDate());
                    loadDiaryForSelectedDate();
                    setupWeekCalendar();
                }
        );

        rvWeekDays.setLayoutManager(
                new GridLayoutManager(
                        requireContext(),
                        7
                )
        );

        rvWeekDays.setAdapter(weekAdapter);

        String monthYear =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.US
                ).format(today.getTime());

        tvMonthYear.setText(monthYear);

        weekAdapter.setSelected(selectedCalendarDate);
    }

    private void loadDiaryForSelectedDate() {
        List<DiaryEntry> entries =
                facade.getEntriesByDate(selectedDiaryDate);

        if (entries != null && !entries.isEmpty()) {
            DiaryEntry existingEntry = entries.get(0);

            etDiaryTitle.setText(existingEntry.getTitle());
            etDiaryContent.setText(existingEntry.getContent());

            selectedMood = existingEntry.getMood();
            updateMoodSelection(selectedMood);

        } else {
            etDiaryTitle.setText("");
            etDiaryContent.setText("");

            selectedMood = facade.getMoodByDate(selectedDiaryDate);
            updateMoodSelection(selectedMood);
        }

        setupDailyReflection();
    }

    private void saveEntry() {
        saveEntry(true);
    }

    private void saveEntry(boolean showToast) {
        String title = etDiaryTitle.getText().toString().trim();
        String content = etDiaryContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etDiaryTitle.setError("Diary title cannot be empty");
            etDiaryTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(content)) {
            etDiaryContent.setError("Diary content cannot be empty");
            etDiaryContent.requestFocus();
            return;
        }

        String reflectionQuestion =
                tvReflectionQuestionLabel == null
                        ? ""
                        : tvReflectionQuestionLabel.getText().toString();

        List<DiaryEntry> entries =
                facade.getEntriesByDate(selectedDiaryDate);

        if (entries != null && !entries.isEmpty()) {
            DiaryEntry existingEntry = entries.get(0);

            existingEntry.setTitle(title);
            existingEntry.setContent(content);
            existingEntry.setMood(selectedMood);
            existingEntry.setReflectionQuestion(reflectionQuestion);

            facade.updateEntry(existingEntry);

            if (showToast) {
                Toast.makeText(
                        requireContext(),
                        "Diary updated",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } else {
            facade.saveEntry(
                    title,
                    content,
                    selectedDiaryDate,
                    selectedMood,
                    reflectionQuestion,
                    false
            );

            if (showToast) {
                Toast.makeText(
                        requireContext(),
                        "Diary saved",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        refreshRecentEntries();
        setupWeekCalendar();
        updateRecentMemoriesTitle();

    }

    private void updateMoodSelection(String mood) {
        if (getView() == null) {
            return;
        }

        ImageView moodHappy =
                getView().findViewById(R.id.ivMoodHappy);

        ImageView moodGood =
                getView().findViewById(R.id.ivMoodGood);

        ImageView moodNeutral =
                getView().findViewById(R.id.ivMoodNeutral);

        ImageView moodSad =
                getView().findViewById(R.id.ivMoodSad);

        ImageView moodMiserable =
                getView().findViewById(R.id.ivMoodMiserable);

        ImageView[] moods = {
                moodHappy,
                moodGood,
                moodNeutral,
                moodSad,
                moodMiserable
        };

        resetAllMoods(moods);

        if (mood == null || mood.trim().isEmpty()) {
            selectedMood = null;
            return;
        }

        selectedMood = mood;

        switch (mood) {
            case "happy":
                moodHappy.setImageResource(
                        R.drawable.ic_mood_happy_act
                );
                moodHappy.setAlpha(1f);
                break;

            case "good":
                moodGood.setImageResource(
                        R.drawable.ic_mood_good_act
                );
                moodGood.setAlpha(1f);
                break;

            case "sad":
                moodSad.setImageResource(
                        R.drawable.ic_mood_sad_act
                );
                moodSad.setAlpha(1f);
                break;

            case "miserable":
                moodMiserable.setImageResource(
                        R.drawable.ic_mood_miserable_act
                );
                moodMiserable.setAlpha(1f);
                break;

            case "neutral":
                moodNeutral.setImageResource(
                        R.drawable.ic_mood_neutral_act
                );
                moodNeutral.setAlpha(1f);
                break;
        }
    }

    private void setupRecentEntries() {
        ArrayList<DiaryEntry> entries =
                new ArrayList<>(
                        facade.getRecentEntries()
                );

        diaryAdapter = new DiaryAdapter(
                requireContext(),
                entries,
                entry -> {
                    DiaryDetailDialog dialog =
                            DiaryDetailDialog.newInstance(
                                    entry,
                                    new DiaryDetailDialog
                                            .OnDiaryActionListener() {

                                        @Override
                                        public void onUpdated(
                                                DiaryEntry updatedEntry
                                        ) {
                                            refreshRecentEntries();

                                            if (updatedEntry
                                                    .getDate()
                                                    .equals(
                                                            selectedDiaryDate
                                                    )) {

                                                etDiaryTitle.setText(
                                                        updatedEntry
                                                                .getTitle()
                                                );

                                                etDiaryContent.setText(
                                                        updatedEntry
                                                                .getContent()
                                                );

                                                selectedMood =
                                                        updatedEntry
                                                                .getMood();

                                                updateMoodSelection(
                                                        updatedEntry
                                                                .getMood()
                                                );

                                                if (!TextUtils.isEmpty(
                                                        updatedEntry.getReflectionQuestion()
                                                )) {
                                                    tvReflectionQuestionLabel.setText(
                                                            updatedEntry.getReflectionQuestion()
                                                    );
                                                }
                                            }
                                        }

                                        @Override
                                        public void onDelete(
                                                DiaryEntry deletedEntry
                                        ) {
                                            facade.deleteEntry(
                                                    deletedEntry.getId()
                                            );

                                            refreshRecentEntries();

                                            Toast.makeText(
                                                    requireContext(),
                                                    "Entry deleted",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                            updateRecentMemoriesTitle();

                                        }
                                    }
                            );

                    dialog.show(
                            getParentFragmentManager(),
                            "DiaryDetail"
                    );
                }
        );

        rvRecentEntries.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        rvRecentEntries.setAdapter(diaryAdapter);
    }

    private void refreshRecentEntries() {
        if (diaryAdapter != null) {
            diaryAdapter.setEntries(
                    new ArrayList<>(
                            facade.getRecentEntries()
                    )
            );
        } else {
            setupRecentEntries();
        }
    }

    private void openCalendar() {
        Intent intent = new Intent(
                requireContext(),
                CalendarActivity.class
        );

        startActivity(intent);
    }

    private void openSearchSheet() {
        if (isSearchMode) {
            etDiarySearch.requestFocus();
            showKeyboard(etDiarySearch);
            return;
        }

        isSearchMode = true;

        btnDiarySearch.setVisibility(View.GONE);
        layoutDiarySearch.setVisibility(View.VISIBLE);

        cardWeeklyCalendar.setVisibility(View.GONE);
        cardDiaryReflection.setVisibility(View.GONE);

        tvEntriesTitle.setText("Search Results");

        showSearchResults(
                facade.getRecentEntries()
        );

        etDiarySearch.requestFocus();

        etDiarySearch.postDelayed(
                () -> showKeyboard(etDiarySearch),
                150
        );
    }

    private void showSearchResults(List<DiaryEntry> results) {
        ArrayList<DiaryEntry> resultList =
                new ArrayList<>();

        if (results != null) {
            resultList.addAll(results);
        }

        if (diaryAdapter == null) {
            setupRecentEntries();
        }

        diaryAdapter.setEntries(resultList);

        boolean isEmpty = resultList.isEmpty();

        tvDiaryEmptySearch.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );

        rvRecentEntries.setVisibility(
                isEmpty ? View.GONE : View.VISIBLE
        );

        if (isSearchMode) {
            tvEntriesTitle.setText(
                    isEmpty
                            ? "Search Results"
                            : "Search Results ("
                              + resultList.size()
                              + ")"
            );
        }
    }

    private void closeSearchMode() {
        isSearchMode = false;

        etDiarySearch.setText("");
        etDiarySearch.clearFocus();

        layoutDiarySearch.setVisibility(View.GONE);
        btnDiarySearch.setVisibility(View.VISIBLE);

        cardWeeklyCalendar.setVisibility(View.VISIBLE);
        cardDiaryReflection.setVisibility(View.VISIBLE);

        tvEntriesTitle.setText("Recent Entries");

        tvDiaryEmptySearch.setVisibility(View.GONE);
        rvRecentEntries.setVisibility(View.VISIBLE);

        refreshRecentEntries();
        hideKeyboard();
    }

    private void showKeyboard(View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) requireContext()
                        .getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(
                    view,
                    InputMethodManager.SHOW_IMPLICIT
            );
        }
    }

    private void hideKeyboard() {
        if (getView() == null) {
            return;
        }

        InputMethodManager inputMethodManager =
                (InputMethodManager) requireContext()
                        .getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    getView().getWindowToken(),
                    0
            );
        }
    }

    private void searchDiaryEntries(String query) {
        List<DiaryEntry> all = facade.getRecentEntries();
        ArrayList<DiaryEntry> filtered = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(all);
        } else {
            String q = query.toLowerCase();

            for (DiaryEntry entry : all) {
                if (entry.getTitle().toLowerCase().contains(q) ||
                        entry.getContent().toLowerCase().contains(q) ||
                        entry.getDate().toLowerCase().contains(q) ||
                        (entry.getMood() != null && entry.getMood().toLowerCase().contains(q))) {

                    filtered.add(entry);
                }
            }
        }

        diaryAdapter.setEntries(filtered);

        boolean empty = filtered.isEmpty();
        tvDiaryEmptySearch.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvRecentEntries.setVisibility(empty ? View.GONE : View.VISIBLE);

        tvEntriesTitle.setText(empty ? "Search Results" : "Search Results (" + filtered.size() + ")");
    }


    private void performSearch(String query) {
        List<DiaryEntry> searchResults =
                facade.searchEntries(query);

        if (diaryAdapter == null) {
            setupRecentEntries();
        }

        diaryAdapter.setEntries(
                new ArrayList<>(searchResults)
        );

        if (searchResults.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "No diary entries found",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    requireContext(),
                    searchResults.size()
                            + " entries found",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void setupMoodBar(View root) {
        ImageView moodHappy =
                root.findViewById(R.id.ivMoodHappy);

        ImageView moodGood =
                root.findViewById(R.id.ivMoodGood);

        ImageView moodNeutral =
                root.findViewById(R.id.ivMoodNeutral);

        ImageView moodSad =
                root.findViewById(R.id.ivMoodSad);

        ImageView moodMiserable =
                root.findViewById(R.id.ivMoodMiserable);

        ImageView[] moods = {
                moodHappy,
                moodGood,
                moodNeutral,
                moodSad,
                moodMiserable
        };

        resetAllMoods(moods);

        moodHappy.setOnClickListener(
                v -> selectMoodForSelectedDate(
                        moods,
                        moodHappy,
                        "happy",
                        R.drawable.ic_mood_happy_act
                )
        );

        moodGood.setOnClickListener(
                v -> selectMoodForSelectedDate(
                        moods,
                        moodGood,
                        "good",
                        R.drawable.ic_mood_good_act
                )
        );

        moodNeutral.setOnClickListener(
                v -> selectMoodForSelectedDate(
                        moods,
                        moodNeutral,
                        "neutral",
                        R.drawable.ic_mood_neutral_act
                )
        );

        moodSad.setOnClickListener(
                v -> selectMoodForSelectedDate(
                        moods,
                        moodSad,
                        "sad",
                        R.drawable.ic_mood_sad_act
                )
        );

        moodMiserable.setOnClickListener(
                v -> selectMoodForSelectedDate(
                        moods,
                        moodMiserable,
                        "miserable",
                        R.drawable.ic_mood_miserable_act
                )
        );
    }

    private void selectMoodForSelectedDate(
            @NonNull ImageView[] moods,
            @NonNull ImageView selected,
            @NonNull String mood,
            int activeDrawable
    ) {
        resetAllMoods(moods);

        selected.setImageResource(activeDrawable);
        selected.setAlpha(1f);

        selectedMood = mood;

        facade.saveMoodForDate(
                selectedDiaryDate,
                mood
        );
    }

    private void resetAllMoods(
            @NonNull ImageView[] moods
    ) {
        moods[0].setImageResource(
                R.drawable.ic_mood_happy
        );

        moods[1].setImageResource(
                R.drawable.ic_mood_good
        );

        moods[2].setImageResource(
                R.drawable.ic_mood_neutral
        );

        moods[3].setImageResource(
                R.drawable.ic_mood_sad
        );

        moods[4].setImageResource(
                R.drawable.ic_mood_miserable
        );

        for (ImageView moodView : moods) {
            moodView.setAlpha(0.45f);
        }
    }

    /**
     * yyyy-MM-dd -> MMM dd, yyyy
     */
    private String convertDateFormat(String dateStr) {
        if (TextUtils.isEmpty(dateStr)) {
            return "";
        }

        try {
            Date date = CALENDAR_DATE_FORMAT.parse(dateStr);
            if (date != null) {
                return DB_DATE_FORMAT.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateStr;
    }
    private void switchToMemories() {
        tabMemories.setBackgroundResource(R.drawable.bg_tab_selected);
        tabMemories.setTextColor(Color.WHITE);

        tabMyDiary.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabMyDiary.setTextColor(Color.parseColor("#7A6CFF"));

        cardWeeklyCalendar.setVisibility(View.GONE);
        cardDiaryReflection.setVisibility(View.GONE);

        tvEntriesTitle.setText("Memories");
        tvEntriesTitle.setVisibility(View.GONE);
        layoutDiarySearch.setVisibility(View.VISIBLE);
        searchDiaryEntries(etDiarySearch.getText().toString());
        showSearchResults(facade.getRecentEntries());
        tvEntriesTitle.setText("Recent Memories");

        rvRecentEntries.setVisibility(View.VISIBLE);
        refreshRecentEntries();


        List<DiaryEntry> entries = facade.getRecentEntries();
        if (entries == null || entries.isEmpty()) {
            layoutMemoryCapsule.setVisibility(View.VISIBLE);
            rvRecentEntries.setVisibility(View.GONE);
            tvDiaryEmptySearch.setVisibility(View.GONE);
        } else {
            layoutMemoryCapsule.setVisibility(View.GONE);
            rvRecentEntries.setVisibility(View.VISIBLE);
            diaryAdapter.setEntries(entries);
        }
    }


    private void switchToMyDiary() {

        tabMyDiary.setBackgroundResource(R.drawable.bg_tab_selected);
        tabMyDiary.setTextColor(Color.WHITE);

        tabMemories.setBackgroundResource(R.drawable.bg_tab_unselected);
        tabMemories.setTextColor(Color.parseColor("#7A6CFF"));
        layoutDiarySearch.setVisibility(View.GONE);
        tvEntriesTitle.setVisibility(View.VISIBLE);

        cardWeeklyCalendar.setVisibility(View.VISIBLE);
        cardDiaryReflection.setVisibility(View.VISIBLE);
        layoutMemoryCapsule.setVisibility(View.GONE);

        List<DiaryEntry> entries = facade.getRecentEntries();

        if (entries == null || entries.isEmpty()) {
            // هیچ مموری وجود ندارد → عنوان Recent Memories را مخفی کن
            tvEntriesTitle.setVisibility(View.GONE);
        } else {
            tvEntriesTitle.setVisibility(View.VISIBLE);
            tvEntriesTitle.setText("Recent Memories");
        }

        refreshRecentEntries();
        updateRecentMemoriesTitle();


    }
    private void updateRecentMemoriesTitle() {
        List<DiaryEntry> entries = facade.getRecentEntries();

        if (entries == null || entries.isEmpty()) {
            tvEntriesTitle.setVisibility(View.GONE);
        } else {
            tvEntriesTitle.setVisibility(View.VISIBLE);
            tvEntriesTitle.setText("Recent Memories");
        }
    }


}
