package com.workshop2.medrecog;

import android.Manifest;
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
                        String extractedText = visionText.getText();
                        if (!extractedText.isEmpty()) {
                            Toast.makeText(this, "Extracted Text: " + extractedText, Toast.LENGTH_LONG).show();

                            // Send the extracted text to the Search activity
                            Intent intent = new Intent(MedicineReco.this, Search.class);
                            intent.putExtra("extractedText", extractedText); // Pass the extracted text
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "No text found in the image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("TextRecognition", "Error: " + e.getMessage()));
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
