package com.example.data_encryption.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoKeyGenerator {

    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256); // 256-bit AES key
        return keyGenerator.generateKey();
    }

//    public static SecretKey generateKeyWithKeystore() throws Exception {
//        KeyGenerator keyGenerator = KeyGenerator.getInstance(
//                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
//        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
//                "my_key_alias",
//                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
//                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//                .setUserAuthenticationRequired(true) // Requires fingerprint authentication
//                .setUserAuthenticationValidityDurationSeconds(30) // Time window after auth
//                .build();
//        keyGenerator.init(keyGenParameterSpec);
//        return keyGenerator.generateKey();
//    }

    // Encrypt the file with AES key
    public static void encryptFile(Uri fileUri, SecretKey secretKey, Context context) {
        try {
            if (fileUri == null) {
                Toast.makeText(context, "Invalid file URI", Toast.LENGTH_LONG).show();
                return;
            }

            // Read file content
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(fileUri);

            if (inputStream == null) {
                Toast.makeText(context, "Unable to open file", Toast.LENGTH_LONG).show();
                Log.e("stepbystep", "InputStream is null. FileUri: " + fileUri.toString());
                return;
            }

            // Reading the file into byte array
            byte[] fileData;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            fileData = byteArrayOutputStream.toByteArray();
            inputStream.close();

            if (fileData == null || fileData.length == 0) {
                Toast.makeText(context, "File is empty or could not be read", Toast.LENGTH_LONG).show();
                Log.e("stepbystep", "File data is null or empty");
                return;
            }

            // Encrypt the file data using the AES key
            if (secretKey == null) {
                Toast.makeText(context, "SecretKey is null. Cannot encrypt.", Toast.LENGTH_LONG).show();
                Log.e("stepbystep", "SecretKey is null");
                return;
            }

            byte[] encryptedData = CryptoKeyGenerator.encryptData(fileData, secretKey);

            // Get the file name from URI and prepare the output file path
            String fileName = getFileNameFromUri(context, fileUri);
            if (fileName == null || fileName.isEmpty()) {
                Toast.makeText(context, "File name could not be determined", Toast.LENGTH_LONG).show();
                Log.e("stepbystep", "File name is null or empty");
                return;
            }

            File appDir = context.getExternalFilesDir(null);
            File combinedFile = new File(appDir, fileName);

            try {
                // Encrypt the AES key with RSA
//                PublicKey rsaPublicKey = getRSAPublicKey(); // Replace with actual key retrieval
//                if (rsaPublicKey == null) {
//                    Toast.makeText(context, "RSA PublicKey is null", Toast.LENGTH_LONG).show();
//                    Log.e("stepbystep", "RSA PublicKey is null");
//                    return;
//                }
//
//                byte[] encryptedAESKey = encryptAESKeyWithRSA(rsaPublicKey, secretKey);
//                if (encryptedAESKey == null) {
//                    Toast.makeText(context, "Failed to encrypt AES Key", Toast.LENGTH_LONG).show();
//                    Log.e("stepbystep", "Encrypted AES Key is null or empty");
//                    return;
//                }

                byte[] aesKeyBytes = secretKey.getEncoded();
                // Combine the encrypted file data and the encrypted AES key into one file
                try (FileOutputStream fos = new FileOutputStream(combinedFile)) {
                    // Write the encrypted file content
                    fos.write(encryptedData);
                    // Append the encrypted AES key
//                    fos.write(encryptedAESKey);
                    fos.write(aesKeyBytes);
                }

                Toast.makeText(context, "Combined ENCRYPTED DATA with KEY saved: " + combinedFile.getPath(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Error saving combined file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("stepbystep", "Exception occurred: " + e.getMessage(), e);
            }


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error encrypting file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("stepbystep", "Exception occurred: " + e.getMessage(), e);
        }
    }

    public static byte[] encryptData(byte[] plaintext, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // GCM requires an initialization vector (IV)
        byte[] iv = cipher.getIV();

        byte[] ciphertext = cipher.doFinal(plaintext);
        // Combine IV and ciphertext for storage
        byte[] encryptedData = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length);
        System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);
        return encryptedData;
    }

    // Method to wrap (encrypt) the AES key with the RSA public key
