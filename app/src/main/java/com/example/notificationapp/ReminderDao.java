package com.example.notificationapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert
    long insert(Reminder reminder);

    @Query("SELECT * FROM reminders ORDER BY date")
    List<Reminder> getAllReminders();

    @Delete
    void delete(Reminder reminder);
}
