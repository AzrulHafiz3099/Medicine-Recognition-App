package com.workshop2.medrecog;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private TextView profileName;
    private TextView profileEmail;
    private TextView profileDateOfBirth;
    private TextView profilePhonenumber;
    private ImageView profilePicture;
    private ImageView updateProfilePicture;
    private ImageView datepicker;
    private ImageView imgBack;
    private Button btnSave;

    private String profilePictureName = "";  // To store the profile picture name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Initialize views
        profileName = findViewById(R.id.Fullname);
        profileEmail = findViewById(R.id.Email);
        profileDateOfBirth = findViewById(R.id.DateOfBirth);
        profilePhonenumber = findViewById(R.id.PhoneNumber);
        profilePicture = findViewById(R.id.ProfilePicture);
        updateProfilePicture = findViewById(R.id.Update_ProfilePicture);
        datepicker = findViewById(R.id.datepicker);
        btnSave = findViewById(R.id.SaveButton);
        imgBack = findViewById(R.id.img_back);

        // Handle back button behavior with the new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Close the activity
            }
        });

        imgBack.setOnClickListener(v -> {
            onBackPressed(); // Call the overridden onBackPressed method
        });

        // Fetch and display the user profile
        getUserProfile();

        updateProfilePicture.setOnClickListener(v -> openImagePicker());

        // Set up the date picker when the datepicker ImageView is clicked
        datepicker.setOnClickListener(v -> showDatePickerDialog());

        btnSave.setOnClickListener(v -> updateUserProfile());
    }

    private void showDatePickerDialog() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog to allow the user to select a date
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Profile.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay;
                    profileDateOfBirth.setText(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void getUserProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getString(R.string.api_url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if ("success".equals(status)) {
                            JSONObject data = jsonResponse.getJSONObject("data");
                            profileName.setText(data.getString("Fullname"));
                            profileEmail.setText(data.getString("Email"));
                            profileDateOfBirth.setText(data.getString("DateOfBirth"));
                            profilePhonenumber.setText(data.getString("PhoneNumber"));

                            profilePictureName = data.getString("ProfilePicture");
                            if (!profilePictureName.isEmpty()) {
                                loadProfilePictureFromStorage(profilePictureName);
                            } else {
                                profilePicture.setImageResource(R.drawable.default_profile_picture);
                            }
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("ProfileResponseError", "JSON Parsing error", e);
                        Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ProfileError", "Error occurred", error);
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "getProfile");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void updateUserProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwt_token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getString(R.string.api_url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if ("success".equals(status)) {
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("UpdateProfileResponseError", "JSON Parsing error", e);
                        Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("UpdateProfileError", "Error occurred", error);
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "updateProfile");
                params.put("fullname", profileName.getText().toString());
                params.put("email", profileEmail.getText().toString());
                params.put("phone_number", profilePhonenumber.getText().toString());
                params.put("date_of_birth", profileDateOfBirth.getText().toString());

                // Pass the updated profile picture name, or the same one if no change
                params.put("profile_picture", profilePictureName);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void loadProfilePictureFromStorage(String profilePictureName) {
        try {
            File imagesDirectory = new File(getApplicationContext().getFilesDir(), "images");
            File profilePictureFile = new File(imagesDirectory, profilePictureName);

            if (profilePictureFile.exists()) {
                profilePicture.post(() -> {
                    int size = Math.min(profilePicture.getWidth(), profilePicture.getHeight());
                    Glide.with(Profile.this)
                            .load(profilePictureFile)
                            .apply(RequestOptions.circleCropTransform())
                            .override(size, size)
                            .into(profilePicture);
                });
            } else {
                profilePicture.setImageResource(R.drawable.default_profile_picture);
            }
        } catch (Exception e) {
            Log.e("LoadImageError", "Error loading profile picture", e);
            profilePicture.setImageResource(R.drawable.default_profile_picture);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                profilePictureName = getFileName(selectedImageUri); // Update profile picture name

                saveImageToInternalStorage(selectedImageUri, profilePictureName);

                Glide.with(Profile.this)
                        .load(selectedImageUri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profilePicture);
            }
        }
    }

    private void saveImageToInternalStorage(Uri sourceUri, String fileName) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            File imagesDirectory = new File(getApplicationContext().getFilesDir(), "images");
            if (!imagesDirectory.exists()) {
                imagesDirectory.mkdirs();
            }

            File destinationFile = new File(imagesDirectory, fileName);
            try (OutputStream outputStream = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                Log.d("Image", "Image saved successfully");
                //Toast.makeText(this, "Image saved successfully!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        return result != null ? result : uri.getLastPathSegment();
    }
}
