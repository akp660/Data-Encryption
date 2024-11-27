package com.example.data_encryption.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONObject;
import java.io.InputStream;
import java.io.FileInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoKeyGenerator {

    private static final String KEY_NAME = "my_secure_key";

    // Generate AES key
    public static void generateCryptoKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // AES key size
            keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Generate AES key derived from biometrics
    public static byte[] generateKeyFromBiometrics() {
        try {
            generateCryptoKey();
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            String biometricData = key.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] aesKey = digest.digest(biometricData.getBytes());

            // Log the AES key in Base64 for readability
            Log.d("stepbystep", "Generated AES Key (Base64): " + Base64.encodeToString(aesKey, Base64.DEFAULT));
            return aesKey;
        } catch (Exception e) {
            Log.e("stepbystep", "Error generating AES key: " + e.getMessage());
            return null;
        }
    }

    // Encrypt AES key with receiver's RSA public key
    public static String encryptAESKeyWithRSA(byte[] aesKey, String receiverRSAPublicKey) {
        try {
            byte[] publicBytes = Base64.decode(receiverRSAPublicKey, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // Log the RSA public key in Base64 format
            Log.d("stepbystep", "Receiver's RSA Public Key (Base64): " + receiverRSAPublicKey);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedKey = cipher.doFinal(aesKey);
            return Base64.encodeToString(encryptedKey, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("stepbystep", "Error encrypting AES key with RSA: " + e.getMessage());
            return null;
        }
    }

//    // This method encrypts the file and saves it to the downloads folder
//    public static void encryptFileSavePackageToDownloads(Activity activity, File fileToEncrypt, byte[] biometricKey, String rsaPublicKeyString) {
//        try {
//            // Convert the string RSA public key into a PublicKey object
//            RSAPublicKey rsaPublicKey = getPublicKeyFromString(rsaPublicKeyString);
//
//            // Initialize the Cipher for RSA encryption
//            Cipher cipher = Cipher.getInstance("RSA");
//            cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
//
//            // Create input and output streams for the file
//            FileInputStream fileInputStream = new FileInputStream(fileToEncrypt);
//            File encryptedFile = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "encrypted_" + fileToEncrypt.getName());
//            FileOutputStream fileOutputStream = new FileOutputStream(encryptedFile);
//
//            // Encrypt the file and write it to the output file
//            CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, cipher);
//            CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);
//
//            // Buffer to read the file in chunks
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = cipherInputStream.read(buffer)) != -1) {
//                cipherOutputStream.write(buffer, 0, length);
//            }
//
//            cipherInputStream.close();
//            cipherOutputStream.close();
//            fileInputStream.close();
//            fileOutputStream.close();
//
//            // Notify the user that the encryption was successful
//            Toast.makeText(activity, "File encrypted successfully. Saved to: " + encryptedFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("stepbystep", "Error encrypting AES key with RSA: " + e.getMessage());
//            Toast.makeText(activity, "Error during file encryption: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }

//    // This helper method converts the RSA public key string into a PublicKey object
//    private static RSAPublicKey getPublicKeyFromString(String keyString) throws Exception {
//        byte[] keyBytes = android.util.Base64.decode(keyString, android.util.Base64.DEFAULT);
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        return (RSAPublicKey) keyFactory.generatePublic(new java.security.spec.X509EncodedKeySpec(keyBytes));
//    }
//
//    // Get AES key bytes
//    public static byte[] getAESKeyBytes(SecretKey secretKey) {
//        return secretKey.getEncoded();
//    }

    // Generate SecretKey from bytes
    public static SecretKey generateSecretKeyFromBytes(byte[] keyBytes) {
        if (keyBytes == null) {
            throw new IllegalArgumentException("Key bytes cannot be null");
        }
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    // Encrypt file using AES key
    public static void encryptFile(Activity activity, File inputFile, byte[] key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, 0, key.length, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            File internalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File encryptedFile = new File(internalStorage, "encryptedFile");
            FileOutputStream outputStream = new FileOutputStream(encryptedFile);
            outputStream.write(outputBytes);
            outputStream.flush();
            outputStream.close();

            System.out.println("File successfully encrypted and saved to Downloads.");
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.toString());
        }
    }

    public static void combineEncryptedFileAndKey(Activity activity, File encryptedFile, String encryptedAESKey) {
        try {
            // Define output file
            File combinedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "output.encrypted");

            // Write encrypted AES key and encrypted file data sequentially
            try (FileOutputStream fos = new FileOutputStream(combinedFile)) {
                // Write the encrypted AES key length
                byte[] aesKeyBytes = encryptedAESKey.getBytes();
                fos.write(intToBytes(aesKeyBytes.length));

                // Write the encrypted AES key
                fos.write(aesKeyBytes);

                // Write the encrypted file
                try (InputStream encryptedFileStream = new FileInputStream(encryptedFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = encryptedFileStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                    }
                }
            }

            // Notify user about successful file combination
            Toast.makeText(activity, "File and key combined successfully! Saved at: " + combinedFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d("stepbystep", "File and key combined successfully! Saved at: " + combinedFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, "Failed to combine file and key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper function to convert an integer to a 4-byte array.
     */
    private static byte[] intToBytes(int value) {
        return new byte[] {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }


    // Combine encrypted file and AES key into a JSON package
    public static File combineEncryptedFileAndKey(Activity activity, byte[] encryptedFileBytes, String encryptedAESKey) {
        try {
            JSONObject packageJson = new JSONObject();
            packageJson.put("encrypted_file", Base64.encodeToString(encryptedFileBytes, Base64.DEFAULT));
            packageJson.put("encrypted_key", encryptedAESKey);

            String packageString = packageJson.toString();

            File combinedFile = new File(activity.getExternalFilesDir(null), "secure_package.json");
            try (FileOutputStream fos = new FileOutputStream(combinedFile)) {
                fos.write(packageString.getBytes());
            }

            System.out.println("Combined package saved at: " + combinedFile.getAbsolutePath());
            return combinedFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Encrypt file and prepare for sharing
    public static void encryptFileAndPrepareForSharing(Activity activity, File inputFile, byte[] aesKey, String receiverRSAPublicKey) {
        try {
            // Encrypt the file
            SecretKey secretKey = new SecretKeySpec(aesKey, 0, aesKey.length, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] encryptedFileBytes = cipher.doFinal(inputBytes);

            // Encrypt the AES key with the receiver's RSA public key
            String encryptedAESKey = encryptAESKeyWithRSA(aesKey, receiverRSAPublicKey);

            if (encryptedAESKey == null) {
                System.out.println("Failed to encrypt AES key.");
                return;
            }

            // Combine encrypted file and key into a JSON package
            File packageFile = combineEncryptedFileAndKey(activity, encryptedFileBytes, encryptedAESKey);

            if (packageFile != null) {
                // Share the package file
                downloadAndSharePackageFile(activity, packageFile);
            }
        } catch (Exception e) {
            System.out.println("Error during encryption and sharing: " + e.toString());
        }
    }

    // Share the combined package file
    public static void downloadAndSharePackageFile(Activity activity, File packageFile) {
        try {
            // Use FileProvider to get a content URI for the file
            Uri fileUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".fileprovider",
                    packageFile
            );

            // Create an Intent to share the file
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/json");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start the file sharing Intent
            activity.startActivity(Intent.createChooser(shareIntent, "Share Encrypted Package"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sharing package file: " + e.toString());
        }
    }

    // Extract encrypted file and key from JSON package
    public static Pair<byte[], String> extractEncryptedFileAndKey(Activity activity, File packageFile) {
        try (FileInputStream fis = new FileInputStream(packageFile)) {
            byte[] packageBytes = new byte[(int) packageFile.length()];
            fis.read(packageBytes);
            String packageString = new String(packageBytes);

            JSONObject packageJson = new JSONObject(packageString);
            byte[] encryptedFileBytes = Base64.decode(packageJson.getString("encrypted_file"), Base64.DEFAULT);
            String encryptedAESKey = packageJson.getString("encrypted_key");

            return new Pair<>(encryptedFileBytes, encryptedAESKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Decrypt AES key with RSA private key
    public static byte[] decryptAESKeyWithRSA(String encryptedAESKey, String privateKeyBase64) {
        try {
            byte[] privateBytes = Base64.decode(privateKeyBase64, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] encryptedKeyBytes = Base64.decode(encryptedAESKey, Base64.DEFAULT);
            byte[] decryptedAESKey = cipher.doFinal(encryptedKeyBytes);

            return decryptedAESKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
