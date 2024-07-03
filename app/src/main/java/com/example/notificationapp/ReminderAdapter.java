package com.example.notificationapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private final List<Reminder> reminderList;
    private final OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Reminder reminder);
    }

    public ReminderAdapter(List<Reminder> reminderList, OnDeleteClickListener onDeleteClickListener) {
        this.reminderList = reminderList;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        holder.nameTextView.setText(reminder.getName());
        holder.dateTextView.setText(reminder.getDate());
        holder.deleteButton.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(reminder));
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView dateTextView;
        Button deleteButton;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
