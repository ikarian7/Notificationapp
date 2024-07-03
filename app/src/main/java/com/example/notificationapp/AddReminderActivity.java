package com.example.notificationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddReminderActivity extends AppCompatActivity {

    private EditText nameEditText;
    private DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        nameEditText = findViewById(R.id.nameEditText);
        datePicker = findViewById(R.id.datePicker);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();

            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth();
            int year = datePicker.getYear();

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String date = dateFormat.format(calendar.getTime());

            Intent resultIntent = new Intent();
            resultIntent.putExtra("name", name);
            resultIntent.putExtra("date", date);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
