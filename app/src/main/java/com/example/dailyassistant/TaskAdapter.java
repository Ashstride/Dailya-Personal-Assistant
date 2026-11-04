package com.example.dailyassistant;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.PorterDuffXfermode;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> originalTasks;
    private final List<Task> filteredTasks;
    private final boolean isLimited;


    private OnTaskStatusChangeListener statusChangeListener;
    private OnTaskActionListener actionListener;

    private static final int[] BG_COLORS = {
            Color.parseColor("#E2F6EC"), // Green
            Color.parseColor("#E3F0FF"), // Blue
            Color.parseColor("#FFE5EE"), // Pink
            Color.parseColor("#FFF0D4"), // Yellow
            Color.parseColor("#F1E8FF")  // Purple
    };

    private static final int[] THEME_COLORS = {
            Color.parseColor("#2BB361"), // Green
            Color.parseColor("#4A90E2"), // Blue
            Color.parseColor("#FF528F"), // Pink
            Color.parseColor("#F2B134"), // Yellow
            Color.parseColor("#8E44FF")  // Purple
    };

    public interface OnTaskStatusChangeListener {
        void onStatusChanged(Task task, boolean isDone, int position);
    }

    public interface OnTaskActionListener {
        void onDelete(Task task, int position);
        void onEdit(Task task, int position);
    }

    public void setOnTaskStatusChangeListener(OnTaskStatusChangeListener listener) {
        this.statusChangeListener = listener;
    }

    public void setOnTaskActionListener(OnTaskActionListener listener) {
        this.actionListener = listener;
    }

    private TaskAdapter(Builder builder) {
        this.originalTasks = new ArrayList<>();
        if (builder.tasks != null) {
            for (Task task : builder.tasks) {
                this.originalTasks.add(task.clone());
            }
        }

        this.filteredTasks = new ArrayList<>();
        for (Task task : this.originalTasks) {
            this.filteredTasks.add(task.clone());
        }

        this.isLimited = builder.isLimited;

    }

    public static class Builder {
        private List<Task> tasks;
        private boolean isLimited = false;

        public Builder setTasks(List<Task> tasks) {
            this.tasks = tasks;
            return this;
        }

        public Builder setLimited(boolean limited) {
            this.isLimited = limited;
            return this;
        }

        public TaskAdapter build() {
            return new TaskAdapter(this);
        }
    }

    public void filter(String query) {
        filteredTasks.clear();

        if (query == null || query.trim().isEmpty()) {
            for (Task task : originalTasks) {
                filteredTasks.add(task.clone());
            }
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Task task : originalTasks) {
                if (task.getTitle() != null
                        && task.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredTasks.add(task.clone());
                }
            }
        }

        notifyDataSetChanged();
    }

    public void filterByStatus(String statusType) {
        filteredTasks.clear();

        for (Task task : originalTasks) {
            if ("ALL".equalsIgnoreCase(statusType)) {
                filteredTasks.add(task.clone());
            } else if ("PENDING".equalsIgnoreCase(statusType)) {
                if (!task.isDone()) {
                    filteredTasks.add(task.clone());
                }
            } else if ("DONE".equalsIgnoreCase(statusType)) {
                if (task.isDone()) {
                    filteredTasks.add(task.clone());
                }
            } else if ("OVERDUE".equalsIgnoreCase(statusType)) {
                if (!task.isDone() && task.isOverdue()) {
                    filteredTasks.add(task.clone());
                }
            }
        }

        notifyDataSetChanged();
    }

    public void updateData(final List<Task> newTasks) {
        final List<Task> newClonedTasks = new ArrayList<>();
        if (newTasks != null) {
            for (Task task : newTasks) {
                newClonedTasks.add(task.clone());
            }
        }

        final List<Task> oldFilteredTasks = new ArrayList<>();
        for (Task task : filteredTasks) {
            oldFilteredTasks.add(task.clone());
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldFilteredTasks.size();
            }

            @Override
            public int getNewListSize() {
                return newClonedTasks.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                String oldId = oldFilteredTasks.get(oldItemPosition).getId();
                String newId = newClonedTasks.get(newItemPosition).getId();

                if (oldId == null && newId == null) return true;
                if (oldId == null || newId == null) return false;
                return oldId.equals(newId);
            }


            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Task oldTask = oldFilteredTasks.get(oldItemPosition);
                Task newTask = newClonedTasks.get(newItemPosition);

                return oldTask.isDone() == newTask.isDone()
                        && safeEquals(oldTask.getTitle(), newTask.getTitle())
                        && safeEquals(oldTask.getDueDate(), newTask.getDueDate())
                        && safeEquals(oldTask.getTime(), newTask.getTime())
                        && oldTask.getPriority() == newTask.getPriority();
            }
        });

        originalTasks.clear();
        filteredTasks.clear();

        for (Task task : newClonedTasks) {
            originalTasks.add(task.clone());
            filteredTasks.add(task.clone());
        }

        diffResult.dispatchUpdatesTo(this);
    }




    private boolean safeEquals(String first, String second) {
        if (first == null && second == null) return true;
        if (first == null || second == null) return false;
        return first.equals(second);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = filteredTasks.get(position);

        String safeTitle = safeTrim(task.getTitle());
        holder.title.setText(safeTitle.isEmpty() ? "(No title)" : safeTitle);

        bindTaskMeta(holder, task);
        bindTaskColors(holder, task, position);
        bindDoneState(holder, task);
        bindCheckboxClick(holder, task);
        bindLongPressMenu(holder, task);
    }


    private void bindTaskMeta(TaskViewHolder holder, Task task) {
        String time = safeTrim(task.getTime());
        String dueDate = safeTrim(task.getDueDate());

        String metaText;
        if (!time.isEmpty() && !dueDate.isEmpty()) {
            metaText = time + " • " + formatDateToShortLabelOrRaw(dueDate);
        } else if (!time.isEmpty()) {
            metaText = time;
        } else if (!dueDate.isEmpty()) {
            metaText = formatDateToShortLabelOrRaw(dueDate);
        } else {
            metaText = "";
        }

        if (metaText.isEmpty()) {
            holder.metaRow.setVisibility(View.GONE);
            holder.time.setText("");
            holder.bellIcon.setVisibility(View.GONE);
            return;
        }

        holder.metaRow.setVisibility(View.VISIBLE);
        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText(metaText);

        if (task.shouldShowReminderIcon()) {
            holder.bellIcon.setVisibility(View.VISIBLE);
        } else {
            holder.bellIcon.setVisibility(View.GONE);
        }
    }

    private void bindTaskColors(TaskViewHolder holder, Task task, int position) {


        int colorIndex = task.getColorIndex();

        int activeThemeColor = THEME_COLORS[colorIndex];
        int activeBgColor = BG_COLORS[colorIndex];

        if (holder.taskIcon.getBackground() != null) {
            GradientDrawable iconBg = (GradientDrawable) holder.taskIcon.getBackground().mutate();
            iconBg.setColor(activeBgColor);
            holder.taskIcon.setBackground(iconBg);
        }

        holder.taskIcon.setColorFilter(activeThemeColor, PorterDuff.Mode.SRC_IN);

        if (holder.taskCheckbox.getBackground() != null) {
            GradientDrawable checkboxDrawable =
                    (GradientDrawable) holder.taskCheckbox.getBackground().mutate();
            checkboxDrawable.setStroke(4, activeThemeColor);
            checkboxDrawable.setColor(task.isDone() ? activeThemeColor : Color.TRANSPARENT);
            holder.taskCheckbox.setBackground(checkboxDrawable);
        }
    }





    private void bindDoneState(TaskViewHolder holder, Task task) {
        if (task.isDone()) {
            holder.title.setPaintFlags(
                    holder.title.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            );
            holder.title.setTextColor(Color.parseColor("#9E9E9E"));
            holder.time.setTextColor(Color.parseColor("#B0B0B0"));
        } else {
            holder.title.setPaintFlags(
                    holder.title.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG)
            );
            holder.title.setTextColor(Color.parseColor("#212121"));
            holder.time.setTextColor(Color.parseColor("#757575"));
        }
    }

    private void bindCheckboxClick(TaskViewHolder holder, Task task) {
        holder.taskCheckbox.setOnClickListener(v -> {
            int position = holder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;


            Task clickedTask = filteredTasks.get(position);
            boolean newStatus = !clickedTask.isDone();
            clickedTask.setDone(newStatus);


            bindDoneState(holder, clickedTask);
            bindTaskColors(holder, clickedTask, position);

            v.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();

                        int p = holder.getBindingAdapterPosition();
                        if (p != RecyclerView.NO_POSITION) {
                            notifyItemChanged(p);
                        }

                        if (statusChangeListener != null) {
                            statusChangeListener.onStatusChanged(clickedTask, newStatus, p);
                        }
                    })
                    .start();
        });
    }


    private void setItemSelectedState(TaskViewHolder holder, boolean selected) {
        View targetCard = holder.taskCard != null ? holder.taskCard : holder.itemView;

        targetCard.animate()
                .scaleX(selected ? 1.025f : 1f)
                .scaleY(selected ? 1.025f : 1f)
                .setDuration(150)
                .start();

        targetCard.setBackgroundResource(
                selected ? R.drawable.bg_task_card_selected : R.drawable.bg_task_card
        );

        targetCard.setElevation(dpToPx(targetCard.getContext(), selected ? 12 : 6));
        targetCard.setTranslationZ(dpToPx(targetCard.getContext(), selected ? 6 : 0));

        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    private void equalizeActionWidths(TextView firstAction, TextView secondAction) {
        int unspecified = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        firstAction.measure(unspecified, unspecified);
        secondAction.measure(unspecified, unspecified);

        int maxWidth = Math.max(firstAction.getMeasuredWidth(), secondAction.getMeasuredWidth());

        ViewGroup.LayoutParams firstParams = firstAction.getLayoutParams();
        firstParams.width = maxWidth;
        firstAction.setLayoutParams(firstParams);

        ViewGroup.LayoutParams secondParams = secondAction.getLayoutParams();
        secondParams.width = maxWidth;
        secondAction.setLayoutParams(secondParams);
    }

    private int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    private void bindLongPressMenu(TaskViewHolder holder, Task task) {
        holder.itemView.setOnLongClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return true;
            if (actionListener == null) return true;

            View anchorCard = holder.taskCard != null ? holder.taskCard : holder.itemView;

            // فعال کردن حالت انتخاب و زوم
            setItemSelectedState(holder, true);

            // ۱. محاسبه موقعیت دقیق کارت روی صفحه برای ایجاد سوراخ در لایه Dim
            int[] loc = new int[2];
            anchorCard.getLocationOnScreen(loc);

            // با توجه به اینکه scale در setItemSelectedState روی 1.025f است
            final float scale = 1.025f;
            int cardW = anchorCard.getWidth();
            int cardH = anchorCard.getHeight();
            int centerX = loc[0] + cardW / 2;
            int centerY = loc[1] + cardH / 2;
            int scaledW = Math.round(cardW * scale);
            int scaledH = Math.round(cardH * scale);

            int holeLeft = centerX - scaledW / 2;
            int holeTop = centerY - scaledH / 2;
            int holeRight = holeLeft + scaledW;
            int holeBottom = holeTop + scaledH;

            // ۲. ساخت ریشه اوورلی تمام‌صفحه
            FrameLayout overlayRoot = new FrameLayout(v.getContext());
            overlayRoot.setBackgroundColor(Color.TRANSPARENT);

            // ۳. لایه تیره با شدت ۲۰ و سوراخ شفاف روی تسک انتخابی
            DimOverlayView dimView = new DimOverlayView(v.getContext());
            dimView.setHole(holeLeft, holeTop, holeRight, holeBottom, dpToPx(v.getContext(), 24));

            overlayRoot.addView(dimView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // ۴. اینفلیت کردن دکمه‌های Edit/Delete
            View menuView = LayoutInflater.from(v.getContext())
                    .inflate(R.layout.popup_task_actions, null, false);

            TextView actionEdit = menuView.findViewById(R.id.actionEdit);
            TextView actionDelete = menuView.findViewById(R.id.actionDelete);
            equalizeActionWidths(actionEdit, actionDelete);

            menuView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int menuW = menuView.getMeasuredWidth();
            int menuH = menuView.getMeasuredHeight();

            // مقدار پدینگ اضافه برای جلوگیری از برش سایه
            int popupPadding = dpToPx(v.getContext(), 20);

            // قرار دادن منو کنار کارت با فضای کافی برای سایه
            FrameLayout.LayoutParams menuLp = new FrameLayout.LayoutParams(menuW, menuH);
            menuLp.leftMargin = loc[0] + cardW - menuW - dpToPx(v.getContext(), 12) + popupPadding;
            menuLp.topMargin = loc[1] - popupPadding;
            overlayRoot.addView(menuView, menuLp);


            // ۵. نمایش پاپ‌آپ تمام‌صفحه
            PopupWindow popupWindow = new PopupWindow(overlayRoot,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // لمس ناحیه تیره باعث بسته شدن شود
            dimView.setOnClickListener(vv -> popupWindow.dismiss());

            popupWindow.setOnDismissListener(() -> {
                setItemSelectedState(holder, false);
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            });

            actionEdit.setOnClickListener(view -> {
                popupWindow.dismiss();
                actionListener.onEdit(task, adapterPosition);
            });

            actionDelete.setOnClickListener(view -> {
                popupWindow.dismiss();
                actionListener.onDelete(task, adapterPosition);
            });

            popupWindow.showAtLocation(anchorCard, Gravity.NO_GRAVITY, 0, 0);
            return true;
        });
    }



    private String formatDateToShortLabelOrRaw(String dueDate) {
        String shortLabel = formatDateToShortLabel(dueDate);
        return shortLabel.isEmpty() ? dueDate : shortLabel;
    }

    private String formatDateToShortLabel(String dueDate) {
        if (dueDate == null || dueDate.trim().isEmpty()) return "";

        Date date = tryParseDate(dueDate.trim());
        if (date == null) return "";

        Calendar taskCal = Calendar.getInstance();
        taskCal.setTime(date);

        Calendar todayCal = Calendar.getInstance();

        Calendar tomorrowCal = Calendar.getInstance();
        tomorrowCal.add(Calendar.DAY_OF_YEAR, 1);

        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);

        if (isSameDay(taskCal, todayCal)) return "Today";
        if (isSameDay(taskCal, tomorrowCal)) return "Tomorrow";
        if (isSameDay(taskCal, yesterdayCal)) return "Yesterday";

        return new SimpleDateFormat("d MMM", Locale.US).format(taskCal.getTime());
    }

    private Date tryParseDate(String dueDate) {
        String[] formats = new String[] {
                "d/M/yyyy",
                "dd/MM/yyyy",
                "yyyy-MM-dd",
                "d-MM-yyyy",
                "dd-MM-yyyy"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(format, Locale.US);
                parser.setLenient(false);
                return parser.parse(dueDate);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private boolean isSameDay(Calendar firstCal, Calendar secondCal) {
        return firstCal.get(Calendar.YEAR) == secondCal.get(Calendar.YEAR)
                && firstCal.get(Calendar.DAY_OF_YEAR) == secondCal.get(Calendar.DAY_OF_YEAR);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public int getItemCount() {
        return isLimited ? Math.min(filteredTasks.size(), 3) : filteredTasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView time;
        ImageView taskIcon;
        ImageView bellIcon;
        View taskCheckbox;
        LinearLayout metaRow;
        View taskCard;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.taskTitle);
            time = itemView.findViewById(R.id.taskTime);
            taskIcon = itemView.findViewById(R.id.taskIcon);
            bellIcon = itemView.findViewById(R.id.taskBellIcon);
            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
            metaRow = itemView.findViewById(R.id.taskMetaRow);
            taskCard = itemView.findViewById(R.id.taskCard);
        }
    }

    private static class DimOverlayView extends View {
        private final Paint clearPaint;
        private RectF holeRect;
        private float holeRadius;

        DimOverlayView(Context context) {
            super(context);
            setClickable(true);
            clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        void setHole(int left, int top, int right, int bottom, float radius) {
            holeRect = new RectF(left, top, right, bottom);
            holeRadius = radius;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // شدت تیره‌شدن کل صفحه (می‌توانی 0x8A را کم/زیاد کنی)
            canvas.drawColor(0x66000000);
            if (holeRect != null) {
                int saved = canvas.saveLayer(
                        holeRect.left, holeRect.top,
                        holeRect.right, holeRect.bottom,
                        null, Canvas.ALL_SAVE_FLAG
                );
                canvas.drawRoundRect(holeRect, holeRadius, holeRadius, clearPaint);
                canvas.restoreToCount(saved);
            }
        }
    }



}
