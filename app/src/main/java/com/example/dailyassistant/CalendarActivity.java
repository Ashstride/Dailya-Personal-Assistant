package com.example.dailyassistant;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity implements CalendarAdapter.OnDayClickListener {

    private ImageButton btnBack;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private TextView txtMonthYear;
    private TextView txtSelectedDate;
    private RecyclerView rvCalendar;
    private RecyclerView rvEntries;

    private CalendarAdapter calendarAdapter;
    private DiaryAdapter diaryAdapter;
    private Calendar currentMonth;
    private DiaryManagerFacade diaryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        diaryManager = DiaryManagerFacade.getInstance(this);
        currentMonth = Calendar.getInstance();

        initViews();
        setupCalendar();
        setupDiaryRecycler();
        loadCalendar();

        String today = getTodayDate();
        txtSelectedDate.setText(today);
        loadEntries(today);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        txtMonthYear = findViewById(R.id.txtMonthYear);
        txtSelectedDate = findViewById(R.id.txtSelectedDate);
        rvCalendar = findViewById(R.id.rvCalendar);
        rvEntries = findViewById(R.id.rvEntries);

        btnBack.setOnClickListener(v -> finish());

        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadCalendar();
        });
    }

    private void setupCalendar() {
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        calendarAdapter = new CalendarAdapter(new ArrayList<>(), this);
        rvCalendar.setAdapter(calendarAdapter);
    }

    private void setupDiaryRecycler() {
        rvEntries.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadCalendar() {
        txtMonthYear.setText(
                new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(currentMonth.getTime())
        );

        List<CalendarDayItem> days = new ArrayList<>();

        Calendar firstDayOfMonth = (Calendar) currentMonth.clone();
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        int daysInMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOffset = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        int requiredRows = (int) Math.ceil((firstDayOffset + daysInMonth) / 7.0);
        int totalCells = requiredRows * 7;

        Calendar calendar = (Calendar) firstDayOfMonth.clone();
        calendar.add(Calendar.DAY_OF_MONTH, -firstDayOffset);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.US);
        String today = getTodayDate();

        for (int i = 0; i < totalCells; i++) {
            Calendar temp = (Calendar) calendar.clone();
            String date = dateFormat.format(temp.getTime());

            boolean isCurrentMonth =
                    temp.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
                            && temp.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR);

            boolean isToday = date.equals(today);
            List<DiaryEntry> entries = diaryManager.getEntriesByDate(date);
            boolean hasDiary = entries != null && !entries.isEmpty();

            days.add(new CalendarDayItem(
                    dayNameFormat.format(temp.getTime()),
                    String.valueOf(temp.get(Calendar.DAY_OF_MONTH)),
                    date,
                    isCurrentMonth,
                    isToday,
                    hasDiary
            ));

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        calendarAdapter.update(days);
    }

    private void loadEntries(String date) {
        List<DiaryEntry> entries = diaryManager.getEntriesByDate(date);

        diaryAdapter = new DiaryAdapter(this, entries, entry -> {
            DiaryDetailDialog dialog = DiaryDetailDialog.newInstance(
                    entry,
                    new DiaryDetailDialog.OnDiaryActionListener() {
                        @Override
                        public void onUpdated(DiaryEntry updatedEntry) {
                            loadEntries(updatedEntry.getDate());
                            loadCalendar();
                        }

                        @Override
                        public void onDelete(DiaryEntry selectedEntry) {
                            diaryManager.deleteEntry(selectedEntry.getId());
                            loadEntries(date);
                            loadCalendar();
                        }
                    }
            );

            dialog.show(getSupportFragmentManager(), "DiaryDetailDialog");
        });

        rvEntries.setAdapter(diaryAdapter);
    }

    @Override
    public void onDayClick(CalendarDayItem day) {
        txtSelectedDate.setText(day.getDate());
        loadEntries(day.getDate());
        calendarAdapter.setSelected(day.getDate());
    }

    private String getTodayDate() {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Calendar.getInstance().getTime());
    }
}
