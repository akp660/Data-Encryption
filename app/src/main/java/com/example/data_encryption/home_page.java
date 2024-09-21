package com.example.data_encryption;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.data_encryption.utils.OpenFileManager;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class home_page extends AppCompatActivity {

    MaterialButton uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        uploadButton = findViewById(R.id.materialCardView).findViewById(R.id.uploadButton);

        setOnClickListeners();
        System.out.println("started");

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
        try {
            OpenFileManager.handleActivityResult(home_page.this, requestCode, resultCode, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}