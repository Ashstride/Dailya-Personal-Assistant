package com.example.dailyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class DiaryFullEditorActivity extends AppCompatActivity {

    private static final String ACTION_DIARY_SYNC = "com.example.dailyassistant.action.DIARY_SYNC";
    private static final String EXTRA_SILENT_SAVE = "SILENT_SAVE";
    private static final float UNSELECTED_ALPHA = 0.45f;

    private EditText etTitle;
    private EditText etContent;
    private ImageButton btnBack;
    private TextView btnSave;
    private TextView tvDailyQuestion;
    private LinearLayout layoutMoodContainer;

    private ImageButton ivHappy, ivGood, ivNeutral, ivSad, ivMiserable;

    private String selectedMood = null;
    private String currentQuestion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_DailyAssistant);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_full_editor);

        initViews();

        if (!areViewsReady()) {
            Toast.makeText(this, "Failed to load Diary Editor page", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupMoodSelectors();
        loadDataFromIntent();
        setupLiveSync();
        setupBackPressedDispatcher();

        btnBack.setOnClickListener(v -> finishWithResult(true));
        btnSave.setOnClickListener(v -> saveAndReturn());
    }

    private void initViews() {
        etTitle = findViewById(R.id.etDiaryTitle);
        etContent = findViewById(R.id.etDiaryContent);
        btnBack = findViewById(R.id.btnBackDiaryDetail);
        btnSave = findViewById(R.id.btnWriteEntry);
        tvDailyQuestion = findViewById(R.id.tvReflectionQuestionLabel);
        layoutMoodContainer = findViewById(R.id.layoutMoodContainer);
        ivHappy = findViewById(R.id.diaryMoodHappy);
        ivGood = findViewById(R.id.diaryMoodGood);
        ivNeutral = findViewById(R.id.diaryMoodNeutral);
        ivSad = findViewById(R.id.diaryMoodSad);
        ivMiserable = findViewById(R.id.diaryMoodMiserable);
    }

    private boolean areViewsReady() {
        return etTitle != null && etContent != null && btnBack != null && btnSave != null &&
                tvDailyQuestion != null && layoutMoodContainer != null && ivHappy != null &&
                ivGood != null && ivNeutral != null && ivSad != null && ivMiserable != null;
    }

    private void setupBackPressedDispatcher() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWithResult(true);
            }
        });
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            updateMoodUI(selectedMood);
            return;
        }

        String title = intent.getStringExtra("DIARY_TITLE");
        String content = intent.getStringExtra("DIARY_CONTENT");
        String mood = intent.getStringExtra("DIARY_MOOD");
        currentQuestion = intent.getStringExtra("DAILY_QUESTION");

        if (currentQuestion != null) {
            tvDailyQuestion.setText(currentQuestion);
        }

        if (title != null) etTitle.setText(title);
        if (content != null) etContent.setText(content);
        if (mood != null && !mood.trim().isEmpty()) {
            selectedMood = mood.toLowerCase().trim();
        }

        updateMoodUI(selectedMood);
    }

    private void setupLiveSync() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { syncToFragment(); }
        };

        etTitle.addTextChangedListener(watcher);
        etContent.addTextChangedListener(watcher);
    }

    private void syncToFragment() {
        Intent syncIntent = new Intent(ACTION_DIARY_SYNC);
        syncIntent.setPackage(getPackageName());
        syncIntent.putExtra("UPDATED_TITLE", etTitle.getText().toString());
        syncIntent.putExtra("UPDATED_CONTENT", etContent.getText().toString());
        syncIntent.putExtra("UPDATED_MOOD", selectedMood);
        syncIntent.putExtra("UPDATED_QUESTION", currentQuestion);
        sendBroadcast(syncIntent);
    }

    private void setupMoodSelectors() {
        ivHappy.setOnClickListener(v -> selectMood("happy"));
        ivGood.setOnClickListener(v -> selectMood("good"));
        ivNeutral.setOnClickListener(v -> selectMood("neutral"));
        ivSad.setOnClickListener(v -> selectMood("sad"));
        ivMiserable.setOnClickListener(v -> selectMood("miserable"));
    }

    private void selectMood(String mood) {
        selectedMood = mood;
        updateMoodUI(selectedMood);
        syncToFragment();
    }

    private void updateMoodUI(String mood) {
        ivHappy.setImageResource(R.drawable.ic_mood_happy);
        ivGood.setImageResource(R.drawable.ic_mood_good);
        ivNeutral.setImageResource(R.drawable.ic_mood_neutral);
        ivSad.setImageResource(R.drawable.ic_mood_sad);
        ivMiserable.setImageResource(R.drawable.ic_mood_miserable);

        ivHappy.setAlpha(UNSELECTED_ALPHA);
        ivGood.setAlpha(UNSELECTED_ALPHA);
        ivNeutral.setAlpha(UNSELECTED_ALPHA);
        ivSad.setAlpha(UNSELECTED_ALPHA);
        ivMiserable.setAlpha(UNSELECTED_ALPHA);

        if (mood == null || mood.trim().isEmpty()) {
            return; // هیچ مودی انتخاب نشده
        }

        switch (mood) {
            case "happy":
                ivHappy.setImageResource(R.drawable.ic_mood_happy_act);
                ivHappy.setAlpha(1f);
                break;
            case "good":
                ivGood.setImageResource(R.drawable.ic_mood_good_act);
                ivGood.setAlpha(1f);
                break;
            case "neutral":
                ivNeutral.setImageResource(R.drawable.ic_mood_neutral_act);
                ivNeutral.setAlpha(1f);
                break;
            case "sad":
                ivSad.setImageResource(R.drawable.ic_mood_sad_act);
                ivSad.setAlpha(1f);
                break;
            case "miserable":
                ivMiserable.setImageResource(R.drawable.ic_mood_miserable_act);
                ivMiserable.setAlpha(1f);
                break;
            default:
                break;
        }
    }


    private void saveAndReturn() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Please write some text first.", Toast.LENGTH_SHORT).show();
            return;
        }
        finishWithResult(false);
    }

    private void finishWithResult(boolean silent) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("UPDATED_TITLE", etTitle.getText().toString());
        resultIntent.putExtra("UPDATED_CONTENT", etContent.getText().toString());
        resultIntent.putExtra("UPDATED_MOOD", selectedMood);
        resultIntent.putExtra("UPDATED_QUESTION", currentQuestion);
        resultIntent.putExtra(EXTRA_SILENT_SAVE, silent);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
