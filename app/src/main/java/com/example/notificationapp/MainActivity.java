package com.example.notificationapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_REMINDER_REQUEST = 1; // Request code for startActivityForResult
    private static final int NOTIFICATION_ID = 1; // Unique notification ID
    private static final int REQUEST_ALARM_PERMISSION = 100; // Request code for alarm permission

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList;

    private ReminderDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = ReminderDatabase.getInstance(this);
        reminderList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReminderAdapter(reminderList, new ReminderAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(Reminder reminder) {
                deleteReminder(reminder);
            }
        });
        recyclerView.setAdapter(adapter);

        loadReminders();

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start AddReminderActivity
                Intent intent = new Intent(MainActivity.this, AddReminderActivity.class);
                startActivityForResult(intent, ADD_REMINDER_REQUEST);
            }
        });

        // Check and request alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmPermissionGranted()) {
                requestAlarmPermission();
            }
        }
    }

    private boolean alarmPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return getSystemService(AlarmManager.class).canScheduleExactAlarms();
        }
        return true;
    }

    private void requestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }

    private void loadReminders() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                reminderList.clear();
                reminderList.addAll(database.reminderDao().getAllReminders());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_REMINDER_REQUEST && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            String date = data.getStringExtra("date");

            if (name != null && date != null) {
                Reminder reminder = new Reminder(name, date);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        database.reminderDao().insert(reminder);
                        loadReminders();

                        try {
                            scheduleNotification(parseDate(date), name);
                        } catch (ParseException e) {
                            Log.e("MainActivity", "Failed to parse date", e);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Failed to parse date", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        }
    }

    private void scheduleNotification(Date date, String name) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Set time to midday (12:00 PM)
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Subtract 1 day
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        // Create an intent for NotificationReceiver
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra("name", name);

        // Create a PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Get AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // Schedule the alarm
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private Date parseDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.parse(date);
    }

    private void deleteReminder(Reminder reminder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.reminderDao().delete(reminder);
                loadReminders();

                // Optionally, cancel the scheduled notification for this reminder
                cancelNotification(reminder);
            }
        }).start();
    }

    private void cancelNotification(Reminder reminder) {
        // Create an intent for NotificationReceiver
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra("name", reminder.getName());

        // Create a PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Get AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // Cancel the alarm
            alarmManager.cancel(pendingIntent);
        }
    }
}
