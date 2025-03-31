package com.example.data_encryption.Fragments;
import com.example.data_encryption.utils.RSAKeyManager;
import com.example.data_encryption.utils.BioManager;
import com.example.data_encryption.utils.AuthHandler;
import com.example.data_encryption.MediaViewer.AudioPlayerDialogFragment;
import com.example.data_encryption.MediaViewer.DocViewerDialogFragment;
import com.example.data_encryption.MediaViewer.ImageViewerDialogFragment;
import com.example.data_encryption.MediaViewer.PdfViewerDialogFragment;
import com.example.data_encryption.MediaViewer.VideoPlayerDialogFragment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.fragment.app.DialogFragment;

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

                BioManager.authenticateUser(getActivity(), new AuthHandler() {
                    @Override
                    public void onAuthSuccess() throws IOException {
                        try {
                            // Proceed with decryption after successful authentication
                            separateCombinedFile(selectedFileUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error processing file: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onAuthFailure() throws IOException {
                        Toast.makeText(getContext(), "Authentication failed. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String email = sharedPreferences.getString("userName", "");
            PrivateKey rsaPrivateKey = RSAKeyManager.getRSAPrivateKey(email);
            if (rsaPrivateKey == null) { 
                Log.d("DecryptFragment", "RSA private key retrieval failed");
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

            // Step 3: Create a temporary file and open it with appropriate viewer
            Log.d("DecryptFragment", "Step 3: Opening decrypted file with appropriate viewer...");
            openDecryptedFile(decryptedFileContent, filename);
            Log.d("DecryptFragment", "File opened successfully.");

            Toast.makeText(getContext(), "File decrypted successfully!", Toast.LENGTH_LONG).show();
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
    private void openDecryptedFile(byte[] decryptedContent, String filename) throws Exception {
        Context context = getContext();
        if (context == null) return;

        // Create a temporary file in the cache directory
        File tempFile = new File(context.getCacheDir(), "temp_" + filename);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(decryptedContent);
        }

        // Get the MIME type
        String mimeType = getMimeType(filename);
        
        // Create URI for the temporary file
        Uri uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                context.getApplicationContext().getPackageName() + ".provider",
                tempFile
        );

        // Open file with appropriate viewer based on MIME type
        if (mimeType.startsWith("image/")) {
            showDialogFragment(ImageViewerDialogFragment.newInstance(uri));
        } else if (mimeType.equals("application/pdf")) {
            showDialogFragment(PdfViewerDialogFragment.newInstance(uri));
        } else if (mimeType.equals("application/msword") || 
                   mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            showDialogFragment(DocViewerDialogFragment.newInstance(uri));
        } else if (mimeType.startsWith("audio/")) {
            showDialogFragment(AudioPlayerDialogFragment.newInstance(uri));
        } else if (mimeType.startsWith("video/")) {
            showDialogFragment(VideoPlayerDialogFragment.newInstance(uri));
        } else {
            // For other file types, use system default viewer
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    private void showDialogFragment(DialogFragment dialogFragment) {
        dialogFragment.show(getChildFragmentManager(), dialogFragment.getClass().getSimpleName());
    }

    private static String getMimeType(String fileName) {
        if (fileName.toLowerCase().endsWith(".pdf")) {
            return "application/pdf";
        }

        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(fileName);
        if (extension != null) {
            String mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            return mimeType != null ? mimeType : "*/*";
        }
        return "*/*";
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