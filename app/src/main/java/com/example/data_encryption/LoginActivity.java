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
import com.example.data_encryption.utils.RSAKeyManager;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    TextView signup;
    CardView homepage;
    EditText passwordField, useridField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signup = findViewById(R.id.signUp);
        homepage = findViewById(R.id.cardView3);

        useridField = findViewById(R.id.userid);
        passwordField = findViewById(R.id.password);

        homepage.setOnClickListener(view -> handleLogin());

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                triggerVibration();
            }
        });
    }

    private void handleLogin() {
        String email = useridField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Log.d("LoginActivity", "Validation failed: One or more fields are empty");
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (RSAKeyManager.doesKeyPairExist(email)) {
            Log.d("LoginActivity", "EXISTING KEY PAIR");
        } else {
            Log.d("LoginActivity", "NON EXISTING KEY PAIR");
            Toast.makeText(this, "Key pair not found. Please sign up.", Toast.LENGTH_SHORT).show();
            return; // Stop further login process
        }

        ApiRequestManager apiRequestManager = new ApiRequestManager(this);
        Log.d("LoginActivity", "Making API request with username " + email + " and password " + password);

        apiRequestManager.authenticate(email, password, new ApiResponseListener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                Log.d("LoginActivity", "API success response: " + response);
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                String token = response.getString("token");

                saveTokenAndEmail(token, email);

                Intent intent = new Intent(LoginActivity.this, home_page_2.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onError(VolleyError error) {
                Log.d("LoginActivity", "API error: " + error.getMessage());

                if (error.networkResponse != null) {
                    int statusCode = error.networkResponse.statusCode;
                    String errorMessage;
                    if (statusCode == 401) {
                        errorMessage = "Incorrect password.";
                    } else if (statusCode == 404) {
                        errorMessage = "User not found.";
                    } else {
                        errorMessage = "Login failed: " + new String(error.networkResponse.data);
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: Unknown error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveTokenAndEmail(String token, String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("userName", email);
        editor.apply();
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
