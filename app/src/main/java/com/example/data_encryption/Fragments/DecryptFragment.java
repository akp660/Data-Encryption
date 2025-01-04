package com.example.data_encryption.Fragments;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.data_encryption.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DecryptFragment extends Fragment {
    private static final int PICK_FILE_REQUEST = 1;
    private CardView cardUpload;
    private Uri selectedFileUri;
    private static final int RSA_KEY_LENGTH = 256; // Length of encrypted AES key (for RSA 2048-bit)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_decrypt, container, false);
        cardUpload = view.findViewById(R.id.cardView);
        cardUpload.setOnClickListener(v -> {
            triggerVibration();
            openFileManager();
        });
        return view;
    }
    private void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                try {
                    // Read and separate the combined file
                    separateCombinedFile(selectedFileUri);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error processing file: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void separateCombinedFile(Uri fileUri) {
        try {
            // Get content resolver
            Context context = getContext();
            if (context == null) return;

            // Read the combined file
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show();
                return;
            }

            String filename = getFileNameFromUri(context, fileUri);
            if (filename == null || filename.isEmpty()) {
                Toast.makeText(context, "File name could not be determined", Toast.LENGTH_LONG).show();
                Log.e("stepbystep", "File name is null or empty");
                return;
            }
            // Read all data into byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] combinedData = byteArrayOutputStream.toByteArray();
            inputStream.close();
            // Separate the data
            if (combinedData.length > RSA_KEY_LENGTH) {
                // Extract encrypted file content and encrypted AES key
                byte[] encryptedFileContent = Arrays.copyOfRange(combinedData, 0,
                        combinedData.length - RSA_KEY_LENGTH);
                byte[] encryptedAESKey = Arrays.copyOfRange(combinedData,
                        combinedData.length - RSA_KEY_LENGTH, combinedData.length);
                // Log the sizes for debugging
                Log.d("DecryptFragment", "Combined file size: " + combinedData.length);
                Log.d("DecryptFragment", "Encrypted content size: " + encryptedFileContent.length);
                Log.d("DecryptFragment", "Encrypted AES key size: " + encryptedAESKey.length);

                // separated the encrypted file content and the encrypted AES key
                startDecryption(encryptedFileContent, encryptedAESKey,filename);
            } else {
                Toast.makeText(context, "Invalid file format or corrupted file",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error separating file: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
    private void startDecryption(byte[] encryptedContent, byte[] encryptedAESKey,String filename) {
        try {
            // Step 1: Decrypt AES key using RSA private key
//            byte[] rsaPrivateKey = loadRSAPrivateKey(); // Load your RSA private key
            String base64PrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCNtdiUmPwBos20JyLKwgWDx39Nm/3qKlGQoiHk1yc1HUiDF6kxEV6UJb2bBDQGTLN88LhIjfk73FkZiLQg44VsJhQ4IwMNMxkbgD2RBRZzU+82Yn35pt6d6Lp1xHw3KvdETpYvYzWD5S+3XVEQ93txcvzK1Sg6z4sNL7TEdlUbJN+1iROeKqKDxBNM54IviSyAHIdG51Awe2Fxt9cIdo/FPZJDqoLX0lUdav1WIItI+OZ5kaDLznFLdXBJpf4Ld90RPCK1O2wPwGBJlPj5TUTu0zBxN/4KJkT8cuVzBBjXOv594LeAjJEMl5wPGAQ6JkmK1ADzH63qN0HtviarXzO1AgMBAAECggEAQ9xCeDpgZ4omVnBtTUonKOlmUWxzZejZAVlawMK1QE6jFD/G4jvuniQKjYqwPRoK07hUj04JfXxx4HPhymQZ1uXPReiiQ/ZlvhElqmYUz6pwgYsdx2j87wJtFAtA7owxN7EoY03vaRddx8G0mdrhbr0BSEJWDv+nF2vydSgzbhj+ZnXpIfApfU4jiVmsQqpBc4gQEgPNH8exYl4zmTRmzJhscrgIKp7M1WxepPWFDtnNP/zkjLSdMJXrkV3dkmxHlIWnL0ZY5EYqFvhk7MaZjXLkr6u4Ii560COk2Pd7QgxoHbGL7nLGf46EfW07I2fg7Q97Ew+cvfnE5XBF5krGWwKBgQC8AQDbmR7m22fzDAAQiryTInAGvi3YWqXmjwUktzjdLd8kLEvc4/BzVYY48xJMNpN1gYG5/zK6+ONpnkOG0EERKptSEFhifg/1D3Q3gvxWWi1uoA/amP6D8mN4ORw0gw2hVzF8stofvCmZukhwji/0iNwlcsfRJRG0OW/LkaPpEwKBgQDA9pcb5BjAoCkvlLaJbFTJL1QwFm1xOz206j8s2+Ncd9TdBwPJYtcdaXC2+2fLoxCDJAlB21OVW5yZvZeJZA7hIrUxqF6lOyZCGPxOa6Nix2XMwmKMoRojyvs5v+R9ETKN9355L3RqhD1gMz8Wsu6wr7l2Vw7vWfQV5Pp6KIkRFwKBgAvMgFJH1NGmOWreeO6Q1m7hfWhe7R+j6L+EgE0ilpYC9/scMJKnV4LVfjv6vU49Kpn4S7zxkCx9zD/np9NBJKRAKUlIL1PXF4dItgF53f5JYIqNzxDoAykiwC1eYC/HfcZ/Y2KxEtFlDLNSJpOxyL6vDCnpfzLYAblu1V5QQJozAoGASCguC4z5QJbjr9pBhBQRhIYBSlYoqM4JXiy2YRT86WgaHmjwHo+qd2Ildxd+EeUxWIjSOWFF2TU/0zHVh9f1xHSRIzed5NXAkbj8KGsR1u9Pfwk1hvb7amUOGuNKEwaqS/I/xhtbwjUfKmkfb2KL5WBgzwLxH8oYf1N34tRjpRMCgYEAr82RbjTuqV3GtyquhgfvnHOq0RWSIFfHInW7x8SIKCROUqcpdyX11jr+3NDTFJ1tnGJp19Vf86MrXM/L6j2f4U+zclAJTqljYmqbLr0lS7lmcwXTmCY2q7LVVAH+1TkW9BxzoGaIht2vlTErOYIR9wlXRwuiDd7obJ27fWuy/yU=";
            byte[] rsaPrivateKey = Base64.decode(base64PrivateKey, Base64.DEFAULT);
            byte[] aesKey = decryptAESKeyWithRSA(encryptedAESKey, rsaPrivateKey);

            // Step 2: Decrypt the file content using the decrypted AES key
            byte[] decryptedFileContent = decryptFileWithAES(encryptedContent, aesKey);

            // Step 3: Save the decrypted file in the Downloads directory
            saveDecryptedFile(decryptedFileContent,filename);

            Toast.makeText(getContext(), "File decrypted and saved in Downloads!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("DecryptFragment", "Decryption failed: " + e.getMessage());
            Toast.makeText(getContext(), "Decryption failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public static String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                result = cursor.getString(columnIndex);
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    private byte[] decryptAESKeyWithRSA(byte[] encryptedAESKey, byte[] rsaPrivateKey) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(rsaPrivateKey));
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        return rsaCipher.doFinal(encryptedAESKey);
    }
    private byte[] decryptFileWithAES(byte[] encryptedContent, byte[] aesKey) throws Exception {
        // AES-GCM requires the IV and ciphertext to be separated
        int ivLength = 12; // Standard IV length for GCM mode
        if (encryptedContent.length < ivLength) {
            throw new IllegalArgumentException("Invalid encrypted data format");
        }

        // Extract the IV and ciphertext
        byte[] iv = Arrays.copyOfRange(encryptedContent, 0, ivLength);
        byte[] ciphertext = Arrays.copyOfRange(encryptedContent, ivLength, encryptedContent.length);

        // Set up the AES cipher in GCM mode
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv); // 128-bit authentication tag

        aesCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);

        // Perform the decryption
        return aesCipher.doFinal(ciphertext);
    }
    private void saveDecryptedFile(byte[] decryptedContent,String filename) throws Exception {
//        Context context = getContext();
//        if (context == null) return;
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String newFilename = "decrypted_"+filename.substring(10);
        // Create the file path
        File decryptedFile = new File(downloadsDir, newFilename);
        // Write the decrypted content to the file
        try (FileOutputStream fos = new FileOutputStream(decryptedFile)) {
            fos.write(decryptedContent);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Error writing decrypted file: " + e.getMessage());
        }
        // Log or inform that the file was saved successfully
        System.out.println("Decrypted file saved to: " + decryptedFile.getAbsolutePath());
    }

    private void triggerVibration() {
        Context context = getContext();
        if (context != null) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50,
                            VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(50);
                }
            }
        }
    }
}