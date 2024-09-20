package com.example.data_encryption;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.data_encryption.utils.OpenFileManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class home_page extends AppCompatActivity {

    MaterialButton uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        uploadButton = findViewById(R.id.materialCardView).findViewById(R.id.uploadButton);

        setOnClickListeners();

    }

    public void setOnClickListeners(){
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenFileManager.manageFile(home_page.this);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        OpenFileManager.handleActivityResult(home_page.this, requestCode, resultCode, data);
    }

}