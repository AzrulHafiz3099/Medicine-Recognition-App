package com.workshop2.medrecog;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Drugreminder extends AppCompatActivity {

    private TextView dateTextView, timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drugreminder);

        // Initialize the TextViews
        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);

        // Initialize a Calendar instance
        Calendar calendar = Calendar.getInstance();

        // Set default date and time
        updateDate(calendar);
        updateTime(calendar);

        // Set OnClickListener for Date TextView
        dateTextView.setOnClickListener(v -> {
            Calendar currentDate = Calendar.getInstance();
            new DatePickerDialog(Drugreminder.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        updateDate(calendar);
                    },
                    currentDate.get(Calendar.YEAR),
                    currentDate.get(Calendar.MONTH),
                    currentDate.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Set OnClickListener for Time TextView
        timeTextView.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            new TimePickerDialog(Drugreminder.this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        updateTime(calendar);
                    },
                    currentTime.get(Calendar.HOUR_OF_DAY),
                    currentTime.get(Calendar.MINUTE),
                    false // Use 12-hour format
            ).show();
        });
    }

    // Update the date TextView
    private void updateDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        dateTextView.setText(dateFormat.format(calendar.getTime()));
    }

    // Update the time TextView
    private void updateTime(Calendar calendar) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        timeTextView.setText(timeFormat.format(calendar.getTime()));
    }
}
