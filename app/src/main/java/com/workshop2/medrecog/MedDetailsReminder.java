package com.workshop2.medrecog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MedDetailsReminder extends AppCompatActivity {

    private String patientID, title, description, date, time, symptomID, drugID, name, dosageUsageText, dosageFormText,  genericNameText;
    private ImageView drugImage, imgBack;
    private TextView genericName, dosage, dosageForm, dosageUsage;
    private Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meddetailsreminder);

        drugImage = findViewById(R.id.img_drugImage);
        imgBack = findViewById(R.id.img_back);
        genericName = findViewById(R.id.txtGenericName);
        dosage = findViewById(R.id.txt_Dosage);
        dosageForm = findViewById(R.id.txt_DosageForm);
        dosageUsage = findViewById(R.id.txt_DosageUsage);
        btnAdd = findViewById(R.id.btnAdd);

        // Create Notification Channel (For Android 8.0 and higher)
        createNotificationChannel();

        // Get intent data
        Intent intent = getIntent();
        patientID = intent.getStringExtra("patientID");
        name = intent.getStringExtra("name");
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");
        symptomID = intent.getStringExtra("symptomID");
        drugID = intent.getStringExtra("drugID");

        Log.d("MedDetailsReminder", "Patient ID: " + patientID);
        Log.d("MedDetailsReminder", "Name: " + name);
        Log.d("MedDetailsReminder", "Title: " + title);
        Log.d("MedDetailsReminder", "Description: " + description);
        Log.d("MedDetailsReminder", "Date: " + date);
        Log.d("MedDetailsReminder", "Time: " + time);
        Log.d("MedDetailsReminder", "Symptom ID: " + symptomID);
        Log.d("MedDetailsReminder", "Drug ID: " + drugID);

        fetchDrugDetails(drugID);

        imgBack.setOnClickListener(v -> {
            Intent intent1 = new Intent(MedDetailsReminder.this, Addreminder.class);
            intent1.putExtra("patientID", patientID);
            intent1.putExtra("name", name);
            intent1.putExtra("title", title);
            intent1.putExtra("description", description);
            intent1.putExtra("date", date);
            intent1.putExtra("time", time);
            intent1.putExtra("symptomID", symptomID);
            startActivity(intent1);
        });

        btnAdd.setOnClickListener(v -> addReminder());
    }

    private void fetchDrugDetails(String drugID) {
        String url = getString(R.string.api_drug_details);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            JSONObject drugDetails = jsonResponse.getJSONObject("drugDetails");

                            // Build the image URL
                            String imageUrl = getString(R.string.drug_image_url) + drugDetails.getString("DrugImage");
                            Log.d("imageurl", "imageurl: " + imageUrl);

                            genericNameText = drugDetails.getString("GenericName");
                            String dosageText = drugDetails.getString("Dosage");
                            dosageFormText = drugDetails.getString("Dosage_Form");
                            dosageUsageText = drugDetails.getString("DosageUsage");

                            // Load the image using Glide
                            Glide.with(MedDetailsReminder.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.default_profile_picture) // Placeholder image
                                    .into(drugImage);

                            genericName.setText(genericNameText);
                            dosage.setText(dosageText);
                            dosageForm.setText(dosageFormText);
                            dosageUsage.setText(dosageUsageText);

                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(MedDetailsReminder.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(MedDetailsReminder.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(MedDetailsReminder.this, "Error fetching drug details", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getDrugDetails");
                params.put("DrugID", drugID);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void addReminder() {
        String url = getString(R.string.api_reminder); // Replace with your actual API URL

        // Convert the date to the required format
        String formattedDate = convertDateToDatabaseFormat(date);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            String reminderID = jsonResponse.getString("ReminderID");
                            Log.d("ReminderID", "Reminder ID: " + reminderID);

                            // Get the reminder time and convert it to the delay
                            long delayMillis = calculateDelay(time); // You'll need to calculate the delay in milliseconds

                            // Create input data for the worker
                            Data inputData = new Data.Builder()
                                    .putString("title", title)
                                    .putString("description", description)
                                    .putString("patientID", patientID)
                                    .putString("reminderID", reminderID)
                                    .putString("name", name)
                                    .putString("genericName", genericNameText)
                                    .putString("dosageForm", dosageFormText)
                                    .putString("dosageUsage", dosageUsageText)
                                    .build();

                            // Create a OneTimeWorkRequest to schedule the alarm
                            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                                    .setInputData(inputData)
                                    .build();

                            // Schedule the work
                            WorkManager.getInstance(MedDetailsReminder.this).enqueue(workRequest);

                            Toast.makeText(MedDetailsReminder.this, "Reminder added successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MedDetailsReminder.this, Drugreminder.class);
                            startActivity(intent);

                        } else {
                            String message = jsonResponse.getString("message");
                            Log.d("Response", response);
                            Log.d("MedDetailsReminder", "message " + message);
                            Toast.makeText(MedDetailsReminder.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(MedDetailsReminder.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(MedDetailsReminder.this, "Error adding reminder", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "addReminder");
                params.put("PatientID", patientID);
                params.put("Title", title);
                params.put("Description", description);
                params.put("GenericName", genericName.getText().toString());
                params.put("ReminderDate", formattedDate);
                params.put("ReminderTime", convertTo24HourFormat(time)); // Convert time to 24-hour format
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }


    private String convertDateToDatabaseFormat(String inputDate) {
        try {
            // Parse the input date
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            Date date = inputFormat.parse(inputDate);

            // Format it for the database
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            // Return the original date if parsing fails
            return inputDate;
        }
    }

    private String convertTo24HourFormat(String time) {
        try {
            // Parse the 12-hour time format (e.g., 8:33 PM)
            SimpleDateFormat inputFormat = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
            Date date = inputFormat.parse(time);

            // Format it to 24-hour time format (e.g., 20:33:00)
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return time; // Return the original time if parsing fails
        }
    }


    private long calculateDelay(String reminderTime) {
        try {
            String formattedDate = convertDateToDatabaseFormat(date);

            String formattedReminderTime = convertTo24HourFormat(reminderTime);
            // Combine the date and time into a single datetime string
            String combinedDateTime = formattedDate + " " + formattedReminderTime; // 'date' should already be in "yyyy-MM-dd" format
            Log.d ("CombinedDateTime", combinedDateTime);
            // Parse the combined datetime
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
            Date reminderDate = format.parse(combinedDateTime);

            // Calculate the delay in milliseconds from the current time
            if (reminderDate != null) {
                long currentTimeMillis = System.currentTimeMillis();
                long reminderTimeMillis = reminderDate.getTime();

                // Ensure the reminder time is in the future, if not, adjust to the next day
                if (reminderTimeMillis < currentTimeMillis) {
                    reminderTimeMillis += TimeUnit.DAYS.toMillis(1); // Add one day if the reminder time is in the past
                }

                return reminderTimeMillis - currentTimeMillis;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // Default delay if there's an error
    }


    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Reminder Channel";
            String description = "Channel for medication reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("reminder_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
