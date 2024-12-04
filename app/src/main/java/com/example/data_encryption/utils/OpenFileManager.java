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

import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.SecretKey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import javax.crypto.SecretKey;

public class OpenFileManager {

    private static final int PICK_FILE_REQUEST = 1;

    public static void manageFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        activity.startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    public static void manageFile(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        fragment.startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    public static boolean handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data, FileSelectedCallback callback) {
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                if (BioManager.isBiometricAvailable(activity)) {
                    BioManager.authenticateUser(activity, new AuthHandler() {
                        @Override
                        public void onAuthSuccess() {
                            try {
                                Toast.makeText(activity, "Encrypting file now...", Toast.LENGTH_SHORT).show();

                                SecretKey secretKey = CryptoKeyGenerator.generateAESKey();
                                CryptoKeyGenerator.encryptFile(selectedFileUri, secretKey, activity.getApplicationContext());

                                callback.onFileSelected(true); // Notify encryption success
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(activity, "Encryption failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                callback.onFileSelected(false); // Notify encryption failure
                            }
                        }

                        @Override
                        public void onAuthFailure() {
                            Toast.makeText(activity, "Authentication failed. File not encrypted.", Toast.LENGTH_SHORT).show();
                            callback.onFileSelected(false);
                        }
                    });
                    return true;
                } else {
                    Toast.makeText(activity, "Biometrics not available", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    // Callback interface for notifying file selection results
    public interface FileSelectedCallback {
        void onFileSelected(boolean isSuccess);
    }
}

