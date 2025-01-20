package com.workshop2.medrecog;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.workshop2.medrecog.databinding.RegisterBinding;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;



public class Register extends AppCompatActivity {

    private RegisterBinding binding;
    private String profilePicFilePath; // Store the file path
    private String profilePicFileName; // Store the file name

    private Uri profilePicUri; // Add this as a member variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = RegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set onClick listener for the Register button
        binding.registerButton.setOnClickListener(this::onRegisterButtonClick);

        // Set onClick listener for the Date of Birth field
        binding.dateOfBirth.setOnClickListener(this::showDatePickerDialog);

        // Set onClick listener for the Upload button (profile picture upload)
        binding.uploadButton.setOnClickListener(v -> openImagePicker());

    }

    // Function to show the DatePickerDialog
    public void showDatePickerDialog(View view) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        String date = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay;
                        binding.dateOfBirth.setText(date);
                    }
                },
                year, month, day);

        datePickerDialog.show();
    }

    // Function to handle Register button click
    public void onRegisterButtonClick(View view) {
        String fullName = binding.fullName.getText().toString().trim();
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        String phoneNumber = binding.phoneNumber.getText().toString().trim();
        String dateOfBirth = binding.dateOfBirth.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(Register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Register.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (profilePicFileName == null || profilePicFileName.isEmpty()) {
            Toast.makeText(Register.this, "Please upload a profile picture", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getString(R.string.api_url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RegisterResponse", response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if ("success".equals(status)) {
                                String userID = jsonResponse.getString("UserID");
                                Log.d("UserID", userID);

                                // Save image to internal storage after successful registration
                                if (profilePicUri != null) {
                                    String filePath = getPathFromUri(profilePicUri);
                                    if (filePath != null) {
                                        File selectedFile = new File(filePath);
                                        saveImageToInternalStorage(selectedFile, profilePicFileName);
                                    }
                                }

                                // Create the cart for the user
                                createCart(userID);
                            } else {
                                Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Register.this, "Error parsing response register: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Register.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("RegisterError", "Error occurred", error);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "register");
                params.put("full_name", fullName);
                params.put("email", email);
                params.put("password", password);
                params.put("phone_number", phoneNumber);
                params.put("date_of_birth", dateOfBirth);
                params.put("profile_picture", profilePicFileName);
                return params;
            }
        };

        Volley.newRequestQueue(Register.this).add(stringRequest);
    }


    private void createCart(String userID) {
        String url = getString(R.string.api_cart);

        StringRequest cartRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("CartResponse", response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            Log.d("Status : ", status);

                            if ("success".equals(status)) {
                                Toast.makeText(Register.this, "User successfully registered!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Register.this, Login.class);
                                startActivity(intent);
                                finish();
                            } else {
                                String message = jsonResponse.has("message") ? jsonResponse.getString("message") : "Unknown error";
                                Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Register.this, "Error parsing response cart", Toast.LENGTH_SHORT).show();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Register.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("CartError", "Error occurred", error);
                    }
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("action", "addCart");
                params.put("UserID", userID);
                return params;
            }
        };

        Volley.newRequestQueue(Register.this).add(cartRequest);
    }

    // Method to open the image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*"); // Only allow image files
        startActivityForResult(intent, 100); // 100 is the request code for image selection
    }

    // Handle the result of the image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                profilePicUri = selectedImageUri;
                profilePicFileName = getFileName(selectedImageUri);

//                // Save the image to internal storage
//                String filePath = getPathFromUri(profilePicUri);
//                if (filePath != null) {
//                    File selectedFile = new File(filePath);
//                    saveImageToInternalStorage(selectedFile, profilePicFileName);
//                }

                // Display the selected image
                ImageView profilePictureImageView = findViewById(R.id.profile_picture);
                profilePictureImageView.setImageURI(selectedImageUri);
            }
        }
    }

    private void saveImageToInternalStorage(File sourceFile, String fileName) {
        try {
            File imagesDirectory = new File(getApplicationContext().getFilesDir(), "images");
            if (!imagesDirectory.exists()) {
                imagesDirectory.mkdirs();
            }

            File destinationFile = new File(imagesDirectory, fileName);

            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
                profilePicFilePath = destinationFile.getAbsolutePath();
                Log.d("Image", "Image saved as " + destinationFile.getAbsolutePath());
                //Toast.makeText(this, "Image saved to: " + destinationFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
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

    private String getPathFromUri(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
        }
        return path;
    }

}
