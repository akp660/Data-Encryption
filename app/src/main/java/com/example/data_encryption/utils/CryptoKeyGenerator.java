package com.example.data_encryption.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Pair;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
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
            return digest.digest(biometricData.getBytes());
        } catch (Exception e) {
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

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedKey = cipher.doFinal(aesKey);
            return Base64.encodeToString(encryptedKey, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get AES key bytes
    public static byte[] getAESKeyBytes(SecretKey secretKey) {
        return secretKey.getEncoded();
    }

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
            return cipher.doFinal(encryptedKeyBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Decrypt file using AES key
    public static File decryptFile(Activity activity, byte[] encryptedFileBytes, byte[] aesKey) {
        try {
            SecretKey secretKey = new SecretKeySpec(aesKey, 0, aesKey.length, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedBytes = cipher.doFinal(encryptedFileBytes);

            File decryptedFile = new File(activity.getExternalFilesDir(null), "decrypted_file.txt");
            try (FileOutputStream fos = new FileOutputStream(decryptedFile)) {
                fos.write(decryptedBytes);
            }

            System.out.println("Decrypted file saved at: " + decryptedFile.getAbsolutePath());
            return decryptedFile;
        } catch (Exception e) {
            System.out.println("Error during decryption: " + e.toString());
            return null;
        }
    }

    // Process received package on receiver's side
    public static void processReceivedPackage(Activity activity, File packageFile, String receiverRSAPrivateKey) {
        try {
            Pair<byte[], String> extractedData = extractEncryptedFileAndKey(activity, packageFile);

            if (extractedData != null) {
                byte[] encryptedFileBytes = extractedData.first;
                String encryptedAESKey = extractedData.second;

                byte[] decryptedAESKey = decryptAESKeyWithRSA(encryptedAESKey, receiverRSAPrivateKey);

                if (decryptedAESKey != null) {
                    File decryptedFile = decryptFile(activity, encryptedFileBytes, decryptedAESKey);
                    if (decryptedFile != null) {
                        System.out.println("Decryption process completed successfully.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error during processing: " + e.toString());
        }
    }
}
