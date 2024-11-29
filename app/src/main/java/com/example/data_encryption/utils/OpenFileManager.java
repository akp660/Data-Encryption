package com.example.data_encryption.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
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

    public static boolean handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data) throws IOException {
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    if (BioManager.isBiometricAvailable(activity)) {
                        BioManager.authenticateUser(activity, new AuthHandler() {
                            @Override
                            public void onAuthSuccess() throws IOException {
                                FileImageHandler.setDefaultFileData(activity, true);
                                Toast.makeText(activity, "Encrypting file now...", Toast.LENGTH_SHORT).show();

                                // Generate biometric key
                                byte[] biometricKey = CryptoKeyGenerator.generateKeyFromBiometrics();
                                if (biometricKey == null) {
                                    Toast.makeText(activity, "Failed to generate biometric key", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Generate SecretKey from biometric key bytes
                                SecretKey aesKey = CryptoKeyGenerator.generateSecretKeyFromBytes(biometricKey);
                                if (aesKey == null) {
                                    Toast.makeText(activity, "Failed to generate AES SecretKey", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Get the selected file
                                File selectedFile = getFileFromUri(activity, selectedFileUri);
                                if (selectedFile != null) {
                                    // Encrypt the selected file
                                    // Assuming encryptFile() performs in-place encryption and does not return a File
                                    CryptoKeyGenerator.encryptFile(activity, selectedFile, biometricKey);

// Proceed with further logic
                                    String rsaPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuIKFgdbw3Tn0lcZ4xRJjTytn5LsIp2P6s5W8wDh6x9eP2BhBlxFgqOqJ2KUf4sYI9dZ/vHupNcvC8vb/Bf+KoB8emqH5c+wLNiRIH5z3UnlHJW9kjcJ4jb3MI4ON7YX5m1PCmXr2LRgrB4rU1BcA5sCMsDvbFgByxu5ObKH1tdM9+jfNFHTzNc+lznTx0dwbgGogD8DHjcB1kxuLFSrKfErUNkH9OSJQ1U8T9Fb0ErmjO8rlAeSmUanHE/ebFGnJihpB1dVwckFCefj06z6qlbVNZUL7AzQf8DrW24D6g+/T5/hh3jKrkWlfYzL4uFVRt8V06jKG3S7W8gD3Osm0OQIDAQAB";
                                    String encryptedAESKey = CryptoKeyGenerator.encryptAESKeyWithRSA(biometricKey, rsaPublicKey);
                                    if (encryptedAESKey != null) {
                                        // Call the combine method
                                        CryptoKeyGenerator.combineEncryptedFileAndKeyPreserveFormatAndDownload(activity, selectedFile, encryptedAESKey, selectedFileUri);
                                        Toast.makeText(activity, "File encrypted and saved successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(activity, "Failed to encrypt AES key", Toast.LENGTH_SHORT).show();
                                    }


// Now combine the encrypted file and key
//                                    Uri selectedFileUri = data.getData(); // Ensure correct Uri
//                                    CryptoKeyGenerator.combineEncryptedFileAndKeyPreserveFormatAndDownload(activity, selectFile, encryptedAESKey, selectedFileUri);

// Notify user
                                    Toast.makeText(activity, "File encrypted and saved successfully!", Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(activity, "Failed to retrieve selected file", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onAuthFailure() throws IOException {
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

    private static File getFileFromUri(Context context, Uri uri) throws IOException {
        // Open the input stream from the provided URI
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Unable to open InputStream from URI");
        }

        // Create a temporary file in the app's cache directory
        File tempFile = new File(context.getCacheDir(), "temp_file");
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        // Buffer for reading and writing data
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }

        // Close streams to release resources
        outputStream.close();
        inputStream.close();

        return tempFile;
    }

    // Now, pass the context into this method to access the ContentResolver
    public static String getFileNameFromUri(Context context, Uri uri) {
        String fileName = null;
        if (uri != null) {
            ContentResolver contentResolver = context.getContentResolver(); // Use the passed context
            // Query the URI to get the file name
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);  // Get the file name from the cursor
                    }
                }
            } catch (Exception e) {
                Log.e("FilePicker", "Error retrieving file name: " + e.getMessage());
            }
        }
        return fileName;
    }
}
