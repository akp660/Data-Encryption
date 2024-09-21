package com.example.data_encryption.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OpenFileManager {

    private static final int PICK_FILE_REQUEST = 1;

    public static void manageFile(Activity activity){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try{
            activity.startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE_REQUEST);
        }
        catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(activity, "Please install a file manager", Toast.LENGTH_SHORT).show();
        }
    }

    public static void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data) throws IOException {
        System.out.println("Activity result");
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK){
            if (data!=null){
                Uri selectedFileUri = data.getData();
                if (selectedFileUri!=null){
                    String filePath = selectedFileUri.getPath();
//                    Toast.makeText(activity.getApplicationContext(), "Selected file: " + filePath, Toast.LENGTH_SHORT).show();

                    if (BioManager.isBiometricAvailable(activity)){
                        BioManager.authenticateUser(activity, new AuthHandler() {
                            @Override
                            public void onAuthSuccess() throws IOException {
                                Toast.makeText(activity, "Encrypt file now", Toast.LENGTH_SHORT).show();
                                byte[] biometricKey = CryptoKeyGenerator.generateKeyFromBiometrics();
                                CryptoKeyGenerator.encryptFile(activity, getFileFromUri(activity, selectedFileUri), biometricKey);

                            }

                            @Override
                            public void onAuthFailure() throws IOException {
                                Toast.makeText(activity, "File not encrypted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        Toast.makeText(activity, "Biometrics not available", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(activity, "No file selected", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(activity, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(activity, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    public static File getFileFromUri(Context context, Uri uri) throws IOException {
        // Create a temporary file in the cache directory
        File tempFile = File.createTempFile("tempFile", null, context.getCacheDir());
        tempFile.deleteOnExit();

        // Use content resolver to open the input stream and write to the temporary file
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tempFile;  // Return the temp file with the contents of the Uri
    }
}
