package com.workshop2.medrecog;

import android.app.Notification;
import android.content.Intent;
import android.util.Log;
import android.app.NotificationManager;
import android.content.Context;
import android.widget.Toast;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReminderWorker extends Worker {

    private String patientID, reminderID, name, geenericName,dosageForm,dosageUsage;

    public ReminderWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        String title = getInputData().getString("title");
        String description = getInputData().getString("description");
        patientID = getInputData().getString("patientID");
        reminderID = getInputData().getString("reminderID");
        name = getInputData().getString("name");
        geenericName = getInputData().getString("genericName");
        dosageForm = getInputData().getString("dosageForm");
        dosageUsage = getInputData().getString("dosageUsage");


        Log.d("ReminderWorker", "Title: " + title);
        Log.d("ReminderWorker", "Description: " + description);
        Log.d("ReminderWorker", "Patient ID: " + patientID);
        Log.d("ReminderWorker", "Reminder ID: " + reminderID);
        Log.d("ReminderWorker", "Name: " + name);

        // Trigger alarm or notification here
        sendNotification(title, description);

        return Result.success();
    }

    private void sendNotification(String title, String description) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "reminder_channel")
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description + "\nReminder for : " + name + "\nPlease take medication immediately." + "\nMedicine : " + geenericName + "\nRecommended Dosage : " + dosageUsage))
                .setSmallIcon(R.drawable.ic_notification)
                .build();

        notificationManager.notify(0, notification);
        updateReminder();
    }

    private void updateReminder() {
        String url = getApplicationContext().getString(R.string.api_reminder);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if ("success".equals(status)) {
                            Log.d("ReminderWorker", "Reminder updated successfully");
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(getApplicationContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getApplicationContext(), "Error adding reminder", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updateReminder");
                params.put("PatientID", patientID);
                params.put("ReminderID", reminderID);
                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }
}