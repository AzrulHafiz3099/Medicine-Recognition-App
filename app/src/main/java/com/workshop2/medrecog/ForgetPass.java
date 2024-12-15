package com.workshop2.medrecog;

import android.content.Intent;
import android.os.Bundle;  // Import Bundle class
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgetPass extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpass);  // Ensure you're setting the correct layout here
    }

    public void onLoginButtonClick(View view) {
        // You may want to show a Toast for debugging purposes to verify button click
        Toast.makeText(this, "Redirecting to Login", Toast.LENGTH_SHORT).show();

        // Intent to navigate to the Login activity
        Intent intent = new Intent(ForgetPass.this, Login.class);
        startActivity(intent);  // Start the Login activity
    }
}