//    public static byte[] wrapAESKey(SecretKey secretKey, PublicKey rsaPublicKey) throws Exception {
//        // Check if the AES key is null
//        if (secretKey == null) {
//            Log.e("stepbystep", "SecretKey is null");
//            throw new NullPointerException("SecretKey is null");
//        } else {
//            Log.d("stepbystep", "AES SecretKey is not null");
//        }
//
//        // Check if the RSA public key is null
//        if (rsaPublicKey == null) {
//            Log.e("stepbystep", "RSA PublicKey is null");
//            throw new NullPointerException("PublicKey is null");
//        } else {
//            Log.d("stepbystep", "RSA PublicKey is not null");
//            Log.d("stepbystep", "RSA PublicKey Algorithm: " + rsaPublicKey.getAlgorithm());
//        }
//        Log.d("stepbystep", "RSA Public Key: " + rsaPublicKey.getEncoded().length + " bytes");
//        Log.d("stepbystep", "Generated AES key: " + Arrays.toString(secretKey.getEncoded()));
//
//
//        // Wrap AES key with RSA public key using Cipher
//        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        cipher.init(Cipher.WRAP_MODE, rsaPublicKey);
//
//        byte[] wrappedKey = cipher.wrap(secretKey);
//        if (wrappedKey == null || wrappedKey.length == 0) {
//            Log.e("stepbystep", "Wrapped AES key is null or empty");
//            throw new NullPointerException("Wrapped AES key is null or empty");
//        } else {
//            Log.d("stepbystep", "AES key wrapped successfully, length: " + wrappedKey.length);
//        }
//
//        return wrappedKey;
//    }

    public static byte[] encryptAESKeyWithRSA(PublicKey rsaPublicKey, SecretKey aesSecretKey) throws Exception {
        // Check if the public key and AES secret key are not null
        if (rsaPublicKey == null || aesSecretKey == null) {
            throw new IllegalArgumentException("Public key and AES secret key cannot be null");
        }

        // Initialize cipher for RSA encryption
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);

        // Encrypt the AES key
        return rsaCipher.doFinal(aesSecretKey.getEncoded());
    }

    // to retrieve the RSA public key
