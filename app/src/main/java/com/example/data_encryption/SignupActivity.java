package com.example.data_encryption;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.VolleyError;
import com.example.data_encryption.ApiDirectory.ApiRequestManager;
import com.example.data_encryption.ApiDirectory.ApiResponseListener;
import com.example.data_encryption.ApiDirectory.model.UserModel;
import com.example.data_encryption.utils.RSAKeyManager;

public class SignupActivity extends AppCompatActivity {

    private TextView login;
    private CardView signUpBtn;
    private EditText nameField, emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        login = findViewById(R.id.logIn);
        signUpBtn = findViewById(R.id.cardView3);
        nameField = findViewById(R.id.name);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);

        // Check if RSA Key Pair exists
        if (!RSAKeyManager.doesKeyPairExist()) {
            RSAKeyManager.generateRSAKeyPair(this);
            Toast.makeText(this, "RSA Key Pair generated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "RSA Key Pair already exists", Toast.LENGTH_SHORT).show();
        }

        login.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            triggerVibration();
        });

        signUpBtn.setOnClickListener(view -> handleSignUp());
    }

    private void handleSignUp() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        Log.d("SignupActivity", "Name: " + name + ", Email: " + email + ", Password: " + password);

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Log.d("SignupActivity", "Validation failed: One or more fields are empty");
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve RSA public key
        String rsaPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuIKFgdbw3Tn0lcZ4xRJjTytn5LsIp2P6s5W8wDh6x9eP2BhBlxFgqOqJ2KUf4sYI9dZ/vHupNcvC8vb/Bf+KoB8emqH5c+wLNiRIH5z3UnlHJW9kjcJ4jb3MI4ON7YX5m1PCmXr2LRgrB4rU1BcA5sCMsDvbFgByxu5ObKH1tdM9+jfNFHTzNc+lznTx0dwbgGogD8DHjcB1kxuLFSrKfErUNkH9OSJQ1U8T9Fb0ErmjO8rlAeSmUanHE/ebFGnJihpB1dVwckFCefj06z6qlbVNZUL7AzQf8DrW24D6g+/T5/hh3jKrkWlfYzL4uFVRt8V06jKG3S7W8gD3Osm0OQIDAQAB";
        if (rsaPublicKey == null) {
            Log.d("SignupActivity", "RSA public key retrieval failed");
            Toast.makeText(this, "Failed to retrieve RSA public key", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Log.d("SignupActivity", "RSA Public Key: " + rsaPublicKey);
        }

        UserModel user = new UserModel(name, email, rsaPublicKey, password);

        // Send user data to the API
        ApiRequestManager apiRequestManager = new ApiRequestManager(this);
        Log.d("SignupActivity", "Making API request with UserModel: " + user.toString());

        apiRequestManager.signUp(user, new ApiResponseListener<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d("SignupActivity", "API success response: " + response);
                Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }

            @Override
            public void onError(VolleyError error) {
                Log.d("SignupActivity", "API error: " + error.getMessage());
                Toast.makeText(SignupActivity.this, "Signup failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Save name in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName", name);
        editor.apply();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void triggerVibration() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }
}
