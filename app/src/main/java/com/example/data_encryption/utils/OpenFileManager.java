package com.example.data_encryption.utils;

import static com.example.data_encryption.utils.CryptoKeyGenerator.encryptFile;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.SecretKey;

public class OpenFileManager {

    private static final int PICK_FILE_REQUEST = 1;

    public static void manageFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            activity.startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, "Please install a file manager", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    if (BioManager.isBiometricAvailable(activity)) {
                        BioManager.authenticateUser(activity, new AuthHandler() {
                            @Override
                            public void onAuthSuccess() {
                                try {
                                    FileImageHandler.setDefaultFileData(activity, true);
                                    Toast.makeText(activity, "Encrypting file now...", Toast.LENGTH_SHORT).show();

                                    SecretKey secretKey = CryptoKeyGenerator.generateAESKey();
                                    CryptoKeyGenerator.encryptFile(selectedFileUri, secretKey, activity.getApplicationContext());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(activity, "Encryption failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onAuthFailure() {
                                Toast.makeText(activity, "Authentication failed. File not encrypted.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return true;
                    } else {
                        Toast.makeText(activity, "Biometrics not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, "No file selected", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

}
