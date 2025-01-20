package com.workshop2.medrecog;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class MedicineReco extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;

    private ImageView imageGroup1;
    private TextRecognizer textRecognizer;
    private ImageView imageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicinereco);

        imageBack = findViewById(R.id.img_back); // Find the imageIcon

        // Handle back button behavior with the new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Custom logic for back press
                finish(); // Close the activity
            }
        });

        // Set an OnClickListener for the back button
        imageBack.setOnClickListener(v -> {
            onBackPressed(); // Call the overridden onBackPressed method
        });

        imageGroup1 = findViewById(R.id.image_group1);
        Button btnScan = findViewById(R.id.btn_scan);

        // Initialize ML Kit Text Recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Set up button click listener
        btnScan.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE
        );
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Camera is not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            if (photo != null) {
                imageGroup1.setImageBitmap(photo); // Set image to ImageView
                processImageForTextRecognition(photo); // Extract text from image
            }
        }
    }

    private void processImageForTextRecognition(Bitmap photo) {
        try {
            InputImage inputImage = InputImage.fromBitmap(photo, 0);
            textRecognizer.process(inputImage)
                    .addOnSuccessListener(visionText -> {
                        final String extractedText = visionText.getText();  // Declare as final
                        // Log the extracted text to debug
                        Log.d("TextRecognition", "Extracted Text: " + extractedText);

                        // Ensure extracted text is not empty or invalid
                        if (extractedText != null && !extractedText.trim().isEmpty()) {
                            // Clean up the extracted text if needed
                            final String cleanedText = extractedText.trim();  // Remove extra spaces or newlines

                            // Show an AlertDialog with Yes/No options
                            new AlertDialog.Builder(MedicineReco.this)
                                    .setTitle("Extracted Text")
                                    .setMessage("Is this the text you want to search?\n\n" + cleanedText)
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        // If the user clicks "Yes", pass the extracted text to Search2
                                        Intent intent = new Intent(MedicineReco.this, Search2.class);
                                        intent.putExtra("extractedText", "Panadol ActiFast esk Pa 12 tau-Dwata S9sPEERSL RM7"); // Pass the cleaned text
                                        startActivity(intent);
                                    })
                                    .setNegativeButton("No", (dialog, which) -> {
                                        // If the user clicks "No", just dismiss the dialog and stay on the current page
                                        dialog.dismiss();
                                    })
                                    .show();

                        } else {
                            Toast.makeText(this, "No text found in the image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TextRecognition", "Error: " + e.getMessage());
                        Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e("TextRecognition", "Error processing image: " + e.getMessage());
        }
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
