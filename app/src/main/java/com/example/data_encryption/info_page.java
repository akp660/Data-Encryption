package com.example.data_encryption;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class info_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


// Override the onBackPressed to handle custom back press logic
    @Override
    public void onBackPressed() {
        // Handle the back press like you would in the back button click
        super.onBackPressed();
        Intent intent = new Intent(info_page.this, home_page_2.class);
        startActivity(intent);

        // Trigger the slide-in animation
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }
}