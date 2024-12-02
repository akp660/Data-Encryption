package com.example.data_encryption;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Use a Handler to introduce a delay of 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // After the delay, start the Main Activity
                Intent intent = new Intent(Splash_Screen.this, home_page_2.class);
                startActivity(intent);
                finish(); // Close SplashScreenActivity so the user can't navigate back to it
            }
        }, 3000); // 2000 milliseconds = 2 seconds

    }
}