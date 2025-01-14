package com.example.data_encryption.Fragments;
import com.example.data_encryption.utils.RSAKeyManager;
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
            Log.d("DecryptFragment", "Step 1: Decrypting AES key using RSA private key...");
            PrivateKey rsaPrivateKey = RSAKeyManager.getRSAPrivateKey();
            if (rsaPrivateKey == null) { Log.d("DecryptFragment", "RSA private key retrieval failed");
                Toast.makeText(getContext(), "Failed to retrieve RSA private key", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("DecryptFragment", "RSA private key decoded successfully.");
            byte[] aesKey = decryptAESKeyWithRSA(encryptedAESKey, rsaPrivateKey);
            Log.d("DecryptFragment", "AES key decrypted successfully.");

            // Step 2: Decrypt the file content using the decrypted AES key
            Log.d("DecryptFragment", "Step 2: Decrypting file content using the decrypted AES key...");
            byte[] decryptedFileContent = decryptFileWithAES(encryptedContent, aesKey);
            Log.d("DecryptFragment", "File content decrypted successfully.");

            // Step 3: Save the decrypted file in the Downloads directory
            Log.d("DecryptFragment", "Step 3: Saving the decrypted file in the Downloads directory...");
            saveDecryptedFile(decryptedFileContent, filename);
            Log.d("DecryptFragment", "File saved successfully.");

            Toast.makeText(getContext(), "File decrypted and saved in Downloads!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d("DecryptFragment", "Decryption failed: " + e.getMessage());
            Toast.makeText(getContext(), "Decryption failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private byte[] decryptAESKeyWithRSA(byte[] encryptedAESKey, PrivateKey rsaPrivateKey) throws Exception {
        Log.d("DecryptFragment", "AES key decryption start.");

        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);

        Log.d("DecryptFragment", "Encrypted AES key size: " + encryptedAESKey.length);
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