//    public static PublicKey getRSAPublicKey() throws Exception {
//        // Use a public key in X.509/SPKI format
//        String base64PublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjbXYlJj8AaLNtCciysIFg8d/TZv96ipRkKIh5NcnNR1IgxepMRFelCW9mwQ0BkyzfPC4SI35O9xZGYi0IOOFbCYUOCMDDTMZG4A9kQUWc1PvNmJ9+abenei6dcR8Nyr3RE6WL2M1g+Uvt11REPd7cXL8ytUoOs+LDS+0xHZVGyTftYkTniqig8QTTOeCL4ksgByHRudQMHthcbfXCHaPxT2SQ6qC19JVHWr9ViCLSPjmeZGgy85xS3VwSaX+C3fdETwitTtsD8BgSZT4+U1E7tMwcTf+CiZE/HLlcwQY1zr+feC3gIyRDJecDxgEOiZJitQA8x+t6jdB7b4mq18ztQIDAQAB";
//
//        try {
//            byte[] decodedKey = Base64.decode(base64PublicKey, Base64.DEFAULT);
//            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            return keyFactory.generatePublic(keySpec);
//        } catch (Exception e) {
//            Log.e("getRSAPublicKey", "Error loading public key: " + e.getMessage());
//            throw e;
//        }
//    }
//    public static PublicKey getRSAPublicKey() throws Exception {
//        // Replace with the actual way to retrieve the RSA public key (e.g., from a file or server)
//        // For now, we assume the key is stored as a Base64 string
////        String base64PublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuIKFgdbw3Tn0lcZ4xRJjTytn5LsIp2P6s5W8wDh6x9eP2BhBlxFgqOqJ2KUf4sYI9dZ/vHupNcvC8vb/Bf+KoB8emqH5c+wLNiRIH5z3UnlHJW9kjcJ4jb3MI4ON7YX5m1PCmXr2LRgrB4rU1BcA5sCMsDvbFgByxu5ObKH1tdM9+jfNFHTzNc+lznTx0dwbgGogD8DHjcB1kxuLFSrKfErUNkH9OSJQ1U8T9Fb0ErmjO8rlAeSmUanHE/ebFGnJihpB1dVwckFCefj06z6qlbVNZUL7AzQf8DrW24D6g+/T5/hh3jKrkWlfYzL4uFVRt8V06jKG3S7W8gD3Osm0OQIDAQAB";
////        String base64PublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp9BkwsPC9Uf2unyEbJZd+jHC8f6T3uUKNAcJuMgCrWGEuF4pQUScqXwaCbzhJKvRY+UtLxw2lCVKwK3l1OSB9/RlHQRSuEsYD9f0ldxPStqhHvOq5xXCXJ2tuIZOh9Yp0k0g6hUcqHHhHHzqMOvDo/FX3RxCjZnM02KCekyZiRDBg8G6+aTQeLlZRc00Kb7ApJ0nWH2R9WYiFQVqS7YiOQBvnGQCGDHkuZ1/SbL4z3LIx6ezf8rm0aGJBcgxf1Eaz9xaXO3ovFRenXyb2BNbyLAewUcSiSK1IqvxLCQK+9tZxqGEyL7emNmCxK1L2A1KusDK2pYC1vPvPgdWJQIDAQAB/vHupNcvC8vb/Bf+KoB8emqH5c+wLNiRIH5z3UnlHJW9kjcJ4jb3MI4ON7YX5m1PCmXr2LRgrB4rU1BcA5sCMsDvbFgByxu5ObKH1tdM9+jfNFHTzNc+lznTx0dwbgGogD8DHjcB1kxuLFSrKfErUNkH9OSJQ1U8T9Fb0ErmjO8rlAeSmUanHE/ebFGnJihpB1dVwckFCefj06z6qlbVNZUL7AzQf8DrW24D6g+/T5/hh3jKrkWlfYzL4uFVRt8V06jKG3S7W8gD3Osm0OQIDAQAB";
//
//        String base64PublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoZxF3ANb+l+zxHqU7QB2kK0LZw34Ht+JZ9aPPD6GqBLzFbhb65FpPBXqP+BT8dsDmGeD84JhrMC+CCp7YGNPqZ3BGTyLeCWP+jeK75+pq+dI/9N+A0ZDxt3Gl8lJ9mRz8ZbgbhnY9vvYr/HQiLxhW1jSMXp2kWcEt/zE9zP1A+hgxIJQ5AvtDG2Ur95kV1G/SY+LXo9fqaxb2XL5bFCwbdTjN3dR+G/RD4PXbrWJuoEVXBqSgvCXe09kWH3JfbIbADxQCOlaPp7AiGxKpuvkGlwvIJbMZvQstT6VfyHtYqVwRZEk6qY1L0t7THy8AQvQ7OhsiWyV3WIHQknZqQIDAQAB";
//        Log.d("getRSAPublicKey", "Base64 Public Key: " + base64PublicKey);
//        byte[] decodedKey = Base64.decode(base64PublicKey, Base64.DEFAULT);
//        if (decodedKey == null || decodedKey.length == 0) {
//            Log.e("getRSAPublicKey", "Decoded key is null or empty");
//            throw new IllegalArgumentException("Public key decoding failed");
//        }
//        Log.d("getRSAPublicKey", "Decoded Key Length: " + decodedKey.length);
//
////        byte[] decodedKey = Base64.decode(base64PublicKey, Base64.DEFAULT);
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
//        return keyFactory.generatePublic(keySpec);
//    }

    // Method to get the file name from URI (already exists in your code)
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
}

