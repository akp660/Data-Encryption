package com.example.data_encryption;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.data_encryption.utils.BioManager;
import com.example.data_encryption.utils.OpenFileManager;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    View downloadButton, newFileButton, bioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadButton = findViewById(R.id.downloadButton);
        newFileButton = findViewById(R.id.newFileButton);
        bioButton = findViewById(R.id.bioButton);
        
        setOnClickFunctions();

    }

    public void setOnClickFunctions(){
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Download files", Toast.LENGTH_SHORT).show();
            }
        });

        newFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenFileManager.manageFile(MainActivity.this);
            }
        });

//        bioButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (BioManager.isBiometricAvailable(MainActivity.this)){
//                    BioManager.authenticateUser(MainActivity.this);
//                }
//                else{
//                    Toast.makeText(MainActivity.this, "Biometrics not available on this device", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
//        super.onActivityResult(requestCode,resultCode,data);
//        try {
//            OpenFileManager.handleActivityResult(MainActivity.this, requestCode, resultCode, data);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}