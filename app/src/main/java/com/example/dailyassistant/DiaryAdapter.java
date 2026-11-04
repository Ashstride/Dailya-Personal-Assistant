package com.example.dailyassistant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    public interface OnDiaryClickListener {
        void onClick(DiaryEntry entry);
    }

    private final Context context;
    private List<DiaryEntry> entries;
    private final OnDiaryClickListener listener;

    public DiaryAdapter(Context context, List<DiaryEntry> entries, OnDiaryClickListener listener) {
        this.context = context;
        this.entries = entries;
        this.listener = listener;
    }

    public void setEntries(List<DiaryEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diary_entry, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry entry = entries.get(position);

        holder.tvTitle.setText(entry.getTitle());
        holder.tvDate.setText(entry.getDate());
        holder.tvPreview.setText(getPreview(entry.getContent()));
        holder.ivMood.setImageResource(getMoodIcon(entry.getMood()));

        holder.itemView.setOnClickListener(v -> listener.onClick(entry));
    }

    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }

    private String getPreview(String content) {
        if (content == null) return "";
        content = content.trim();
        if (content.length() <= 80) return content;
        return content.substring(0, 80) + "...";
    }

    private int getMoodIcon(String mood) {
        if (mood == null) return R.drawable.ic_mood_neutral;

        switch (mood.toLowerCase()) {
            case "happy":
                return R.drawable.ic_mood_happy;
            case "good":
                return R.drawable.ic_mood_good;
            case "sad":
                return R.drawable.ic_mood_sad;
            case "miserable":
                return R.drawable.ic_mood_miserable;
            default:
                return R.drawable.ic_mood_neutral;
        }
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPreview, tvDate;
        ImageView ivMood;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEntryTitle);
            tvPreview = itemView.findViewById(R.id.tvEntryPreview);
            tvDate = itemView.findViewById(R.id.tvEntryDate);
            ivMood = itemView.findViewById(R.id.ivEntryMood);
        }
    }
}
