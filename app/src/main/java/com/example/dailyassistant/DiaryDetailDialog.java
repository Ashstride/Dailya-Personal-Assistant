package com.example.dailyassistant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DiaryDetailDialog extends BottomSheetDialogFragment {

    public interface OnDiaryActionListener {

        // بعد از ذخیره تغییرات اجرا می‌شود
        void onUpdated(DiaryEntry entry);

        void onDelete(DiaryEntry entry);
    }

    private static final String ARG_ID = "arg_id";

    private OnDiaryActionListener listener;
    private DiaryEntry currentEntry;
    private DiaryManagerFacade facade;

    private LinearLayout layoutViewMode;
    private LinearLayout layoutEditMode;

    private TextView tvTitle;
    private TextView tvDate;
    private TextView tvContent;

    private ImageView ivDetailMood;

    private EditText etEditTitle;
    private EditText etEditContent;


    private ImageView ivMoodHappy;
    private ImageView ivMoodGood;
    private ImageView ivMoodNeutral;
    private ImageView ivMoodSad;
    private ImageView ivMoodMiserable;

    private String selectedEditMood = "neutral";

    public DiaryDetailDialog() {
        // Constructor خالی برای Fragment ضروری است
    }

    public static DiaryDetailDialog newInstance(
            DiaryEntry entry,
            OnDiaryActionListener listener
    ) {
        DiaryDetailDialog dialog = new DiaryDetailDialog();

        Bundle args = new Bundle();
        args.putString(ARG_ID, entry.getId());
        dialog.setArguments(args);

        dialog.listener = listener;
        dialog.currentEntry = entry;

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.dialog_diary_detail,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        facade = DiaryManagerFacade.getInstance(requireContext());

        findViews(view);
        restoreEntryIfNeeded();
        showEntryInformation();
        setupMoodClicks();
        setupButtons();
    }


    private void findViews(View view) {

        layoutViewMode = view.findViewById(R.id.layoutViewMode);
        layoutEditMode = view.findViewById(R.id.layoutEditMode);

        tvTitle = view.findViewById(R.id.tvDetailTitle);
        tvDate = view.findViewById(R.id.tvDetailDate);
        tvContent = view.findViewById(R.id.tvDetailContent);

        ivDetailMood = view.findViewById(R.id.ivDetailMood);
        etEditTitle = view.findViewById(R.id.etEditTitle);

        etEditContent = view.findViewById(R.id.etEditContent);

        ivMoodHappy = view.findViewById(R.id.ivEditMoodHappy);
        ivMoodGood = view.findViewById(R.id.ivEditMoodGood);
        ivMoodNeutral = view.findViewById(R.id.ivEditMoodNeutral);
        ivMoodSad = view.findViewById(R.id.ivEditMoodSad);
        ivMoodMiserable = view.findViewById(R.id.ivEditMoodMiserable);
    }

    /**
     * اگر BottomSheet توسط Android دوباره ساخته شد،
     * Entry را با ID از Repository دریافت می‌کنیم.
     */
    private void restoreEntryIfNeeded() {

        if (currentEntry != null) {
            return;
        }

        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        String entryId = args.getString(ARG_ID);

        if (!TextUtils.isEmpty(entryId)) {
            currentEntry = facade.getEntryById(entryId);
        }
    }

    private void showEntryInformation() {

        if (currentEntry == null) {
            dismiss();
            return;
        }

        tvTitle.setText(currentEntry.getTitle());
        tvDate.setText(currentEntry.getDate());
        tvContent.setText(currentEntry.getContent());

        selectedEditMood = normalizeMood(currentEntry.getMood());

        ivDetailMood.setImageResource(
                getMoodIcon(selectedEditMood, false)
        );
    }

    private void setupButtons() {

        Button btnEdit = requireView().findViewById(R.id.btnEditEntry);
        Button btnDelete = requireView().findViewById(R.id.btnDeleteEntry);
        Button btnSave = requireView().findViewById(R.id.btnSaveEdit);
        Button btnCancel = requireView().findViewById(R.id.btnCancelEdit);

        btnEdit.setOnClickListener(v -> enterEditMode());

        btnCancel.setOnClickListener(v -> exitEditModeWithoutSaving());

        btnSave.setOnClickListener(v -> saveChanges());

        btnDelete.setOnClickListener(v -> {

            if (currentEntry == null) {
                return;
            }

            if (listener != null) {
                listener.onDelete(currentEntry);
            }

            dismiss();
        });
    }

    private void enterEditMode() {

        if (currentEntry == null) {
            return;
        }

        // قرار دادن عنوان فعلی
        etEditTitle.setText(currentEntry.getTitle());

        // قرار دادن متن فعلی
        etEditContent.setText(currentEntry.getContent());

        // قرار دادن نشانگر عنوان در انتهای متن
        etEditTitle.setSelection(
                etEditTitle.getText().length()
        );

        selectedEditMood =
                normalizeMood(currentEntry.getMood());

        updateEditMoodSelection(selectedEditMood);

        layoutViewMode.setVisibility(View.GONE);
        layoutEditMode.setVisibility(View.VISIBLE);

        // ابتدا فیلد عنوان فعال شود
        etEditTitle.requestFocus();
    }


    private void exitEditModeWithoutSaving() {

        layoutEditMode.setVisibility(View.GONE);
        layoutViewMode.setVisibility(View.VISIBLE);

        if (currentEntry != null) {
            selectedEditMood = normalizeMood(currentEntry.getMood());
        }
    }

    private void saveChanges() {

        if (currentEntry == null) {
            return;
        }

        String editedTitle =
                etEditTitle.getText().toString().trim();

        String editedContent =
                etEditContent.getText().toString().trim();

        // اعتبارسنجی عنوان
        if (TextUtils.isEmpty(editedTitle)) {
            etEditTitle.setError("Diary title cannot be empty");
            etEditTitle.requestFocus();
            return;
        }

        // اعتبارسنجی متن
        if (TextUtils.isEmpty(editedContent)) {
            etEditContent.setError("Diary content cannot be empty");
            etEditContent.requestFocus();
            return;
        }

        // تغییر اطلاعات آبجکت
        currentEntry.setTitle(editedTitle);
        currentEntry.setContent(editedContent);
        currentEntry.setMood(selectedEditMood);

        // ذخیره تغییرات
        facade.updateEntry(currentEntry);

        // هماهنگ نگه داشتن Mood روز
        facade.saveMoodForDate(
                currentEntry.getDate(),
                selectedEditMood
        );

        // به‌روزرسانی حالت نمایش
        tvTitle.setText(currentEntry.getTitle());
        tvContent.setText(currentEntry.getContent());

        ivDetailMood.setImageResource(
                getMoodIcon(currentEntry.getMood(), false)
        );

        layoutEditMode.setVisibility(View.GONE);
        layoutViewMode.setVisibility(View.VISIBLE);

        if (listener != null) {
            listener.onUpdated(currentEntry);
        }

        Toast.makeText(
                requireContext(),
                "Diary updated",
                Toast.LENGTH_SHORT
        ).show();
    }


    private void setupMoodClicks() {

        ivMoodHappy.setOnClickListener(v ->
                selectEditMood("happy")
        );

        ivMoodGood.setOnClickListener(v ->
                selectEditMood("good")
        );

        ivMoodNeutral.setOnClickListener(v ->
                selectEditMood("neutral")
        );

        ivMoodSad.setOnClickListener(v ->
                selectEditMood("sad")
        );

        ivMoodMiserable.setOnClickListener(v ->
                selectEditMood("miserable")
        );
    }

    private void selectEditMood(String mood) {

        selectedEditMood = mood;
        updateEditMoodSelection(mood);
    }

    private void updateEditMoodSelection(String mood) {

        resetEditMoodIcons();

        switch (normalizeMood(mood)) {

            case "happy":
                ivMoodHappy.setImageResource(
                        R.drawable.ic_mood_happy_act
                );
                ivMoodHappy.setAlpha(1f);
                break;

            case "good":
                ivMoodGood.setImageResource(
                        R.drawable.ic_mood_good_act
                );
                ivMoodGood.setAlpha(1f);
                break;

            case "sad":
                ivMoodSad.setImageResource(
                        R.drawable.ic_mood_sad_act
                );
                ivMoodSad.setAlpha(1f);
                break;

            case "miserable":
                ivMoodMiserable.setImageResource(
                        R.drawable.ic_mood_miserable_act
                );
                ivMoodMiserable.setAlpha(1f);
                break;

            default:
                ivMoodNeutral.setImageResource(
                        R.drawable.ic_mood_neutral_act
                );
                ivMoodNeutral.setAlpha(1f);
                break;
        }
    }

    private void resetEditMoodIcons() {

        ivMoodHappy.setImageResource(
                R.drawable.ic_mood_happy
        );

        ivMoodGood.setImageResource(
                R.drawable.ic_mood_good
        );

        ivMoodNeutral.setImageResource(
                R.drawable.ic_mood_neutral
        );

        ivMoodSad.setImageResource(
                R.drawable.ic_mood_sad
        );

        ivMoodMiserable.setImageResource(
                R.drawable.ic_mood_miserable
        );

        ivMoodHappy.setAlpha(0.45f);
        ivMoodGood.setAlpha(0.45f);
        ivMoodNeutral.setAlpha(0.45f);
        ivMoodSad.setAlpha(0.45f);
        ivMoodMiserable.setAlpha(0.45f);
    }

    private String normalizeMood(String mood) {

        if (TextUtils.isEmpty(mood)) {
            return "neutral";
        }

        switch (mood.toLowerCase()) {

            case "happy":
            case "good":
            case "neutral":
            case "sad":
            case "miserable":
                return mood.toLowerCase();

            default:
                return "neutral";
        }
    }

    /**
     * active = true  -> آیکون فعال
     * active = false -> آیکون عادی
     */
    private int getMoodIcon(String mood, boolean active) {

        switch (normalizeMood(mood)) {

            case "happy":
                return active
                        ? R.drawable.ic_mood_happy_act
                        : R.drawable.ic_mood_happy;

            case "good":
                return active
                        ? R.drawable.ic_mood_good_act
                        : R.drawable.ic_mood_good;

            case "sad":
                return active
                        ? R.drawable.ic_mood_sad_act
                        : R.drawable.ic_mood_sad;

            case "miserable":
                return active
                        ? R.drawable.ic_mood_miserable_act
                        : R.drawable.ic_mood_miserable;

            default:
                return active
                        ? R.drawable.ic_mood_neutral_act
                        : R.drawable.ic_mood_neutral;
        }
    }
}
